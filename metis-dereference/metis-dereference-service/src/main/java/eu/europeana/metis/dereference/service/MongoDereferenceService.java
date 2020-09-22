package eu.europeana.metis.dereference.service;

import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.external.model.Part;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Resource;
import eu.europeana.enrichment.api.external.model.Timespan;
import eu.europeana.metis.dereference.IncomingRecordToEdmConverter;
import eu.europeana.metis.dereference.ProcessedEntity;
import eu.europeana.metis.dereference.RdfRetriever;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.ProcessedEntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.dereference.service.utils.GraphUtils;
import eu.europeana.metis.dereference.service.utils.VocabularyCandidates;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
  public EnrichmentResultList dereference(String resourceId)
      throws TransformerException, JAXBException, URISyntaxException {

    // Sanity check
    if (resourceId == null) {
      throw new IllegalArgumentException("Parameter resourceId cannot be null.");
    }

    // Perform the actual dereferencing.
    final Collection<EnrichmentBase> resultList = dereferenceResource(resourceId);

    // Prepare the result: empty if we didn't find an entity.
    final List<EnrichmentBaseWrapper> enrichmentBaseWrapperList = EnrichmentBaseWrapper
        .createNullOriginalFieldEnrichmentBaseWrapperList(resultList);
    return new EnrichmentResultList(enrichmentBaseWrapperList);
  }

  /**
   * <p>
   * This method dereferences a resource. If the resource's vocabulary specifies a positive
   * iteration count, this method also repeatedly retrieves the 'broader' resources and returns
   * those as well.
   * </p>
   * <p>
   * A resource has references to its 'broader' resources (see {@link
   * #extractBroaderResources(EnrichmentBase, Set)}). As such, the resources form a directed graph
   * and the iteration count is the distance from the requested resource. This method performs a
   * breadth-first search through this graph to retrieve all resources within a certain distance
   * from the requested resource.
   * </p>
   *
   * @param resourceId The resource to dereference.
   * @return A collection of dereferenced resources.
   */
  private Collection<EnrichmentBase> dereferenceResource(String resourceId)
      throws JAXBException, TransformerException, URISyntaxException {

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
      } catch (JAXBException | TransformerException | URISyntaxException e) {
        LOGGER.warn(String.format("Problem occurred while dereferencing broader resource %s.", key),
            e);
        return null;
      }
    };

    // Perform the breadth-first search to search for broader terms (if needed).
    final int iterations = resource.getRight().getIterations();
    final Map<String, EnrichmentBase> result;
    if (iterations > 0) {
      result = GraphUtils.breadthFirstSearch(resourceId, resource.getLeft(),
          resource.getRight().getIterations(), valueResolver, this::extractBroaderResources);
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
    } else if (resource instanceof Timespan) {
      resourceIdStream = getStream(((Timespan) resource).getIsPartOfList()).map(Part::getResource);
    } else if (resource instanceof Place) {
      resourceIdStream = getStream(((Place) resource).getIsPartOfList()).map(Part::getResource);
    } else {
      resourceIdStream = Stream.empty();
    }
    resourceIdStream.filter(Objects::nonNull).forEach(destination::add);
  }

  private static <T> Stream<T> getStream(Collection<T> collection) {
    return collection == null ? Stream.empty() : collection.stream();
  }

  Pair<EnrichmentBase, Vocabulary> computeEnrichmentBaseVocabularyPair(String resourceId)
      throws JAXBException, TransformerException, URISyntaxException {

    // Try to get the entity and its vocabulary from the cache.
    final ProcessedEntity cachedEntity = processedEntityDao.get(resourceId);

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
   * @throws TransformerException if an exception occurred during transformation of the original
   * entity
   */
  private Pair<String, Vocabulary> computeEntityVocabularyPair(String resourceId,
      ProcessedEntity cachedEntity) throws URISyntaxException, TransformerException {

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

  private void saveEntity(String resourceId, ProcessedEntity cachedEntity,
      Pair<String, Vocabulary> transformedEntityAndVocabularyPair) {
    //Save entity
    ProcessedEntity entityToCache = (cachedEntity == null) ? new ProcessedEntity() : cachedEntity;
    entityToCache.setResourceId(resourceId);
    entityToCache.setXml(transformedEntityAndVocabularyPair.getLeft());
    entityToCache.setVocabularyId(transformedEntityAndVocabularyPair.getRight().getId().toString());
    processedEntityDao.save(entityToCache);
  }

  private Pair<EnrichmentBase, Vocabulary> convertToEnrichmentBaseVocabularyPair(String entityXml,
      Vocabulary entityVocabulary) throws JAXBException {
    final Pair<EnrichmentBase, Vocabulary> result;
    if (entityXml == null || entityVocabulary == null) {
      result = null;
    } else {
      final StringReader reader = new StringReader(entityXml);
      final JAXBContext context = JAXBContext.newInstance(EnrichmentBase.class);
      final EnrichmentBase resource = (EnrichmentBase) context.createUnmarshaller()
          .unmarshal(reader);
      result = new ImmutablePair<>(resource, entityVocabulary);
    }
    return result;
  }

  private Pair<String, Vocabulary> retrieveAndTransformEntity(String resourceId)
      throws TransformerException, URISyntaxException {

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

  private String retrieveOriginalEntity(String resourceId, VocabularyCandidates candidates) {
    if (candidates.isEmpty()) {
      return null;
    }
    final String originalEntity = candidates.getVocabulariesSuffixes().stream().map(suffix -> {
      try {
        return retriever.retrieve(resourceId, suffix);
      } catch (IOException e) {
        LOGGER.warn("Failed to retrieve: {} with message: {}", resourceId, e.getMessage());
        LOGGER.debug("Problem retrieving resource.", e);
        return null;
      }
    }).filter(Objects::nonNull).findAny().orElse(null);
    if (originalEntity == null) {
      LOGGER.info("No entity XML for uri {}", resourceId);
    }
    return originalEntity;
  }

  private String transformEntity(Vocabulary vocabulary, String originalEntity, String resourceId)
      throws TransformerException {
    final IncomingRecordToEdmConverter converter = new IncomingRecordToEdmConverter(vocabulary);
    final String result;
    try {
      result = converter.convert(originalEntity, resourceId);
      if (result == null) {
        LOGGER.info("Could not transform entity {} as it results is an empty XML.", resourceId);
      }
    } catch (TransformerException e) {
      LOGGER.warn("Error transforming entity: {} with message: {}", resourceId, e.getMessage());
      LOGGER.debug("Transformation issue: ", e);
      return null;
    }
    return result;
  }
}
