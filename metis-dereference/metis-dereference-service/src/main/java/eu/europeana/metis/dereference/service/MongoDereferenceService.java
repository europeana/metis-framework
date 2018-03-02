package eu.europeana.metis.dereference.service;

import java.io.StringReader;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.rest.client.EnrichmentClient;
import eu.europeana.metis.dereference.OriginalEntity;
import eu.europeana.metis.dereference.ProcessedEntity;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.CacheDao;
import eu.europeana.metis.dereference.service.dao.EntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.dereference.service.utils.RdfRetriever;
import eu.europeana.metis.dereference.service.utils.VocabularyMatchUtils;
import eu.europeana.metis.dereference.service.utils.IncomingRecordToEdmConverter;

/**
 * Mongo implementation of the dereference service Created by ymamakis on 2/11/16.
 */
public class MongoDereferenceService implements DereferenceService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoDereferenceService.class);

  private final RdfRetriever retriever;
  private final CacheDao cacheDao;
  private final EntityDao entityDao;
  private final VocabularyDao vocabularyDao;
  private final EnrichmentClient enrichmentClient;

  @Autowired
  public MongoDereferenceService(RdfRetriever retriever, CacheDao cacheDao, EntityDao entityDao,
      VocabularyDao vocabularyDao, EnrichmentClient enrichmentClient) {
    this.retriever = retriever;
    this.cacheDao = cacheDao;
    this.entityDao = entityDao;
    this.vocabularyDao = vocabularyDao;
    this.enrichmentClient = enrichmentClient;
  }

  @Override
  public EnrichmentResultList dereference(String resourceId)
      throws TransformerException, JAXBException {

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
      throws JAXBException, TransformerException {

    // Try to get it from the cache.
    final ProcessedEntity cachedEntity = cacheDao.get(resourceId);
    String entityString = cachedEntity != null ? cachedEntity.getXml() : null;

    // If it is not in the cache, get it from the source vocabulary and cache it.
    if (entityString == null) {
      entityString = retrieveFromSourceVocabulary(resourceId);
      if (entityString != null) {
        final ProcessedEntity entityToCache = new ProcessedEntity();
        entityToCache.setXml(entityString);
        entityToCache.setURI(resourceId);
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

  private String retrieveFromSourceVocabulary(String resourceId) throws TransformerException {

    // Get the vocabularies that potentially match the given resource ID.
    final List<Vocabulary> vocabularyCandidates =
        VocabularyMatchUtils.findVocabulariesForResource(resourceId, vocabularyDao::getByUri);
    if (vocabularyCandidates.isEmpty()) {
      return null;
    }

    // Obtain the original entity
    final String originalEntity = retrieveStoredOriginalEntity(resourceId);
    if (originalEntity == null) {
      LOGGER.info("No entity XML for uri {}", resourceId);
      return null;
    }

    // Find the vocabulary that applies to the entity.
    final Vocabulary vocabulary =
        VocabularyMatchUtils.findByEntity(vocabularyCandidates, originalEntity, resourceId);
    if (vocabulary == null) {
      return null;
    }

    // Transform the original entity.
    try {
      return new IncomingRecordToEdmConverter(vocabulary).convert(originalEntity, resourceId);
    } catch (TransformerException e) {
      LOGGER.error("Error transforming entity: {} with message :{}", resourceId, e.getMessage());
      throw e;
    }
  }

  private String retrieveStoredOriginalEntity(String resourceId) {

    // Get the entity from the own store
    OriginalEntity originalEntity = entityDao.get(resourceId);

    // If we can't find it, get it from the remote source.
    if (originalEntity == null) {
      String originalXml = retriever.retrieve(resourceId);
      String value = originalXml.contains("<html>") ? null : originalXml;
      originalEntity = new OriginalEntity();
      originalEntity.setURI(resourceId);
      originalEntity.setXml(value);
      entityDao.save(originalEntity);
    }

    // Done.
    return originalEntity.getXml();
  }
}
