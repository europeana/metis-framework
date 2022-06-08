package eu.europeana.metis.dereference.service.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import eu.europeana.metis.dereference.ProcessedEntity;
import eu.europeana.metis.dereference.service.dao.ProcessedEntityDao;
import eu.europeana.metis.mongo.embedded.EmbeddedLocalhostMongo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ProcessedEntityDao} class
 */
class ProcessedEntityDaoTest {

  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;

  private static ProcessedEntityDao processedEntityDao;

  @BeforeAll
  static void prepare() {
    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();

    embeddedLocalhostMongo.start();
    final String mongoHost = embeddedLocalhostMongo.getMongoHost();
    final int mongoPort = embeddedLocalhostMongo.getMongoPort();

    final MongoClient mongoClient = MongoClients.create(String.format("mongodb://%s:%s", mongoHost, mongoPort));
    processedEntityDao = new ProcessedEntityDao(mongoClient, "metis-dereference");
  }

  @BeforeEach
  void setupDb() {
    initDatabaseWithEntities();
  }

  @AfterEach
  void tearDownDb() {
    processedEntityDao.purgeAll();
  }

  @Test
  void processDaoPurgeAll() {
    assertEquals(5, processedEntityDao.size());

    processedEntityDao.purgeAll();

    for (int i = 1; i < 5; i++) {
      assertNull(processedEntityDao.getByResourceId("http://www.test" + i + ".uri/"));
    }
    assertEquals(0, processedEntityDao.size());
  }

  @Test
  void processDaoPurgeByNullOrEmptyXML() {
    assertEquals(5, processedEntityDao.size());

    processedEntityDao.purgeByNullOrEmptyXml();

    assertNull(processedEntityDao.getByResourceId("http://www.test5.uri/"));
    for (int i = 1; i < 4; i++) {
      assertEquals("http://www.test" + i + ".uri/",
          processedEntityDao.getByResourceId("http://www.test" + i + ".uri/").getResourceId());
    }
    assertEquals(4, processedEntityDao.size());
  }

  @Test
  void processDaoPurgeByResourceId() {
    assertEquals(5, processedEntityDao.size());

    processedEntityDao.purgeByResourceId("http://www.test1.uri/");

    assertNull(processedEntityDao.getByResourceId("http://www.test1.uri/"));
    assertEquals(4, processedEntityDao.size());
  }

  @Test
  void processDaoPurgeByVocabulary() {
    assertEquals(5, processedEntityDao.size());

    processedEntityDao.purgeByVocabularyId("vocabularyId1");

    assertNull(processedEntityDao.getByVocabularyId("vocabularyId1"));
    for (int i = 2; i < 5; i++) {
      assertEquals("vocabularyId" + i,
          processedEntityDao.getByVocabularyId("vocabularyId" + i).getVocabularyId());
    }
    assertEquals(4, processedEntityDao.size());
  }

  // side note flapdoodle embedded mongo
  // doesn't support DuplicateExceptionKey Mongo.

  void initDatabaseWithEntities() {
    for (int i = 1; i <= 5; i++) {
      final ProcessedEntity processedEntity = new ProcessedEntity();
      processedEntity.setResourceId("http://www.test" + i + ".uri/");
      processedEntity.setVocabularyId("vocabularyId" + i);
      if (i == 5) {
        processedEntity.setXml(null);
      } else {
        processedEntity.setXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?><item>value</item>");
      }
      processedEntityDao.save(processedEntity);
    }
  }

  @AfterAll
  static void destroy() {
    embeddedLocalhostMongo.stop();
  }
}
