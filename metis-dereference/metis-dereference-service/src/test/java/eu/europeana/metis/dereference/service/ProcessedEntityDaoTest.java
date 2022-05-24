package eu.europeana.metis.dereference.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import eu.europeana.metis.dereference.ProcessedEntity;
import eu.europeana.metis.dereference.service.dao.ProcessedEntityDao;
import eu.europeana.metis.mongo.embedded.EmbeddedLocalhostMongo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ProcessedEntityDaoTest {


  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;

  private static ProcessedEntityDao processedEntityDao;


  @BeforeAll
  static void prepare() {
    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    embeddedLocalhostMongo.start();
    String mongoHost = embeddedLocalhostMongo.getMongoHost();
    int mongoPort = embeddedLocalhostMongo.getMongoPort();
    MongoClient mongoClient = MongoClients.create(String.format("mongodb://%s:%s", mongoHost, mongoPort));
    processedEntityDao = new ProcessedEntityDao(mongoClient, "metis-dereference");
  }


  @Test
  void processDaoPurgeAll() {

    ProcessedEntity processedEntity = new ProcessedEntity();
    processedEntity.setResourceId("http://www.test.uri/");
    processedEntity.setVocabularyId("vocabularyId");
    processedEntityDao.save(processedEntity);
    processedEntityDao.purgeAll();
    assertNull(processedEntityDao.getByResourceId("http://www.test.uri/"));
  }

  @Test
  void processDaoPurgeByNullOrEmptyXML() {

    ProcessedEntity processedEntity = new ProcessedEntity();
    processedEntity.setResourceId("http://www.test.uri/");
    processedEntity.setVocabularyId("vocabularyId");
    processedEntity.setXml(null);
    processedEntityDao.save(processedEntity);
    processedEntityDao.purgeByNullOrEmptyXml();
    assertNull(processedEntityDao.getByResourceId("http://www.test.uri/"));
  }

  @Test
  void processDaoPurgeByResourceId() {

    ProcessedEntity processedEntity = new ProcessedEntity();
    processedEntity.setResourceId("http://www.test.uri/");
    processedEntity.setVocabularyId("vocabularyId");
    processedEntityDao.save(processedEntity);
    processedEntityDao.purgeByResourceId("http://www.test.uri/");
    assertNull(processedEntityDao.getByResourceId("http://www.test.uri/"));
  }

  @Test
  void processDaoPurgeByVocabulary() {

    ProcessedEntity processedEntity = new ProcessedEntity();
    processedEntity.setResourceId("http://www.test.uri/");
    processedEntity.setVocabularyId("vocabularyId");
    processedEntityDao.save(processedEntity);
    processedEntityDao.purgeByVocabularyId("vocabularyId");
    assertNull(processedEntityDao.getByVocabularyId("vocabularyId"));
  }

  @AfterAll
  static void destroy() {
    embeddedLocalhostMongo.stop();
  }


}
