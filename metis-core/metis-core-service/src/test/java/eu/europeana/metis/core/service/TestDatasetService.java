package eu.europeana.metis.core.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import eu.europeana.metis.RestEndpoints;
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
import eu.europeana.metis.core.rest.Record;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import eu.europeana.metis.utils.NetworkUtil;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class TestDatasetService {

  private final int portForWireMock = NetworkUtil.getAvailableLocalPort();
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(portForWireMock));

  private DatasetDao datasetDao;
  private DatasetXsltDao datasetXsltDao;
  private WorkflowDao workflowDao;
  private WorkflowExecutionDao workflowExecutionDao;
  private ScheduledWorkflowDao scheduledWorkflowDao;
  private DatasetService datasetService;
  private RedissonClient redissonClient;

  private static final String DATASET_CREATION_LOCK = "datasetCreationLock";

  public TestDatasetService() throws IOException {
  }

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
    datasetService.setMetisCoreUrl(String.format("http://localhost:%d", portForWireMock));
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
    datasetService.updateDataset(metisUser, dataset,
        TestObjectFactory.createXslt(TestObjectFactory.createDataset(dataset.getDatasetName()))
            .getXslt());

    ArgumentCaptor<Dataset> dataSetArgumentCaptor = ArgumentCaptor.forClass(Dataset.class);
    verify(datasetDao, times(1)).update(dataSetArgumentCaptor.capture());
    assertEquals(dataset.getProvider(), dataSetArgumentCaptor.getValue().getProvider());
    assertEquals(dataset.getUpdatedDate(), dataSetArgumentCaptor.getValue().getUpdatedDate());
    assertEquals(storedDataset.getCreatedByUserId(),
        dataSetArgumentCaptor.getValue().getCreatedByUserId());
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
    assertEquals(storedDataset.getCreatedByUserId(),
        dataSetArgumentCaptor.getValue().getCreatedByUserId());
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
    Dataset storedDataset = TestObjectFactory
        .createDataset(String.format("%s%s", TestObjectFactory.DATASETNAME, 10));
    storedDataset.setOrganizationId(metisUser.getOrganizationId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(storedDataset);
    when(datasetDao.getDatasetByOrganizationIdAndDatasetName(dataset.getOrganizationId(),
        dataset.getDatasetName())).thenReturn(new Dataset());
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
    when(workflowExecutionDao.existsAndNotCompleted(Integer.toString(TestObjectFactory.DATASETID))).thenReturn(null);
    datasetService.deleteDatasetByDatasetId(metisUser, Integer.toString(TestObjectFactory.DATASETID));
    verify(datasetDao, times(1)).deleteByDatasetId(Integer.toString(TestObjectFactory.DATASETID));
    verify(workflowExecutionDao, times(1)).deleteAllByDatasetId(Integer.toString(TestObjectFactory.DATASETID));
    verify(scheduledWorkflowDao, times(1)).deleteAllByDatasetId(Integer.toString(TestObjectFactory.DATASETID));
  }

  @Test(expected = UserUnauthorizedException.class)
  public void testDeleteDatasetByDatasetIdUnauthorizedUserAccountRole() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(null);
    datasetService.deleteDatasetByDatasetId(metisUser, Integer.toString(TestObjectFactory.DATASETID));
    verify(datasetDao, times(0)).deleteByDatasetId(Integer.toString(TestObjectFactory.DATASETID));
  }

  @Test(expected = NoDatasetFoundException.class)
  public void testDeleteDatasetByDatasetIdNoDatasetFoundException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    when(datasetDao.getDatasetByDatasetId(anyString())).thenReturn(null);
    datasetService.deleteDatasetByDatasetId(metisUser, Integer.toString(TestObjectFactory.DATASETID));
    verify(datasetDao, times(0)).deleteByDatasetId(Integer.toString(TestObjectFactory.DATASETID));
  }

  @Test(expected = UserUnauthorizedException.class)
  public void testDeleteDatasetByDatasetIdUnauthorizedUserForDataset() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowExecutionDao.existsAndNotCompleted(Integer.toString(TestObjectFactory.DATASETID))).thenReturn(null);
    datasetService.deleteDatasetByDatasetId(metisUser, Integer.toString(TestObjectFactory.DATASETID));
    verify(datasetDao, times(0)).deleteByDatasetId(Integer.toString(TestObjectFactory.DATASETID));
  }

  @Test(expected = BadContentException.class)
  public void testDeleteDatasetDatasetExecutionIsActive() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowExecutionDao.existsAndNotCompleted(Integer.toString(TestObjectFactory.DATASETID)))
        .thenReturn("ObjectId");
    datasetService.deleteDatasetByDatasetId(metisUser, Integer.toString(TestObjectFactory.DATASETID));
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
    when(datasetDao.getDatasetByDatasetId(Integer.toString(TestObjectFactory.DATASETID))).thenReturn(null);
    datasetService.getDatasetByDatasetId(metisUser, Integer.toString(TestObjectFactory.DATASETID));
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
    when(datasetDao.getDatasetByDatasetId(Integer.toString(TestObjectFactory.DATASETID))).thenReturn(null);
    datasetService.getDatasetXsltByDatasetId(metisUser, Integer.toString(TestObjectFactory.DATASETID));
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
    datasetXslt.setDatasetId("-1");
    when(datasetXsltDao.create(any(DatasetXslt.class))).thenReturn(TestObjectFactory.XSLTID);
    when(datasetXsltDao.getById(TestObjectFactory.XSLTID)).thenReturn(datasetXslt);
    DatasetXslt defaultDatasetXslt = datasetService
        .createDefaultXslt(metisUser, datasetXslt.getXslt());
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
    when(datasetXsltDao.getLatestXsltForDatasetId(Integer.toString(TestObjectFactory.DATASETID)))
        .thenReturn(datasetXslt);

    DatasetXslt datasetXsltByDatasetId = datasetService
        .getLatestXsltForDatasetId(Integer.toString(TestObjectFactory.DATASETID));
    Assert.assertEquals(datasetXslt.getXslt(), datasetXsltByDatasetId.getXslt());
    Assert.assertEquals(datasetXslt.getDatasetId(), datasetXsltByDatasetId.getDatasetId());
  }

  @Test(expected = NoXsltFoundException.class)
  public void getLatestXsltForDatasetIdNoXsltFoundException() throws Exception {
    when(datasetXsltDao.getById(TestObjectFactory.XSLTID)).thenReturn(null);
    datasetService.getLatestXsltForDatasetId(Integer.toString(TestObjectFactory.DATASETID));
  }

  @Test
  public void transformRecordsUsingLatestDefaultXslt() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    DatasetXslt datasetXslt = TestObjectFactory.createXslt(dataset);
    when(datasetXsltDao.getLatestXsltForDatasetId(DatasetXsltDao.DEFAULT_DATASET_ID))
        .thenReturn(datasetXslt);
    List<Record> listOfRecords = TestObjectFactory.createListOfRecords(5);
    listOfRecords.get(0).setXmlRecord("invalid xml");

    String xsltUrl = RestEndpoints
        .resolve(RestEndpoints.DATASETS_XSLT_XSLTID, datasetXslt.getId().toString());
    wireMockRule.stubFor(get(urlEqualTo(xsltUrl))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/plain")
            .withBody(datasetXslt.getXslt())));

    List<Record> records = datasetService
        .transformRecordsUsingLatestDefaultXslt(metisUser, dataset.getDatasetId(), listOfRecords);
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc;
    Assert.assertTrue(!records.get(0).getXmlRecord().contains("record")); //First record is invalid
    for (int i = 1; i < records.size(); i++) {
      doc = dBuilder.parse(new InputSource(new StringReader(records.get(i).getXmlRecord())));
      Assert.assertEquals(1, doc.getElementsByTagName("record").getLength());
      Assert.assertEquals(Integer.toString(i),
          doc.getElementsByTagName("element").item(0).getTextContent());
    }
  }

  @Test(expected = NoXsltFoundException.class)
  public void transformRecordsUsingLatestDefaultXslt_NoXsltFoundException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(datasetXsltDao.getLatestXsltForDatasetId(DatasetXsltDao.DEFAULT_DATASET_ID))
        .thenReturn(null);
    List<Record> listOfRecords = TestObjectFactory.createListOfRecords(1);
    datasetService
        .transformRecordsUsingLatestDefaultXslt(metisUser, dataset.getDatasetId(), listOfRecords);
  }

  @Test
  public void transformRecordsUsingLatestDatasetXslt() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    dataset.setXsltId(new ObjectId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    DatasetXslt datasetXslt = TestObjectFactory.createXslt(dataset);
    when(datasetXsltDao.getById(dataset.getXsltId().toString())).thenReturn(datasetXslt);
    List<Record> listOfRecords = TestObjectFactory.createListOfRecords(5);

    String xsltUrl = RestEndpoints
        .resolve(RestEndpoints.DATASETS_XSLT_XSLTID, datasetXslt.getId().toString());
    wireMockRule.stubFor(get(urlEqualTo(xsltUrl))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "text/plain")
            .withBody(datasetXslt.getXslt())));

    List<Record> records = datasetService
        .transformRecordsUsingLatestDatasetXslt(metisUser, dataset.getDatasetId(), listOfRecords);
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc;
    for (int i = 0; i < records.size(); i++) {
      doc = dBuilder.parse(new InputSource(new StringReader(records.get(i).getXmlRecord())));
      Assert.assertEquals(1, doc.getElementsByTagName("record").getLength());
      Assert.assertEquals(Integer.toString(i),
          doc.getElementsByTagName("element").item(0).getTextContent());
    }
  }

  @Test(expected = NoXsltFoundException.class)
  public void transformRecordsUsingLatestDatasetXslt_NoXsltFoundException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    List<Record> listOfRecords = TestObjectFactory.createListOfRecords(1);
    datasetService
        .transformRecordsUsingLatestDatasetXslt(metisUser, dataset.getDatasetId(), listOfRecords);
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
    int nextPage = 1;
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

