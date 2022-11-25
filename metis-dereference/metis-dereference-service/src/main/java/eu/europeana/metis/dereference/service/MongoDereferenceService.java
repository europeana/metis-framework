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
import java.util.ArrayList;
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
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
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
   * @return Pair of enrichment results, dereference status.
   * @throws IllegalArgumentException In case the Parameter is null.
   */
  @Override
  public Pair<List<EnrichmentBase>, DereferenceResultStatus> dereference(String resourceId) {
    // Sanity check
    if (resourceId == null) {
      throw new IllegalArgumentException("Parameter resourceId cannot be null.");
    }

    // Perform the actual dereferencing.
    Pair<Collection<EnrichmentBase>, DereferenceResultStatus> dereferenceResult = dereferenceResource(resourceId);
    return new ImmutablePair<>(new ArrayList<>(dereferenceResult.getLeft()), dereferenceResult.getRight());
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
  private Pair<Collection<EnrichmentBase>, DereferenceResultStatus> dereferenceResource(String resourceId) {
    try {
      // Get the main object to dereference. If null, we are done.
      final Triple<EnrichmentBase, Vocabulary, DereferenceResultStatus> resource = computeEnrichmentBaseVocabularyTriple(resourceId);

      // No EnrichmentBase and no Vocabulary.
      if (resource.getLeft() == null && resource.getMiddle() == null && resource.getRight() == DereferenceResultStatus.SUCCESS) {
        return new ImmutablePair<>(Collections.emptyList(), DereferenceResultStatus.NO_VOCABULARY_MATCHING);
      // No EnrichmentBase, no Vocabulary and an error occurred.
      } else if (resource.getLeft() == null && resource.getMiddle() == null) {
        return new ImmutablePair<>(Collections.emptyList(), resource.getRight());
      }

      // Create value resolver that catches exceptions and logs them.
      final Function<String, Pair<EnrichmentBase, DereferenceResultStatus>> valueResolver = key -> {
        Triple<EnrichmentBase, Vocabulary, DereferenceResultStatus> result;
        try {
          result = computeEnrichmentBaseVocabularyTriple(key);
          if (result.getLeft() == null && result.getMiddle() == null && result.getRight() == DereferenceResultStatus.SUCCESS) {
            // No EnrichmentBase + Status
            return new ImmutablePair<>(null, DereferenceResultStatus.NO_ENTITY_FOR_VOCABULARY);
          } else {
            // EnrichmentBase + Status
            return new ImmutablePair<>(result.getLeft(), result.getRight());
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

      // Perform the breadth-first search to search for broader terms (if needed).
      final int iterations = resource.getMiddle().getIterations();
      final Map<String, Pair<EnrichmentBase, DereferenceResultStatus>> result;
      if (iterations > 0) {
        result = GraphUtils
            .breadthFirstSearch(resourceId, new ImmutablePair<>(resource.getLeft(), resource.getRight()),
                resource.getMiddle().getIterations(),
                valueResolver, this::extractBroaderResources);
      } else {
        result = new HashMap<>();
        result.put(resourceId, new ImmutablePair<>(resource.getLeft(), resource.getRight()));
      }
      // Done
      return new ImmutablePair<>(
          result.values().stream().map(Pair::getLeft).collect(Collectors.toList()),
          result.values().stream().map(Pair::getRight).filter(Objects::nonNull).findFirst()
                .orElse(DereferenceResultStatus.UNKNOWN_ENTITY)
      );
    } catch (JAXBException jaxbException) {
      LOGGER.warn(String.format("Problem occurred while dereferencing resource %s.", resourceId), jaxbException);
      // No EnrichmentBase + Status
      return new ImmutablePair<>(Collections.emptyList(), DereferenceResultStatus.ENTITY_FOUND_XML_XLT_ERROR);
    } catch (URISyntaxException uriSyntaxException) {
      LOGGER.warn(String.format("Problem occurred while dereferencing resource %s.", resourceId), uriSyntaxException);
      // No EnrichmentBase + Status
      return new ImmutablePair<>(Collections.emptyList(), DereferenceResultStatus.INVALID_URL);
    }
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
   * @return a triple with the computed values
   * @throws URISyntaxException if the resource identifier url is invalid
   * @throws TransformerException if an exception occurred during transformation of the original entity
   */
  private Triple<String, Vocabulary, DereferenceResultStatus> computeEntityVocabularyTriple(String resourceId,
      ProcessedEntity cachedEntity) throws URISyntaxException {

    final Triple<String, Vocabulary, DereferenceResultStatus> transformedEntityVocabularyTriple;

    //Check if vocabulary actually exists
    Vocabulary cachedVocabulary = null;
    boolean cachedVocabularyChanged = false;
    if (cachedEntity != null && StringUtils.isNotBlank(cachedEntity.getVocabularyId())) {
      cachedVocabulary = vocabularyDao.get(cachedEntity.getVocabularyId());
      cachedVocabularyChanged = cachedVocabulary == null;
    }

    // If we do not have any cached entity, we need to compute it
    if (cachedEntity == null || cachedVocabularyChanged) {
      transformedEntityVocabularyTriple = retrieveAndTransformEntity(resourceId);
      saveEntity(resourceId, cachedEntity,
          new ImmutablePair<>(transformedEntityVocabularyTriple.getLeft(),
              transformedEntityVocabularyTriple.getMiddle()));
    } else {
      // If we have something in the cache we return that instead
      transformedEntityVocabularyTriple = new ImmutableTriple<>(cachedEntity.getXml(),
          cachedVocabulary, DereferenceResultStatus.SUCCESS);
    }

    return transformedEntityVocabularyTriple;
  }

  private Triple<String, Vocabulary, DereferenceResultStatus> retrieveAndTransformEntity(String resourceId)
      throws URISyntaxException {

    final VocabularyCandidates vocabularyCandidates = VocabularyCandidates
        .findVocabulariesForUrl(resourceId, vocabularyDao::getByUriSearch);

    String transformedEntity = null;
    Vocabulary chosenVocabulary = null;
    Pair<String, DereferenceResultStatus> originalEntity = new ImmutablePair<>(resourceId, null);
    Pair<String, DereferenceResultStatus> entityTransformed = new ImmutablePair<>(null, null);
    //Only if we have vocabularies we continue
    if (!vocabularyCandidates.isEmpty()) {
      originalEntity = retrieveOriginalEntity(resourceId, vocabularyCandidates);
      //If original entity exists, try transformation
      if (originalEntity.getLeft() != null && originalEntity.getRight() == DereferenceResultStatus.SUCCESS) {
        // Transform the original entity and find vocabulary if applicable.
        for (Vocabulary vocabulary : vocabularyCandidates.getVocabularies()) {
          entityTransformed = transformEntity(vocabulary, originalEntity.getLeft(), resourceId);
          transformedEntity = entityTransformed.getLeft();
          if (transformedEntity != null) {
            chosenVocabulary = vocabulary;
            break;
          }
        }
        // There was an update in transforming, so we update the result status.
        if (originalEntity.getRight() != entityTransformed.getRight()) {
          originalEntity = new ImmutablePair<>(originalEntity.getLeft(), entityTransformed.getRight());
        }
      }
    }


    final ImmutableTriple<String, Vocabulary, DereferenceResultStatus> entityVocabularyTriple;
    // If retrieval or transformation of entity failed, and we have one vocabulary then we store that
    if (transformedEntity == null && vocabularyCandidates.getVocabularies().size() == 1) {
      entityVocabularyTriple = new ImmutableTriple<>(null,
          vocabularyCandidates.getVocabularies().get(0), originalEntity.getRight());
    } else {
      entityVocabularyTriple = new ImmutableTriple<>(transformedEntity, chosenVocabulary, originalEntity.getRight());
    }

    return entityVocabularyTriple;
  }

  private void saveEntity(String resourceId, ProcessedEntity cachedEntity,
      Pair<String, Vocabulary> transformedEntityAndVocabularyPair) {

    final String entityXml = transformedEntityAndVocabularyPair.getLeft();
    final Vocabulary vocabulary = transformedEntityAndVocabularyPair.getRight();
    final String vocabularyIdString = Optional.ofNullable(vocabulary).map(Vocabulary::getId)
                                              .map(ObjectId::toString).orElse(null);
    //Save entity
    ProcessedEntity entityToCache = (cachedEntity == null) ? new ProcessedEntity() : cachedEntity;
    entityToCache.setResourceId(resourceId);
    entityToCache.setXml(entityXml);
    entityToCache.setVocabularyId(vocabularyIdString);
    processedEntityDao.save(entityToCache);
  }

  private Pair<EnrichmentBase, Vocabulary> convertToEnrichmentBaseVocabularyPair(String entityXml,
      Vocabulary entityVocabulary) throws JAXBException {
    final Pair<EnrichmentBase, Vocabulary> result;
    if (entityXml == null || entityVocabulary == null) {
      result = null;
    } else {
      result = new ImmutablePair<>(EnrichmentBaseConverter.convertToEnrichmentBase(entityXml),
          entityVocabulary);
    }
    return result;
  }

  private Pair<String, DereferenceResultStatus> transformEntity(Vocabulary vocabulary,
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
    return new ImmutablePair<>(result.orElse(null), resultStatus);
  }

  private Pair<String, DereferenceResultStatus> retrieveOriginalEntity(String resourceId, VocabularyCandidates candidates)
      throws URISyntaxException {
    DereferenceResultStatus dereferenceResultStatus = DereferenceResultStatus.SUCCESS;
    // Check the input (check the resource ID for URI syntax).
    if (candidates.isEmpty()) {
      dereferenceResultStatus = DereferenceResultStatus.NO_VOCABULARY_MATCHING;
      return new ImmutablePair<>(null, dereferenceResultStatus);
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
    return new ImmutablePair<>(originalEntity, dereferenceResultStatus);
  }

  Triple<EnrichmentBase, Vocabulary, DereferenceResultStatus> computeEnrichmentBaseVocabularyTriple(String resourceId)
      throws JAXBException, URISyntaxException {

    // Try to get the entity and its vocabulary from the cache.
    final ProcessedEntity cachedEntity = processedEntityDao.getByResourceId(resourceId);
    final Triple<String, Vocabulary, DereferenceResultStatus> entityVocabularyTriple = computeEntityVocabularyTriple(resourceId,
        cachedEntity);

    // Parse the entity.
    final Pair<EnrichmentBase, Vocabulary> enrichmentBaseVocabularyPair;
    if (entityVocabularyTriple.getLeft() == null || entityVocabularyTriple.getMiddle() == null) {
      enrichmentBaseVocabularyPair = null;
    } else {
      enrichmentBaseVocabularyPair = convertToEnrichmentBaseVocabularyPair(
          entityVocabularyTriple.getLeft(), entityVocabularyTriple.getMiddle());
    }
    if (enrichmentBaseVocabularyPair == null) {
      return new ImmutableTriple<>(null, null, entityVocabularyTriple.getRight());
    } else {
      return new ImmutableTriple<>(enrichmentBaseVocabularyPair.getLeft(),
          enrichmentBaseVocabularyPair.getRight(),
          entityVocabularyTriple.getRight());
    }
  }
}
