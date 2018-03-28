package eu.europeana.metis.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.europeana.metis.authentication.user.AccountRole;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.DatasetXsltDao;
import eu.europeana.metis.core.dao.ScheduledWorkflowDao;
import eu.europeana.metis.core.dao.WorkflowDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetXslt;
import eu.europeana.metis.core.exceptions.DatasetAlreadyExistsException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoXsltFoundException;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

public class TestDatasetService {

  private DatasetDao datasetDao;
  private DatasetXsltDao datasetXsltDao;
  private WorkflowDao workflowDao;
  private WorkflowExecutionDao workflowExecutionDao;
  private ScheduledWorkflowDao scheduledWorkflowDao;
  private DatasetService datasetService;
  private RedissonClient redissonClient;

  private static final String DATASET_CREATION_LOCK = "datasetCreationLock";

  @Before
  public void prepare() {
    datasetDao = Mockito.mock(DatasetDao.class);
    datasetXsltDao = Mockito.mock(DatasetXsltDao.class);
    workflowDao = Mockito.mock(WorkflowDao.class);
    workflowExecutionDao = Mockito.mock(WorkflowExecutionDao.class);
    scheduledWorkflowDao = Mockito.mock(ScheduledWorkflowDao.class);
    redissonClient = Mockito.mock(RedissonClient.class);

    datasetService = new DatasetService(datasetDao, datasetXsltDao, workflowDao,
        workflowExecutionDao,
        scheduledWorkflowDao, redissonClient);
  }

