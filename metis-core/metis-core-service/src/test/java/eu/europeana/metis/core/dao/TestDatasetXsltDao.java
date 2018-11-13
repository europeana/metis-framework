package eu.europeana.metis.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetXslt;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mongodb.morphia.Datastore;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-03-02
 */
class TestDatasetXsltDao {

  private static DatasetXsltDao datasetXsltDao;
  private static DatasetXslt datasetXslt;
  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;
  private static MorphiaDatastoreProvider provider;

  @BeforeAll
  static void prepare() {
    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    embeddedLocalhostMongo.start();
    String mongoHost = embeddedLocalhostMongo.getMongoHost();
    int mongoPort = embeddedLocalhostMongo.getMongoPort();
    ServerAddress address = new ServerAddress(mongoHost, mongoPort);
    MongoClient mongoClient = new MongoClient(address);
    provider = new MorphiaDatastoreProvider(mongoClient, "test");

    datasetXsltDao = new DatasetXsltDao(provider);

    Dataset dataset = TestObjectFactory.createDataset("testName");
    datasetXslt = TestObjectFactory.createXslt(dataset);
  }

  @AfterAll
  static void destroy() {
    embeddedLocalhostMongo.stop();
  }

  @AfterEach
  void cleanUp() {
    Datastore datastore = provider.getDatastore();
    datastore.delete(datastore.createQuery(DatasetXslt.class));
  }

  @Test
  void testCreateRetrieveXslt() {
    String xsltId = datasetXsltDao.create(datasetXslt);
    DatasetXslt storedDatasetXslt = datasetXsltDao.getById(xsltId);
    assertEquals(datasetXslt.getDatasetId(), storedDatasetXslt.getDatasetId());
    assertEquals(datasetXslt.getXslt(), storedDatasetXslt.getXslt());
  }

  @Test
  void testUpdateRetrieveXslt() {
    datasetXsltDao.create(datasetXslt);
    String xsltId = datasetXsltDao.update(datasetXslt);

    DatasetXslt storedDatasetXslt = datasetXsltDao.getById(xsltId);
    assertEquals(datasetXslt.getDatasetId(), storedDatasetXslt.getDatasetId());
    assertEquals(datasetXslt.getXslt(), storedDatasetXslt.getXslt());
  }

  @Test
  void testDeleteXslt() {
    String xsltId = datasetXsltDao.create(datasetXslt);
    DatasetXslt storedDatasetXslt = datasetXsltDao.getById(xsltId);
    datasetXsltDao.delete(storedDatasetXslt);
    storedDatasetXslt = datasetXsltDao.getById(xsltId);
    assertNull(storedDatasetXslt);
  }

  @Test
  void testDeleteAllByDatasetId() {
    String xsltId1 = datasetXsltDao.create(datasetXslt);
    String xsltId2 = datasetXsltDao.create(datasetXslt);
    String xsltId3 = datasetXsltDao.create(datasetXslt);
    assertTrue(datasetXsltDao.deleteAllByDatasetId(datasetXslt.getDatasetId()));
    assertNull(datasetXsltDao.getById(xsltId1));
    assertNull(datasetXsltDao.getById(xsltId2));
    assertNull(datasetXsltDao.getById(xsltId3));
  }

  @Test
  void getLatestXsltForDatasetId() {
    datasetXsltDao.create(datasetXslt);
    datasetXsltDao.create(datasetXslt);
    String xsltId3 = datasetXsltDao.create(datasetXslt);
    DatasetXslt latestDatasetXsltForDatasetId = datasetXsltDao.getLatestXsltForDatasetId(datasetXslt.getDatasetId());
    assertEquals(xsltId3, latestDatasetXsltForDatasetId.getId().toString());
  }
}
