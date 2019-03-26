package eu.europeana.metis.core.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.cloud.service.mcs.exception.DataSetAlreadyExistsException;
import eu.europeana.cloud.service.mcs.exception.MCSException;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetIdSequence;
import eu.europeana.metis.core.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.utils.TestObjectFactory;
import eu.europeana.metis.exception.ExternalTaskException;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mongodb.morphia.Datastore;

class TestDatasetDao {

  private static DatasetDao datasetDao;
  private static Dataset dataset;
  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;
  private static MorphiaDatastoreProvider provider;
  private static DataSetServiceClient ecloudDataSetServiceClient;

  @BeforeAll
  static void prepare() {
    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    embeddedLocalhostMongo.start();
    String mongoHost = embeddedLocalhostMongo.getMongoHost();
    int mongoPort = embeddedLocalhostMongo.getMongoPort();
    ServerAddress address = new ServerAddress(mongoHost, mongoPort);
    MongoClient mongoClient = new MongoClient(address);
    provider = new MorphiaDatastoreProvider(mongoClient, "test");
    ecloudDataSetServiceClient = Mockito.mock(DataSetServiceClient.class);

    datasetDao = new DatasetDao(provider, ecloudDataSetServiceClient);
    datasetDao.setDatasetsPerRequest(1);
    datasetDao.setEcloudProvider("ecloudProvider");

    dataset = TestObjectFactory.createDataset("testName");
  }

  @AfterAll
  static void destroy() {
    embeddedLocalhostMongo.stop();
  }

  @AfterEach
  void cleanUp() {
    Datastore datastore = provider.getDatastore();
    datastore.delete(datastore.createQuery(Dataset.class));
    datastore.delete(datastore.createQuery(DatasetIdSequence.class));
    Mockito.reset(ecloudDataSetServiceClient);
  }

  @Test
  void testCreateRetrieveDataset() {
    datasetDao.create(dataset);
    Dataset storedDataset = datasetDao.getDatasetByDatasetId(dataset.getDatasetId());
    assertEquals(dataset.getDatasetName(), storedDataset.getDatasetName());
    assertEquals(dataset.getCountry(), storedDataset.getCountry());
    assertEquals(dataset.getCreatedDate(), storedDataset.getCreatedDate());
    assertEquals(dataset.getDataProvider(), storedDataset.getDataProvider());
    assertEquals(dataset.getDescription(), storedDataset.getDescription());
    assertEquals(dataset.getLanguage(), storedDataset.getLanguage());
    assertEquals(dataset.getNotes(), storedDataset.getNotes());
    assertEquals(dataset.getReplacedBy(), storedDataset.getReplacedBy());
    assertEquals(dataset.getUpdatedDate(), storedDataset.getUpdatedDate());
  }


  @Test
  void testUpdateRetrieveDataset() {
    datasetDao.create(dataset);
    datasetDao.update(dataset);
    Dataset storedDataset = datasetDao.getDatasetByDatasetId(dataset.getDatasetId());
    assertEquals(dataset.getDatasetName(), storedDataset.getDatasetName());
    assertEquals(dataset.getCountry(), storedDataset.getCountry());
    assertEquals(dataset.getCreatedDate(), storedDataset.getCreatedDate());
    assertEquals(dataset.getDataProvider(), storedDataset.getDataProvider());
    assertEquals(dataset.getDescription(), storedDataset.getDescription());
    assertEquals(dataset.getLanguage(), storedDataset.getLanguage());
    assertEquals(dataset.getNotes(), storedDataset.getNotes());
    assertEquals(dataset.getReplacedBy(), storedDataset.getReplacedBy());
    assertEquals(dataset.getUpdatedDate(), storedDataset.getUpdatedDate());
  }

  @Test
  void testDeleteDataset() {
    datasetDao.create(dataset);
    Dataset storedDataset = datasetDao.getDatasetByDatasetId(dataset.getDatasetId());
    datasetDao.delete(storedDataset);
    storedDataset = datasetDao.getDatasetByDatasetId(dataset.getDatasetId());
    assertNull(storedDataset);
  }

  @Test
  void testDelete() {
    String key = datasetDao.create(dataset);
    Dataset storedDataset = datasetDao.getById(key);
    assertTrue(datasetDao.delete(storedDataset));
    assertNull(datasetDao.getById(key));
  }

