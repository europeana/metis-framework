/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
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
  private RdfRetriever retriever;
  private CacheDao cacheDao;
  private EntityDao entityDao;
  private VocabularyDao vocabularyDao;
  private EnrichmentClient enrichmentClient;

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
  public EnrichmentResultList dereference(String resourceId) throws TransformerException, JAXBException {
    EnrichmentResultList toReturn = new EnrichmentResultList();
    EnrichmentBase fromEntity = checkInEntityCollection(resourceId);

    if (fromEntity != null) {
      toReturn.getResult().add(fromEntity);
      return toReturn;
    }

    EnrichmentBase enriched = readFromCache(resourceId);
    if (enriched != null) {
      toReturn.getResult().add(enriched);
      return toReturn;
    }
    
    final List<Vocabulary> vocabularies = VocabularyMatchUtils.findVocabulariesForResource(resourceId, vocabularyDao::getByUri);
    if (vocabularies.isEmpty()) {
      return toReturn;
    }

    enriched = transformEntity(resourceId, vocabularies);
    if (enriched != null) {
      toReturn.getResult().add(enriched);
    }
    
    return toReturn;
  }

  private EnrichmentBase transformEntity(String resourceId, List<Vocabulary> vocabularies)
      throws TransformerException, JAXBException {
    OriginalEntity originalEntity = entityDao.get(resourceId);
    
    if (originalEntity == null) {
      String originalXml = retriever.retrieve(resourceId);
      String value = originalXml.contains("<html>") ? null : originalXml;
      originalEntity = storeEntity(resourceId, value);
    }

    if (originalEntity.getXml() == null) {
      LOGGER.info("No entity XML for uri {}", resourceId);
      
      return null;
    }
    
    return getEnrichmentFromVocabularyAndStoreInCache(resourceId, vocabularies, originalEntity.getXml());
  }

  private OriginalEntity storeEntity(String resourceId, String value) {
    OriginalEntity originalEntity = new OriginalEntity();
    originalEntity.setURI(resourceId);
    originalEntity.setXml(value);

    entityDao.save(originalEntity);
    
    return originalEntity;
  }

  private EnrichmentBase getEnrichmentFromVocabularyAndStoreInCache(String resourceId, List<Vocabulary> vocabularies,
      String entityString) throws TransformerException, JAXBException {
    
    final Vocabulary vocabulary = VocabularyMatchUtils.findByEntity(vocabularies, entityString, resourceId);
    if (vocabulary == null) {
      return null;
    }

    final String transformed;
    try {
      transformed = new IncomingRecordToEdmConverter(vocabulary).convert(entityString, resourceId);
    } catch (TransformerException e) {
      LOGGER.error("Error transforming entity: {} with message :{}", resourceId, e.getMessage());
      throw e;
    }

    storeInCache(resourceId, transformed);

    return deserialize(transformed);
  }

  private void storeInCache(String resourceId, String transformed) {
    ProcessedEntity entity = new ProcessedEntity();
    entity.setXml(transformed);
    entity.setURI(resourceId);
    cacheDao.save(entity);
  }

  private EnrichmentBase readFromCache(String resourceId) throws JAXBException {
    ProcessedEntity entity = cacheDao.get(resourceId);
    return entity != null ? deserialize(entity.getXml()) : null;
  }

  private EnrichmentBase checkInEntityCollection(String resourceId) {
    return enrichmentClient.getByUri(resourceId);
  }

  private EnrichmentBase deserialize(String enrichment) throws JAXBException {
    final StringReader reader = new StringReader(enrichment);
    final JAXBContext context = JAXBContext.newInstance(EnrichmentBase.class);
    return (EnrichmentBase) context.createUnmarshaller().unmarshal(reader);
  }
}
