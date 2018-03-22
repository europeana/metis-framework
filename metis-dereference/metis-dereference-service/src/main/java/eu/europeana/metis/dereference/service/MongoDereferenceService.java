package eu.europeana.metis.dereference.service;

import java.io.StringReader;
import java.net.URISyntaxException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.rest.client.EnrichmentClient;
import eu.europeana.metis.dereference.ProcessedEntity;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.CacheDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
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

    // First try to get the entity from the entity collection.
    EnrichmentBase enrichedEntity = enrichmentClient.getByUri(resourceId);

    // Otherwise, get it from the source.
    if (enrichedEntity == null) {
      enrichedEntity = retrieveCachedEntity(resourceId);
    }

    // Prepare the result: empty if we didn't find an entity.
    final EnrichmentResultList result = new EnrichmentResultList();
    if (enrichedEntity != null) {
      result.getResult().add(enrichedEntity);
    }

    // Done.
    return result;
  }

  private EnrichmentBase retrieveCachedEntity(String resourceId)
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
    final EnrichmentBase result;
    if (entityString != null) {
      final StringReader reader = new StringReader(entityString);
      final JAXBContext context = JAXBContext.newInstance(EnrichmentBase.class);
      result = (EnrichmentBase) context.createUnmarshaller().unmarshal(reader);
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
