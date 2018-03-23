package eu.europeana.metis.dereference.service;

import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import eu.europeana.enrichment.api.external.model.Concept;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.external.model.Part;
import eu.europeana.enrichment.api.external.model.Place;
import eu.europeana.enrichment.api.external.model.Resource;
import eu.europeana.enrichment.api.external.model.Timespan;
import eu.europeana.enrichment.rest.client.EnrichmentClient;
import eu.europeana.metis.dereference.ProcessedEntity;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.CacheDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.dereference.service.utils.GraphUtils;
import eu.europeana.metis.dereference.service.utils.IncomingRecordToEdmConverter;
import eu.europeana.metis.dereference.service.utils.RdfRetriever;
import eu.europeana.metis.dereference.service.utils.VocabularyCandidates;

/**
 * Mongo implementation of the dereference service Created by ymamakis on 2/11/16.
 */
public class MongoDereferenceService implements DereferenceService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoDereferenceService.class);

  private final RdfRetriever retriever;
  private final CacheDao cacheDao;
  private final VocabularyDao vocabularyDao;
  private final EnrichmentClient enrichmentClient;

  /**
   * Constructor.
   * 
   * @param retriever Object that retrieves entities from their source services.
   * @param cacheDao Object that accesses the cache of processed entities.
   * @param vocabularyDao Object that accesses vocabularies.
   * @param enrichmentClient Object that accesses the enrichment service.
   */
  @Autowired
  public MongoDereferenceService(RdfRetriever retriever, CacheDao cacheDao,
      VocabularyDao vocabularyDao, EnrichmentClient enrichmentClient) {
    this.retriever = retriever;
    this.cacheDao = cacheDao;
    this.vocabularyDao = vocabularyDao;
    this.enrichmentClient = enrichmentClient;
  }

  @Override
  public EnrichmentResultList dereference(String resourceId)
      throws TransformerException, JAXBException, URISyntaxException {

    // Sanity check
    if (resourceId == null) {
      throw new IllegalArgumentException("Parameter resourceId cannot be null.");
    }

    // First look in the Europeana entity collection. Otherwise get it from the source.
    final EnrichmentBase enrichedEntity = enrichmentClient.getByUri(resourceId);
    final Collection<EnrichmentBase> resultList;
    if (enrichedEntity == null) {
      resultList = dereferenceFromSource(resourceId);
    } else {
      resultList = Collections.singleton(enrichedEntity);
    }

    // Prepare the result: empty if we didn't find an entity.
    return new EnrichmentResultList(resultList);
  }

  /**
   * <p>
   * This method dereferences a resource. If the resource's vocabulary specifies a positive
   * iteration count, this method also repeatedly retrieves the 'broader' resources and returns
   * those as well.
   * </p>
   * <p>
   * A resource has references to its 'broader' resources (see
   * {@link #extractBroaderResources(EnrichmentBase, Set)}). As such, the resources form a directed
   * graph and the iteration count is the distance from the requested resource. This method performs
   * a breadth-first search through this graph to retrieve all resources within a certain distance
   * from the requested resource.
   * </p>
   * 
   * @param resourceId The resource to dereference.
   * @return A collection of dereferenced resources.
   * @throws JAXBException
   * @throws TransformerException
   * @throws URISyntaxException
   */
  private Collection<EnrichmentBase> dereferenceFromSource(String resourceId)
      throws JAXBException, TransformerException, URISyntaxException {

    // Get the main object to dereference. If null, we are done.
    final Pair<EnrichmentBase, Vocabulary> resource = retrieveCachedEntity(resourceId);
    if (resource == null) {
      return Collections.emptyList();
    }

    // Create value resolver that catches exceptions and logs them.
    final Function<String, EnrichmentBase> valueResolver = key -> {
      Pair<EnrichmentBase, Vocabulary> result;
      try {
        result = retrieveCachedEntity(key);
        return result == null ? null : result.getLeft();
      } catch (JAXBException | TransformerException | URISyntaxException e) {
        LOGGER.warn("Problem occurred while dereferencing broader resource " + key + ".", e);
        return null;
      }
    };

    // Perform the breadth-first search to search for broader terms (if needed).
    final int iterations = resource.getRight().getIterations();
    final Collection<EnrichmentBase> result;
    if (iterations > 0) {
      result = GraphUtils.breadthFirstSearch(resourceId, resource.getLeft(),
          resource.getRight().getIterations(), valueResolver, this::extractBroaderResources);
    } else {
      result = Collections.singleton(resource.getLeft());
    }

    // Done
    return result;
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

  private Pair<EnrichmentBase, Vocabulary> retrieveCachedEntity(String resourceId)
      throws JAXBException, TransformerException, URISyntaxException {

    // Try to get the entity and its vocabulary from the cache.
    final ProcessedEntity cachedEntity = cacheDao.get(resourceId);
    String entityString = null;
    Vocabulary vocabulary = null;
    if (cachedEntity != null) {
      entityString = cachedEntity.getXml();
      vocabulary = cachedEntity.getVocabularyId() == null ? null
          : vocabularyDao.get(cachedEntity.getVocabularyId());
    }

    // If we have the entity, but the vocabulary ID is no longer registered, try to get the
    // vocabulary without resolving the resource.
    final VocabularyCandidates candidates =
        VocabularyCandidates.findVocabulariesForUrl(resourceId, vocabularyDao::getByUriSearch);
    if (entityString != null && vocabulary == null) {
      vocabulary = candidates.findVocabularyWithoutTypeRules();
    }

    // If not in the cache, or no vocabulary was found, we need to resolve the resource.
    if (entityString == null || vocabulary == null) {
      final Pair<String, Vocabulary> transformedEntity =
          retrieveTransformedEntity(resourceId, candidates);
      if (transformedEntity != null) {
        entityString = transformedEntity.getLeft();
        vocabulary = transformedEntity.getRight();
        final ProcessedEntity entityToCache = new ProcessedEntity();
        entityToCache.setXml(entityString);
        entityToCache.setResourceId(resourceId);
        entityToCache.setVocabularyId(vocabulary.getId());
        cacheDao.save(entityToCache);
      }
    }

    // Parse the entity.
    final Pair<EnrichmentBase, Vocabulary> result;
    if (entityString != null && vocabulary != null) {
      final StringReader reader = new StringReader(entityString);
      final JAXBContext context = JAXBContext.newInstance(EnrichmentBase.class);
      final EnrichmentBase resource =
          (EnrichmentBase) context.createUnmarshaller().unmarshal(reader);
      result = new ImmutablePair<>(resource, vocabulary);
    } else {
      result = null;
    }

    // Done
    return result;
  }

  private Pair<String, Vocabulary> retrieveTransformedEntity(String resourceId,
      VocabularyCandidates candidates) throws TransformerException {

    // Get the original entity given the list of vocabulary candidates
    final String originalEntity = retrieveOriginalEntity(resourceId, candidates);

    // If we could not resolve the entity, or there was no vocabulary, we are done.
    if (originalEntity == null) {
      return null;
    }

    // Try to find the vocabulary.
    final Vocabulary vocabulary = candidates.findVocabularyForType(originalEntity);
    if (vocabulary == null) {
      return null;
    }

    // Transform the original entity.
    final IncomingRecordToEdmConverter converter = new IncomingRecordToEdmConverter(vocabulary);
    final String transformedEntity = converter.convert(originalEntity, resourceId);
    return new ImmutablePair<>(transformedEntity, vocabulary);
  }

  private String retrieveOriginalEntity(String resourceId, VocabularyCandidates candidates) {
    if (candidates.isEmpty()) {
      return null;
    }
    final String originalEntity = retriever.retrieve(resourceId, candidates.getCandidateSuffixes());
    if (originalEntity == null) {
      LOGGER.info("No entity XML for uri {}", resourceId);
    }
    return originalEntity;
  }
}
