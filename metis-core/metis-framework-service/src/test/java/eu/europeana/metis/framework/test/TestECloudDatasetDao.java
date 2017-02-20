package eu.europeana.metis.framework.test;

import eu.europeana.cloud.client.uis.rest.CloudException;
import eu.europeana.cloud.client.uis.rest.UISClient;
import eu.europeana.cloud.common.model.DataProviderProperties;
import eu.europeana.cloud.common.model.DataSet;
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.metis.framework.dao.ecloud.EcloudDatasetDao;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-02-20
 */
public class TestECloudDatasetDao {
  private final static Logger LOGGER = LoggerFactory.getLogger(TestECloudDatasetDao.class);

  private static EcloudDatasetDao ecloudDatasetDao;
  private static DataSetServiceClient dataSetServiceClient;
  private static UISClient uisClient;
  private static DataSet dataSet;
  private static String baseMcsf = "ecloud.baseMcsUrl";
  private static String baseUisf = "ecloud.baseUisUrl";
  private static String usernamef = "ecloud.username";
  private static String passwordf = "ecloud.password";
  private static String ecloudProviderf = "ecloud.provider";
  private static String ecloudPropertiesFile = "ecloud.properties";

  @BeforeClass
  public static void beforeTests() throws IOException {
    Properties properties = new Properties();
    InputStream input = null;
    input = TestECloudDatasetDao.class.getClassLoader().getResourceAsStream(ecloudPropertiesFile);

    if(input==null){
      LOGGER.error("Unable to find properties file: " + ecloudPropertiesFile);
      return;
    }

    properties.load(input);
    uisClient = new UISClient(properties.getProperty(baseUisf), properties.getProperty(usernamef), properties.getProperty(passwordf));
    ecloudDatasetDao = new EcloudDatasetDao();
    dataSetServiceClient = new DataSetServiceClient(properties.getProperty(baseMcsf), properties.getProperty(usernamef), properties.getProperty(passwordf));
    ReflectionTestUtils.setField(ecloudDatasetDao,"dataSetServiceClient",dataSetServiceClient);
    ecloudDatasetDao.setEcloudProvider(properties.getProperty(ecloudProviderf));

    try {
      uisClient.createProvider("sprovider1s",
          new DataProviderProperties("sprovider1s", "sprovider1s", "sprovider1s", "sprovider1s",
              "sprovider1s", "sprovider1s", "sprovider1s", "sprovider1s"));
    } catch (CloudException e) {
      if (e.getMessage().equals("PROVIDER_ALREADY_EXISTS")) {
        LOGGER.info("Provider not created already existent");
      } else {
        return;
      }
    }

    dataSet = new DataSet();
    dataSet.setId("sdataset1s");
    dataSet.setProviderId(ecloudDatasetDao.getEcloudProvider());
    dataSet.setDescription("test_description");
  }

  @Test
  public void testCreateDataset() {
    String uri = ecloudDatasetDao.create(dataSet);
    Assert.assertNotNull(uri);
    ecloudDatasetDao.delete(dataSet);
  }

  @Test
  public void testGetDatasetById() {
    ecloudDatasetDao.create(dataSet);
    DataSet ds = ecloudDatasetDao.getById(dataSet.getId());
    Assert.assertEquals(dataSet.getId(), ds.getId());
    Assert.assertEquals(dataSet.getProviderId(), ds.getProviderId());
    Assert.assertEquals(dataSet.getDescription(), ds.getDescription());
    ecloudDatasetDao.delete(dataSet);
  }

  @Test
  public void testUpdateDataset() {
    ecloudDatasetDao.create(dataSet);
    dataSet.setDescription("changed");
    String id = ecloudDatasetDao.update(dataSet);
    Assert.assertNotNull(id);
    DataSet ds = ecloudDatasetDao.getById(dataSet.getId());
    Assert.assertEquals(dataSet.getDescription(), ds.getDescription());
  }

  @Test
  public void testDeleteDataset() {
    ecloudDatasetDao.create(dataSet);
    boolean deleted = ecloudDatasetDao.delete(dataSet);
    Assert.assertTrue(deleted);
  }

  @AfterClass
  public static void afterTests() {
    // TODO: 20-2-17 Delete provider when done. UIS client doesn't support it yet.

    //Delete dataset
    ecloudDatasetDao.delete(dataSet);
  }

}
