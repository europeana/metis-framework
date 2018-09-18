package eu.europeana.metis.core.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import eu.europeana.metis.RestEndpoints;
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
import eu.europeana.metis.exception.GenericMetisException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import eu.europeana.metis.utils.NetworkUtil;

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
  private Authorizer authorizer;

  private static final String DATASET_CREATION_LOCK = "datasetCreationLock";

  public TestDatasetService() throws IOException {
  }

  private static void expectException(Class<? extends GenericMetisException> exceptionType,
      TestAction action) throws GenericMetisException {
    try {
      action.test();
      fail();
    } catch (GenericMetisException e) {
      if (!e.getClass().equals(exceptionType)) {
        throw e;
      }
    }
  }

  private interface TestAction {

    void test() throws GenericMetisException;
  }

  @Before
  public void prepare() {
    datasetDao = mock(DatasetDao.class);
    datasetXsltDao = mock(DatasetXsltDao.class);
    workflowDao = mock(WorkflowDao.class);
    workflowExecutionDao = mock(WorkflowExecutionDao.class);
    scheduledWorkflowDao = mock(ScheduledWorkflowDao.class);
    redissonClient = mock(RedissonClient.class);
    authorizer = mock(Authorizer.class);

    datasetService = new DatasetService(datasetDao, datasetXsltDao, workflowDao,
        workflowExecutionDao, scheduledWorkflowDao, redissonClient, authorizer);
    datasetService.setMetisCoreUrl(String.format("http://localhost:%d", portForWireMock));
  }

  @Test
  public void testCreateDataset() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
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
    verify(authorizer, times(1)).authorizeWriteNewDataset(metisUser);
    verifyNoMoreInteractions(authorizer);
  }

  @Test
  public void testCreateDatasetUnauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    doThrow(UserUnauthorizedException.class).when(authorizer).authorizeWriteNewDataset(metisUser);
    expectException(UserUnauthorizedException.class, () -> {
      datasetService.createDataset(metisUser, dataset);
    });
    verify(datasetDao, times(0)).create(dataset);
  }

  @Test
  public void testCreateDatasetAlreadyExists() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);

    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(DATASET_CREATION_LOCK)).thenReturn(rlock);
    when(datasetDao.getDatasetByOrganizationIdAndDatasetName(metisUser.getOrganizationId(),
        dataset.getDatasetName())).thenReturn(dataset);
    expectException(DatasetAlreadyExistsException.class, () -> {
      datasetService.createDataset(metisUser, dataset);
    });
    verify(datasetDao, times(0)).create(any(Dataset.class));
    verify(datasetDao, times(0)).getById(null);
  }

  @Test
  public void testUpdateDataset() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setProvider("newProvider");
    Dataset storedDataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    storedDataset.setUpdatedDate(new Date(-1000));
    storedDataset.setOrganizationId(metisUser.getOrganizationId());
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    when(authorizer.authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(storedDataset);
    when(datasetXsltDao.create(any(DatasetXslt.class))).thenReturn(TestObjectFactory.XSLTID);
    datasetService.updateDataset(metisUser, dataset,
        TestObjectFactory.createXslt(TestObjectFactory.createDataset(dataset.getDatasetName()))
            .getXslt());
    verify(authorizer, times(1))
        .authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId());
    verifyNoMoreInteractions(authorizer);

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
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setProvider("newProvider");
    Dataset storedDataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    storedDataset.setUpdatedDate(new Date(-1000));
    storedDataset.setOrganizationId(metisUser.getOrganizationId());
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    when(authorizer.authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(storedDataset);
    when(datasetXsltDao.create(any(DatasetXslt.class))).thenReturn(TestObjectFactory.XSLTID);
    datasetService.updateDataset(metisUser, dataset, null);
    verify(authorizer, times(1))
        .authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId());
    verifyNoMoreInteractions(authorizer);

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
  public void testUpdateDatasetUnauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    doThrow(UserUnauthorizedException.class).when(authorizer)
        .authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId());
    expectException(UserUnauthorizedException.class, () -> {
      datasetService.updateDataset(metisUser, dataset, null);
    });
    verify(datasetDao, times(0)).update(dataset);
  }

  @Test(expected = DatasetAlreadyExistsException.class)
  public void testUpdateDatasetDatasetAlreadyExistsException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    Dataset storedDataset = TestObjectFactory
        .createDataset(String.format("%s%s", TestObjectFactory.DATASETNAME, 10));
    storedDataset.setOrganizationId(metisUser.getOrganizationId());
    when(authorizer.authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(storedDataset);
    when(datasetDao.getDatasetByOrganizationIdAndDatasetName(dataset.getOrganizationId(),
        dataset.getDatasetName())).thenReturn(new Dataset());
    datasetService.updateDataset(metisUser, dataset, null);
  }

  @Test(expected = BadContentException.class)
  public void testUpdateDatasetDatasetExecutionIsActive() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    when(authorizer.authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn("ObjectId");
    datasetService.updateDataset(metisUser, dataset, null);
  }

  @Test(expected = UserUnauthorizedException.class)
  public void testUpdateDatasetNoDatasetFoundException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    doThrow(UserUnauthorizedException.class).when(authorizer)
        .authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId());
    datasetService.updateDataset(metisUser, dataset, null);
  }

  @Test
  public void testDeleteDatasetByDatasetId() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    when(authorizer.authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowExecutionDao.existsAndNotCompleted(Integer.toString(TestObjectFactory.DATASETID)))
        .thenReturn(null);
    datasetService
        .deleteDatasetByDatasetId(metisUser, Integer.toString(TestObjectFactory.DATASETID));
    verify(authorizer, times(1))
        .authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId());
    verifyNoMoreInteractions(authorizer);
    verify(datasetDao, times(1)).deleteByDatasetId(Integer.toString(TestObjectFactory.DATASETID));
    verify(workflowExecutionDao, times(1))
        .deleteAllByDatasetId(Integer.toString(TestObjectFactory.DATASETID));
    verify(scheduledWorkflowDao, times(1))
        .deleteAllByDatasetId(Integer.toString(TestObjectFactory.DATASETID));
  }

  @Test
  public void testDeleteDatasetByDatasetIdUnauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    doThrow(UserUnauthorizedException.class).when(authorizer)
        .authorizeWriteExistingDatasetById(metisUser, datasetId);
    expectException(UserUnauthorizedException.class, () -> {
      datasetService.deleteDatasetByDatasetId(metisUser, datasetId);
    });
    verify(datasetDao, times(0)).deleteByDatasetId(datasetId);
  }

  @Test
  public void testDeleteDatasetByDatasetIdNoDatasetFoundException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    doThrow(NoDatasetFoundException.class).when(authorizer)
        .authorizeWriteExistingDatasetById(metisUser, datasetId);
    expectException(NoDatasetFoundException.class, () -> {
      datasetService.deleteDatasetByDatasetId(metisUser, datasetId);
    });
    verify(datasetDao, times(0)).deleteByDatasetId(datasetId);
  }

  @Test(expected = BadContentException.class)
  public void testDeleteDatasetDatasetExecutionIsActive() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    when(authorizer.authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowExecutionDao.existsAndNotCompleted(Integer.toString(TestObjectFactory.DATASETID)))
        .thenReturn("ObjectId");
    datasetService
        .deleteDatasetByDatasetId(metisUser, Integer.toString(TestObjectFactory.DATASETID));
  }

  @Test
  public void testGetDatasetByDatasetName() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    when(authorizer.authorizeReadExistingDatasetByName(metisUser, TestObjectFactory.DATASETNAME))
        .thenReturn(dataset);
    Dataset returnedDataset = datasetService
        .getDatasetByDatasetName(metisUser, TestObjectFactory.DATASETNAME);
    assertNotNull(returnedDataset);
    verify(authorizer, times(1))
        .authorizeReadExistingDatasetByName(metisUser, TestObjectFactory.DATASETNAME);
    verifyNoMoreInteractions(authorizer);
  }

  @Test
  public void testGetDatasetByDatasetNameUnauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    doThrow(UserUnauthorizedException.class).when(authorizer)
        .authorizeReadExistingDatasetByName(metisUser, TestObjectFactory.DATASETNAME);
    expectException(UserUnauthorizedException.class, () -> {
      datasetService.getDatasetByDatasetName(metisUser, TestObjectFactory.DATASETNAME);
    });
    verify(datasetDao, times(0)).getDatasetByDatasetName(TestObjectFactory.DATASETNAME);
  }

  @Test(expected = NoDatasetFoundException.class)
  public void testGetDatasetByDatasetNameNoDatasetFoundException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    doThrow(NoDatasetFoundException.class).when(authorizer)
        .authorizeReadExistingDatasetByName(metisUser, TestObjectFactory.DATASETNAME);
    datasetService.getDatasetByDatasetName(metisUser, TestObjectFactory.DATASETNAME);
  }

  @Test
  public void testGetDatasetByDatasetId() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    when(authorizer.authorizeReadExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(dataset);
    Dataset returnedDataset =
        datasetService.getDatasetByDatasetId(metisUser, dataset.getDatasetId());
    assertNotNull(returnedDataset);
    verify(authorizer, times(1))
        .authorizeReadExistingDatasetById(metisUser, dataset.getDatasetId());
    verifyNoMoreInteractions(authorizer);
  }

  @Test
  public void testGetDatasetByDatasetIdUnauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    doThrow(UserUnauthorizedException.class).when(authorizer)
        .authorizeReadExistingDatasetById(metisUser, datasetId);
    expectException(UserUnauthorizedException.class, () -> {
      datasetService.getDatasetByDatasetId(metisUser, datasetId);
    });
    verify(datasetDao, times(0)).getDatasetByDatasetId(datasetId);
  }

  @Test(expected = NoDatasetFoundException.class)
  public void testGetDatasetByDatasetIdNoDatasetFoundException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    doThrow(NoDatasetFoundException.class).when(authorizer)
        .authorizeReadExistingDatasetById(metisUser, datasetId);
    datasetService.getDatasetByDatasetId(metisUser, datasetId);
  }

  @Test
  public void getDatasetXsltByDatasetId() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    dataset.setXsltId(new ObjectId());
    when(authorizer.authorizeReadExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(dataset);
    DatasetXslt datasetXslt = TestObjectFactory.createXslt(dataset);
    when(datasetXsltDao.getById(dataset.getXsltId().toString())).thenReturn(datasetXslt);

    DatasetXslt datasetXsltByDatasetId = datasetService
        .getDatasetXsltByDatasetId(metisUser, dataset.getDatasetId());
    verify(authorizer, times(1))
        .authorizeReadExistingDatasetById(metisUser, dataset.getDatasetId());
    verifyNoMoreInteractions(authorizer);
    Assert.assertEquals(datasetXslt.getXslt(), datasetXsltByDatasetId.getXslt());
    Assert.assertEquals(datasetXslt.getDatasetId(), datasetXsltByDatasetId.getDatasetId());
  }

  @Test(expected = UserUnauthorizedException.class)
  public void getDatasetXsltByDatasetIdUnauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    doThrow(UserUnauthorizedException.class).when(authorizer)
        .authorizeReadExistingDatasetById(metisUser, datasetId);
    datasetService.getDatasetXsltByDatasetId(metisUser, datasetId);
  }

  @Test(expected = NoXsltFoundException.class)
  public void getDatasetXsltByDatasetIdNoXsltFoundException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    when(authorizer.authorizeReadExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(datasetXsltDao.getById(anyString())).thenReturn(null);
    datasetService.getDatasetXsltByDatasetId(metisUser, dataset.getDatasetId());
  }

  @Test(expected = NoDatasetFoundException.class)
  public void getDatasetXsltByDatasetIdNoDatasetFoundException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    doThrow(NoDatasetFoundException.class).when(authorizer)
        .authorizeReadExistingDatasetById(metisUser, datasetId);
    datasetService.getDatasetXsltByDatasetId(metisUser, datasetId);
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
    DatasetXslt datasetXslt = TestObjectFactory
        .createXslt(TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME));
    datasetXslt.setDatasetId("-1");
    when(datasetXsltDao.create(any(DatasetXslt.class))).thenReturn(TestObjectFactory.XSLTID);
    when(datasetXsltDao.getById(TestObjectFactory.XSLTID)).thenReturn(datasetXslt);
    DatasetXslt defaultDatasetXslt = datasetService
        .createDefaultXslt(metisUser, datasetXslt.getXslt());
    Assert.assertEquals(datasetXslt.getDatasetId(), defaultDatasetXslt.getDatasetId());
    verify(authorizer, times(1)).authorizeWriteDefaultXslt(metisUser);
    verifyNoMoreInteractions(authorizer);
  }

  @Test(expected = UserUnauthorizedException.class)
  public void createDefaultXsltUserUnauthorizedException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    DatasetXslt datasetXslt = TestObjectFactory
        .createXslt(TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME));
    doThrow(UserUnauthorizedException.class).when(authorizer).authorizeWriteDefaultXslt(metisUser);
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
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    when(authorizer.authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(dataset);
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
    verify(authorizer, times(1))
        .authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId());
    verifyNoMoreInteractions(authorizer);
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc;
    assertFalse(
        records.get(0).getXmlRecord().contains("edm:ProvidedCHO")); //First record is invalid
    for (int i = 1; i < records.size(); i++) {
      doc = dBuilder.parse(new InputSource(new StringReader(records.get(i).getXmlRecord())));
      assertEquals(1, doc.getElementsByTagName("edm:ProvidedCHO").getLength());
      assertTrue(doc.getElementsByTagName("edm:ProvidedCHO").item(0).getAttributes()
          .getNamedItem("rdf:about").getTextContent().contains(Integer.toString(i)));
    }
  }

  @Test(expected = NoXsltFoundException.class)
  public void transformRecordsUsingLatestDefaultXslt_NoXsltFoundException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
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
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    dataset.setXsltId(new ObjectId());
    when(authorizer.authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(dataset);
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
    verify(authorizer, times(1))
        .authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId());
    verifyNoMoreInteractions(authorizer);
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc;
    for (int i = 0; i < records.size(); i++) {
      doc = dBuilder.parse(new InputSource(new StringReader(records.get(i).getXmlRecord())));
      Assert.assertEquals(1, doc.getElementsByTagName("edm:ProvidedCHO").getLength());
      assertTrue(doc.getElementsByTagName("edm:ProvidedCHO").item(0).getAttributes()
          .getNamedItem("rdf:about").getTextContent().contains(Integer.toString(i)));
    }
  }

  @Test(expected = NoXsltFoundException.class)
  public void transformRecordsUsingLatestDatasetXslt_NoXsltFoundException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setOrganizationId(metisUser.getOrganizationId());
    when(authorizer.authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(dataset);
    List<Record> listOfRecords = TestObjectFactory.createListOfRecords(1);
    datasetService.transformRecordsUsingLatestDatasetXslt(metisUser, dataset.getDatasetId(),
        listOfRecords);
  }

  @Test
  public void testGetAllDatasetsByProvider() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    List<Dataset> list = new ArrayList<>();
    String provider = "myProvider";
    int nextPage = 1;
    when(datasetDao.getAllDatasetsByProvider(provider, nextPage)).thenReturn(list);
    List<Dataset> retList = datasetService.getAllDatasetsByProvider(metisUser, provider, nextPage);
    verify(authorizer, times(1)).authorizeReadAllDatasets(metisUser);
    verifyNoMoreInteractions(authorizer);
    assertSame(list, retList);
  }

  @Test
  public void testGetAllDatasetsByProviderUnauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    String provider = "myProvider";
    int nextPage = 1;
    doThrow(UserUnauthorizedException.class).when(authorizer).authorizeReadAllDatasets(metisUser);
    expectException(UserUnauthorizedException.class, () -> {
      datasetService.getAllDatasetsByProvider(metisUser, provider, nextPage);
    });
    verify(datasetDao, times(0)).getAllDatasetsByProvider(provider, nextPage);
  }

  @Test
  public void testGetAllDatasetsByIntermidiateProvider() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    List<Dataset> list = new ArrayList<>();
    String provider = "myProvider";
    int nextPage = 1;
    when(datasetDao.getAllDatasetsByIntermediateProvider(provider, nextPage)).thenReturn(list);
    List<Dataset> retList = datasetService
        .getAllDatasetsByIntermediateProvider(metisUser, provider, nextPage);
    verify(authorizer, times(1)).authorizeReadAllDatasets(metisUser);
    verifyNoMoreInteractions(authorizer);
    assertSame(list, retList);
  }

  @Test
  public void testGetAllDatasetsByIntermidiateProviderUnauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    String intermediateProvider = "myProvider";
    int nextPage = 1;
    doThrow(UserUnauthorizedException.class).when(authorizer).authorizeReadAllDatasets(metisUser);
    expectException(UserUnauthorizedException.class, () -> {
      datasetService.getAllDatasetsByIntermediateProvider(metisUser, intermediateProvider,
          nextPage);
    });
    verify(datasetDao, times(0)).getAllDatasetsByIntermediateProvider(intermediateProvider,
        nextPage);
  }

  @Test
  public void testGetAllDatasetsByDataProvider() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    List<Dataset> list = new ArrayList<>();
    String provider = "myProvider";
    int nextPage = 1;
    when(datasetDao.getAllDatasetsByDataProvider(provider, nextPage)).thenReturn(list);
    List<Dataset> retList = datasetService
        .getAllDatasetsByDataProvider(metisUser, provider, nextPage);
    verify(authorizer, times(1)).authorizeReadAllDatasets(metisUser);
    verifyNoMoreInteractions(authorizer);
    assertSame(list, retList);
  }

  @Test
  public void testGetAllDatasetsByDataProviderUnauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    String dataProvider = "myProvider";
    int nextPage = 1;
    doThrow(UserUnauthorizedException.class).when(authorizer).authorizeReadAllDatasets(metisUser);
    expectException(UserUnauthorizedException.class, () -> {
      datasetService.getAllDatasetsByDataProvider(metisUser, dataProvider, nextPage);
    });
    verify(datasetDao, times(0)).getAllDatasetsByDataProvider(dataProvider, nextPage);
  }

  @Test
  public void testGetAllDatasetsByOrganizationId() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    List<Dataset> list = new ArrayList<>();
    String organizationId = "organizationId";
    int nextPage = 1;
    when(datasetDao.getAllDatasetsByOrganizationId(organizationId, nextPage)).thenReturn(list);
    List<Dataset> retList = datasetService
        .getAllDatasetsByOrganizationId(metisUser, organizationId, nextPage);
    verify(authorizer, times(1)).authorizeReadAllDatasets(metisUser);
    verifyNoMoreInteractions(authorizer);
    assertSame(list, retList);
  }

  @Test
  public void testGetAllDatasetsByOrganizationIdUnauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    String organizationId = "organizationId";
    int nextPage = 1;
    doThrow(UserUnauthorizedException.class).when(authorizer).authorizeReadAllDatasets(metisUser);
    expectException(UserUnauthorizedException.class, () -> {
      datasetService.getAllDatasetsByOrganizationId(metisUser, organizationId, nextPage);
    });
    verify(datasetDao, times(0)).getAllDatasetsByOrganizationId(organizationId, nextPage);
  }

  @Test
  public void testGetAllDatasetsByOrganizationName() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    List<Dataset> list = new ArrayList<>();
    String organizationName = "organizationName";
    int nextPage = 1;
    when(datasetDao.getAllDatasetsByOrganizationName(organizationName, nextPage))
        .thenReturn(list);
    List<Dataset> retList = datasetService
        .getAllDatasetsByOrganizationName(metisUser, organizationName, nextPage);
    verify(authorizer, times(1)).authorizeReadAllDatasets(metisUser);
    verifyNoMoreInteractions(authorizer);
    assertSame(list, retList);
  }

  @Test
  public void testGetAllDatasetsByOrganizationNameUnauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    String organizationName = "organizationName";
    int nextPage = 1;
    doThrow(UserUnauthorizedException.class).when(authorizer).authorizeReadAllDatasets(metisUser);
    expectException(UserUnauthorizedException.class, () -> {
      datasetService.getAllDatasetsByOrganizationName(metisUser, organizationName, nextPage);
    });
    verify(datasetDao, times(0)).getAllDatasetsByOrganizationName(organizationName, nextPage);
  }

  @Test
  public void testGetDatasetsPerRequestLimit() {
    when(datasetDao.getDatasetsPerRequest()).thenReturn(5);
    assertEquals(5, datasetService.getDatasetsPerRequestLimit());
  }
}