  @Test
  public void testCreateDataset() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(DATASET_CREATION_LOCK)).thenReturn(rlock);
    when(datasetDao.getDatasetByOrganizationIdAndDatasetName(dataset.getOrganizationId(),
        dataset.getDatasetName())).thenReturn(null);
    when(datasetDao.findNextInSequenceDatasetId()).thenReturn(1);
    datasetService.createDataset(metisUser, dataset);
    ArgumentCaptor<Dataset> datasetArgumentCaptor = ArgumentCaptor.forClass(Dataset.class);
    verify(datasetDao, times(1)).
        create(datasetArgumentCaptor.capture());
    verify(datasetDao, times(1)).getById(null);
    assertEquals(dataset.getDatasetName(), datasetArgumentCaptor.getValue().getDatasetName());
    assertEquals(metisUser.getUserId(), datasetArgumentCaptor.getValue().getCreatedByUserId());
    assertEquals(metisUser.getOrganizationId(),
        datasetArgumentCaptor.getValue().getOrganizationId());
    assertEquals(metisUser.getOrganizationName(),
        datasetArgumentCaptor.getValue().getOrganizationName());
  }

  @Test(expected = UserUnauthorizedException.class)
  public void testCreateDatasetUnauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(null);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    datasetService.createDataset(metisUser, dataset);
    verify(datasetDao, times(0)).create(dataset);
  }

  @Test(expected = DatasetAlreadyExistsException.class)
  public void testCreateDatasetAlreadyExists() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);

    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(DATASET_CREATION_LOCK)).thenReturn(rlock);
    when(datasetDao.getDatasetByOrganizationIdAndDatasetName(metisUser.getOrganizationId(),
        dataset.getDatasetName())).thenReturn(dataset);
    datasetService.createDataset(metisUser, dataset);
    verify(datasetDao, times(0)).create(any(Dataset.class));
    verify(datasetDao, times(0)).getById(null);
  }

  @Test
  public void testUpdateDataset() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setProvider("newProvider");
    Dataset storedDataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    storedDataset.setUpdatedDate(new Date(-1000));
    storedDataset.setOrganizationId(metisUser.getOrganizationId());
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(storedDataset);
    when(datasetXsltDao.create(any(DatasetXslt.class))).thenReturn(TestObjectFactory.XSLTID);
    datasetService.updateDataset(metisUser, dataset, TestObjectFactory.createXslt(TestObjectFactory.createDataset(dataset.getDatasetName())).getXslt());

    ArgumentCaptor<Dataset> dataSetArgumentCaptor = ArgumentCaptor.forClass(Dataset.class);
    verify(datasetDao, times(1)).update(dataSetArgumentCaptor.capture());
    assertEquals(dataset.getProvider(), dataSetArgumentCaptor.getValue().getProvider());
    assertEquals(dataset.getUpdatedDate(), dataSetArgumentCaptor.getValue().getUpdatedDate());
    assertEquals(storedDataset.getCreatedByUserId(), dataSetArgumentCaptor.getValue().getCreatedByUserId());
    assertNotEquals(storedDataset.getUpdatedDate(),
        dataSetArgumentCaptor.getValue().getUpdatedDate());
  }

  @Test
  public void testUpdateDatasetNonNullXslt() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setProvider("newProvider");
    Dataset storedDataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    storedDataset.setUpdatedDate(new Date(-1000));
    storedDataset.setOrganizationId(metisUser.getOrganizationId());
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(storedDataset);
    when(datasetXsltDao.create(any(DatasetXslt.class))).thenReturn(TestObjectFactory.XSLTID);
    datasetService.updateDataset(metisUser, dataset, null);

    ArgumentCaptor<Dataset> dataSetArgumentCaptor = ArgumentCaptor.forClass(Dataset.class);
    verify(datasetDao, times(1)).update(dataSetArgumentCaptor.capture());
    assertEquals(dataset.getProvider(), dataSetArgumentCaptor.getValue().getProvider());
    assertEquals(dataset.getUpdatedDate(), dataSetArgumentCaptor.getValue().getUpdatedDate());
    assertEquals(storedDataset.getCreatedByUserId(), dataSetArgumentCaptor.getValue().getCreatedByUserId());
    assertNotEquals(storedDataset.getUpdatedDate(),
        dataSetArgumentCaptor.getValue().getUpdatedDate());
  }

  @Test(expected = UserUnauthorizedException.class)
  public void testUpdateDatasetUnauthorizedUserAccountRole() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(null);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setProvider("newProvider");
    datasetService.updateDataset(metisUser, dataset, null);
    verify(datasetDao, times(0)).update(dataset);
  }

  @Test(expected = UserUnauthorizedException.class)
  public void testUpdateDatasetUnauthorizedUserForDataset() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setProvider("newProvider");
    Dataset storedDataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    storedDataset.setUpdatedDate(new Date(-1000));
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(storedDataset);
    datasetService.updateDataset(metisUser, dataset, null);
    verify(datasetDao, times(0)).update(dataset);
  }

  @Test(expected = DatasetAlreadyExistsException.class)
  public void testUpdateDatasetDatasetAlreadyExistsException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    Dataset storedDataset = TestObjectFactory.createDataset(String.format("%s%s", TestObjectFactory.DATASETNAME, 10));
    storedDataset.setOrganizationId(metisUser.getOrganizationId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(storedDataset);
    when(datasetDao.getDatasetByOrganizationIdAndDatasetName(dataset.getOrganizationId(), dataset.getDatasetName())).thenReturn(new Dataset());
    datasetService.updateDataset(metisUser, dataset, null);
  }

  @Test(expected = BadContentException.class)
  public void testUpdateDatasetDatasetExecutionIsActive() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn("ObjectId");
    datasetService.updateDataset(metisUser, dataset, null);
  }

  @Test(expected = NoDatasetFoundException.class)
  public void testUpdateDatasetNoDatasetFoundException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(null);
    datasetService.updateDataset(metisUser, dataset, null);
  }

  @Test
  public void testDeleteDatasetByDatasetId() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowExecutionDao.existsAndNotCompleted(TestObjectFactory.DATASETID)).thenReturn(null);
    datasetService.deleteDatasetByDatasetId(metisUser, TestObjectFactory.DATASETID);
    verify(datasetDao, times(1)).deleteByDatasetId(TestObjectFactory.DATASETID);
    verify(workflowExecutionDao, times(1)).deleteAllByDatasetId(TestObjectFactory.DATASETID);
    verify(scheduledWorkflowDao, times(1)).deleteAllByDatasetId(TestObjectFactory.DATASETID);
  }

  @Test(expected = UserUnauthorizedException.class)
  public void testDeleteDatasetByDatasetIdUnauthorizedUserAccountRole() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(null);
    datasetService.deleteDatasetByDatasetId(metisUser, TestObjectFactory.DATASETID);
    verify(datasetDao, times(0)).deleteByDatasetId(TestObjectFactory.DATASETID);
  }

  @Test(expected = NoDatasetFoundException.class)
  public void testDeleteDatasetByDatasetIdNoDatasetFoundException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    when(datasetDao.getDatasetByDatasetId(anyInt())).thenReturn(null);
    datasetService.deleteDatasetByDatasetId(metisUser, TestObjectFactory.DATASETID);
    verify(datasetDao, times(0)).deleteByDatasetId(TestObjectFactory.DATASETID);
  }

  @Test(expected = UserUnauthorizedException.class)
  public void testDeleteDatasetByDatasetIdUnauthorizedUserForDataset() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowExecutionDao.existsAndNotCompleted(TestObjectFactory.DATASETID)).thenReturn(null);
    datasetService.deleteDatasetByDatasetId(metisUser, TestObjectFactory.DATASETID);
    verify(datasetDao, times(0)).deleteByDatasetId(TestObjectFactory.DATASETID);
  }

  @Test(expected = BadContentException.class)
  public void testDeleteDatasetDatasetExecutionIsActive() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowExecutionDao.existsAndNotCompleted(TestObjectFactory.DATASETID))
        .thenReturn("ObjectId");
    datasetService.deleteDatasetByDatasetId(metisUser, TestObjectFactory.DATASETID);
  }

  @Test
  public void testGetDatasetByDatasetName() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    Dataset returnedDataset = datasetService
        .getDatasetByDatasetName(metisUser, TestObjectFactory.DATASETNAME);
    assertNotNull(returnedDataset);
  }

  @Test(expected = UserUnauthorizedException.class)
  public void testGetDatasetByDatasetNameUnauthorizedUserAccountRole() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(null);
    datasetService.getDatasetByDatasetName(metisUser, TestObjectFactory.DATASETNAME);
    verify(datasetDao, times(0)).getDatasetByDatasetName(TestObjectFactory.DATASETNAME);
  }

  @Test(expected = UserUnauthorizedException.class)
  public void testGetDatasetByDatasetNameUnauthorizedUserForDataset() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    datasetService.getDatasetByDatasetName(metisUser, TestObjectFactory.DATASETNAME);
    verify(datasetDao, times(0)).getDatasetByDatasetName(TestObjectFactory.DATASETNAME);
  }

  @Test(expected = NoDatasetFoundException.class)
  public void testGetDatasetByDatasetNameNoDatasetFoundException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(null);
    datasetService.getDatasetByDatasetName(metisUser, TestObjectFactory.DATASETNAME);
  }

  @Test
  public void testGetDatasetByDatasetId() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    datasetService.getDatasetByDatasetId(metisUser, dataset.getDatasetId());
  }

  @Test(expected = UserUnauthorizedException.class)
  public void testGetDatasetByDatasetIdUnauthorizedUserAccountRole() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(null);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    datasetService.getDatasetByDatasetId(metisUser, dataset.getDatasetId());
    verify(datasetDao, times(0)).getDatasetByDatasetId(dataset.getDatasetId());
  }

  @Test(expected = UserUnauthorizedException.class)
  public void testGetDatasetByDatasetIdUnauthorizedUserForDataset() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    datasetService.getDatasetByDatasetId(metisUser, dataset.getDatasetId());
    verify(datasetDao, times(0)).getDatasetByDatasetId(dataset.getDatasetId());
  }

  @Test(expected = NoDatasetFoundException.class)
  public void testGetDatasetByDatasetIdNoDatasetFoundException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(null);
    datasetService.getDatasetByDatasetId(metisUser, TestObjectFactory.DATASETID);
  }

  @Test
  public void getDatasetXsltByDatasetId() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    dataset.setXsltId(new ObjectId());
    DatasetXslt datasetXslt = TestObjectFactory.createXslt(dataset);
    when(datasetXsltDao.getById(dataset.getXsltId().toString())).thenReturn(datasetXslt);

    DatasetXslt datasetXsltByDatasetId = datasetService
        .getDatasetXsltByDatasetId(metisUser, dataset.getDatasetId());
    Assert.assertEquals(datasetXslt.getXslt(), datasetXsltByDatasetId.getXslt());
    Assert.assertEquals(datasetXslt.getDatasetId(), datasetXsltByDatasetId.getDatasetId());
  }

  @Test(expected = UserUnauthorizedException.class)
  public void getDatasetXsltByDatasetIdUserUnauthorizedException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.PROVIDER_VIEWER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    datasetService.getDatasetXsltByDatasetId(metisUser, dataset.getDatasetId());
  }

  @Test(expected = NoXsltFoundException.class)
  public void getDatasetXsltByDatasetIdNoXsltFoundException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(datasetXsltDao.getById(anyString())).thenReturn(null);
    datasetService.getDatasetXsltByDatasetId(metisUser, dataset.getDatasetId());
  }

  @Test(expected = NoDatasetFoundException.class)
  public void getDatasetXsltByDatasetIdNoDatasetFoundException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(null);
    datasetService.getDatasetXsltByDatasetId(metisUser, TestObjectFactory.DATASETID);
  }

  @Test
  public void getDatasetXsltByXsltId() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    DatasetXslt datasetXslt = TestObjectFactory.createXslt(dataset);
    when(datasetXsltDao.getById(TestObjectFactory.XSLTID)).thenReturn(datasetXslt);

    DatasetXslt datasetXsltByDatasetId = datasetService
        .getDatasetXsltByXsltId(TestObjectFactory.XSLTID);
    Assert.assertEquals(datasetXslt.getXslt(), datasetXsltByDatasetId.getXslt());
    Assert.assertEquals(datasetXslt.getDatasetId(), datasetXsltByDatasetId.getDatasetId());
  }

  @Test(expected = NoXsltFoundException.class)
  public void getDatasetXsltByXsltIdNoXsltFoundException() throws Exception {
    when(datasetXsltDao.getById(TestObjectFactory.XSLTID)).thenReturn(null);
    datasetService.getDatasetXsltByXsltId(TestObjectFactory.XSLTID);
  }

  @Test
  public void createDefaultXslt() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.METIS_ADMIN);
    DatasetXslt datasetXslt = TestObjectFactory
        .createXslt(TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME));
    datasetXslt.setDatasetId(-1);
    when(datasetXsltDao.create(any(DatasetXslt.class))).thenReturn(TestObjectFactory.XSLTID);
    when(datasetXsltDao.getById(TestObjectFactory.XSLTID)).thenReturn(datasetXslt);
    DatasetXslt defaultDatasetXslt = datasetService.createDefaultXslt(metisUser, datasetXslt.getXslt());
    Assert.assertEquals(datasetXslt.getDatasetId(), defaultDatasetXslt.getDatasetId());
  }

  @Test(expected = UserUnauthorizedException.class)
  public void createDefaultXsltUserUnauthorizedException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    DatasetXslt datasetXslt = TestObjectFactory
        .createXslt(TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME));
    datasetService.createDefaultXslt(metisUser, datasetXslt.getXslt());
  }

  @Test
  public void getLatestXsltForDatasetId() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    DatasetXslt datasetXslt = TestObjectFactory.createXslt(dataset);
    when(datasetXsltDao.getLatestXsltForDatasetId(TestObjectFactory.DATASETID)).thenReturn(datasetXslt);

    DatasetXslt datasetXsltByDatasetId = datasetService
        .getLatestXsltForDatasetId(TestObjectFactory.DATASETID);
    Assert.assertEquals(datasetXslt.getXslt(), datasetXsltByDatasetId.getXslt());
    Assert.assertEquals(datasetXslt.getDatasetId(), datasetXsltByDatasetId.getDatasetId());
  }

  @Test(expected = NoXsltFoundException.class)
  public void getLatestXsltForDatasetIdNoXsltFoundException() throws Exception {
    when(datasetXsltDao.getById(TestObjectFactory.XSLTID)).thenReturn(null);
    datasetService.getLatestXsltForDatasetId(TestObjectFactory.DATASETID);
  }

  @Test
  public void testGetAllDatasetsByProvider() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    List<Dataset> list = new ArrayList<>();
    String provider = "myProvider";
    int nextPage = 1;
    when(datasetDao.getAllDatasetsByProvider(provider, nextPage)).thenReturn(list);
    List<Dataset> retList = datasetService.getAllDatasetsByProvider(metisUser, provider, nextPage);
    assertSame(list, retList);
  }

  @Test(expected = UserUnauthorizedException.class)
  public void testGetAllDatasetsByProviderUnauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(null);
    String provider = "myProvider";
    int nextPage = 1;
    datasetService.getAllDatasetsByProvider(metisUser, provider, nextPage);
    verify(datasetDao, times(0)).getAllDatasetsByProvider(provider, nextPage);
  }

  @Test
  public void testGetAllDatasetsByIntermidiateProvider() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    List<Dataset> list = new ArrayList<>();
    String provider = "myProvider";
    int nextPage = 1;
    when(datasetDao.getAllDatasetsByIntermediateProvider(provider, nextPage)).thenReturn(list);
    List<Dataset> retList = datasetService
        .getAllDatasetsByIntermediateProvider(metisUser, provider, nextPage);
    assertSame(list, retList);
  }

  @Test(expected = UserUnauthorizedException.class)
  public void testGetAllDatasetsByIntermidiateProviderUnauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(null);
    String intermediateProvider = "myProvider";
    int nextPage = 1;
    datasetService.getAllDatasetsByIntermediateProvider(metisUser, intermediateProvider, nextPage);
    verify(datasetDao, times(0))
        .getAllDatasetsByIntermediateProvider(intermediateProvider, nextPage);
  }

  @Test
  public void testGetAllDatasetsByDataProvider() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    List<Dataset> list = new ArrayList<>();
    String provider = "myProvider";
    int  nextPage = 1;
    when(datasetDao.getAllDatasetsByDataProvider(provider, nextPage)).thenReturn(list);
    List<Dataset> retList = datasetService
        .getAllDatasetsByDataProvider(metisUser, provider, nextPage);
    assertSame(list, retList);
  }

  @Test(expected = UserUnauthorizedException.class)
  public void testGetAllDatasetsByDataProviderUnauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(null);
    String dataProvider = "myProvider";
    int nextPage = 1;
    datasetService.getAllDatasetsByDataProvider(metisUser, dataProvider, nextPage);
    verify(datasetDao, times(0)).getAllDatasetsByDataProvider(dataProvider, nextPage);
  }

  @Test
  public void testGetAllDatasetsByOrganizationId() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    List<Dataset> list = new ArrayList<>();
    String organizationId = "organizationId";
    int nextPage = 1;
    when(datasetDao.getAllDatasetsByOrganizationId(organizationId, nextPage)).thenReturn(list);
    List<Dataset> retList = datasetService
        .getAllDatasetsByOrganizationId(metisUser, organizationId, nextPage);
    assertSame(list, retList);
  }

  @Test(expected = UserUnauthorizedException.class)
  public void testGetAllDatasetsByOrganizationIdUnauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(null);
    String organizationId = "organizationId";
    int nextPage = 1;
    datasetService.getAllDatasetsByOrganizationId(metisUser, organizationId, nextPage);
    verify(datasetDao, times(0)).getAllDatasetsByOrganizationId(organizationId, nextPage);
  }

  @Test
  public void testGetAllDatasetsByOrganizationName() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    List<Dataset> list = new ArrayList<>();
    String organizationName = "organizationName";
    int nextPage = 1;
    when(datasetDao.getAllDatasetsByOrganizationName(organizationName, nextPage))
        .thenReturn(list);
    List<Dataset> retList = datasetService
        .getAllDatasetsByOrganizationName(metisUser, organizationName, nextPage);
    assertSame(list, retList);
  }

  @Test(expected = UserUnauthorizedException.class)
  public void testGetAllDatasetsByOrganizationNameUnauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(null);
    String organizationName = "organizationName";
    int nextPage = 1;
    datasetService.getAllDatasetsByOrganizationName(metisUser, organizationName, nextPage);
    verify(datasetDao, times(0)).getAllDatasetsByOrganizationName(organizationName, nextPage);

  }

  @Test
  public void testExistsDatasetByDatasetName() {
    when(datasetDao.existsDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(true);
    assertTrue(datasetService.existsDatasetByDatasetName(TestObjectFactory.DATASETNAME));
  }

  @Test
  public void testGetDatasetsPerRequestLimit() {
    when(datasetDao.getDatasetsPerRequest()).thenReturn(5);
    assertEquals(5, datasetService.getDatasetsPerRequestLimit());
  }
}

