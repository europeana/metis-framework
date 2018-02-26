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

import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.cloud.service.mcs.exception.DataSetAlreadyExistsException;
import eu.europeana.cloud.service.mcs.exception.MCSException;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.ScheduledWorkflowDao;
import eu.europeana.metis.core.dao.WorkflowDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoScheduledWorkflowFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowFoundException;
import eu.europeana.metis.core.exceptions.PluginExecutionNotAllowed;
import eu.europeana.metis.core.exceptions.ScheduledWorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.WorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.WorkflowExecutionAlreadyExistsException;
import eu.europeana.metis.core.execution.ExecutionRules;
import eu.europeana.metis.core.execution.WorkflowExecutorManager;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.OrderField;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.EnrichmentPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ExecutionProgress;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.core.workflow.plugins.ValidationExternalPluginMetadata;
import eu.europeana.metis.exception.BadContentException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
  private static ScheduledWorkflowDao scheduledWorkflowDao;
  private static DatasetDao datasetDao;
  private static WorkflowExecutorManager workflowExecutorManager;
  private static OrchestratorService orchestratorService;
  private static DataSetServiceClient ecloudDataSetServiceClient;
  private static RedissonClient redissonClient;

  @BeforeClass
  public static void prepare() throws IOException {
    workflowExecutionDao = Mockito.mock(WorkflowExecutionDao.class);
    workflowDao = Mockito.mock(WorkflowDao.class);
    scheduledWorkflowDao = Mockito.mock(ScheduledWorkflowDao.class);
    datasetDao = Mockito.mock(DatasetDao.class);
    workflowExecutorManager = Mockito.mock(WorkflowExecutorManager.class);
    ecloudDataSetServiceClient = Mockito.mock(DataSetServiceClient.class);
    redissonClient = Mockito.mock(RedissonClient.class);

    orchestratorService = new OrchestratorService(workflowDao, workflowExecutionDao,
        scheduledWorkflowDao, datasetDao, workflowExecutorManager, ecloudDataSetServiceClient,
        redissonClient);
    orchestratorService.setEcloudProvider("ecloudProvider");
  }

  @After
  public void cleanUp() {
    Mockito.reset(workflowExecutionDao);
    Mockito.reset(workflowDao);
    Mockito.reset(scheduledWorkflowDao);
    Mockito.reset(datasetDao);
    Mockito.reset(workflowExecutorManager);
    Mockito.reset(ecloudDataSetServiceClient);
    Mockito.reset(redissonClient);
  }

  @Test
  public void createWorkflow() throws Exception {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    orchestratorService.createWorkflow(workflow);

    InOrder inOrder = Mockito.inOrder(workflowDao);
    inOrder.verify(workflowDao, times(1)).exists(workflow);
    inOrder.verify(workflowDao, times(1)).create(workflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test(expected = WorkflowAlreadyExistsException.class)
  public void createWorkflow_AlreadyExists() throws Exception {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(workflowDao.exists(workflow)).thenReturn(new ObjectId().toString());

    orchestratorService.createWorkflow(workflow);

    InOrder inOrder = Mockito.inOrder(workflowDao);
    inOrder.verify(workflowDao, times(1)).exists(workflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void updateWorkflow() throws Exception {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(workflowDao.exists(workflow)).thenReturn(new ObjectId().toString());
    orchestratorService.updateWorkflow(workflow);
    InOrder inOrder = Mockito.inOrder(workflowDao);
    inOrder.verify(workflowDao, times(1)).exists(workflow);
    inOrder.verify(workflowDao, times(1)).update(workflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test(expected = NoWorkflowFoundException.class)
  public void updateUserWorkflow_NoUserWorkflowFound() throws Exception {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    orchestratorService.updateWorkflow(workflow);
    InOrder inOrder = Mockito.inOrder(workflowDao);
    inOrder.verify(workflowDao, times(1)).exists(workflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void deleteWorkflow() {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    orchestratorService
        .deleteWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName());

    ArgumentCaptor<String> workflowOwnerArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> workflowNameArgumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(workflowDao, times(1)).deleteWorkflow(workflowOwnerArgumentCaptor.capture(),
        workflowNameArgumentCaptor.capture());
    Assert.assertEquals(workflow.getWorkflowOwner(), workflowOwnerArgumentCaptor.getValue());
    Assert.assertEquals(workflow.getWorkflowName(), workflowNameArgumentCaptor.getValue());
  }

  @Test
  public void getWorkflow() {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(workflowDao
        .getWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName()))
        .thenReturn(workflow);

    Workflow retrievedWorkflow = orchestratorService
        .getWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName());
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
  public void addWorkflowInQueueOfWorkflowExecutions()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName()))
        .thenReturn(workflow);
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(anyString())).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(rlock).unlock();
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService.addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(),
        workflow.getWorkflowOwner(), workflow.getWorkflowName(), null, 0);
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutions_AddHTTPHarvest()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setHarvestingMetadata(new HTTPHarvestPluginMetadata());
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName()))
        .thenReturn(workflow);
    when(redissonClient.getFairLock(anyString())).thenReturn(Mockito.mock(RLock.class));
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService.addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(),
        workflow.getWorkflowOwner(), workflow.getWorkflowName(), null, 0);
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutions_AddFakeHarvest()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setHarvestingMetadata(new ValidationExternalPluginMetadata());
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName()))
        .thenReturn(workflow);
    OaipmhHarvestPlugin oaipmhHarvestPlugin = new OaipmhHarvestPlugin();
    oaipmhHarvestPlugin.setStartedDate(new Date());
    ExecutionProgress executionProgress = new ExecutionProgress();
    executionProgress.setProcessedRecords(5);
    oaipmhHarvestPlugin.setExecutionProgress(executionProgress);
    when(workflowExecutionDao
        .getLatestFinishedWorkflowExecutionByDatasetIdAndPluginType(dataset.getDatasetId(),
            ExecutionRules.getHarvestPluginGroup())).thenReturn(oaipmhHarvestPlugin);
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(anyString())).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(rlock).unlock();
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService.addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(),
        workflow.getWorkflowOwner(), workflow.getWorkflowName(), null, 0);
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutions_NoHarvestPlugin()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    workflow.setHarvestPlugin(false);

    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName()))
        .thenReturn(workflow);
    OaipmhHarvestPlugin oaipmhHarvestPlugin = new OaipmhHarvestPlugin();
    oaipmhHarvestPlugin.setStartedDate(new Date());
    ExecutionProgress executionProgress = new ExecutionProgress();
    executionProgress.setProcessedRecords(5);
    oaipmhHarvestPlugin.setExecutionProgress(executionProgress);
    when(workflowExecutionDao
        .getLatestFinishedWorkflowExecutionByDatasetIdAndPluginType(dataset.getDatasetId(),
            ExecutionRules.getHarvestPluginGroup())).thenReturn(oaipmhHarvestPlugin);
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(anyString())).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(rlock).unlock();
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService.addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(),
        workflow.getWorkflowOwner(), workflow.getWorkflowName(), null, 0);
  }

  @Test(expected = PluginExecutionNotAllowed.class)
  public void addWorkflowInQueueOfWorkflowExecutions_NoHarvestPlugin_NoProcessPlugin()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    workflow.setHarvestPlugin(false);
    List<AbstractMetisPluginMetadata> abstractMetisPluginMetadata = new ArrayList<>();
    EnrichmentPluginMetadata enrichmentPluginMetadata = new EnrichmentPluginMetadata();
    abstractMetisPluginMetadata.add(enrichmentPluginMetadata);
    workflow.setMetisPluginsMetadata(abstractMetisPluginMetadata);

    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName()))
        .thenReturn(workflow);
    OaipmhHarvestPlugin oaipmhHarvestPlugin = new OaipmhHarvestPlugin();
    oaipmhHarvestPlugin.setStartedDate(new Date());
    when(workflowExecutionDao
        .getLatestFinishedWorkflowExecutionByDatasetIdAndPluginType(dataset.getDatasetId(),
            ExecutionRules.getHarvestPluginGroup())).thenReturn(oaipmhHarvestPlugin);
    orchestratorService.addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(),
        workflow.getWorkflowOwner(), workflow.getWorkflowName(), null, 0);
  }

  @Test(expected = PluginExecutionNotAllowed.class)
  public void addWorkflowInQueueOfWorkflowExecutions_PluginShouldNotBeSupported()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    List<AbstractMetisPluginMetadata> metisPluginsMetadata = workflow.getMetisPluginsMetadata();
    metisPluginsMetadata.add(new EnrichmentPluginMetadata());
    workflow.setMetisPluginsMetadata(metisPluginsMetadata);
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName()))
        .thenReturn(workflow);
    orchestratorService.addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(),
        workflow.getWorkflowOwner(), workflow.getWorkflowName(), null, 0);
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutionsEcloudDatasetAlreadyGenerated()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setEcloudDatasetId("f525f64c-fea0-44bf-8c56-88f30962734c");
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName()))
        .thenReturn(workflow);
    when(redissonClient.getFairLock(anyString())).thenReturn(Mockito.mock(RLock.class));
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService.addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(),
        workflow.getWorkflowOwner(), workflow.getWorkflowName(), null, 0);
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutionsEcloudDatasetAlreadyExistsInEcloud()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName()))
        .thenReturn(workflow);
    when(ecloudDataSetServiceClient.createDataSet(any(), any(), any()))
        .thenThrow(new DataSetAlreadyExistsException());
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(anyString())).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(rlock).unlock();
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService.addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(),
        workflow.getWorkflowOwner(), workflow.getWorkflowName(), null, 0);
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutionsEcloudDatasetCreationFails()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName()))
        .thenReturn(workflow);
    when(ecloudDataSetServiceClient.createDataSet(any(), any(), any()))
        .thenThrow(new MCSException());
    when(redissonClient.getFairLock(anyString())).thenReturn(Mockito.mock(RLock.class));
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService.addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(),
        workflow.getWorkflowOwner(), workflow.getWorkflowName(), null, 0);
  }

  @Test(expected = NoDatasetFoundException.class)
  public void addWorkflowInQueueOfWorkflowExecutions_NoDatasetFoundException()
      throws Exception {
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(null);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(TestObjectFactory.DATASETID,
            TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME, null, 0);
  }

  @Test(expected = NoWorkflowFoundException.class)
  public void addWorkflowInQueueOfWorkflowExecutions_NoWorkflowFoundException()
      throws Exception {

    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(null);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(TestObjectFactory.DATASETID,
            TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME, null, 0);
  }

  @Test(expected = WorkflowExecutionAlreadyExistsException.class)
  public void addWorkflowInQueueOfWorkflowExecutions_WorkflowExecutionAlreadyExistsException()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(redissonClient.getFairLock(anyString())).thenReturn(Mockito.mock(RLock.class));
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId()))
        .thenReturn(new ObjectId().toString());
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(TestObjectFactory.DATASETID,
            TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME, null, 0);
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutionsByWorkflow()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao.exists(workflow)).thenReturn(null);
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(anyString())).thenReturn(rlock);
    doNothing().when(rlock).lock();
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(rlock).unlock();
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(), workflow,
            null, 0);
  }

  @Test(expected = NoDatasetFoundException.class)
  public void addWorkflowInQueueOfWorkflowExecutionsByWorkflow_NoDatasetFoundException()
      throws Exception {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(null);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(TestObjectFactory.DATASETID, workflow,
            null, 0);
  }

  @Test(expected = WorkflowAlreadyExistsException.class)
  public void addWorkflowInQueueOfWorkflowExecutionsByWorkflow_WorkflowAlreadyExistsException()
      throws Exception {

    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao.exists(workflow)).thenReturn(new ObjectId().toString());
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(TestObjectFactory.DATASETID,
            workflow, null, 0);
  }

  @Test(expected = WorkflowExecutionAlreadyExistsException.class)
  public void addWorkflowInQueueOfWorkflowExecutionsByWorkflow_WorkflowExecutionAlreadyExistsException()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao.exists(workflow)).thenReturn(null);
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(anyString())).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId()))
        .thenReturn(new ObjectId().toString());
    doNothing().when(rlock).unlock();
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(TestObjectFactory.DATASETID,
            workflow, null, 0);
  }

  @Test
  public void cancelWorkflowExecution() throws Exception {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    when(workflowExecutionDao.getById(TestObjectFactory.EXECUTIONID))
        .thenReturn(workflowExecution);
    doNothing().when(workflowExecutorManager)
        .cancelWorkflowExecution(workflowExecution);
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
  public void getScheduledWorkflowsPerRequest() {
    orchestratorService.getScheduledWorkflowsPerRequest();
    verify(scheduledWorkflowDao, times(1)).getScheduledWorkflowPerRequest();
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
        .getLatestFinishedWorkflowExecutionByDatasetIdAndPluginType(TestObjectFactory.DATASETID,
            ExecutionRules.getHarvestPluginGroup())).thenReturn(oaipmhHarvestPlugin);
    Assert.assertEquals(PluginType.OAIPMH_HARVEST, orchestratorService
        .getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(
            TestObjectFactory.DATASETID, PluginType.VALIDATION_EXTERNAL, null).getPluginType());
  }

  @Test(expected = PluginExecutionNotAllowed.class)
  public void getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution_PluginExecutionNotAllowed()
      throws Exception {
    when(workflowExecutionDao
        .getLatestFinishedWorkflowExecutionByDatasetIdAndPluginType(TestObjectFactory.DATASETID,
            ExecutionRules.getHarvestPluginGroup())).thenReturn(null);
    orchestratorService
        .getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(
            TestObjectFactory.DATASETID, PluginType.VALIDATION_EXTERNAL, null);
  }

  @Test(expected = PluginExecutionNotAllowed.class)
  public void getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution_PluginExecutionNotAllowed_ProcessedRecordSameAsErrors()
      throws Exception {
    when(workflowExecutionDao
        .getLatestFinishedWorkflowExecutionByDatasetIdAndPluginType(TestObjectFactory.DATASETID,
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
        TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME,
        workflowStatuses, OrderField.ID, false, 0);
    verify(workflowExecutionDao, times(1))
        .getAllWorkflowExecutions(anyInt(), anyString(), anyString(), anySet(),
            any(OrderField.class), anyBoolean(), anyInt());
    verifyNoMoreInteractions(workflowExecutionDao);
  }

  @Test
  public void getScheduledWorkflowByDatasetName() {
    orchestratorService.getScheduledWorkflowByDatasetId(TestObjectFactory.DATASETID);
    verify(scheduledWorkflowDao, times(1)).getScheduledWorkflowByDatasetId(anyInt());
    verifyNoMoreInteractions(scheduledWorkflowDao);
  }

  @Test
  public void scheduleWorkflow() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(null);
    when(scheduledWorkflowDao.create(scheduledWorkflow))
        .thenReturn(new ObjectId().toString());
    orchestratorService.scheduleWorkflow(scheduledWorkflow);
  }

  @Test(expected = NoDatasetFoundException.class)
  public void scheduleWorkflow_NoDatasetFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(null);
    orchestratorService.scheduleWorkflow(scheduledWorkflow);
  }

  @Test(expected = NoWorkflowFoundException.class)
  public void scheduleWorkflow_NoWorkflowFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(null);
    orchestratorService.scheduleWorkflow(scheduledWorkflow);
  }

  @Test(expected = ScheduledWorkflowAlreadyExistsException.class)
  public void scheduleWorkflow_ScheduledWorkflowAlreadyExistsException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(new ObjectId().toString());
    orchestratorService.scheduleWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void scheduleUserWorkflow_BadContentException_nullPointerDate() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflow.setPointerDate(null);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(null);
    orchestratorService.scheduleWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void scheduleWorkflow_BadContentException_NULLScheduleFrequence() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflow.setScheduleFrequence(ScheduleFrequence.NULL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(null);
    orchestratorService.scheduleWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void scheduleWorkflow_BadContentException_nullScheduleFrequence() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflow.setScheduleFrequence(null);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(null);
    orchestratorService.scheduleWorkflow(scheduledWorkflow);
  }

  @Test
  public void getAllScheduledWorkflows() {
    orchestratorService
        .getAllScheduledWorkflows(ScheduleFrequence.ONCE, 0);
    verify(scheduledWorkflowDao, times(1))
        .getAllScheduledWorkflows(any(ScheduleFrequence.class), anyInt());
  }

  @Test
  public void getAllScheduledUserWorkflowsByDateRangeONCE() {
    orchestratorService
        .getAllScheduledWorkflowsByDateRangeONCE(LocalDateTime.now(), LocalDateTime.now(),
            0);
    verify(scheduledWorkflowDao, times(1))
        .getAllScheduledWorkflowsByDateRangeONCE(any(LocalDateTime.class),
            any(LocalDateTime.class), anyInt());
  }

  @Test
  public void updateScheduledWorkflow() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    Workflow workflow = TestObjectFactory.createWorkflowObject();

    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(new ObjectId().toString());
    when(scheduledWorkflowDao.update(scheduledWorkflow))
        .thenReturn(new ObjectId().toString());
    orchestratorService.updateScheduledWorkflow(scheduledWorkflow);
  }

  @Test(expected = NoWorkflowFoundException.class)
  public void updateScheduledUserWorkflow_NoUserWorkflowFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(null);
    orchestratorService.updateScheduledWorkflow(scheduledWorkflow);
  }

  @Test(expected = NoScheduledWorkflowFoundException.class)
  public void updateScheduledWorkflow_NoScheduledWorkflowFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    Workflow workflow = TestObjectFactory.createWorkflowObject();

    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(null);
    orchestratorService.updateScheduledWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void updateScheduledWorkflow_BadContentException_nullPointerDate() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflow.setPointerDate(null);
    Workflow workflow = TestObjectFactory.createWorkflowObject();

    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(new ObjectId().toString());
    orchestratorService.updateScheduledWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void updateScheduledWorkflow_BadContentException_NULLScheduleFrequence()
      throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflow.setScheduleFrequence(ScheduleFrequence.NULL);
    Workflow workflow = TestObjectFactory.createWorkflowObject();

    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(new ObjectId().toString());
    orchestratorService.updateScheduledWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void updateScheduledWorkflow_BadContentException_nullScheduleFrequence()
      throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflow.setScheduleFrequence(null);
    Workflow workflow = TestObjectFactory.createWorkflowObject();

    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(new ObjectId().toString());
    orchestratorService.updateScheduledWorkflow(scheduledWorkflow);
  }

  @Test
  public void deleteScheduledWorkflow() {
    orchestratorService
        .deleteScheduledWorkflow(TestObjectFactory.DATASETID);
    verify(scheduledWorkflowDao, times(1))
        .deleteScheduledWorkflow(anyInt());
  }
}
