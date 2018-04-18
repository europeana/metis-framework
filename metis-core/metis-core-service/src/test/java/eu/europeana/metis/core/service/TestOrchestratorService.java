package eu.europeana.metis.core.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.DatasetXsltDao;
import eu.europeana.metis.core.dao.WorkflowDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetExecutionInformation;
import eu.europeana.metis.core.dataset.DatasetXslt;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowFoundException;
import eu.europeana.metis.core.exceptions.PluginExecutionNotAllowed;
import eu.europeana.metis.core.exceptions.WorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.WorkflowExecutionAlreadyExistsException;
import eu.europeana.metis.core.execution.ExecutionRules;
import eu.europeana.metis.core.execution.WorkflowExecutorManager;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.OrderField;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.EnrichmentPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutionProgress;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPlugin;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.core.workflow.plugins.TransformationPluginMetadata;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-06
 */
public class TestOrchestratorService {

  private static WorkflowExecutionDao workflowExecutionDao;
  private static WorkflowDao workflowDao;
  private static DatasetDao datasetDao;
  private static DatasetXsltDao datasetXsltDao;
  private static WorkflowExecutorManager workflowExecutorManager;
  private static OrchestratorService orchestratorService;
  private static RedissonClient redissonClient;

  @BeforeClass
  public static void prepare() throws IOException {
    workflowExecutionDao = Mockito.mock(WorkflowExecutionDao.class);
    workflowDao = Mockito.mock(WorkflowDao.class);
    datasetDao = Mockito.mock(DatasetDao.class);
    datasetXsltDao = Mockito.mock(DatasetXsltDao.class);
    workflowExecutorManager = Mockito.mock(WorkflowExecutorManager.class);
    redissonClient = Mockito.mock(RedissonClient.class);

    orchestratorService = new OrchestratorService(workflowDao, workflowExecutionDao, datasetDao,
        datasetXsltDao, workflowExecutorManager,
        redissonClient);
    orchestratorService.setMetisCoreUrl("https://some.url.com");
  }

  @After
  public void cleanUp() {
    Mockito.reset(workflowExecutionDao);
    Mockito.reset(workflowDao);
    Mockito.reset(datasetDao);
    Mockito.reset(workflowExecutorManager);
    Mockito.reset(redissonClient);
  }

