package eu.europeana.metis.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.DeleteOptions;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetXslt;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProviderImpl;
import eu.europeana.metis.core.utils.TestObjectFactory;
import eu.europeana.metis.mongo.embedded.EmbeddedLocalhostMongo;
import java.util.Date;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-03-02
 */
class TestDatasetXsltDao {

  private static DatasetXsltDao datasetXsltDao;
  private static DatasetXslt datasetXslt;
  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;
  private static MorphiaDatastoreProviderImpl provider;

  @BeforeAll
  static void prepare() {
    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    embeddedLocalhostMongo.start();
    String mongoHost = embeddedLocalhostMongo.getMongoHost();
    int mongoPort = embeddedLocalhostMongo.getMongoPort();
    MongoClient mongoClient = MongoClients
        .create(String.format("mongodb://%s:%s", mongoHost, mongoPort));
    provider = new MorphiaDatastoreProviderImpl(mongoClient, "test");

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
    datastore.find(DatasetXslt.class).delete(new DeleteOptions().multi(true));
  }

  @Test
  void testCreateRetrieveXslt() {
    String xsltId = datasetXsltDao.create(datasetXslt).getId().toString();
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
    String xsltId = datasetXsltDao.create(datasetXslt).getId().toString();
    DatasetXslt storedDatasetXslt = datasetXsltDao.getById(xsltId);
    datasetXsltDao.delete(storedDatasetXslt);
    storedDatasetXslt = datasetXsltDao.getById(xsltId);
    assertNull(storedDatasetXslt);
  }

  @Test
  void testDeleteAllByDatasetId() {
    String xsltId1 = datasetXsltDao.create(datasetXslt).getId().toString();
    String xsltId2 = datasetXsltDao.create(datasetXslt).getId().toString();
    String xsltId3 = datasetXsltDao.create(datasetXslt).getId().toString();
    assertTrue(datasetXsltDao.deleteAllByDatasetId(datasetXslt.getDatasetId()));
    assertNull(datasetXsltDao.getById(xsltId1));
    assertNull(datasetXsltDao.getById(xsltId2));
    assertNull(datasetXsltDao.getById(xsltId3));
  }

  @Test
  void getLatestXsltForDatasetId() {
    final Dataset dataset = TestObjectFactory.createDataset("testName");
    final DatasetXslt datasetXslt1 = TestObjectFactory.createXslt(dataset);
    datasetXslt1.setCreatedDate(new Date(1000));
    final DatasetXslt datasetXslt2 = TestObjectFactory.createXslt(dataset);
    datasetXslt2.setCreatedDate(new Date(2000));
    final DatasetXslt datasetXslt3 = TestObjectFactory.createXslt(dataset);
    datasetXslt3.setCreatedDate(new Date(3000));

    datasetXsltDao.create(datasetXslt1);
    datasetXsltDao.create(datasetXslt2);
    String xsltId3 = datasetXsltDao.create(datasetXslt3).getId().toString();
    DatasetXslt latestDatasetXsltForDatasetId = datasetXsltDao
        .getLatestXsltForDatasetId(datasetXslt3.getDatasetId());
    assertEquals(xsltId3, latestDatasetXsltForDatasetId.getId().toString());
  }
}