  @Test
  void testDeleteByDatasetId() {
    String key = datasetDao.create(dataset);
    Dataset storedDataset = datasetDao.getById(key);
    assertTrue(datasetDao.deleteByDatasetId(storedDataset.getDatasetId()));
    assertNull(datasetDao.getById(key));
  }

  @Test
  void testGetByDatasetName() {
    Dataset createdDataset = datasetDao.getById(datasetDao.create(dataset));
    Dataset storedDataset = datasetDao.getDatasetByDatasetName(createdDataset.getDatasetName());
    assertEquals(createdDataset.getDatasetId(), storedDataset.getDatasetId());
  }

  @Test
  void testGetByDatasetId() {
    Dataset createdDataset = datasetDao.getById(datasetDao.create(dataset));
    Dataset storedDataset = datasetDao.getDatasetByDatasetId(createdDataset.getDatasetId());
    assertEquals(createdDataset.getDatasetName(), storedDataset.getDatasetName());
  }

  @Test
  void getDatasetByOrganizationIdAndDatasetName() {
    Dataset createdDataset = datasetDao.getById(datasetDao.create(dataset));
    Dataset storedDataset = datasetDao
        .getDatasetByOrganizationIdAndDatasetName(createdDataset.getOrganizationId(),
            createdDataset.getDatasetName());
    assertEquals(createdDataset.getDatasetId(), storedDataset.getDatasetId());
  }

  @Test
  void testExistsDatasetByDatasetName() {
    Dataset createdDataset = datasetDao.getById(datasetDao.create(dataset));
    assertTrue(datasetDao.existsDatasetByDatasetName(createdDataset.getDatasetName()));
    datasetDao.deleteByDatasetId(createdDataset.getDatasetId());
    assertFalse(datasetDao.existsDatasetByDatasetName(createdDataset.getDatasetName()));
  }

  @Test
  void testGetAllDatasetByProvider() {
    Dataset ds1 = TestObjectFactory.createDataset("dataset1");
    //add some required fields (indexed)
    ds1.setProvider("myProvider");
    ds1.setEcloudDatasetId("id1");
    ds1.setDatasetId(Integer.toString(TestObjectFactory.DATASETID + 1));
    datasetDao.create(ds1);

    Dataset ds2 = TestObjectFactory.createDataset("dataset2");
    //add some required fields (indexed)
    ds2.setProvider("myProvider");
    ds2.setEcloudDatasetId("id2");
    ds2.setDatasetId(Integer.toString(TestObjectFactory.DATASETID + 2));
    datasetDao.create(ds2);

    Dataset ds3 = TestObjectFactory.createDataset("dataset3");
    //add some required fields (indexed)
    ds3.setProvider("otherProvider");
    ds3.setEcloudDatasetId("id3");
    ds3.setDatasetId(Integer.toString(TestObjectFactory.DATASETID + 3));
    datasetDao.create(ds3);

    int nextPage = 0;
    int allDatasetsCount = 0;
    do {
      ResponseListWrapper<Dataset> datasetResponseListWrapper = new ResponseListWrapper<>();
      datasetResponseListWrapper.setResultsAndLastPage(
          datasetDao.getAllDatasetsByProvider("myProvider", nextPage), datasetDao
              .getDatasetsPerRequest(), nextPage);
      allDatasetsCount += datasetResponseListWrapper.getListSize();
      nextPage = datasetResponseListWrapper.getNextPage();
    } while (nextPage != -1);

    assertEquals(2, allDatasetsCount);
  }

