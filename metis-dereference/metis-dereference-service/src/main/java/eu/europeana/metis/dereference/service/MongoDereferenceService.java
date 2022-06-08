package eu.europeana.metis.dereference.service;

import static eu.europeana.metis.utils.CommonStringValues.CRLF_PATTERN;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
  public MongoDereferenceService(ProcessedEntityDao processedEntityDao,
      VocabularyDao vocabularyDao) {
    this(new RdfRetriever(), processedEntityDao, vocabularyDao);
  }

  /**
   * Constructor.
   *
   * @param retriever Object that retrieves entities from their source services.
   * @param processedEntityDao Object managing the processed entity cache.
   * @param vocabularyDao Object that accesses vocabularies.
   */
  MongoDereferenceService(RdfRetriever retriever, ProcessedEntityDao processedEntityDao,
      VocabularyDao vocabularyDao) {
    this.retriever = retriever;
    this.processedEntityDao = processedEntityDao;
    this.vocabularyDao = vocabularyDao;
  }

  @Override
  public List<EnrichmentBase> dereference(String resourceId)
      throws TransformerException, JAXBException, URISyntaxException {

    // Sanity check
    if (resourceId == null) {
      throw new IllegalArgumentException("Parameter resourceId cannot be null.");
    }

    // Perform the actual dereferencing.
    return new ArrayList<>(dereferenceResource(resourceId));
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
   * @return A collection of dereferenced resources. Is not null, but could be empty.
   */
  private Collection<EnrichmentBase> dereferenceResource(String resourceId)
      throws JAXBException, URISyntaxException {

    // Get the main object to dereference. If null, we are done.
    final Pair<EnrichmentBase, Vocabulary> resource = computeEnrichmentBaseVocabularyPair(
        resourceId);
    if (resource == null) {
      return Collections.emptyList();
    }

    // Create value resolver that catches exceptions and logs them.
    final Function<String, EnrichmentBase> valueResolver = key -> {
      Pair<EnrichmentBase, Vocabulary> result;
      try {
        result = computeEnrichmentBaseVocabularyPair(key);
        return result == null ? null : result.getLeft();
      } catch (JAXBException | URISyntaxException e) {
        LOGGER.warn(String.format("Problem occurred while dereferencing broader resource %s.", key),
            e);
        return null;
      }
    };

    // Perform the breadth-first search to search for broader terms (if needed).
    final int iterations = resource.getRight().getIterations();
    final Map<String, EnrichmentBase> result;
    if (iterations > 0) {
      result = GraphUtils
          .breadthFirstSearch(resourceId, resource.getLeft(), resource.getRight().getIterations(),
              valueResolver, this::extractBroaderResources);
    } else {
      result = new HashMap<>();
      result.put(resourceId, resource.getLeft());
    }

    // Done
    return result.values();
  }

  private void extractBroaderResources(EnrichmentBase resource, Set<String> destination) {
    final Stream<String> resourceIdStream;
    if (resource instanceof Concept) {
      resourceIdStream = getStream(((Concept) resource).getBroader()).map(Resource::getResource);
    } else if (resource instanceof TimeSpan) {
      resourceIdStream = Optional.ofNullable(((TimeSpan) resource).getIsPartOf()).stream()
                                 .flatMap(List::stream).map(LabelResource::getResource);
    } else if (resource instanceof Place) {
      resourceIdStream = Optional.ofNullable(((Place) resource).getIsPartOf()).stream()
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
   * @return a pair with the computed values
   * @throws URISyntaxException if the resource identifier url is invalid
   * @throws TransformerException if an exception occurred during transformation of the original entity
   */
  private Pair<String, Vocabulary> computeEntityVocabularyPair(String resourceId,
      ProcessedEntity cachedEntity) throws URISyntaxException {

    final Pair<String, Vocabulary> transformedEntityVocabularyPair;

    //Check if vocabulary actually exists
    Vocabulary cachedVocabulary = null;
    boolean cachedVocabularyChanged = false;
    if (cachedEntity != null && StringUtils.isNotBlank(cachedEntity.getVocabularyId())) {
      cachedVocabulary = vocabularyDao.get(cachedEntity.getVocabularyId());
      cachedVocabularyChanged = cachedVocabulary == null;
    }

    // If we do not have any cached entity, we need to compute it
    if (cachedEntity == null || cachedVocabularyChanged) {
      transformedEntityVocabularyPair = retrieveAndTransformEntity(resourceId);
      saveEntity(resourceId, cachedEntity, transformedEntityVocabularyPair);
    } else {
      // If we have something in the cache we return that instead
      transformedEntityVocabularyPair = new ImmutablePair<>(cachedEntity.getXml(),
          cachedVocabulary);
    }

    return transformedEntityVocabularyPair;
  }

  private Pair<String, Vocabulary> retrieveAndTransformEntity(String resourceId) throws URISyntaxException {

    final VocabularyCandidates vocabularyCandidates = VocabularyCandidates
        .findVocabulariesForUrl(resourceId, vocabularyDao::getByUriSearch);

    String transformedEntity = null;
    Vocabulary chosenVocabulary = null;
    //Only if we have vocabularies we continue
    if (!vocabularyCandidates.isEmpty()) {
      String originalEntity = retrieveOriginalEntity(resourceId, vocabularyCandidates);
      //If original entity exists, try transformation
      if (originalEntity != null) {
        // Transform the original entity and find vocabulary if applicable.
        for (Vocabulary vocabulary : vocabularyCandidates.getVocabularies()) {
          transformedEntity = transformEntity(vocabulary, originalEntity, resourceId);
          if (transformedEntity != null) {
            chosenVocabulary = vocabulary;
            break;
          }
        }
      }
    }

    final ImmutablePair<String, Vocabulary> entityVocabularyPair;
    // If retrieval or transformation of entity failed and we have one vocabulary then we store that
    if (transformedEntity == null && vocabularyCandidates.getVocabularies().size() == 1) {
      entityVocabularyPair = new ImmutablePair<>(null,
          vocabularyCandidates.getVocabularies().get(0));
    } else {
      entityVocabularyPair = new ImmutablePair<>(transformedEntity, chosenVocabulary);
    }

    return entityVocabularyPair;
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

  private String transformEntity(Vocabulary vocabulary, String originalEntity, String resourceId) {
    Optional<String> result;
    try {
      final IncomingRecordToEdmTransformer incomingRecordToEdmTransformer = new IncomingRecordToEdmTransformer(
          vocabulary.getXslt());
      result = incomingRecordToEdmTransformer.transform(originalEntity, resourceId);
    } catch (TransformerException | BadContentException | ParserConfigurationException e) {
      LOGGER.warn("Error transforming entity: {} with message: {}", resourceId, e.getMessage());
      LOGGER.debug("Transformation issue: ", e);
      result = Optional.empty();
    }
    return result.orElse(null);
  }

  private String retrieveOriginalEntity(String resourceId, VocabularyCandidates candidates)
      throws URISyntaxException {

    // Check the input (check the resource ID for URI syntax).
    if (candidates.isEmpty()) {
      return null;
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
    }
    return originalEntity;
  }

  Pair<EnrichmentBase, Vocabulary> computeEnrichmentBaseVocabularyPair(String resourceId)
      throws JAXBException, URISyntaxException {

    // Try to get the entity and its vocabulary from the cache.
    final ProcessedEntity cachedEntity = processedEntityDao.getByResourceId(resourceId);
    final Pair<String, Vocabulary> entityVocabularyPair = computeEntityVocabularyPair(resourceId,
        cachedEntity);

    // Parse the entity.
    final Pair<EnrichmentBase, Vocabulary> enrichmentBaseVocabularyPair;
    if (entityVocabularyPair.getLeft() == null || entityVocabularyPair.getRight() == null) {
      enrichmentBaseVocabularyPair = null;
    } else {
      enrichmentBaseVocabularyPair = convertToEnrichmentBaseVocabularyPair(
          entityVocabularyPair.getLeft(), entityVocabularyPair.getRight());
    }
    return enrichmentBaseVocabularyPair;
  }
}
