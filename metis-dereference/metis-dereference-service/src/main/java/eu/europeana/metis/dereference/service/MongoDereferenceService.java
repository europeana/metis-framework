package eu.europeana.metis.dereference.service;

import static eu.europeana.metis.utils.CommonStringValues.CRLF_PATTERN;

import edu.emory.mathcs.backport.java.util.Collections;
import eu.europeana.enrichment.api.external.DereferenceResultStatus;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.LabelResource;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Resource;
import eu.europeana.enrichment.api.external.model.TimeSpan;
import eu.europeana.enrichment.utils.EnrichmentBaseConverter;
import eu.europeana.metis.dereference.DereferenceResult;
import eu.europeana.metis.dereference.IncomingRecordToEdmTransformer;
import eu.europeana.metis.dereference.ProcessedEntity;
import eu.europeana.metis.dereference.RdfRetriever;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.ProcessedEntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.dereference.service.utils.GraphUtils;
import eu.europeana.metis.dereference.service.utils.VocabularyCandidates;
import eu.europeana.metis.exception.BadContentException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Mongo implementation of the dereference service Created by ymamakis on 2/11/16.
 */
@Component
public class MongoDereferenceService implements DereferenceService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoDereferenceService.class);

  private final RdfRetriever retriever;
  private final ProcessedEntityDao processedEntityDao;
  private final VocabularyDao vocabularyDao;

  /**
   * Constructor.
   *
   * @param processedEntityDao Object managing the processed entity cache.
   * @param vocabularyDao Object that accesses vocabularies.
   */
  @Autowired
  public MongoDereferenceService(ProcessedEntityDao processedEntityDao, VocabularyDao vocabularyDao) {
    this(new RdfRetriever(), processedEntityDao, vocabularyDao);
  }

  /**
   * Constructor.
   *
   * @param retriever Object that retrieves entities from their source services.
   * @param processedEntityDao Object managing the processed entity cache.
   * @param vocabularyDao Object that accesses vocabularies.
   */
  MongoDereferenceService(RdfRetriever retriever, ProcessedEntityDao processedEntityDao, VocabularyDao vocabularyDao) {
    this.retriever = retriever;
    this.processedEntityDao = processedEntityDao;
    this.vocabularyDao = vocabularyDao;
  }

  /**
   * Mongo dereference implementation
   *
   * @param resourceId The resource ID (URI) to dereference
   * @return Dereference results with dereference status.
   * @throws IllegalArgumentException In case the Parameter is null.
   */
  @Override
  public DereferenceResult dereference(String resourceId) {
    // Sanity check
    if (resourceId == null) {
      throw new IllegalArgumentException("Parameter resourceId cannot be null.");
    }

    return dereferenceResource(resourceId);
  }

  /**
   * <p>
   * This method dereferences a resource. If the resource's vocabulary specifies a positive iteration count, this method also
   * repeatedly retrieves the 'broader' resources and returns those as well.
   * </p>
   * <p>
   * A resource has references to its 'broader' resources (see {@link #extractBroaderResources(EnrichmentBase, Set)}). As such,
   * the resources form a directed graph and the iteration count is the distance from the requested resource. This method performs
   * a breadth-first search through this graph to retrieve all resources within a certain distance from the requested resource.
   * </p>
   *
   * @param resourceId The resource to dereference.
   * @return A collection of Pair dereferenced resources and the status of dereference process. Is not null, but could be empty.
   * and contains the status of the deferenced resources. NO_VOCABULARY_MATCHING, this occurs if there is no enrichment base and
   * no vocabulary. NO_ENTITY_FOR_VOCABULARY, this means the resource was found but no vocabulary and enrichment was found.
   * ENTITY_FOUND_XLT_ERROR, this occurs when an JAXBExcetion happens. INVALID_URL, this occurs when an URIException happens.
   * UNKNOWN_ENTITY, this occurs is the entity is unknown.
   */
  private DereferenceResult dereferenceResource(String resourceId) {
    DereferenceResult dereferenceResult = null;
    try {
      // Get the main object to dereference. If null, we are done.
      //Triple<EnrichmentBase, Vocabulary, DereferenceResultStatus>
      final EnrichmentEntityVocabulary resource = computeEnrichmentBaseVocabularyTriple(resourceId);

      dereferenceResult = checkEmptyEnrichmentBaseAndVocabulary(dereferenceResult, resource);

      if (dereferenceResult == null) {
        // Create value resolver that catches exceptions and logs them.
        final Function<String, Pair<EnrichmentBase, DereferenceResultStatus>> valueResolver = getValueResolver();

        // Perform the breadth-first search to search for broader terms (if needed).
        final int iterations = resource.getVocabulary().getIterations();
        final Map<String, Pair<EnrichmentBase, DereferenceResultStatus>> result;
        if (iterations > 0) {
          result = GraphUtils
              .breadthFirstSearch(resourceId,
                  new ImmutablePair<>(resource.getEnrichmentBase(), resource.getDereferenceResultStatus()),
                  resource.getVocabulary().getIterations(),
                  valueResolver, this::extractBroaderResources);
        } else {
          result = new HashMap<>();
          result.put(resourceId, new ImmutablePair<>(resource.getEnrichmentBase(), resource.getDereferenceResultStatus()));
        }
        // Done
        dereferenceResult = new DereferenceResult(
            result.values().stream().map(Pair::getLeft).collect(Collectors.toList()),
            result.values().stream().map(Pair::getRight).filter(Objects::nonNull).findFirst()
                  .orElse(DereferenceResultStatus.UNKNOWN_ENTITY));
      }
    } catch (JAXBException jaxbException) {
      LOGGER.warn(String.format("Problem occurred while dereferencing resource %s.", resourceId), jaxbException);
      // No EnrichmentBase + Status
      dereferenceResult = new DereferenceResult(Collections.emptyList(), DereferenceResultStatus.ENTITY_FOUND_XML_XLT_ERROR);
    } catch (URISyntaxException uriSyntaxException) {
      LOGGER.warn(String.format("Problem occurred while dereferencing resource %s.", resourceId), uriSyntaxException);
      // No EnrichmentBase + Status
      dereferenceResult = new DereferenceResult(Collections.emptyList(), DereferenceResultStatus.INVALID_URL);
    }
    return dereferenceResult;
  }

  private Function<String, Pair<EnrichmentBase, DereferenceResultStatus>> getValueResolver() {
    return key -> {
      //Triple<EnrichmentBase, Vocabulary, DereferenceResultStatus>
      EnrichmentEntityVocabulary result;
      try {
        result = computeEnrichmentBaseVocabularyTriple(key);
        if (result.getEnrichmentBase() == null && result.getVocabulary() == null
            && result.getDereferenceResultStatus() == DereferenceResultStatus.SUCCESS) {
          // No EnrichmentBase + Status
          return new ImmutablePair<>(null, DereferenceResultStatus.NO_ENTITY_FOR_VOCABULARY);
        } else {
          // EnrichmentBase + Status
          return new ImmutablePair<>(result.getEnrichmentBase(), result.getDereferenceResultStatus());
        }
      } catch (JAXBException jaxbException) {
        LOGGER.warn(String.format("Problem occurred while dereferencing broader resource %s.", key), jaxbException);
        // No EnrichmentBase + Status
        return new ImmutablePair<>(null, DereferenceResultStatus.ENTITY_FOUND_XML_XLT_ERROR);
      } catch (URISyntaxException uriSyntaxException) {
        LOGGER.warn(String.format("Problem occurred while dereferencing broader resource %s.", key), uriSyntaxException);
        // No EnrichmentBase + Status
        return new ImmutablePair<>(null, DereferenceResultStatus.INVALID_URL);
      }
    };
  }

  private static DereferenceResult checkEmptyEnrichmentBaseAndVocabulary(DereferenceResult dereferenceResult,
      EnrichmentEntityVocabulary resource) {
    // No EnrichmentBase and no Vocabulary.
    if (resource.getEnrichmentBase() == null && resource.getVocabulary() == null
        && resource.getDereferenceResultStatus() == DereferenceResultStatus.SUCCESS) {
      dereferenceResult = new DereferenceResult(Collections.emptyList(), DereferenceResultStatus.NO_VOCABULARY_MATCHING);
      // No EnrichmentBase, no Vocabulary and an error occurred.
    } else if (resource.getEnrichmentBase() == null && resource.getVocabulary() == null) {
      dereferenceResult = new DereferenceResult(Collections.emptyList(), resource.getDereferenceResultStatus());
    }
    return dereferenceResult;
  }

  private void extractBroaderResources(Pair<EnrichmentBase, DereferenceResultStatus> resource, Set<String> destination) {
    final Stream<String> resourceIdStream;
    if (resource.getLeft() instanceof Concept) {
      resourceIdStream = getStream(((Concept) resource.getLeft()).getBroader()).map(Resource::getResource);
    } else if (resource.getLeft() instanceof TimeSpan) {
      resourceIdStream = Optional.ofNullable(((TimeSpan) resource.getLeft()).getIsPartOf()).stream()
                                 .flatMap(List::stream).map(LabelResource::getResource);
    } else if (resource.getLeft() instanceof Place) {
      resourceIdStream = Optional.ofNullable(((Place) resource.getLeft()).getIsPartOf()).stream()
                                 .flatMap(Collection::stream).map(LabelResource::getResource);
    } else {
      resourceIdStream = Stream.empty();
    }
    resourceIdStream.filter(Objects::nonNull).forEach(destination::add);
  }

  private static <T> Stream<T> getStream(Collection<T> collection) {
    return collection == null ? Stream.empty() : collection.stream();
  }

  /**
   * Computes the entity and vocabulary.
   * <p>It will use the cache if it's still valid, otherwise it will retrieve(if applicable) the
   * original entity and transform the result. </p>
   * <p>The possible outcomes are:
   * <ul>
   *   <li>Both items of the pair are null. We do not have a vocabulary candidate or we have more
   *   than one vocabulary candidate and all have not succeed either retrieving the original
   *   entity or transforming the retrieved entity.</li>
   *   <li>Entity xml(Left) is null, and vocabulary(Right) is non null. We have a vocabulary
   *   and the entity xml failed either to be retried or failed transformation.</li>
   *   <li>Entity xml(Left) is non null, and vocabulary(Right) is non null. We have a
   *   successful retrieval and transformation.</li>
   * </ul>
   * </p>
   *
   * @param resourceId the url of the provider entity
   * @param cachedEntity the cached entity object
   * @return a EnrichmentEntityVocabulary with the entity, vocabulary, and status.
   * @throws URISyntaxException if the resource identifier url is invalid
   * @throws TransformerException if an exception occurred during transformation of the original entity
   */
  private EnrichmentEntityVocabulary computeEntityVocabularyTriple(String resourceId,
      ProcessedEntity cachedEntity) throws URISyntaxException {

    final EnrichmentEntityVocabulary transformedEntityVocabulary;

    //Check if vocabulary actually exists
    Vocabulary cachedVocabulary = null;
    boolean cachedVocabularyChanged = false;
    if (cachedEntity != null && StringUtils.isNotBlank(cachedEntity.getVocabularyId())) {
      cachedVocabulary = vocabularyDao.get(cachedEntity.getVocabularyId());
      cachedVocabularyChanged = cachedVocabulary == null;
    }

    // If we do not have any cached entity, we need to compute it
    if (cachedEntity == null || cachedVocabularyChanged) {
      transformedEntityVocabulary = retrieveAndTransformEntity(resourceId);
      saveEntity(resourceId, cachedEntity, new EnrichmentEntityVocabulary(transformedEntityVocabulary.getEntity(),
          transformedEntityVocabulary.getVocabulary()));
    } else {
      // If we have something in the cache we return that instead
      transformedEntityVocabulary = new EnrichmentEntityVocabulary(cachedEntity.getXml(),
          cachedVocabulary, DereferenceResultStatus.SUCCESS);
    }

    return transformedEntityVocabulary;
  }

  private EnrichmentEntityVocabulary retrieveAndTransformEntity(String resourceId) throws URISyntaxException {

    final VocabularyCandidates vocabularyCandidates = VocabularyCandidates
        .findVocabulariesForUrl(resourceId, vocabularyDao::getByUriSearch);

    String transformedEntity = null;
    Vocabulary chosenVocabulary = null;
    //Pair<String, DereferenceResultStatus>
    MongoDereferencedEntity originalEntity = new MongoDereferencedEntity(resourceId, null);
    MongoDereferencedEntity entityTransformed = new MongoDereferencedEntity(null, null);
    //Only if we have vocabularies we continue
    if (!vocabularyCandidates.isEmpty()) {
      originalEntity = retrieveOriginalEntity(resourceId, vocabularyCandidates);
      //If original entity exists, try transformation
      if (originalEntity.getEntity() != null && originalEntity.getDereferenceResultStatus() == DereferenceResultStatus.SUCCESS) {
        // Transform the original entity and find vocabulary if applicable.
        for (Vocabulary vocabulary : vocabularyCandidates.getVocabularies()) {
          entityTransformed = transformEntity(vocabulary, originalEntity.getEntity(), resourceId);
          transformedEntity = entityTransformed.getEntity();
          if (transformedEntity != null) {
            chosenVocabulary = vocabulary;
            break;
          }
        }
        // There was an update in transforming, so we update the result status.
        if (originalEntity.getDereferenceResultStatus() != entityTransformed.getDereferenceResultStatus()) {
          originalEntity = new MongoDereferencedEntity(originalEntity.getEntity(), entityTransformed.getDereferenceResultStatus());
        }
      }
    }

    return evaluateTransformedEntityAndVocabulary(vocabularyCandidates, transformedEntity, chosenVocabulary, originalEntity);
  }

  private static EnrichmentEntityVocabulary evaluateTransformedEntityAndVocabulary(VocabularyCandidates vocabularyCandidates,
      String transformedEntity, Vocabulary chosenVocabulary, MongoDereferencedEntity originalEntity) {
    final EnrichmentEntityVocabulary enrichmentEntityVocabulary;
    // If retrieval or transformation of entity failed, and we have one vocabulary then we store that
    if (transformedEntity == null && vocabularyCandidates.getVocabularies().size() == 1) {
      enrichmentEntityVocabulary = new EnrichmentEntityVocabulary(vocabularyCandidates.getVocabularies().get(0),
          originalEntity.getDereferenceResultStatus());
    } else {
      enrichmentEntityVocabulary = new EnrichmentEntityVocabulary(transformedEntity, chosenVocabulary,
          originalEntity.getDereferenceResultStatus());
    }
    return enrichmentEntityVocabulary;
  }

  private void saveEntity(String resourceId, ProcessedEntity cachedEntity,
      EnrichmentEntityVocabulary transformedEntityAndVocabularyPair) {

    final String entityXml = transformedEntityAndVocabularyPair.getEntity();
    final Vocabulary vocabulary = transformedEntityAndVocabularyPair.getVocabulary();
    final String vocabularyIdString = Optional.ofNullable(vocabulary).map(Vocabulary::getId)
                                              .map(ObjectId::toString).orElse(null);
    //Save entity
    ProcessedEntity entityToCache = (cachedEntity == null) ? new ProcessedEntity() : cachedEntity;
    entityToCache.setResourceId(resourceId);
    entityToCache.setXml(entityXml);
    entityToCache.setVocabularyId(vocabularyIdString);
    processedEntityDao.save(entityToCache);
  }

  private EnrichmentEntityVocabulary convertToEnrichmentBaseVocabulary(String entityXml,
      Vocabulary entityVocabulary) throws JAXBException {
    final EnrichmentEntityVocabulary result;
    if (entityXml == null || entityVocabulary == null) {
      result = null;
    } else {
      result = new EnrichmentEntityVocabulary(EnrichmentBaseConverter.convertToEnrichmentBase(entityXml),
          entityVocabulary);
    }
    return result;
  }

  private MongoDereferencedEntity transformEntity(Vocabulary vocabulary,
      final String originalEntity, final String resourceId) {
    Optional<String> result;
    DereferenceResultStatus resultStatus;
    try {
      final IncomingRecordToEdmTransformer incomingRecordToEdmTransformer = new IncomingRecordToEdmTransformer(
          vocabulary.getXslt());
      result = incomingRecordToEdmTransformer.transform(originalEntity, resourceId);
      resultStatus = DereferenceResultStatus.SUCCESS;
    } catch (TransformerException | BadContentException | ParserConfigurationException e) {
      LOGGER.warn("Error transforming entity: {} with message: {}", resourceId, e.getMessage());
      LOGGER.debug("Transformation issue: ", e);
      resultStatus = DereferenceResultStatus.ENTITY_FOUND_XML_XLT_ERROR;
      result = Optional.empty();
    }
    return new MongoDereferencedEntity(result.orElse(null), resultStatus);
  }

  private MongoDereferencedEntity retrieveOriginalEntity(String resourceId, VocabularyCandidates candidates)
      throws URISyntaxException {
    DereferenceResultStatus dereferenceResultStatus = DereferenceResultStatus.SUCCESS;
    // Check the input (check the resource ID for URI syntax).
    if (candidates.isEmpty()) {
      dereferenceResultStatus = DereferenceResultStatus.NO_VOCABULARY_MATCHING;
      return new MongoDereferencedEntity(null, dereferenceResultStatus);
    }
    new URI(resourceId);

    // Compute the result (a URI syntax issue is considered a problem with the suffix).
    final String originalEntity = candidates.getVocabulariesSuffixes().stream().map(suffix -> {
      try {
        return retriever.retrieve(resourceId, suffix);
      } catch (IOException | URISyntaxException e) {
        LOGGER.warn("Failed to retrieve: {} with message: {}", resourceId, e.getMessage());
        LOGGER.debug("Problem retrieving resource.", e);
        return null;
      }
    }).filter(Objects::nonNull).findAny().orElse(null);

    // Evaluate the result.
    if (originalEntity == null && LOGGER.isInfoEnabled()) {
      LOGGER.info("No entity XML for uri {}", CRLF_PATTERN.matcher(resourceId).replaceAll(""));
      dereferenceResultStatus = DereferenceResultStatus.UNKNOWN_ENTITY;
    }
    return new MongoDereferencedEntity(originalEntity, dereferenceResultStatus);
  }

  EnrichmentEntityVocabulary computeEnrichmentBaseVocabularyTriple(String resourceId)
      throws JAXBException, URISyntaxException {

    // Try to get the entity and its vocabulary from the cache.
    final ProcessedEntity cachedEntity = processedEntityDao.getByResourceId(resourceId);
    final EnrichmentEntityVocabulary entityVocabularyTriple = computeEntityVocabularyTriple(resourceId,
        cachedEntity);

    // Parse the entity.
    final EnrichmentEntityVocabulary enrichmentBaseVocabulary;
    if (entityVocabularyTriple.getEntity() == null || entityVocabularyTriple.getVocabulary() == null) {
      enrichmentBaseVocabulary = null;
    } else {
      enrichmentBaseVocabulary = convertToEnrichmentBaseVocabulary(
          entityVocabularyTriple.getEntity(), entityVocabularyTriple.getVocabulary());
    }
    if (enrichmentBaseVocabulary == null) {
      return new EnrichmentEntityVocabulary(entityVocabularyTriple.getDereferenceResultStatus());
    } else {
      return new EnrichmentEntityVocabulary(enrichmentBaseVocabulary.getEnrichmentBase(),
          enrichmentBaseVocabulary.getVocabulary(),
          entityVocabularyTriple.getDereferenceResultStatus());
    }
  }
}
