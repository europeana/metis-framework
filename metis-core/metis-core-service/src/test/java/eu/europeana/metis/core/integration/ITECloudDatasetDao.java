package eu.europeana.metis.core.integration;

//import eu.europeana.cloud.client.uis.rest.CloudException;
//import eu.europeana.cloud.client.uis.rest.UISClient;
import eu.europeana.cloud.common.model.DataSet;
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.metis.core.dao.ecloud.EcloudDatasetDao;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import software.betamax.junit.Betamax;
import software.betamax.junit.RecorderRule;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-02-20
 */
// TODO: 7-3-17 Fix tests by adding create and delete provider when uis is ready to be used
//  Currently the uis dependency does not permit the deployment of framework-rest because of
//  some spring issues with applicationContext.xml
public class ITECloudDatasetDao {
  private final static Logger LOGGER = LoggerFactory.getLogger(ITECloudDatasetDao.class);
  @ClassRule
  public static RecorderRule recorder = new RecorderRule();

  private static EcloudDatasetDao ecloudDatasetDao;
  private static DataSetServiceClient dataSetServiceClient;
//  private static UISClient uisClient;
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
    input = ITECloudDatasetDao.class.getClassLoader().getResourceAsStream(ecloudPropertiesFile);

    if(input==null){
      LOGGER.error("Unable to find properties file: " + ecloudPropertiesFile);
      return;
    }

    properties.load(input);
//    uisClient = new UISClient(properties.getProperty(baseUisf), properties.getProperty(usernamef), properties.getProperty(passwordf));
    ecloudDatasetDao = new EcloudDatasetDao();
    dataSetServiceClient = new DataSetServiceClient(properties.getProperty(baseMcsf), properties.getProperty(usernamef), properties.getProperty(passwordf));
    ReflectionTestUtils.setField(ecloudDatasetDao,"dataSetServiceClient",dataSetServiceClient);
    ecloudDatasetDao.setEcloudProvider(properties.getProperty(ecloudProviderf));

    dataSet = new DataSet();
    dataSet.setId("sdataset1s");
    dataSet.setProviderId(ecloudDatasetDao.getEcloudProvider());
    dataSet.setDescription("test_description");
  }

  @Test
  @Betamax(tape = "createDataset")
  public void testCreateDataset(){
//    createTestProvider();
    String uri = ecloudDatasetDao.create(dataSet);
    Assert.assertNotNull(uri);
    ecloudDatasetDao.delete(dataSet);
  }

  @Test
  @Betamax(tape = "getDatastById")
  public void testGetDatasetById() {
//    createTestProvider();
    ecloudDatasetDao.create(dataSet);
    DataSet ds = ecloudDatasetDao.getById(dataSet.getId());
    Assert.assertEquals(dataSet.getId(), ds.getId());
    Assert.assertEquals(dataSet.getProviderId(), ds.getProviderId());
    Assert.assertEquals(dataSet.getDescription(), ds.getDescription());
    ecloudDatasetDao.delete(dataSet);
  }

  @Test
  @Betamax(tape = "updateDataset")
  public void testUpdateDataset() {
//    createTestProvider();
    ecloudDatasetDao.create(dataSet);
    dataSet.setDescription("changed");
    String id = ecloudDatasetDao.update(dataSet);
    Assert.assertNotNull(id);
    DataSet ds = ecloudDatasetDao.getById(dataSet.getId());
    Assert.assertEquals(dataSet.getDescription(), ds.getDescription());
    ecloudDatasetDao.delete(dataSet);
  }

  @Test
  @Betamax(tape = "deleteDataset")
  public void testDeleteDataset() {
//    createTestProvider();
    ecloudDatasetDao.create(dataSet);
    boolean deleted = ecloudDatasetDao.delete(dataSet);
    Assert.assertTrue(deleted);
  }

  // TODO: 21-2-17 Remove when delete provider is available
//  private void createTestProvider()
//  {
//    try {
//      uisClient.createProvider("sprovider1s",
//          new DataProviderProperties("sprovider1s", "sprovider1s", "sprovider1s", "sprovider1s",
//              "sprovider1s", "sprovider1s", "sprovider1s", "sprovider1s"));
//    } catch (CloudException e) {
//      if (e.getError().equals("PROVIDER_ALREADY_EXISTS")) {
//        LOGGER.info("Provider not created already existent");
//      } else {
//        return;
//      }
//    }
//  }

}
