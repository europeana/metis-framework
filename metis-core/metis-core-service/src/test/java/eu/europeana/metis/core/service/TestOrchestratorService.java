package eu.europeana.metis.core.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

  @BeforeClass
  public static void prepare() throws IOException {
    workflowExecutionDao = Mockito.mock(WorkflowExecutionDao.class);
    workflowDao = Mockito.mock(WorkflowDao.class);
    scheduledWorkflowDao = Mockito.mock(ScheduledWorkflowDao.class);
    datasetDao = Mockito.mock(DatasetDao.class);
    workflowExecutorManager = Mockito.mock(WorkflowExecutorManager.class);

    orchestratorService = new OrchestratorService(workflowDao, workflowExecutionDao,
        scheduledWorkflowDao, datasetDao, workflowExecutorManager);
  }

  @After
  public void cleanUp()
  {
    Mockito.reset(workflowExecutionDao);
    Mockito.reset(workflowDao);
    Mockito.reset(scheduledWorkflowDao);
    Mockito.reset(datasetDao);
    Mockito.reset(workflowExecutorManager);
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
    orchestratorService.getAllWorkflows(anyString(), anyString());
    InOrder inOrder = Mockito.inOrder(workflowDao);
    inOrder.verify(workflowDao, times(1)).getAllWorkflows(anyString(), anyString());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void getRunningUserWorkflowExecution() {
    orchestratorService.getRunningWorkflowExecution(anyString());
    InOrder inOrder = Mockito.inOrder(workflowExecutionDao);
    inOrder.verify(workflowExecutionDao, times(1))
        .getRunningWorkflowExecution(anyString());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void addUserWorkflowInQueueOfUserWorkflowExecutions()
      throws WorkflowExecutionAlreadyExistsException, NoDatasetFoundException, NoWorkflowFoundException {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(dataset.getDatasetName())).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName()))
        .thenReturn(workflow);
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetName())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService.addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetName(),
        workflow.getWorkflowOwner(), workflow.getWorkflowName(), 0);
  }

  @Test(expected = NoDatasetFoundException.class)
  public void addUserWorkflowInQueueOfUserWorkflowExecutions_NoDatasetFoundException()
      throws Exception {
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(null);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(TestObjectFactory.DATASETNAME,
            TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME, 0);
  }

  @Test(expected = NoWorkflowFoundException.class)
  public void addUserWorkflowInQueueOfUserWorkflowExecutions_NoUserWorkflowFoundException()
      throws Exception {

    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(null);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(TestObjectFactory.DATASETNAME,
            TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME, 0);
  }

  @Test(expected = WorkflowExecutionAlreadyExistsException.class)
  public void addUserWorkflowInQueueOfUserWorkflowExecutions_UserWorkflowExecutionAlreadyExistsException()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetName()))
        .thenReturn(new ObjectId().toString());
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(TestObjectFactory.DATASETNAME,
            TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME, 0);
  }

  @Test
  public void addUserWorkflowInQueueOfUserWorkflowExecutionsByUserWorkflow()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(dataset.getDatasetName())).thenReturn(dataset);
    when(workflowDao.exists(workflow)).thenReturn(null);
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetName())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(dataset.getDatasetName(), workflow, 0);
  }

  @Test(expected = NoDatasetFoundException.class)
  public void addUserWorkflowInQueueOfUserWorkflowExecutionsByUserWorkflow_NoDatasetFoundException()
      throws Exception {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(null);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(TestObjectFactory.DATASETNAME, workflow,
            0);
  }

  @Test(expected = WorkflowAlreadyExistsException.class)
  public void addUserWorkflowInQueueOfUserWorkflowExecutionsByUserWorkflow_UserWorkflowAlreadyExistsException()
      throws Exception {

    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(workflowDao.exists(workflow)).thenReturn(new ObjectId().toString());
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(TestObjectFactory.DATASETNAME,
            workflow, 0);
  }

  @Test(expected = WorkflowExecutionAlreadyExistsException.class)
  public void addUserWorkflowInQueueOfUserWorkflowExecutionsByUserWorkflow_UserWorkflowExecutionAlreadyExistsException()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(workflowDao.exists(workflow)).thenReturn(null);
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetName()))
        .thenReturn(new ObjectId().toString());
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(TestObjectFactory.DATASETNAME,
            workflow, 0);
  }

  @Test
  public void cancelUserWorkflowExecution() throws Exception {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    when(workflowExecutionDao.getRunningOrInQueueExecution(TestObjectFactory.DATASETNAME))
        .thenReturn(workflowExecution);
    doNothing().when(workflowExecutorManager)
        .cancelWorkflowExecution(workflowExecution);
    orchestratorService.cancelWorkflowExecution(TestObjectFactory.DATASETNAME);
  }

  @Test(expected = NoWorkflowExecutionFoundException.class)
  public void cancelUserWorkflowExecution_NoUserWorkflowExecutionFoundException()
      throws Exception {
    when(workflowExecutionDao.getRunningOrInQueueExecution(TestObjectFactory.DATASETNAME))
        .thenReturn(null);
    orchestratorService.cancelWorkflowExecution(TestObjectFactory.DATASETNAME);
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
    String objectId = new ObjectId().toString();
    orchestratorService.getAllWorkflowExecutions(TestObjectFactory.DATASETNAME,
        TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWOWNER, WorkflowStatus.RUNNING,
        objectId);
    verify(workflowExecutionDao, times(1))
        .getAllWorkflowExecutions(anyString(), anyString(), anyString(), any(
            WorkflowStatus.class), anyString());
    verifyNoMoreInteractions(workflowExecutionDao);
  }

  @Test
  public void getAllUserWorkflowExecutionsByWorkflowStatus() {
    String objectId = new ObjectId().toString();
    orchestratorService.getAllWorkflowExecutions(WorkflowStatus.RUNNING, objectId);
    verify(workflowExecutionDao, times(1))
        .getAllWorkflowExecutions(any(WorkflowStatus.class), anyString());
    verifyNoMoreInteractions(workflowExecutionDao);
  }

  @Test
  public void getScheduledUserWorkflowByDatasetName() {
    orchestratorService.getScheduledWorkflowByDatasetName(TestObjectFactory.DATASETNAME);
    verify(scheduledWorkflowDao, times(1)).getScheduledWorkflowByDatasetName(anyString());
    verifyNoMoreInteractions(scheduledWorkflowDao);
  }

  @Test
  public void scheduleUserWorkflow() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(null);
    when(scheduledWorkflowDao.create(scheduledWorkflow))
        .thenReturn(new ObjectId().toString());
    orchestratorService.scheduleWorkflow(scheduledWorkflow);
  }

  @Test(expected = NoDatasetFoundException.class)
  public void scheduleUserWorkflow_NoDatasetFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(null);
    orchestratorService.scheduleWorkflow(scheduledWorkflow);
  }

  @Test(expected = NoWorkflowFoundException.class)
  public void scheduleUserWorkflow_NoUserWorkflowFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
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
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
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
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
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
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
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
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(null);
    orchestratorService.scheduleWorkflow(scheduledWorkflow);
  }

  @Test
  public void getAllScheduledUserWorkflows() {
    orchestratorService
        .getAllScheduledWorkflows(ScheduleFrequence.ONCE, new ObjectId().toString());
    verify(scheduledWorkflowDao, times(1))
        .getAllScheduledWorkflows(any(ScheduleFrequence.class), anyString());
  }

  @Test
  public void getAllScheduledUserWorkflowsByDateRangeONCE() {
    orchestratorService
        .getAllScheduledWorkflowsByDateRangeONCE(LocalDateTime.now(), LocalDateTime.now(),
            new ObjectId().toString());
    verify(scheduledWorkflowDao, times(1))
        .getAllScheduledWorkflowsByDateRangeONCE(any(LocalDateTime.class),
            any(LocalDateTime.class), anyString());
  }

  @Test
  public void updateScheduledUserWorkflow() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();

    when(workflowDao
        .getWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
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
    when(scheduledWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
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
    when(scheduledWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
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
    when(scheduledWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
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
    when(scheduledWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(new ObjectId().toString());
    orchestratorService.updateScheduledWorkflow(scheduledWorkflow);
  }

  @Test
  public void deleteScheduledUserWorkflow() {
    orchestratorService
        .deleteScheduledWorkflow(TestObjectFactory.DATASETNAME);
    verify(scheduledWorkflowDao, times(1))
        .deleteScheduledWorkflow(anyString());
  }
}
