package eu.europeana.metis.core.dao;

import static org.junit.Assert.assertEquals;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.Xslt;
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
public class TestXsltDao {

  private static XsltsDao xsltsDao;
  private static Xslt xslt;
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

    xsltsDao = new XsltsDao(provider);

    Dataset dataset = TestObjectFactory.createDataset("testName");
    xslt = TestObjectFactory.createXslt(dataset);
  }

  @AfterClass
  public static void destroy() {
    embeddedLocalhostMongo.stop();
  }

  @After
  public void cleanUp() {
    Datastore datastore = provider.getDatastore();
    datastore.delete(datastore.createQuery(Xslt.class));
  }

  @Test
  public void testCreateRetrieveXslt() {
    String xsltId = xsltsDao.create(xslt);
    Xslt storedXslt = xsltsDao.getById(xsltId);
    assertEquals(xslt.getDatasetId(), storedXslt.getDatasetId());
    assertEquals(xslt.getXslt(), storedXslt.getXslt());
  }

  @Test
  public void testUpdateRetrieveXslt() {
    xsltsDao.create(xslt);
    String xsltId = xsltsDao.update(xslt);

    Xslt storedXslt = xsltsDao.getById(xsltId);
    assertEquals(xslt.getDatasetId(), storedXslt.getDatasetId());
    assertEquals(xslt.getXslt(), storedXslt.getXslt());
  }

  @Test
  public void testDeleteXslt() {
    String xsltId = xsltsDao.create(xslt);
    Xslt storedXslt = xsltsDao.getById(xsltId);
    xsltsDao.delete(storedXslt);
    storedXslt = xsltsDao.getById(xsltId);
    Assert.assertNull(storedXslt);
  }

  @Test
  public void testDeleteAllByDatasetId() {
    String xsltId1 = xsltsDao.create(xslt);
    String xsltId2 = xsltsDao.create(xslt);
    String xsltId3 = xsltsDao.create(xslt);
    Assert.assertTrue(xsltsDao.deleteAllByDatasetId(xslt.getDatasetId()));
    Assert.assertNull(xsltsDao.getById(xsltId1));
    Assert.assertNull(xsltsDao.getById(xsltId2));
    Assert.assertNull(xsltsDao.getById(xsltId3));
  }

  @Test
  public void getLatestXsltForDatasetId() {
    xsltsDao.create(xslt);
    xsltsDao.create(xslt);
    String xsltId3 = xsltsDao.create(xslt);
    Xslt latestXsltForDatasetId = xsltsDao.getLatestXsltForDatasetId(xslt.getDatasetId());
    Assert.assertEquals(xsltId3, latestXsltForDatasetId.getId().toString());
  }

}