  @Test
  void testGetAllDatasetByIntermediateProvider() {
    Dataset ds1 = TestObjectFactory.createDataset("dataset1");
    //add some required fields (indexed)
    ds1.setIntermediateProvider("myProvider");
    ds1.setEcloudDatasetId("id1");
    ds1.setDatasetId(Integer.toString(TestObjectFactory.DATASETID + 1));
    datasetDao.create(ds1);

    Dataset ds2 = TestObjectFactory.createDataset("dataset2");
    //add some required fields (indexed)
    ds2.setIntermediateProvider("myProvider");
    ds2.setEcloudDatasetId("id2");
    ds2.setDatasetId(Integer.toString(TestObjectFactory.DATASETID + 2));
    datasetDao.create(ds2);

    Dataset ds3 = TestObjectFactory.createDataset("dataset3");
    //add some required fields (indexed)
    ds3.setIntermediateProvider("otherProvider");
    ds3.setEcloudDatasetId("id3");
    ds3.setDatasetId(Integer.toString(TestObjectFactory.DATASETID + 3));
    datasetDao.create(ds3);

    int nextPage = 0;
    int allDatasetsCount = 0;
    do {
      ResponseListWrapper<Dataset> datasetResponseListWrapper = new ResponseListWrapper<>();
      datasetResponseListWrapper.setResultsAndLastPage(
          datasetDao.getAllDatasetsByIntermediateProvider("myProvider", nextPage), datasetDao
              .getDatasetsPerRequest(), nextPage);
      allDatasetsCount += datasetResponseListWrapper.getListSize();
      nextPage = datasetResponseListWrapper.getNextPage();
    } while (nextPage != -1);

    assertEquals(2, allDatasetsCount);
  }

  @Test
  void testGetAllDatasetByDataProvider() {
    Dataset ds1 = TestObjectFactory.createDataset("dataset1");
    //add some required fields (indexed)
    ds1.setDataProvider("myProvider");
    ds1.setEcloudDatasetId("id1");
    ds1.setDatasetId(Integer.toString(TestObjectFactory.DATASETID + 1));
    datasetDao.create(ds1);

    Dataset ds2 = TestObjectFactory.createDataset("dataset2");
    //add some required fields (indexed)
    ds2.setDataProvider("myProvider");
    ds2.setEcloudDatasetId("id2");
    ds2.setDatasetId(Integer.toString(TestObjectFactory.DATASETID + 2));
    datasetDao.create(ds2);

    Dataset ds3 = TestObjectFactory.createDataset("dataset3");
    //add some required fields (indexed)
    ds3.setDataProvider("otherProvider");
    ds3.setEcloudDatasetId("id3");
    ds3.setDatasetId(Integer.toString(TestObjectFactory.DATASETID + 3));
    datasetDao.create(ds3);

    int nextPage = 0;
    int allDatestsCount = 0;
    do {
      ResponseListWrapper<Dataset> datasetResponseListWrapper = new ResponseListWrapper<>();
      datasetResponseListWrapper.setResultsAndLastPage(
          datasetDao.getAllDatasetsByDataProvider("myProvider", nextPage), datasetDao
              .getDatasetsPerRequest(), nextPage);
      allDatestsCount += datasetResponseListWrapper.getListSize();
      nextPage = datasetResponseListWrapper.getNextPage();
    } while (nextPage != -1);

    assertEquals(2, allDatestsCount);
  }

  @Test
  void testGetAllDatasetByOrganizationId() {
    Dataset ds1 = TestObjectFactory.createDataset("dataset1");
    //add some required fields (indexed)
    ds1.setOrganizationId("organizationId1");
    ds1.setEcloudDatasetId("id1");
    ds1.setDatasetId(Integer.toString(TestObjectFactory.DATASETID + 1));
    datasetDao.create(ds1);

    Dataset ds2 = TestObjectFactory.createDataset("dataset2");
    //add some required fields (indexed)
    ds2.setOrganizationId("organizationId1");
    ds2.setEcloudDatasetId("id2");
    ds2.setDatasetId(Integer.toString(TestObjectFactory.DATASETID + 2));
    datasetDao.create(ds2);

    Dataset ds3 = TestObjectFactory.createDataset("dataset3");
    //add some required fields (indexed)
    ds3.setOrganizationId("organizationId2");
    ds3.setEcloudDatasetId("id3");
    ds3.setDatasetId(Integer.toString(TestObjectFactory.DATASETID + 3));
    datasetDao.create(ds3);

    // Check with pagination
    int nextPage = 0;
    int allDatasetsCount = 0;
    do {
      ResponseListWrapper<Dataset> datasetResponseListWrapper = new ResponseListWrapper<>();
      datasetResponseListWrapper.setResultsAndLastPage(
          datasetDao.getAllDatasetsByOrganizationId("organizationId1", nextPage), datasetDao
              .getDatasetsPerRequest(), nextPage);
      allDatasetsCount += datasetResponseListWrapper.getListSize();
      nextPage = datasetResponseListWrapper.getNextPage();
    } while (nextPage != -1);
    assertEquals(2, allDatasetsCount);

    // Check without pagination
    final List<Dataset> resultWithoutPagination =
        datasetDao.getAllDatasetsByOrganizationId("organizationId1");
    assertEquals(2, resultWithoutPagination.size());
  }

