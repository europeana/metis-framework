package eu.europeana.metis.core.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.common.model.dps.SubTaskInfo;
import eu.europeana.cloud.common.model.dps.TaskErrorsInfo;
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.cloud.service.mcs.exception.DataSetAlreadyExistsException;
import eu.europeana.cloud.service.mcs.exception.MCSException;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.ScheduledWorkflowDao;
import eu.europeana.metis.core.dao.WorkflowDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoScheduledWorkflowFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowFoundException;
import eu.europeana.metis.core.exceptions.ScheduledWorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.WorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.WorkflowExecutionAlreadyExistsException;
import eu.europeana.metis.core.execution.WorkflowExecutorManager;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.OrderField;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.TopologyName;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
  private static DpsClient dpsClient;

  @BeforeClass
  public static void prepare() throws IOException {
    workflowExecutionDao = Mockito.mock(WorkflowExecutionDao.class);
    workflowDao = Mockito.mock(WorkflowDao.class);
    scheduledWorkflowDao = Mockito.mock(ScheduledWorkflowDao.class);
    datasetDao = Mockito.mock(DatasetDao.class);
    workflowExecutorManager = Mockito.mock(WorkflowExecutorManager.class);
    ecloudDataSetServiceClient = Mockito.mock(DataSetServiceClient.class);
    dpsClient = Mockito.mock(DpsClient.class);

    orchestratorService = new OrchestratorService(workflowDao, workflowExecutionDao,
        scheduledWorkflowDao, datasetDao, workflowExecutorManager, ecloudDataSetServiceClient,
        dpsClient);
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
  }

  @Test
  public void createUserWorkflow() throws Exception {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    orchestratorService.createWorkflow(workflow);

    InOrder inOrder = Mockito.inOrder(workflowDao);
    inOrder.verify(workflowDao, times(1)).exists(workflow);
    inOrder.verify(workflowDao, times(1)).create(workflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test(expected = WorkflowAlreadyExistsException.class)
  public void createUserWorkflow_AlreadyExists() throws Exception {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(workflowDao.exists(workflow)).thenReturn(new ObjectId().toString());

    orchestratorService.createWorkflow(workflow);

    InOrder inOrder = Mockito.inOrder(workflowDao);
    inOrder.verify(workflowDao, times(1)).exists(workflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void updateUserWorkflow() throws Exception {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(workflowDao.exists(workflow)).thenReturn(new ObjectId().toString());
    orchestratorService.updateWorkflow(workflow);
    InOrder inOrder = Mockito.inOrder(workflowDao);
    inOrder.verify(workflowDao, times(1)).exists(workflow);
    inOrder.verify(workflowDao, times(1)).update(workflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test(expected = NoWorkflowFoundException.class)
  public void updateUserWorkflow_NoUserWorkflowFound() throws Exception {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    orchestratorService.updateWorkflow(workflow);
    InOrder inOrder = Mockito.inOrder(workflowDao);
    inOrder.verify(workflowDao, times(1)).exists(workflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void deleteUserWorkflow() {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
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
  public void getUserWorkflow() {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(workflowDao
        .getWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName()))
        .thenReturn(workflow);

    Workflow retrievedWorkflow = orchestratorService
        .getWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName());
    Assert.assertSame(workflow, retrievedWorkflow);
  }

  @Test
  public void getAllUserWorkflows() {
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
  public void addUserWorkflowInQueueOfUserWorkflowExecutions()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName()))
        .thenReturn(workflow);
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService.addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(),
        workflow.getWorkflowOwner(), workflow.getWorkflowName(), 0);
  }

  @Test
  public void addUserWorkflowInQueueOfUserWorkflowExecutionsEcloudDatasetAlreadyGenerated()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setEcloudDatasetId("f525f64c-fea0-44bf-8c56-88f30962734c");
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName()))
        .thenReturn(workflow);
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService.addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(),
        workflow.getWorkflowOwner(), workflow.getWorkflowName(), 0);
  }

  @Test
  public void addUserWorkflowInQueueOfUserWorkflowExecutionsEcloudDatasetAlreadyExistsInEcloud()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName()))
        .thenReturn(workflow);
    when(ecloudDataSetServiceClient.createDataSet(any(), any(), any()))
        .thenThrow(new DataSetAlreadyExistsException());
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService.addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(),
        workflow.getWorkflowOwner(), workflow.getWorkflowName(), 0);
  }

  @Test
  public void addUserWorkflowInQueueOfUserWorkflowExecutionsEcloudDatasetCreationFails()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName()))
        .thenReturn(workflow);
    when(ecloudDataSetServiceClient.createDataSet(any(), any(), any()))
        .thenThrow(new MCSException());
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService.addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(),
        workflow.getWorkflowOwner(), workflow.getWorkflowName(), 0);
  }

  @Test(expected = NoDatasetFoundException.class)
  public void addUserWorkflowInQueueOfUserWorkflowExecutions_NoDatasetFoundException()
      throws Exception {
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(null);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(TestObjectFactory.DATASETID,
            TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME, 0);
  }

  @Test(expected = NoWorkflowFoundException.class)
  public void addUserWorkflowInQueueOfUserWorkflowExecutions_NoUserWorkflowFoundException()
      throws Exception {

    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(null);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(TestObjectFactory.DATASETID,
            TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME, 0);
  }

  @Test(expected = WorkflowExecutionAlreadyExistsException.class)
  public void addUserWorkflowInQueueOfUserWorkflowExecutions_UserWorkflowExecutionAlreadyExistsException()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId()))
        .thenReturn(new ObjectId().toString());
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(TestObjectFactory.DATASETID,
            TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME, 0);
  }

  @Test
  public void addUserWorkflowInQueueOfUserWorkflowExecutionsByUserWorkflow()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao.exists(workflow)).thenReturn(null);
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetId(), workflow, 0);
  }

  @Test(expected = NoDatasetFoundException.class)
  public void addUserWorkflowInQueueOfUserWorkflowExecutionsByUserWorkflow_NoDatasetFoundException()
      throws Exception {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(null);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(TestObjectFactory.DATASETID, workflow,
            0);
  }

  @Test(expected = WorkflowAlreadyExistsException.class)
  public void addUserWorkflowInQueueOfUserWorkflowExecutionsByUserWorkflow_UserWorkflowAlreadyExistsException()
      throws Exception {

    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao.exists(workflow)).thenReturn(new ObjectId().toString());
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(TestObjectFactory.DATASETID,
            workflow, 0);
  }

  @Test(expected = WorkflowExecutionAlreadyExistsException.class)
  public void addUserWorkflowInQueueOfUserWorkflowExecutionsByUserWorkflow_UserWorkflowExecutionAlreadyExistsException()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao.exists(workflow)).thenReturn(null);
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId()))
        .thenReturn(new ObjectId().toString());
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(TestObjectFactory.DATASETID,
            workflow, 0);
  }

  @Test
  public void cancelUserWorkflowExecution() throws Exception {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    when(workflowExecutionDao.getById(TestObjectFactory.EXECUTIONID))
        .thenReturn(workflowExecution);
    doNothing().when(workflowExecutorManager)
        .cancelWorkflowExecution(workflowExecution);
    orchestratorService.cancelWorkflowExecution(TestObjectFactory.EXECUTIONID);
  }

  @Test(expected = NoWorkflowExecutionFoundException.class)
  public void cancelUserWorkflowExecution_NoUserWorkflowExecutionFoundException()
      throws Exception {
    when(workflowExecutionDao.getById(TestObjectFactory.EXECUTIONID))
        .thenReturn(null);
    orchestratorService.cancelWorkflowExecution(TestObjectFactory.EXECUTIONID);
    verifyNoMoreInteractions(workflowExecutorManager);
  }

  @Test
  public void removeActiveUserWorkflowExecutionsFromList() {
    orchestratorService.removeActiveWorkflowExecutionsFromList(new ArrayList<>());
    verify(workflowExecutorManager, times(1)).getMonitorCheckIntervalInSecs();
    verifyNoMoreInteractions(workflowExecutorManager);
    verify(workflowExecutionDao, times(1)).removeActiveExecutionsFromList(anyList(), anyInt());
    verifyNoMoreInteractions(workflowExecutionDao);
  }

  @Test
  public void addUserWorkflowExecutionToQueue() {
    String objectId = new ObjectId().toString();
    orchestratorService.addWorkflowExecutionToQueue(objectId, 0);
    verify(workflowExecutorManager, times(1)).addWorkflowExecutionToQueue(objectId, 0);
    verifyNoMoreInteractions(workflowExecutorManager);
  }

  @Test
  public void getUserWorkflowExecutionsPerRequest() {
    orchestratorService.getWorkflowExecutionsPerRequest();
    verify(workflowExecutionDao, times(1)).getWorkflowExecutionsPerRequest();
  }

  @Test
  public void getScheduledUserWorkflowsPerRequest() {
    orchestratorService.getScheduledWorkflowsPerRequest();
    verify(scheduledWorkflowDao, times(1)).getScheduledWorkflowPerRequest();
  }

  @Test
  public void getUserWorkflowsPerRequest() {
    orchestratorService.getWorkflowsPerRequest();
    verify(workflowDao, times(1)).getWorkflowsPerRequest();
  }

  @Test
  public void getAllUserWorkflowExecutions() {
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
  public void getAllUserWorkflowExecutionsByWorkflowStatus() {
    orchestratorService.getAllWorkflowExecutions(WorkflowStatus.RUNNING, 0);
    verify(workflowExecutionDao, times(1))
        .getAllWorkflowExecutions(any(WorkflowStatus.class), anyInt());
    verifyNoMoreInteractions(workflowExecutionDao);
  }

  @Test
  public void getScheduledUserWorkflowByDatasetName() {
    orchestratorService.getScheduledWorkflowByDatasetId(TestObjectFactory.DATASETID);
    verify(scheduledWorkflowDao, times(1)).getScheduledWorkflowByDatasetId(anyInt());
    verifyNoMoreInteractions(scheduledWorkflowDao);
  }

  @Test
  public void scheduleUserWorkflow() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
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
  public void scheduleUserWorkflow_NoDatasetFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(null);
    orchestratorService.scheduleWorkflow(scheduledWorkflow);
  }

  @Test(expected = NoWorkflowFoundException.class)
  public void scheduleUserWorkflow_NoUserWorkflowFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(null);
    orchestratorService.scheduleWorkflow(scheduledWorkflow);
  }

  @Test(expected = ScheduledWorkflowAlreadyExistsException.class)
  public void scheduleUserWorkflow_ScheduledUserWorkflowAlreadyExistsException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
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
        .createScheduledUserWorkflowObject();
    scheduledWorkflow.setPointerDate(null);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(null);
    orchestratorService.scheduleWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void scheduleUserWorkflow_BadContentException_NULLScheduleFrequence() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledWorkflow.setScheduleFrequence(ScheduleFrequence.NULL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(null);
    orchestratorService.scheduleWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void scheduleUserWorkflow_BadContentException_nullScheduleFrequence() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledWorkflow.setScheduleFrequence(null);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(null);
    orchestratorService.scheduleWorkflow(scheduledWorkflow);
  }

  @Test
  public void getAllScheduledUserWorkflows() {
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
  public void updateScheduledUserWorkflow() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();

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
        .createScheduledUserWorkflowObject();
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(null);
    orchestratorService.updateScheduledWorkflow(scheduledWorkflow);
  }

  @Test(expected = NoScheduledWorkflowFoundException.class)
  public void updateScheduledUserWorkflow_NoScheduledUserWorkflowFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();

    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(null);
    orchestratorService.updateScheduledWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void updateScheduledUserWorkflow_BadContentException_nullPointerDate() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledWorkflow.setPointerDate(null);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();

    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(new ObjectId().toString());
    orchestratorService.updateScheduledWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void updateScheduledUserWorkflow_BadContentException_NULLScheduleFrequence()
      throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledWorkflow.setScheduleFrequence(ScheduleFrequence.NULL);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();

    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(new ObjectId().toString());
    orchestratorService.updateScheduledWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void updateScheduledUserWorkflow_BadContentException_nullScheduleFrequence()
      throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledWorkflow.setScheduleFrequence(null);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();

    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(new ObjectId().toString());
    orchestratorService.updateScheduledWorkflow(scheduledWorkflow);
  }

  @Test
  public void deleteScheduledUserWorkflow() {
    orchestratorService
        .deleteScheduledWorkflow(TestObjectFactory.DATASETID);
    verify(scheduledWorkflowDao, times(1))
        .deleteScheduledWorkflow(anyInt());
  }

  @Test
  public void getExternalTaskLogs() {
    List<SubTaskInfo> listOfSubTaskInfo = TestObjectFactory.createListOfSubTaskInfo();

    when(dpsClient
        .getDetailedTaskReportBetweenChunks(TopologyName.OAIPMH_HARVEST.getTopologyName(),
            2070373127078497810L,
            1, 100)).thenReturn(listOfSubTaskInfo);
    orchestratorService
        .getExternalTaskLogs(TopologyName.OAIPMH_HARVEST.getTopologyName(), 2070373127078497810L, 1,
            100);
    Assert.assertEquals(2, listOfSubTaskInfo.size());
    Assert.assertTrue(listOfSubTaskInfo.get(0).getAdditionalInformations() == null);
    Assert.assertTrue(listOfSubTaskInfo.get(1).getAdditionalInformations() == null);
  }

  @Test
  public void getExternalTaskReport() {
    TaskErrorsInfo taskErrorsInfo = TestObjectFactory.createTaskErrorsInfoListWithoutIdentifiers(2);
    TaskErrorsInfo taskErrorsInfoWithIdentifiers1 = TestObjectFactory
        .createTaskErrorsInfoWithIdentifiers(taskErrorsInfo.getErrors().get(0).getErrorType(),
            taskErrorsInfo.getErrors().get(0).getMessage());
    TaskErrorsInfo taskErrorsInfoWithIdentifiers2 = TestObjectFactory
        .createTaskErrorsInfoWithIdentifiers(taskErrorsInfo.getErrors().get(1).getErrorType(),
            taskErrorsInfo.getErrors().get(1).getMessage());

    when(dpsClient
        .getTaskErrorsReport(TopologyName.OAIPMH_HARVEST.getTopologyName(),
            TestObjectFactory.EXTERNAL_TASK_ID, null)).thenReturn(taskErrorsInfo);
    when(dpsClient
        .getTaskErrorsReport(TopologyName.OAIPMH_HARVEST.getTopologyName(),
            TestObjectFactory.EXTERNAL_TASK_ID, taskErrorsInfo.getErrors().get(0).getErrorType()))
        .thenReturn(taskErrorsInfoWithIdentifiers1);
    when(dpsClient
        .getTaskErrorsReport(TopologyName.OAIPMH_HARVEST.getTopologyName(),
            TestObjectFactory.EXTERNAL_TASK_ID, taskErrorsInfo.getErrors().get(1).getErrorType()))
        .thenReturn(taskErrorsInfoWithIdentifiers2);

    TaskErrorsInfo externalTaskReport = orchestratorService
        .getExternalTaskReport(TopologyName.OAIPMH_HARVEST.getTopologyName(),
            TestObjectFactory.EXTERNAL_TASK_ID);

    Assert.assertEquals(2, externalTaskReport.getErrors().size());
    Assert.assertTrue(externalTaskReport.getErrors().get(0).getIdentifiers().size() != 0);
    Assert.assertTrue(externalTaskReport.getErrors().get(1).getIdentifiers().size() != 0);
  }
}
