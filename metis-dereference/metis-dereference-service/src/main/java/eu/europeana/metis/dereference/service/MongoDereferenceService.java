package eu.europeana.metis.dereference.service;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
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
import eu.europeana.metis.dereference.service.xslt.XsltTransformer;

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
  public MongoDereferenceService(RdfRetriever retriever,
		  CacheDao cacheDao, EntityDao entityDao,
		  VocabularyDao vocabularyDao,
		  EnrichmentClient enrichmentClient) {
	  this.retriever = retriever;
	  this.cacheDao = cacheDao;
	  this.entityDao = entityDao;
	  this.vocabularyDao = vocabularyDao;
	  this.enrichmentClient = enrichmentClient;
  }

  @Override
  public EnrichmentResultList dereference(String uri)
      throws TransformerException, ParserConfigurationException, IOException, JAXBException {
    EnrichmentResultList toReturn = new EnrichmentResultList();
    EnrichmentBase fromEntity = checkInEntityCollection(uri);

    if (fromEntity != null) {
      toReturn.getResult().add(fromEntity);
      return toReturn;
    }

    String[] splitName = uri.split("/");
    if (splitName.length <= 3) {
      LOGGER.info("Invalid uri {}", uri);
      return toReturn;
    }

    String vocabularyUri = splitName[0] + "/" + splitName[1] + "/"
        + splitName[2] + "/";
    List<Vocabulary> vocs = vocabularyDao.getByUri(vocabularyUri);

    if (vocs == null || vocs.isEmpty()) {
      LOGGER.info("No vocabularies found for uri {}", uri);
      return toReturn;
    }

    EnrichmentBase enriched = readFromCache(uri);
    if (enriched != null) {
      toReturn.getResult().add(enriched);
      return toReturn;
    }

    enriched = transformEntity(uri, vocs);
    if (enriched != null) {
      toReturn.getResult().add(enriched);
    }
    
    return toReturn;
  }

  private EnrichmentBase transformEntity(String uri, List<Vocabulary> vocs)
      throws TransformerException, ParserConfigurationException, JAXBException {
    OriginalEntity originalEntity = entityDao.getByUri(uri);
    
    if (originalEntity == null) {
      String originalXml = retriever.retrieve(uri);
      String value =originalXml.contains("<html>") ? null : originalXml;
      originalEntity = storeEntity(uri, value);
    }

    if (originalEntity.getXml() == null) {
      LOGGER.info("No entity XML for uri {}", uri);
      
      return null;
    }
    
    return getEnrichmentFromVocabularyAndStoreInCache(uri, vocs, originalEntity.getXml());
  }

  private OriginalEntity storeEntity(String uri, String value) {
    OriginalEntity originalEntity = new OriginalEntity();
    originalEntity.setURI(uri);
    originalEntity.setXml(value);

    entityDao.save(originalEntity);
    
    return originalEntity;
  }

  private EnrichmentBase getEnrichmentFromVocabularyAndStoreInCache(String uri, List<Vocabulary> vocs,
      String entityString)
      throws TransformerException, ParserConfigurationException, JAXBException {
    
    final Vocabulary vocabulary = VocabularyDao.findByEntity(vocs, entityString, uri);
    if (vocabulary == null) {
      return null;
    }

    String transformed;
    try {
      transformed = new XsltTransformer()
          .transform(entityString, vocabulary.getXslt());
    } catch (ParserConfigurationException | TransformerException e) {
      LOGGER.error("Error transforming entity: " + uri + " with message :" + e.getMessage());
      throw e;
    }

    storeInCache(uri, transformed);

    return deserialize(transformed);
  }

  private void storeInCache(String uri, String transformed) {
    ProcessedEntity entity = new ProcessedEntity();
    entity.setXml(transformed);
    entity.setURI(uri);
    cacheDao.save(entity);
  }

  private EnrichmentBase readFromCache(String uri) throws JAXBException {
    ProcessedEntity entity = cacheDao.getByUri(uri);
    
    return entity!= null ? deserialize(entity.getXml()): null;
  }

  private EnrichmentBase checkInEntityCollection(String uri) throws IOException {
    return enrichmentClient.getByUri(uri);
  }

  private EnrichmentBase deserialize(String enrichment) throws JAXBException {
    JAXBContext contextA = JAXBContext.newInstance(EnrichmentBase.class);
    StringReader reader = new StringReader(enrichment);
    Unmarshaller unmarshaller = contextA.createUnmarshaller();
    
    return (EnrichmentBase) unmarshaller.unmarshal(reader);
  }
}