  @Test
  void testGetAllDatasetByOrganizationName() {
    Dataset ds1 = TestObjectFactory.createDataset("dataset1");
    //add some required fields (indexed)
    ds1.setOrganizationName("organizationName1");
    ds1.setEcloudDatasetId("id1");
    ds1.setDatasetId(Integer.toString(TestObjectFactory.DATASETID + 1));
    datasetDao.create(ds1);

    Dataset ds2 = TestObjectFactory.createDataset("dataset2");
    //add some required fields (indexed)
    ds2.setOrganizationName("organizationName1");
    ds2.setEcloudDatasetId("id2");
    ds2.setDatasetId(Integer.toString(TestObjectFactory.DATASETID + 2));
    datasetDao.create(ds2);

    Dataset ds3 = TestObjectFactory.createDataset("dataset3");
    //add some required fields (indexed)
    ds3.setOrganizationName("organizationName2");
    ds3.setEcloudDatasetId("id3");
    ds3.setDatasetId(Integer.toString(TestObjectFactory.DATASETID + 3));
    datasetDao.create(ds3);

    int nextPage = 0;
    int allDatasetsCount = 0;
    do {
      ResponseListWrapper<Dataset> datasetResponseListWrapper = new ResponseListWrapper<>();
      datasetResponseListWrapper.setResultsAndLastPage(
          datasetDao.getAllDatasetsByOrganizationName("organizationName1", nextPage), datasetDao
              .getDatasetsPerRequest(), nextPage);
      allDatasetsCount += datasetResponseListWrapper.getListSize();
      nextPage = datasetResponseListWrapper.getNextPage();
    } while (nextPage != -1);

    assertEquals(2, allDatasetsCount);
  }

  @Test
  void testFindNextInSequenceDatasetId() {
    DatasetIdSequence datasetIdSequence = new DatasetIdSequence(0);
    provider.getDatastore().save(datasetIdSequence);

    int nextInSequenceDatasetId = datasetDao.findNextInSequenceDatasetId();
    assertEquals(1, nextInSequenceDatasetId);

    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setDatasetId("2");
    datasetDao.create(dataset);

    nextInSequenceDatasetId = datasetDao.findNextInSequenceDatasetId();
    assertEquals(3, nextInSequenceDatasetId);
  }

  @Test
  void testCheckAndCreateDatasetInEcloud() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset("datasetName");
    datasetDao.create(dataset);
    when(ecloudDataSetServiceClient.createDataSet(any(), any(), any())).thenReturn(null);

    datasetDao.checkAndCreateDatasetInEcloud(dataset);
  }

  @Test
  void testCheckAndCreateDatasetInEcloud_FieldWithEcloudIdIsAlreadyPresent() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset("datasetName");
    dataset.setEcloudDatasetId("f525f64c-fea0-44bf-8c56-88f30962734c");
    datasetDao.create(dataset);
    when(ecloudDataSetServiceClient.createDataSet(any(), any(), any())).thenReturn(null);

    datasetDao.checkAndCreateDatasetInEcloud(dataset);
  }

  @Test
  void testCheckAndCreateDatasetInEcloud_DataSetAlreadyExistsException() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset("datasetName");
    datasetDao.create(dataset);
    when(ecloudDataSetServiceClient.createDataSet(any(), any(), any()))
        .thenThrow(new DataSetAlreadyExistsException("Dataset already exist, not recreating"));

    assertThrows(ExternalTaskException.class,
        () -> datasetDao.checkAndCreateDatasetInEcloud(dataset));
  }

  @Test
  void testCheckAndCreateDatasetInEcloud_MCSException() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset("datasetName");
    datasetDao.create(dataset);
    when(ecloudDataSetServiceClient.createDataSet(any(), any(), any()))
        .thenThrow(new MCSException("An error has occurred during ecloud dataset creation."));

    assertThrows(ExternalTaskException.class,
        () -> datasetDao.checkAndCreateDatasetInEcloud(dataset));
  }
}
