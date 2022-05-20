package eu.europeana.metis.dereference.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import eu.europeana.metis.dereference.ProcessedEntity;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.ProcessedEntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.dereference.vocimport.VocabularyCollectionImporterFactory;
import eu.europeana.metis.mongo.embedded.EmbeddedLocalhostMongo;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MongoDereferencingManagementServiceTest {

  private MongoDereferencingManagementService service;
  private final EmbeddedLocalhostMongo embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
  private Datastore vocabularyDaoDatastore;
  private final ProcessedEntityDao processedEntityDao = mock(ProcessedEntityDao.class);

  @BeforeEach
  void prepare() {
    embeddedLocalhostMongo.start();
    String mongoHost = embeddedLocalhostMongo.getMongoHost();
    int mongoPort = embeddedLocalhostMongo.getMongoPort();

    MongoClient mongoClient = MongoClients
        .create(String.format("mongodb://%s:%s", mongoHost, mongoPort));

    VocabularyDao vocDao = new VocabularyDao(mongoClient, "voctest") {
      {
        vocabularyDaoDatastore = this.getDatastore();
      }
    };
    VocabularyCollectionImporterFactory vocabularyCollectionImporterFactory = mock(VocabularyCollectionImporterFactory.class);

    service = new MongoDereferencingManagementService(vocDao, processedEntityDao, vocabularyCollectionImporterFactory);
  }

  @Test
  void testGetAllVocabularies() {
    Vocabulary voc = new Vocabulary();
    voc.setIterations(0);
    voc.setName("testName");
    voc.setUris(Collections.singleton("http://www.test.uri/"));
    voc.setXslt("testXSLT");
    vocabularyDaoDatastore.save(voc);
    List<Vocabulary> retVoc = service.getAllVocabularies();
    assertEquals(1, retVoc.size());
  }


  @Test
  void purgeAllCache() {
    ProcessedEntity processedEntity = new ProcessedEntity();
    processedEntity.setResourceId("http://www.test.uri/");
    processedEntityDao.save(processedEntity);
    service.emptyCache();
    ProcessedEntity ret = processedEntityDao.getByResourceId("http://www.test.uri/");
    assertNull(ret);
  }

  @Test
  void purgeCacheWithEmptyXML() {
    ProcessedEntity processedEntity = new ProcessedEntity();
    processedEntity.setResourceId("http://www.test.uri/");
    processedEntity.setXml(null);
    processedEntityDao.save(processedEntity);
    service.purgeByNullOrEmptyXml();
    ProcessedEntity ret = processedEntityDao.getByResourceId("http://www.test.uri/");
    assertNull(ret);
  }


  @Test
  void purgeCacheByResourceId() {
    ProcessedEntity processedEntity = new ProcessedEntity();
    processedEntity.setResourceId("http://www.test.uri/");
    processedEntityDao.save(processedEntity);
    service.purgeByResourceId("http://www.test.uri/");
    ProcessedEntity ret = processedEntityDao.getByResourceId("http://www.test.uri/");
    assertNull(ret);
  }

  @Test
  void purgeCacheByVocabularyId() {
    ProcessedEntity processedEntity = new ProcessedEntity();
    processedEntity.setVocabularyId("vocabularyId");
    processedEntityDao.save(processedEntity);
    service.purgeByVocabularyId("vocabularyId");
    ProcessedEntity ret = processedEntityDao.getByVocabularyId("vocabularyId");
    assertNull(ret);
  }

  @AfterEach
  void destroy() {
    embeddedLocalhostMongo.stop();
  }
}
