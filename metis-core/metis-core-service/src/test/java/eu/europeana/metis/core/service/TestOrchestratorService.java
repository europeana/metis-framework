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
import eu.europeana.metis.core.dao.ScheduledUserWorkflowDao;
import eu.europeana.metis.core.dao.UserWorkflowDao;
import eu.europeana.metis.core.dao.UserWorkflowExecutionDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoScheduledUserWorkflowFoundException;
import eu.europeana.metis.core.exceptions.NoUserWorkflowExecutionFoundException;
import eu.europeana.metis.core.exceptions.NoUserWorkflowFoundException;
import eu.europeana.metis.core.exceptions.ScheduledUserWorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.UserWorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.UserWorkflowExecutionAlreadyExistsException;
import eu.europeana.metis.core.execution.UserWorkflowExecutorManager;
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

  private static UserWorkflowExecutionDao userWorkflowExecutionDao;
  private static UserWorkflowDao userWorkflowDao;
  private static ScheduledUserWorkflowDao scheduledUserWorkflowDao;
  private static DatasetDao datasetDao;
  private static UserWorkflowExecutorManager userWorkflowExecutorManager;
  private static OrchestratorService orchestratorService;

  @BeforeClass
  public static void prepare() throws IOException {
    userWorkflowExecutionDao = Mockito.mock(UserWorkflowExecutionDao.class);
    userWorkflowDao = Mockito.mock(UserWorkflowDao.class);
    scheduledUserWorkflowDao = Mockito.mock(ScheduledUserWorkflowDao.class);
    datasetDao = Mockito.mock(DatasetDao.class);
    userWorkflowExecutorManager = Mockito.mock(UserWorkflowExecutorManager.class);

    orchestratorService = new OrchestratorService(userWorkflowDao, userWorkflowExecutionDao,
        scheduledUserWorkflowDao, datasetDao, userWorkflowExecutorManager);
  }

  @After
  public void cleanUp()
  {
    Mockito.reset(userWorkflowExecutionDao);
    Mockito.reset(userWorkflowDao);
    Mockito.reset(scheduledUserWorkflowDao);
    Mockito.reset(datasetDao);
    Mockito.reset(userWorkflowExecutorManager);
  }

  @Test
  public void createUserWorkflow() throws Exception {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    orchestratorService.createUserWorkflow(workflow);

    InOrder inOrder = Mockito.inOrder(userWorkflowDao);
    inOrder.verify(userWorkflowDao, times(1)).exists(workflow);
    inOrder.verify(userWorkflowDao, times(1)).create(workflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test(expected = UserWorkflowAlreadyExistsException.class)
  public void createUserWorkflow_AlreadyExists() throws Exception {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(userWorkflowDao.exists(workflow)).thenReturn(new ObjectId().toString());

    orchestratorService.createUserWorkflow(workflow);

    InOrder inOrder = Mockito.inOrder(userWorkflowDao);
    inOrder.verify(userWorkflowDao, times(1)).exists(workflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void updateUserWorkflow() throws Exception {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(userWorkflowDao.exists(workflow)).thenReturn(new ObjectId().toString());
    orchestratorService.updateUserWorkflow(workflow);
    InOrder inOrder = Mockito.inOrder(userWorkflowDao);
    inOrder.verify(userWorkflowDao, times(1)).exists(workflow);
    inOrder.verify(userWorkflowDao, times(1)).update(workflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test(expected = NoUserWorkflowFoundException.class)
  public void updateUserWorkflow_NoUserWorkflowFound() throws Exception {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    orchestratorService.updateUserWorkflow(workflow);
    InOrder inOrder = Mockito.inOrder(userWorkflowDao);
    inOrder.verify(userWorkflowDao, times(1)).exists(workflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void deleteUserWorkflow() {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    orchestratorService
        .deleteUserWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName());

    ArgumentCaptor<String> workflowOwnerArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> workflowNameArgumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(userWorkflowDao, times(1)).deleteUserWorkflow(workflowOwnerArgumentCaptor.capture(),
        workflowNameArgumentCaptor.capture());
    Assert.assertEquals(workflow.getWorkflowOwner(), workflowOwnerArgumentCaptor.getValue());
    Assert.assertEquals(workflow.getWorkflowName(), workflowNameArgumentCaptor.getValue());
  }

  @Test
  public void getUserWorkflow() {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(userWorkflowDao
        .getUserWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName()))
        .thenReturn(workflow);

    Workflow retrievedWorkflow = orchestratorService
        .getUserWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName());
    Assert.assertSame(workflow, retrievedWorkflow);
  }

  @Test
  public void getAllUserWorkflows() {
    orchestratorService.getAllUserWorkflows(anyString(), anyString());
    InOrder inOrder = Mockito.inOrder(userWorkflowDao);
    inOrder.verify(userWorkflowDao, times(1)).getAllUserWorkflows(anyString(), anyString());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void getRunningUserWorkflowExecution() {
    orchestratorService.getRunningUserWorkflowExecution(anyString());
    InOrder inOrder = Mockito.inOrder(userWorkflowExecutionDao);
    inOrder.verify(userWorkflowExecutionDao, times(1))
        .getRunningUserWorkflowExecution(anyString());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void addUserWorkflowInQueueOfUserWorkflowExecutions()
      throws UserWorkflowExecutionAlreadyExistsException, NoDatasetFoundException, NoUserWorkflowFoundException {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(dataset.getDatasetName())).thenReturn(dataset);
    when(userWorkflowDao
        .getUserWorkflow(workflow.getWorkflowOwner(), workflow.getWorkflowName()))
        .thenReturn(workflow);
    when(userWorkflowExecutionDao.existsAndNotCompleted(dataset.getDatasetName())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(userWorkflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(userWorkflowExecutorManager).addUserWorkflowExecutionToQueue(objectId, 0);
    orchestratorService.addUserWorkflowInQueueOfUserWorkflowExecutions(dataset.getDatasetName(),
        workflow.getWorkflowOwner(), workflow.getWorkflowName(), 0);
  }

  @Test(expected = NoDatasetFoundException.class)
  public void addUserWorkflowInQueueOfUserWorkflowExecutions_NoDatasetFoundException()
      throws Exception {
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(null);
    orchestratorService
        .addUserWorkflowInQueueOfUserWorkflowExecutions(TestObjectFactory.DATASETNAME,
            TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME, 0);
  }

  @Test(expected = NoUserWorkflowFoundException.class)
  public void addUserWorkflowInQueueOfUserWorkflowExecutions_NoUserWorkflowFoundException()
      throws Exception {

    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(null);
    orchestratorService
        .addUserWorkflowInQueueOfUserWorkflowExecutions(TestObjectFactory.DATASETNAME,
            TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME, 0);
  }

  @Test(expected = UserWorkflowExecutionAlreadyExistsException.class)
  public void addUserWorkflowInQueueOfUserWorkflowExecutions_UserWorkflowExecutionAlreadyExistsException()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(userWorkflowExecutionDao.existsAndNotCompleted(dataset.getDatasetName()))
        .thenReturn(new ObjectId().toString());
    orchestratorService
        .addUserWorkflowInQueueOfUserWorkflowExecutions(TestObjectFactory.DATASETNAME,
            TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME, 0);
  }

  @Test
  public void addUserWorkflowInQueueOfUserWorkflowExecutionsByUserWorkflow()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(dataset.getDatasetName())).thenReturn(dataset);
    when(userWorkflowDao.exists(workflow)).thenReturn(null);
    when(userWorkflowExecutionDao.existsAndNotCompleted(dataset.getDatasetName())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(userWorkflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(userWorkflowExecutorManager).addUserWorkflowExecutionToQueue(objectId, 0);
    orchestratorService
        .addUserWorkflowInQueueOfUserWorkflowExecutions(dataset.getDatasetName(), workflow, 0);
  }

  @Test(expected = NoDatasetFoundException.class)
  public void addUserWorkflowInQueueOfUserWorkflowExecutionsByUserWorkflow_NoDatasetFoundException()
      throws Exception {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(null);
    orchestratorService
        .addUserWorkflowInQueueOfUserWorkflowExecutions(TestObjectFactory.DATASETNAME, workflow,
            0);
  }

  @Test(expected = UserWorkflowAlreadyExistsException.class)
  public void addUserWorkflowInQueueOfUserWorkflowExecutionsByUserWorkflow_UserWorkflowAlreadyExistsException()
      throws Exception {

    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(userWorkflowDao.exists(workflow)).thenReturn(new ObjectId().toString());
    orchestratorService
        .addUserWorkflowInQueueOfUserWorkflowExecutions(TestObjectFactory.DATASETNAME,
            workflow, 0);
  }

  @Test(expected = UserWorkflowExecutionAlreadyExistsException.class)
  public void addUserWorkflowInQueueOfUserWorkflowExecutionsByUserWorkflow_UserWorkflowExecutionAlreadyExistsException()
      throws Exception {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(userWorkflowDao.exists(workflow)).thenReturn(null);
    when(userWorkflowExecutionDao.existsAndNotCompleted(dataset.getDatasetName()))
        .thenReturn(new ObjectId().toString());
    orchestratorService
        .addUserWorkflowInQueueOfUserWorkflowExecutions(TestObjectFactory.DATASETNAME,
            workflow, 0);
  }

  @Test
  public void cancelUserWorkflowExecution() throws Exception {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    when(userWorkflowExecutionDao.getRunningOrInQueueExecution(TestObjectFactory.DATASETNAME))
        .thenReturn(workflowExecution);
    doNothing().when(userWorkflowExecutorManager)
        .cancelUserWorkflowExecution(workflowExecution);
    orchestratorService.cancelUserWorkflowExecution(TestObjectFactory.DATASETNAME);
  }

  @Test(expected = NoUserWorkflowExecutionFoundException.class)
  public void cancelUserWorkflowExecution_NoUserWorkflowExecutionFoundException()
      throws Exception {
    when(userWorkflowExecutionDao.getRunningOrInQueueExecution(TestObjectFactory.DATASETNAME))
        .thenReturn(null);
    orchestratorService.cancelUserWorkflowExecution(TestObjectFactory.DATASETNAME);
    verifyNoMoreInteractions(userWorkflowExecutorManager);
  }

  @Test
  public void removeActiveUserWorkflowExecutionsFromList() {
    orchestratorService.removeActiveUserWorkflowExecutionsFromList(new ArrayList<>());
    verify(userWorkflowExecutorManager, times(1)).getMonitorCheckIntervalInSecs();
    verifyNoMoreInteractions(userWorkflowExecutorManager);
    verify(userWorkflowExecutionDao, times(1)).removeActiveExecutionsFromList(anyList(), anyInt());
    verifyNoMoreInteractions(userWorkflowExecutionDao);
  }

  @Test
  public void addUserWorkflowExecutionToQueue() {
    String objectId = new ObjectId().toString();
    orchestratorService.addUserWorkflowExecutionToQueue(objectId, 0);
    verify(userWorkflowExecutorManager, times(1)).addUserWorkflowExecutionToQueue(objectId, 0);
    verifyNoMoreInteractions(userWorkflowExecutorManager);
  }

  @Test
  public void getUserWorkflowExecutionsPerRequest() {
    orchestratorService.getUserWorkflowExecutionsPerRequest();
    verify(userWorkflowExecutionDao, times(1)).getUserWorkflowExecutionsPerRequest();
  }

  @Test
  public void getScheduledUserWorkflowsPerRequest() {
    orchestratorService.getScheduledUserWorkflowsPerRequest();
    verify(scheduledUserWorkflowDao, times(1)).getScheduledUserWorkflowPerRequest();
  }

  @Test
  public void getUserWorkflowsPerRequest() {
    orchestratorService.getUserWorkflowsPerRequest();
    verify(userWorkflowDao, times(1)).getUserWorkflowsPerRequest();
  }

  @Test
  public void getAllUserWorkflowExecutions() {
    String objectId = new ObjectId().toString();
    orchestratorService.getAllUserWorkflowExecutions(TestObjectFactory.DATASETNAME,
        TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWOWNER, WorkflowStatus.RUNNING,
        objectId);
    verify(userWorkflowExecutionDao, times(1))
        .getAllUserWorkflowExecutions(anyString(), anyString(), anyString(), any(
            WorkflowStatus.class), anyString());
    verifyNoMoreInteractions(userWorkflowExecutionDao);
  }

  @Test
  public void getAllUserWorkflowExecutionsByWorkflowStatus() {
    String objectId = new ObjectId().toString();
    orchestratorService.getAllUserWorkflowExecutions(WorkflowStatus.RUNNING, objectId);
    verify(userWorkflowExecutionDao, times(1))
        .getAllUserWorkflowExecutions(any(WorkflowStatus.class), anyString());
    verifyNoMoreInteractions(userWorkflowExecutionDao);
  }

  @Test
  public void getScheduledUserWorkflowByDatasetName() {
    orchestratorService.getScheduledUserWorkflowByDatasetName(TestObjectFactory.DATASETNAME);
    verify(scheduledUserWorkflowDao, times(1)).getScheduledUserWorkflowByDatasetName(anyString());
    verifyNoMoreInteractions(scheduledUserWorkflowDao);
  }

  @Test
  public void scheduleUserWorkflow() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledUserWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(null);
    when(scheduledUserWorkflowDao.create(scheduledWorkflow))
        .thenReturn(new ObjectId().toString());
    orchestratorService.scheduleUserWorkflow(scheduledWorkflow);
  }

  @Test(expected = NoDatasetFoundException.class)
  public void scheduleUserWorkflow_NoDatasetFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(null);
    orchestratorService.scheduleUserWorkflow(scheduledWorkflow);
  }

  @Test(expected = NoUserWorkflowFoundException.class)
  public void scheduleUserWorkflow_NoUserWorkflowFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(null);
    orchestratorService.scheduleUserWorkflow(scheduledWorkflow);
  }

  @Test(expected = ScheduledUserWorkflowAlreadyExistsException.class)
  public void scheduleUserWorkflow_ScheduledUserWorkflowAlreadyExistsException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledUserWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(new ObjectId().toString());
    orchestratorService.scheduleUserWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void scheduleUserWorkflow_BadContentException_nullPointerDate() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledWorkflow.setPointerDate(null);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledUserWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(null);
    orchestratorService.scheduleUserWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void scheduleUserWorkflow_BadContentException_NULLScheduleFrequence() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledWorkflow.setScheduleFrequence(ScheduleFrequence.NULL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledUserWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(null);
    orchestratorService.scheduleUserWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void scheduleUserWorkflow_BadContentException_nullScheduleFrequence() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledWorkflow.setScheduleFrequence(null);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledUserWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(null);
    orchestratorService.scheduleUserWorkflow(scheduledWorkflow);
  }

  @Test
  public void getAllScheduledUserWorkflows() {
    orchestratorService
        .getAllScheduledUserWorkflows(ScheduleFrequence.ONCE, new ObjectId().toString());
    verify(scheduledUserWorkflowDao, times(1))
        .getAllScheduledUserWorkflows(any(ScheduleFrequence.class), anyString());
  }

  @Test
  public void getAllScheduledUserWorkflowsByDateRangeONCE() {
    orchestratorService
        .getAllScheduledUserWorkflowsByDateRangeONCE(LocalDateTime.now(), LocalDateTime.now(),
            new ObjectId().toString());
    verify(scheduledUserWorkflowDao, times(1))
        .getAllScheduledUserWorkflowsByDateRangeONCE(any(LocalDateTime.class),
            any(LocalDateTime.class), anyString());
  }

  @Test
  public void updateScheduledUserWorkflow() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();

    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledUserWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(new ObjectId().toString());
    when(scheduledUserWorkflowDao.update(scheduledWorkflow))
        .thenReturn(new ObjectId().toString());
    orchestratorService.updateScheduledUserWorkflow(scheduledWorkflow);
  }

  @Test(expected = NoUserWorkflowFoundException.class)
  public void updateScheduledUserWorkflow_NoUserWorkflowFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(null);
    orchestratorService.updateScheduledUserWorkflow(scheduledWorkflow);
  }

  @Test(expected = NoScheduledUserWorkflowFoundException.class)
  public void updateScheduledUserWorkflow_NoScheduledUserWorkflowFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();

    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledUserWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(null);
    orchestratorService.updateScheduledUserWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void updateScheduledUserWorkflow_BadContentException_nullPointerDate() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledWorkflow.setPointerDate(null);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();

    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledUserWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(new ObjectId().toString());
    orchestratorService.updateScheduledUserWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void updateScheduledUserWorkflow_BadContentException_NULLScheduleFrequence()
      throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledWorkflow.setScheduleFrequence(ScheduleFrequence.NULL);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();

    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledUserWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(new ObjectId().toString());
    orchestratorService.updateScheduledUserWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void updateScheduledUserWorkflow_BadContentException_nullScheduleFrequence()
      throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledWorkflow.setScheduleFrequence(null);
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();

    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(workflow);
    when(scheduledUserWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(new ObjectId().toString());
    orchestratorService.updateScheduledUserWorkflow(scheduledWorkflow);
  }

  @Test
  public void deleteScheduledUserWorkflow() {
    orchestratorService
        .deleteScheduledUserWorkflow(TestObjectFactory.DATASETNAME);
    verify(scheduledUserWorkflowDao, times(1))
        .deleteScheduledUserWorkflow(anyString());
  }
}
