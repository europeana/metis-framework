package eu.europeana.metis.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.ScheduledWorkflowDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetStatus;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.DatasetAlreadyExistsException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

public class TestDatasetService {

  private DatasetDao datasetDao;
  private WorkflowExecutionDao workflowExecutionDao;
  private ScheduledWorkflowDao scheduledWorkflowDao;
  private DatasetService datasetService;
  private RedissonClient redissonClient;

  private static final String DATASET_CREATION_LOCK = "datasetCreationLock";

  @Before
  public void prepare() {
    datasetDao = Mockito.mock(DatasetDao.class);
    workflowExecutionDao = Mockito.mock(WorkflowExecutionDao.class);
    scheduledWorkflowDao = Mockito.mock(ScheduledWorkflowDao.class);
    redissonClient = Mockito.mock(RedissonClient.class);

    datasetService = new DatasetService(datasetDao, workflowExecutionDao,
        scheduledWorkflowDao, redissonClient);
  }

  @Test
  public void testCreateDataset() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(DATASET_CREATION_LOCK)).thenReturn(rlock);
    when(datasetDao.getDatasetByOrganizationIdAndDatasetName(dataset.getOrganizationId(),
        dataset.getDatasetName())).thenReturn(null);
    when(datasetDao.findNextInSequenceDatasetId()).thenReturn(1);
    datasetService.createDataset(dataset);
    ArgumentCaptor<Dataset> datasetArgumentCaptor = ArgumentCaptor.forClass(Dataset.class);
    verify(datasetDao, times(1)).
        create(datasetArgumentCaptor.capture());
    verify(datasetDao, times(1)).getById(null);
    assertEquals(dataset.getDatasetName(), datasetArgumentCaptor.getValue().getDatasetName());
  }

  @Test(expected = DatasetAlreadyExistsException.class)
  public void testCreateDatasetAlreadyExists() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);

    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(DATASET_CREATION_LOCK)).thenReturn(rlock);
    when(datasetDao.getDatasetByOrganizationIdAndDatasetName(dataset.getOrganizationId(),
        dataset.getDatasetName())).thenReturn(dataset);
    datasetService.createDataset(dataset);
    verify(datasetDao, times(0)).create(any(Dataset.class));
    verify(datasetDao, times(0)).getById(null);
  }

  @Test
  public void testUpdateDataset() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setProvider("newProvider");
    Dataset storedDataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    storedDataset.setDatasetStatus(DatasetStatus.HARVESTED);
    storedDataset.setUpdatedDate(new Date(-1000));
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(storedDataset);
    datasetService.updateDataset(dataset);

    ArgumentCaptor<Dataset> dataSetArgumentCaptor = ArgumentCaptor.forClass(Dataset.class);
    verify(datasetDao, times(1)).update(dataSetArgumentCaptor.capture());
    assertEquals(dataset.getProvider(), dataSetArgumentCaptor.getValue().getProvider());
    assertEquals(storedDataset.getDatasetStatus(), dataSetArgumentCaptor.getValue().getDatasetStatus());
    assertEquals(dataset.getUpdatedDate(), dataSetArgumentCaptor.getValue().getUpdatedDate());
    assertNotEquals(storedDataset.getUpdatedDate(), dataSetArgumentCaptor.getValue().getUpdatedDate());
  }

  @Test(expected = BadContentException.class)
  public void testUpdateDatasetDatasetExecutionIsActive() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn("ObjectId");
    datasetService.updateDataset(dataset);
  }

  @Test(expected = NoDatasetFoundException.class)
  public void testUpdateDatasetNoDatasetFoundException() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(null);
    datasetService.updateDataset(dataset);
  }

  @Test
  public void testDeleteDatasetByDatasetId() throws Exception {
    when(workflowExecutionDao.existsAndNotCompleted(TestObjectFactory.DATASETID)).thenReturn(null);
    datasetService.deleteDatasetByDatasetId(TestObjectFactory.DATASETID);
    verify(datasetDao, times(1)).deleteByDatasetId(TestObjectFactory.DATASETID);
    verify(workflowExecutionDao, times(1)).deleteAllByDatasetId(TestObjectFactory.DATASETID);
    verify(scheduledWorkflowDao, times(1)).deleteAllByDatasetId(TestObjectFactory.DATASETID);
  }

  @Test(expected = BadContentException.class)
  public void testDeleteDatasetDatasetExecutionIsActive() throws Exception {
    when(workflowExecutionDao.existsAndNotCompleted(TestObjectFactory.DATASETID)).thenReturn("ObjectId");
    datasetService.deleteDatasetByDatasetId(TestObjectFactory.DATASETID);
  }

  @Test
  public void testGetDatasetByDatasetName() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    Dataset returnedDataset = datasetService.getDatasetByDatasetName(TestObjectFactory.DATASETNAME);
    assertEquals(dataset.getDatasetStatus(), returnedDataset.getDatasetStatus());
  }

  @Test(expected = NoDatasetFoundException.class)
  public void testGetDatasetByDatasetNameNoDatasetFoundException() throws Exception {
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(null);
    datasetService.getDatasetByDatasetName(TestObjectFactory.DATASETNAME);
  }

  @Test
  public void testGetDatasetByDatasetId() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    Dataset returnedDataset = datasetService.getDatasetByDatasetId(dataset.getDatasetId());
    assertEquals(dataset.getDatasetStatus(), returnedDataset.getDatasetStatus());
  }

  @Test(expected = NoDatasetFoundException.class)
  public void testGetDatasetByDatasetIdNoDatasetFoundException() throws Exception {
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(null);
    datasetService.getDatasetByDatasetId(TestObjectFactory.DATASETID);
  }

  @Test
  public void testGetAllDatasetsByProvider()
  {
    List<Dataset> list = new ArrayList<>();
    String provider = "myProvider";
    String nextPage = "myNextPage";
    when(datasetDao.getAllDatasetsByProvider(provider, nextPage)).thenReturn(list);
    List<Dataset> retList = datasetService.getAllDatasetsByProvider(provider, nextPage);
    assertSame(list, retList);
  }

  @Test
  public void testGetAllDatasetsByIntermidiateProvider()
  {
    List<Dataset> list = new ArrayList<>();
    String provider = "myProvider";
    String nextPage = "myNextPage";
    when(datasetDao.getAllDatasetsByIntermidiateProvider(provider, nextPage)).thenReturn(list);
    List<Dataset> retList = datasetService.getAllDatasetsByIntermidiateProvider(provider, nextPage);
    assertSame(list, retList);
  }

  @Test
  public void testGetAllDatasetsByDataProvider()
  {
    List<Dataset> list = new ArrayList<>();
    String provider = "myProvider";
    String nextPage = "myNextPage";
    when(datasetDao.getAllDatasetsByDataProvider(provider, nextPage)).thenReturn(list);
    List<Dataset> retList = datasetService.getAllDatasetsByDataProvider(provider, nextPage);
    assertSame(list, retList);
  }

  @Test
  public void testGetAllDatasetsByOrganizationId()
  {
    List<Dataset> list = new ArrayList<>();
    String organizationId = "organizationId";
    String nextPage = "myNextPage";
    when(datasetDao.getAllDatasetsByOrganizationId(organizationId, nextPage)).thenReturn(list);
    List<Dataset> retList = datasetService.getAllDatasetsByOrganizationId(organizationId, nextPage);
    assertSame(list, retList);
  }

  @Test
  public void testGetAllDatasetsByOrganizationName()
  {
    List<Dataset> list = new ArrayList<>();
    String organizationName = "organizationName";
    String nextPage = "myNextPage";
    when(datasetDao.getAllDatasetsByOrganizationName(organizationName, nextPage)).thenReturn(list);
    List<Dataset> retList = datasetService.getAllDatasetsByOrganizationName(organizationName, nextPage);
    assertSame(list, retList);
  }

  @Test
  public void testExistsDatasetByDatasetName()
  {
    when(datasetDao.existsDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(true);
    assertTrue(datasetService.existsDatasetByDatasetName(TestObjectFactory.DATASETNAME));
  }

  @Test
  public void testGetDatasetsPerRequestLimit()
  {
    when(datasetDao.getDatasetsPerRequest()).thenReturn(5);
    assertEquals(5, datasetService.getDatasetsPerRequestLimit());
  }
}

