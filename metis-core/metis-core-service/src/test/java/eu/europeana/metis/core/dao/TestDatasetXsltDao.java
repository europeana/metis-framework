package eu.europeana.metis.core.dao;

import static org.junit.Assert.assertEquals;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetXslt;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.morphia.Datastore;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-03-02
 */
public class TestDatasetXsltDao {

  private static DatasetXsltDao datasetXsltDao;
  private static DatasetXslt datasetXslt;
  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;
  private static MorphiaDatastoreProvider provider;

  @BeforeClass
  public static void prepare() throws IOException {
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

  @AfterClass
  public static void destroy() {
    embeddedLocalhostMongo.stop();
  }

  @After
  public void cleanUp() {
    Datastore datastore = provider.getDatastore();
    datastore.delete(datastore.createQuery(DatasetXslt.class));
  }

  @Test
  public void testCreateRetrieveXslt() {
    String xsltId = datasetXsltDao.create(datasetXslt);
    DatasetXslt storedDatasetXslt = datasetXsltDao.getById(xsltId);
    assertEquals(datasetXslt.getDatasetId(), storedDatasetXslt.getDatasetId());
    assertEquals(datasetXslt.getXslt(), storedDatasetXslt.getXslt());
  }

  @Test
  public void testUpdateRetrieveXslt() {
    datasetXsltDao.create(datasetXslt);
    String xsltId = datasetXsltDao.update(datasetXslt);

    DatasetXslt storedDatasetXslt = datasetXsltDao.getById(xsltId);
    assertEquals(datasetXslt.getDatasetId(), storedDatasetXslt.getDatasetId());
    assertEquals(datasetXslt.getXslt(), storedDatasetXslt.getXslt());
  }

  @Test
  public void testDeleteXslt() {
    String xsltId = datasetXsltDao.create(datasetXslt);
    DatasetXslt storedDatasetXslt = datasetXsltDao.getById(xsltId);
    datasetXsltDao.delete(storedDatasetXslt);
    storedDatasetXslt = datasetXsltDao.getById(xsltId);
    Assert.assertNull(storedDatasetXslt);
  }

  @Test
  public void testDeleteAllByDatasetId() {
    String xsltId1 = datasetXsltDao.create(datasetXslt);
    String xsltId2 = datasetXsltDao.create(datasetXslt);
    String xsltId3 = datasetXsltDao.create(datasetXslt);
    Assert.assertTrue(datasetXsltDao.deleteAllByDatasetId(datasetXslt.getDatasetId()));
    Assert.assertNull(datasetXsltDao.getById(xsltId1));
    Assert.assertNull(datasetXsltDao.getById(xsltId2));
    Assert.assertNull(datasetXsltDao.getById(xsltId3));
  }

  @Test
  public void getLatestXsltForDatasetId() {
    datasetXsltDao.create(datasetXslt);
    datasetXsltDao.create(datasetXslt);
    String xsltId3 = datasetXsltDao.create(datasetXslt);
    DatasetXslt latestDatasetXsltForDatasetId = datasetXsltDao.getLatestXsltForDatasetId(datasetXslt.getDatasetId());
    Assert.assertEquals(xsltId3, latestDatasetXsltForDatasetId.getId().toString());
  }
}