  @Test
  public void createWorkflow() throws Exception {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset("datasetName");
    workflow.setDatasetId(dataset.getDatasetId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    orchestratorService.createWorkflow(workflow.getDatasetId(), workflow);

    InOrder inOrder = Mockito.inOrder(workflowDao);
    inOrder.verify(workflowDao, times(1)).exists(workflow);
    inOrder.verify(workflowDao, times(1)).create(workflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test(expected = WorkflowAlreadyExistsException.class)
  public void createWorkflow_AlreadyExists() throws Exception {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset("datasetName");
    workflow.setDatasetId(dataset.getDatasetId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao.exists(workflow)).thenReturn(new ObjectId().toString());

    orchestratorService.createWorkflow(workflow.getDatasetId(), workflow);

    InOrder inOrder = Mockito.inOrder(workflowDao);
    inOrder.verify(workflowDao, times(1)).exists(workflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test(expected = NoDatasetFoundException.class)
  public void createWorkflow_NoDatasetFoundException() throws Exception {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset("datasetName");
    workflow.setDatasetId(dataset.getDatasetId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(null);
    orchestratorService.createWorkflow(workflow.getDatasetId(), workflow);
  }

  @Test
  public void updateWorkflow() throws Exception {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset("datasetName");
    workflow.setDatasetId(dataset.getDatasetId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao.getWorkflow(dataset.getDatasetId())).thenReturn(workflow);
    orchestratorService.updateWorkflow(workflow.getDatasetId(), workflow);
    InOrder inOrder = Mockito.inOrder(workflowDao);
    inOrder.verify(workflowDao, times(1)).getWorkflow(dataset.getDatasetId());
    inOrder.verify(workflowDao, times(1)).update(workflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test(expected = NoDatasetFoundException.class)
  public void updateWorkflow_NoDatasetFoundException() throws Exception {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset("datasetName");
    workflow.setDatasetId(dataset.getDatasetId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(null);
    orchestratorService.updateWorkflow(workflow.getDatasetId(), workflow);
  }

  @Test(expected = NoWorkflowFoundException.class)
  public void updateUserWorkflow_NoUserWorkflowFound() throws Exception {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset("datasetName");
    workflow.setDatasetId(dataset.getDatasetId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    orchestratorService.updateWorkflow(workflow.getDatasetId(), workflow);
    InOrder inOrder = Mockito.inOrder(workflowDao);
    inOrder.verify(workflowDao, times(1)).exists(workflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void deleteWorkflow() {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    orchestratorService
        .deleteWorkflow(workflow.getDatasetId());
    ArgumentCaptor<Integer> workflowDatasetIdArgumentCaptor = ArgumentCaptor
        .forClass(Integer.class);
    verify(workflowDao, times(1)).deleteWorkflow(workflowDatasetIdArgumentCaptor.capture());
    Assert.assertEquals(workflow.getDatasetId(),
        workflowDatasetIdArgumentCaptor.getValue().intValue());
  }

  @Test
  public void getWorkflow() {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);

    Workflow retrievedWorkflow = orchestratorService.getWorkflow(workflow.getDatasetId());
    Assert.assertSame(workflow, retrievedWorkflow);
  }

  @Test
  public void getAllWorkflows() {
    orchestratorService.getAllWorkflows(anyString(), anyInt());
    InOrder inOrder = Mockito.inOrder(workflowDao);
    inOrder.verify(workflowDao, times(1)).getAllWorkflows(anyString(), anyInt());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void getWorkflowExecutionByExecutionId() {
    orchestratorService.getWorkflowExecutionByExecutionId(anyString());
    InOrder inOrder = Mockito.inOrder(workflowExecutionDao);
    inOrder.verify(workflowExecutionDao, times(1))
        .getById(anyString());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutions() throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(anyString())).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    DatasetXslt datasetXslt = TestObjectFactory.createXslt(dataset);
    datasetXslt.setId(new ObjectId(TestObjectFactory.XSLTID));
    when(datasetXsltDao.getLatestXsltForDatasetId(-1)).thenReturn(datasetXslt);
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(rlock).unlock();
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService.addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(), null, 0);
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutions_TransformationUsesCustomXslt()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    workflow.getMetisPluginsMetadata().forEach(abstractMetisPluginMetadata -> {
      if (abstractMetisPluginMetadata instanceof TransformationPluginMetadata) {
        ((TransformationPluginMetadata) abstractMetisPluginMetadata).setCustomXslt(true);
      }
    });
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(anyString())).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    DatasetXslt datasetXslt = TestObjectFactory.createXslt(dataset);
    datasetXslt.setId(new ObjectId(TestObjectFactory.XSLTID));
    dataset.setXsltId(datasetXslt.getId());
    when(datasetXsltDao.getById(dataset.getXsltId().toString())).thenReturn(datasetXslt);
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(rlock).unlock();
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService.addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(), null, 0);
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutions_AddHTTPHarvest()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    HTTPHarvestPluginMetadata httpHarvestPluginMetadata = new HTTPHarvestPluginMetadata();
    httpHarvestPluginMetadata.setEnabled(true);
    workflow.getMetisPluginsMetadata().set(0, httpHarvestPluginMetadata);
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    when(redissonClient.getFairLock(anyString())).thenReturn(Mockito.mock(RLock.class));
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService.addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(), null, 0);
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutions_NoHarvestPlugin()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    workflow.getMetisPluginsMetadata().remove(0);

    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    OaipmhHarvestPlugin oaipmhHarvestPlugin = new OaipmhHarvestPlugin();
    oaipmhHarvestPlugin.setStartedDate(new Date());
    ExecutionProgress executionProgress = new ExecutionProgress();
    executionProgress.setProcessedRecords(5);
    oaipmhHarvestPlugin.setExecutionProgress(executionProgress);
    when(workflowExecutionDao
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(dataset.getDatasetId(),
            ExecutionRules.getHarvestPluginGroup())).thenReturn(oaipmhHarvestPlugin);
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(anyString())).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(rlock).unlock();
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService.addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(), null, 0);
  }

  @Test(expected = PluginExecutionNotAllowed.class)
  public void addWorkflowInQueueOfWorkflowExecutions_NoHarvestPlugin_NoProcessPlugin()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    List<AbstractMetisPluginMetadata> abstractMetisPluginMetadata = new ArrayList<>();
    EnrichmentPluginMetadata enrichmentPluginMetadata = new EnrichmentPluginMetadata();
    enrichmentPluginMetadata.setEnabled(true);
    abstractMetisPluginMetadata.add(enrichmentPluginMetadata);
    workflow.setMetisPluginsMetadata(abstractMetisPluginMetadata);

    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    when(redissonClient.getFairLock(anyString())).thenReturn(Mockito.mock(RLock.class));
    OaipmhHarvestPlugin oaipmhHarvestPlugin = new OaipmhHarvestPlugin();
    oaipmhHarvestPlugin.setStartedDate(new Date());
    when(workflowExecutionDao
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(dataset.getDatasetId(),
            ExecutionRules.getHarvestPluginGroup())).thenReturn(oaipmhHarvestPlugin);
    orchestratorService.addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(), null, 0);
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutionsEcloudDatasetAlreadyGenerated()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setEcloudDatasetId("f525f64c-fea0-44bf-8c56-88f30962734c");
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    when(redissonClient.getFairLock(anyString())).thenReturn(Mockito.mock(RLock.class));
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService.addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(), null, 0);
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutionsEcloudDatasetAlreadyExistsInEcloud()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    when(datasetDao.checkAndCreateDatasetInEcloud(any(Dataset.class))).thenReturn(UUID.randomUUID().toString());
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(anyString())).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(rlock).unlock();
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService.addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(), null, 0);
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutionsEcloudDatasetCreationFails()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    when(datasetDao.checkAndCreateDatasetInEcloud(any(Dataset.class))).thenReturn(UUID.randomUUID().toString());
    when(redissonClient.getFairLock(anyString())).thenReturn(Mockito.mock(RLock.class));
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService.addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(), null, 0);
  }

  @Test(expected = NoDatasetFoundException.class)
  public void addWorkflowInQueueOfWorkflowExecutions_NoDatasetFoundException()
      throws Exception {
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(null);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(TestObjectFactory.DATASETID, null, 0);
  }

  @Test(expected = NoWorkflowFoundException.class)
  public void addWorkflowInQueueOfWorkflowExecutions_NoWorkflowFoundException()
      throws Exception {

    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao.getWorkflow(TestObjectFactory.DATASETID)).thenReturn(null);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(TestObjectFactory.DATASETID, null, 0);
  }

  @Test(expected = WorkflowExecutionAlreadyExistsException.class)
  public void addWorkflowInQueueOfWorkflowExecutions_WorkflowExecutionAlreadyExistsException()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao.getWorkflow(TestObjectFactory.DATASETID)).thenReturn(workflow);
    when(redissonClient.getFairLock(anyString())).thenReturn(Mockito.mock(RLock.class));
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId()))
        .thenReturn(new ObjectId().toString());
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(TestObjectFactory.DATASETID, null, 0);
  }

  @Test
  public void cancelWorkflowExecution() throws Exception {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    when(workflowExecutionDao.getById(TestObjectFactory.EXECUTIONID))
        .thenReturn(workflowExecution);
    doNothing().when(workflowExecutionDao)
        .setCancellingState(workflowExecution);
    orchestratorService.cancelWorkflowExecution(TestObjectFactory.EXECUTIONID);
  }

  @Test(expected = NoWorkflowExecutionFoundException.class)
  public void cancelWorkflowExecution_NoWorkflowExecutionFoundException()
      throws Exception {
    when(workflowExecutionDao.getById(TestObjectFactory.EXECUTIONID))
        .thenReturn(null);
    orchestratorService.cancelWorkflowExecution(TestObjectFactory.EXECUTIONID);
    verifyNoMoreInteractions(workflowExecutorManager);
  }

  @Test
  public void removeActiveWorkflowExecutionsFromList() throws Exception {
    orchestratorService.removeActiveWorkflowExecutionsFromList(new ArrayList<>());
    verify(workflowExecutorManager, times(1)).initiateConsumer();
    verify(workflowExecutorManager, times(1)).getMonitorCheckIntervalInSecs();
    verifyNoMoreInteractions(workflowExecutorManager);
    verify(workflowExecutionDao, times(1)).removeActiveExecutionsFromList(anyList(), anyInt());
    verifyNoMoreInteractions(workflowExecutionDao);
  }

  @Test
  public void addWorkflowExecutionToQueue() {
    String objectId = new ObjectId().toString();
    orchestratorService.addWorkflowExecutionToQueue(objectId, 0);
    verify(workflowExecutorManager, times(1)).addWorkflowExecutionToQueue(objectId, 0);
    verifyNoMoreInteractions(workflowExecutorManager);
  }

  @Test
  public void getWorkflowExecutionsPerRequest() {
    orchestratorService.getWorkflowExecutionsPerRequest();
    verify(workflowExecutionDao, times(1)).getWorkflowExecutionsPerRequest();
  }

  @Test
  public void getWorkflowsPerRequest() {
    orchestratorService.getWorkflowsPerRequest();
    verify(workflowDao, times(1)).getWorkflowsPerRequest();
  }


  @Test
  public void getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution_HarvestPlugin()
      throws Exception {
    Assert.assertNull(orchestratorService
        .getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(
            TestObjectFactory.DATASETID, PluginType.OAIPMH_HARVEST, null));
  }

  @Test
  public void getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution_ProcessPlugin()
      throws Exception {
    OaipmhHarvestPlugin oaipmhHarvestPlugin = new OaipmhHarvestPlugin();
    ExecutionProgress executionProgress = new ExecutionProgress();
    executionProgress.setProcessedRecords(5);
    oaipmhHarvestPlugin.setExecutionProgress(executionProgress);
    when(workflowExecutionDao
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(TestObjectFactory.DATASETID,
            ExecutionRules.getHarvestPluginGroup())).thenReturn(oaipmhHarvestPlugin);
    Assert.assertEquals(PluginType.OAIPMH_HARVEST, orchestratorService
        .getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(
            TestObjectFactory.DATASETID, PluginType.VALIDATION_EXTERNAL, null).getPluginType());
  }

  @Test(expected = PluginExecutionNotAllowed.class)
  public void getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution_PluginExecutionNotAllowed()
      throws Exception {
    when(workflowExecutionDao
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(TestObjectFactory.DATASETID,
            ExecutionRules.getHarvestPluginGroup())).thenReturn(null);
    orchestratorService
        .getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(
            TestObjectFactory.DATASETID, PluginType.VALIDATION_EXTERNAL, null);
  }

  @Test(expected = PluginExecutionNotAllowed.class)
  public void getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution_PluginExecutionNotAllowed_ProcessedRecordSameAsErrors()
      throws Exception {
    when(workflowExecutionDao
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(TestObjectFactory.DATASETID,
            ExecutionRules.getHarvestPluginGroup())).thenReturn(new OaipmhHarvestPlugin());
    orchestratorService
        .getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(
            TestObjectFactory.DATASETID, PluginType.VALIDATION_EXTERNAL, null);
  }

  @Test
  public void getAllWorkflowExecutionsByDatasetId() {
    HashSet<WorkflowStatus> workflowStatuses = new HashSet<>();
    workflowStatuses.add(WorkflowStatus.INQUEUE);
    orchestratorService.getAllWorkflowExecutions(TestObjectFactory.DATASETID,
        TestObjectFactory.WORKFLOWOWNER, workflowStatuses, OrderField.ID, false, 0);
    verify(workflowExecutionDao, times(1))
        .getAllWorkflowExecutions(anyInt(), anyString(), anySet(),
            any(OrderField.class), anyBoolean(), anyInt());
    verifyNoMoreInteractions(workflowExecutionDao);
  }

  @Test
  public void getDatasetExecutionInformation() {
    ExecutionProgress executionProgress = new ExecutionProgress();
    executionProgress.setProcessedRecords(100);
    executionProgress.setErrors(20);
    OaipmhHarvestPlugin oaipmhHarvestPlugin = new OaipmhHarvestPlugin(
        new OaipmhHarvestPluginMetadata());
    oaipmhHarvestPlugin.setFinishedDate(new Date(1000));
    oaipmhHarvestPlugin.setExecutionProgress(executionProgress);
    IndexToPublishPlugin firstPublishPlugin = new IndexToPublishPlugin(
        new IndexToPublishPluginMetadata());
    firstPublishPlugin.setFinishedDate(new Date(2000));
    firstPublishPlugin.setExecutionProgress(executionProgress);
    IndexToPublishPlugin lastPublishPlugin = new IndexToPublishPlugin(
        new IndexToPublishPluginMetadata());
    lastPublishPlugin.setFinishedDate(new Date(3000));
    lastPublishPlugin.setExecutionProgress(executionProgress);

    when(workflowExecutionDao
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(TestObjectFactory.DATASETID,
            EnumSet
                .of(PluginType.HTTP_HARVEST, PluginType.OAIPMH_HARVEST)))
        .thenReturn(oaipmhHarvestPlugin);
    when(workflowExecutionDao
        .getFirstFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(
            TestObjectFactory.DATASETID, EnumSet
                .of(PluginType.PUBLISH))).thenReturn(firstPublishPlugin);
    when(workflowExecutionDao
        .getLastFinishedWorkflowExecutionPluginByDatasetIdAndPluginType(TestObjectFactory.DATASETID,
            EnumSet
                .of(PluginType.PUBLISH))).thenReturn(lastPublishPlugin);

    DatasetExecutionInformation datasetExecutionInformation = orchestratorService
        .getDatasetExecutionInformation(TestObjectFactory.DATASETID);

    Assert.assertEquals(oaipmhHarvestPlugin.getFinishedDate(),
        datasetExecutionInformation.getLastHarvestedDate());
    Assert.assertEquals(
        oaipmhHarvestPlugin.getExecutionProgress().getProcessedRecords() - oaipmhHarvestPlugin
            .getExecutionProgress().getErrors(),
        datasetExecutionInformation.getLastHarvestedRecords());
    Assert.assertEquals(firstPublishPlugin.getFinishedDate(),
        datasetExecutionInformation.getFirstPublishedDate());
    Assert.assertEquals(lastPublishPlugin.getFinishedDate(),
        datasetExecutionInformation.getLastPublishedDate());
    Assert.assertEquals(
        lastPublishPlugin.getExecutionProgress().getProcessedRecords() - lastPublishPlugin
            .getExecutionProgress().getErrors(),
        datasetExecutionInformation.getLastPublishedRecords());
  }
}
