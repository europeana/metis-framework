package eu.europeana.metis.core.test.service;

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
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledUserWorkflow;
import eu.europeana.metis.core.workflow.UserWorkflow;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-06
 */
public class TestOrchestratorService {

  private UserWorkflowExecutionDao userWorkflowExecutionDao;
  private UserWorkflowDao userWorkflowDao;
  private ScheduledUserWorkflowDao scheduledUserWorkflowDao;
  private DatasetDao datasetDao;
  private UserWorkflowExecutorManager userWorkflowExecutorManager;
  private OrchestratorService orchestratorService;

  @Before
  public void prepare() {
    userWorkflowExecutionDao = Mockito.mock(UserWorkflowExecutionDao.class);
    userWorkflowDao = Mockito.mock(UserWorkflowDao.class);
    scheduledUserWorkflowDao = Mockito.mock(ScheduledUserWorkflowDao.class);
    datasetDao = Mockito.mock(DatasetDao.class);
    userWorkflowExecutorManager = Mockito.mock(UserWorkflowExecutorManager.class);

    orchestratorService = new OrchestratorService(userWorkflowDao, userWorkflowExecutionDao,
        scheduledUserWorkflowDao, datasetDao, userWorkflowExecutorManager);
  }

  @Test
  public void testCreateUserWorkflow() throws UserWorkflowAlreadyExistsException {
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    orchestratorService.createUserWorkflow(userWorkflow);

    InOrder inOrder = Mockito.inOrder(userWorkflowDao);
    inOrder.verify(userWorkflowDao, times(1)).exists(userWorkflow);
    inOrder.verify(userWorkflowDao, times(1)).create(userWorkflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test(expected = UserWorkflowAlreadyExistsException.class)
  public void testCreateUserWorkflow_AlreadyExists() throws UserWorkflowAlreadyExistsException {
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    when(userWorkflowDao.exists(userWorkflow)).thenReturn(new ObjectId().toString());

    orchestratorService.createUserWorkflow(userWorkflow);

    InOrder inOrder = Mockito.inOrder(userWorkflowDao);
    inOrder.verify(userWorkflowDao, times(1)).exists(userWorkflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testUpdateUserWorkflow() throws NoUserWorkflowFoundException {
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    when(userWorkflowDao.exists(userWorkflow)).thenReturn(new ObjectId().toString());
    orchestratorService.updateUserWorkflow(userWorkflow);
    InOrder inOrder = Mockito.inOrder(userWorkflowDao);
    inOrder.verify(userWorkflowDao, times(1)).exists(userWorkflow);
    inOrder.verify(userWorkflowDao, times(1)).update(userWorkflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test(expected = NoUserWorkflowFoundException.class)
  public void testUpdateUserWorkflow_NoUserWorkflowFound() throws NoUserWorkflowFoundException {
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    orchestratorService.updateUserWorkflow(userWorkflow);
    InOrder inOrder = Mockito.inOrder(userWorkflowDao);
    inOrder.verify(userWorkflowDao, times(1)).exists(userWorkflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testDeleteUserWorkflow() {
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    orchestratorService
        .deleteUserWorkflow(userWorkflow.getWorkflowOwner(), userWorkflow.getWorkflowName());

    ArgumentCaptor<String> workflowOwnerArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> workflowNameArgumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(userWorkflowDao, times(1)).deleteUserWorkflow(workflowOwnerArgumentCaptor.capture(),
        workflowNameArgumentCaptor.capture());
    Assert.assertEquals(userWorkflow.getWorkflowOwner(), workflowOwnerArgumentCaptor.getValue());
    Assert.assertEquals(userWorkflow.getWorkflowName(), workflowNameArgumentCaptor.getValue());
  }

  @Test
  public void testGetUserWorkflow() {
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    when(userWorkflowDao
        .getUserWorkflow(userWorkflow.getWorkflowOwner(), userWorkflow.getWorkflowName()))
        .thenReturn(userWorkflow);

    UserWorkflow retrievedUserWorkflow = orchestratorService
        .getUserWorkflow(userWorkflow.getWorkflowOwner(), userWorkflow.getWorkflowName());
    Assert.assertSame(userWorkflow, retrievedUserWorkflow);
  }

  @Test
  public void testGetAllUserWorkflows() {
    orchestratorService.getAllUserWorkflows(anyString(), anyString());
    InOrder inOrder = Mockito.inOrder(userWorkflowDao);
    inOrder.verify(userWorkflowDao, times(1)).getAllUserWorkflows(anyString(), anyString());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testGetRunningUserWorkflowExecution() {
    orchestratorService.getRunningUserWorkflowExecution(anyString(), anyString(), anyString());
    InOrder inOrder = Mockito.inOrder(userWorkflowExecutionDao);
    inOrder.verify(userWorkflowExecutionDao, times(1))
        .getRunningUserWorkflowExecution(anyString(), anyString(), anyString());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void testAddUserWorkflowInQueueOfUserWorkflowExecutions()
      throws UserWorkflowExecutionAlreadyExistsException, NoDatasetFoundException, NoUserWorkflowFoundException {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(dataset.getDatasetName())).thenReturn(dataset);
    when(userWorkflowDao
        .getUserWorkflow(userWorkflow.getWorkflowOwner(), userWorkflow.getWorkflowName()))
        .thenReturn(userWorkflow);
    when(userWorkflowExecutionDao.existsAndNotCompleted(dataset.getDatasetName())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(userWorkflowExecutionDao.create(any(UserWorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(userWorkflowExecutorManager).addUserWorkflowExecutionToQueue(objectId, 0);
    orchestratorService.addUserWorkflowInQueueOfUserWorkflowExecutions(dataset.getDatasetName(),
        userWorkflow.getWorkflowOwner(), userWorkflow.getWorkflowName(), 0);
  }

  @Test(expected = NoDatasetFoundException.class)
  public void testAddUserWorkflowInQueueOfUserWorkflowExecutions_NoDatasetFoundException()
      throws UserWorkflowExecutionAlreadyExistsException, NoDatasetFoundException, NoUserWorkflowFoundException {
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(null);
    orchestratorService
        .addUserWorkflowInQueueOfUserWorkflowExecutions(TestObjectFactory.DATASETNAME,
            TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME, 0);
  }

  @Test(expected = NoUserWorkflowFoundException.class)
  public void testAddUserWorkflowInQueueOfUserWorkflowExecutions_NoUserWorkflowFoundException()
      throws UserWorkflowExecutionAlreadyExistsException, NoDatasetFoundException, NoUserWorkflowFoundException {

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
  public void testAddUserWorkflowInQueueOfUserWorkflowExecutions_UserWorkflowExecutionAlreadyExistsException()
      throws UserWorkflowExecutionAlreadyExistsException, NoDatasetFoundException, NoUserWorkflowFoundException {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(userWorkflow);
    when(userWorkflowExecutionDao.existsAndNotCompleted(dataset.getDatasetName()))
        .thenReturn(new ObjectId().toString());
    orchestratorService
        .addUserWorkflowInQueueOfUserWorkflowExecutions(TestObjectFactory.DATASETNAME,
            TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME, 0);
  }

  @Test
  public void testAddUserWorkflowInQueueOfUserWorkflowExecutionsByUserWorkflow()
      throws UserWorkflowExecutionAlreadyExistsException, NoDatasetFoundException, UserWorkflowAlreadyExistsException {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(dataset.getDatasetName())).thenReturn(dataset);
    when(userWorkflowDao.exists(userWorkflow)).thenReturn(null);
    when(userWorkflowExecutionDao.existsAndNotCompleted(dataset.getDatasetName())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(userWorkflowExecutionDao.create(any(UserWorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(userWorkflowExecutorManager).addUserWorkflowExecutionToQueue(objectId, 0);
    orchestratorService
        .addUserWorkflowInQueueOfUserWorkflowExecutions(dataset.getDatasetName(), userWorkflow, 0);
  }

  @Test(expected = NoDatasetFoundException.class)
  public void testAddUserWorkflowInQueueOfUserWorkflowExecutionsByUserWorkflow_NoDatasetFoundException()
      throws UserWorkflowExecutionAlreadyExistsException, NoDatasetFoundException, UserWorkflowAlreadyExistsException {
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(null);
    orchestratorService
        .addUserWorkflowInQueueOfUserWorkflowExecutions(TestObjectFactory.DATASETNAME, userWorkflow,
            0);
  }

  @Test(expected = UserWorkflowAlreadyExistsException.class)
  public void testAddUserWorkflowInQueueOfUserWorkflowExecutionsByUserWorkflow_UserWorkflowAlreadyExistsException()
      throws UserWorkflowExecutionAlreadyExistsException, NoDatasetFoundException, UserWorkflowAlreadyExistsException {

    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(userWorkflowDao.exists(userWorkflow)).thenReturn(new ObjectId().toString());
    orchestratorService
        .addUserWorkflowInQueueOfUserWorkflowExecutions(TestObjectFactory.DATASETNAME,
            userWorkflow, 0);
  }

  @Test(expected = UserWorkflowExecutionAlreadyExistsException.class)
  public void testAddUserWorkflowInQueueOfUserWorkflowExecutionsByUserWorkflow_UserWorkflowExecutionAlreadyExistsException()
      throws UserWorkflowExecutionAlreadyExistsException, NoDatasetFoundException, UserWorkflowAlreadyExistsException {
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(userWorkflowDao.exists(userWorkflow)).thenReturn(null);
    when(userWorkflowExecutionDao.existsAndNotCompleted(dataset.getDatasetName()))
        .thenReturn(new ObjectId().toString());
    orchestratorService
        .addUserWorkflowInQueueOfUserWorkflowExecutions(TestObjectFactory.DATASETNAME,
            userWorkflow, 0);
  }

  @Test
  public void testCancelUserWorkflowExecution() throws NoUserWorkflowExecutionFoundException {
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    when(userWorkflowExecutionDao.getRunningOrInQueueExecution(TestObjectFactory.DATASETNAME))
        .thenReturn(userWorkflowExecution);
    doNothing().when(userWorkflowExecutorManager)
        .cancelUserWorkflowExecution(userWorkflowExecution);
    orchestratorService.cancelUserWorkflowExecution(TestObjectFactory.DATASETNAME);
  }

  @Test(expected = NoUserWorkflowExecutionFoundException.class)
  public void testCancelUserWorkflowExecution_NoUserWorkflowExecutionFoundException()
      throws NoUserWorkflowExecutionFoundException {
    when(userWorkflowExecutionDao.getRunningOrInQueueExecution(TestObjectFactory.DATASETNAME))
        .thenReturn(null);
    orchestratorService.cancelUserWorkflowExecution(TestObjectFactory.DATASETNAME);
    verifyNoMoreInteractions(userWorkflowExecutorManager);
  }

  @Test
  public void testRemoveActiveUserWorkflowExecutionsFromList() {
    orchestratorService.removeActiveUserWorkflowExecutionsFromList(new ArrayList<>());
    verify(userWorkflowExecutorManager, times(1)).initiateConsumer();
    verify(userWorkflowExecutorManager, times(1)).getMonitorCheckIntervalInSecs();
    verifyNoMoreInteractions(userWorkflowExecutorManager);
    verify(userWorkflowExecutionDao, times(1)).removeActiveExecutionsFromList(anyList(), anyInt());
    verifyNoMoreInteractions(userWorkflowExecutionDao);
  }

  @Test
  public void testAddUserWorkflowExecutionToQueue() {
    String objectId = new ObjectId().toString();
    orchestratorService.addUserWorkflowExecutionToQueue(objectId, 0);
    verify(userWorkflowExecutorManager, times(1)).initiateConsumer();
    verify(userWorkflowExecutorManager, times(1)).addUserWorkflowExecutionToQueue(objectId, 0);
    verifyNoMoreInteractions(userWorkflowExecutorManager);
  }

  @Test
  public void testGetUserWorkflowExecutionsPerRequest()
  {
    orchestratorService.getUserWorkflowExecutionsPerRequest();
    verify(userWorkflowExecutionDao, times(1)).getUserWorkflowExecutionsPerRequest();
  }

  @Test
  public void testGetScheduledUserWorkflowsPerRequest()
  {
    orchestratorService.getScheduledUserWorkflowsPerRequest();
    verify(scheduledUserWorkflowDao, times(1)).getScheduledUserWorkflowPerRequest();
  }

  @Test
  public void testGetUserWorkflowsPerRequest()
  {
    orchestratorService.getUserWorkflowsPerRequest();
    verify(userWorkflowDao, times(1)).getUserWorkflowsPerRequest();
  }

  @Test
  public void testGetAllUserWorkflowExecutions() {
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
  public void testGetAllUserWorkflowExecutionsByWorkflowStatus() {
    String objectId = new ObjectId().toString();
    orchestratorService.getAllUserWorkflowExecutions(WorkflowStatus.RUNNING, objectId);
    verify(userWorkflowExecutionDao, times(1))
        .getAllUserWorkflowExecutions(any(WorkflowStatus.class), anyString());
    verifyNoMoreInteractions(userWorkflowExecutionDao);
  }

  @Test
  public void testGetScheduledUserWorkflowByDatasetName() {
    orchestratorService.getScheduledUserWorkflowByDatasetName(TestObjectFactory.DATASETNAME);
    verify(scheduledUserWorkflowDao, times(1)).getScheduledUserWorkflowByDatasetName(anyString());
    verifyNoMoreInteractions(scheduledUserWorkflowDao);
  }

  @Test
  public void testScheduleUserWorkflow()
      throws BadContentException, ScheduledUserWorkflowAlreadyExistsException, NoUserWorkflowFoundException, NoDatasetFoundException {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(userWorkflow);
    when(scheduledUserWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(null);
    when(scheduledUserWorkflowDao.create(scheduledUserWorkflow))
        .thenReturn(new ObjectId().toString());
    orchestratorService.scheduleUserWorkflow(scheduledUserWorkflow);
  }

  @Test(expected = NoDatasetFoundException.class)
  public void testScheduleUserWorkflow_NoDatasetFoundException()
      throws BadContentException, ScheduledUserWorkflowAlreadyExistsException, NoUserWorkflowFoundException, NoDatasetFoundException {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(null);
    orchestratorService.scheduleUserWorkflow(scheduledUserWorkflow);
  }

  @Test(expected = NoUserWorkflowFoundException.class)
  public void testScheduleUserWorkflow_NoUserWorkflowFoundException()
      throws BadContentException, ScheduledUserWorkflowAlreadyExistsException, NoUserWorkflowFoundException, NoDatasetFoundException {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(null);
    orchestratorService.scheduleUserWorkflow(scheduledUserWorkflow);
  }

  @Test(expected = ScheduledUserWorkflowAlreadyExistsException.class)
  public void testScheduleUserWorkflow_ScheduledUserWorkflowAlreadyExistsException()
      throws BadContentException, ScheduledUserWorkflowAlreadyExistsException, NoUserWorkflowFoundException, NoDatasetFoundException {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(userWorkflow);
    when(scheduledUserWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(new ObjectId().toString());
    orchestratorService.scheduleUserWorkflow(scheduledUserWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void testScheduleUserWorkflow_BadContentException_nullPointerDate()
      throws BadContentException, ScheduledUserWorkflowAlreadyExistsException, NoUserWorkflowFoundException, NoDatasetFoundException {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledUserWorkflow.setPointerDate(null);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(userWorkflow);
    when(scheduledUserWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(null);
    orchestratorService.scheduleUserWorkflow(scheduledUserWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void testScheduleUserWorkflow_BadContentException_nullScheduleFrequence()
      throws BadContentException, ScheduledUserWorkflowAlreadyExistsException, NoUserWorkflowFoundException, NoDatasetFoundException {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledUserWorkflow.setScheduleFrequence(ScheduleFrequence.NULL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(userWorkflow);
    when(scheduledUserWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(null);
    orchestratorService.scheduleUserWorkflow(scheduledUserWorkflow);
  }

  @Test
  public void testGetAllScheduledUserWorkflows() {
    orchestratorService
        .getAllScheduledUserWorkflows(ScheduleFrequence.ONCE, new ObjectId().toString());
    verify(scheduledUserWorkflowDao, times(1))
        .getAllScheduledUserWorkflows(any(ScheduleFrequence.class), anyString());
  }

  @Test
  public void testGetAllScheduledUserWorkflowsByDateRangeONCE() {
    orchestratorService
        .getAllScheduledUserWorkflowsByDateRangeONCE(LocalDateTime.now(), LocalDateTime.now(),
            new ObjectId().toString());
    verify(scheduledUserWorkflowDao, times(1))
        .getAllScheduledUserWorkflowsByDateRangeONCE(any(LocalDateTime.class),
            any(LocalDateTime.class), anyString());
  }

  @Test
  public void testUpdateScheduledUserWorkflow()
      throws BadContentException, NoScheduledUserWorkflowFoundException, NoUserWorkflowFoundException {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();

    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(userWorkflow);
    when(scheduledUserWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(new ObjectId().toString());
    when(scheduledUserWorkflowDao.update(scheduledUserWorkflow))
        .thenReturn(new ObjectId().toString());
    orchestratorService.updateScheduledUserWorkflow(scheduledUserWorkflow);
  }

  @Test(expected = NoUserWorkflowFoundException.class)
  public void testUpdateScheduledUserWorkflow_NoUserWorkflowFoundException()
      throws BadContentException, NoScheduledUserWorkflowFoundException, NoUserWorkflowFoundException {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(null);
    orchestratorService.updateScheduledUserWorkflow(scheduledUserWorkflow);
  }

  @Test(expected = NoScheduledUserWorkflowFoundException.class)
  public void testUpdateScheduledUserWorkflow_NoScheduledUserWorkflowFoundException()
      throws BadContentException, NoScheduledUserWorkflowFoundException, NoUserWorkflowFoundException {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();

    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(userWorkflow);
    when(scheduledUserWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(null);
    orchestratorService.updateScheduledUserWorkflow(scheduledUserWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void testUpdateScheduledUserWorkflow_BadContentException_nullPointerDate()
      throws BadContentException, NoScheduledUserWorkflowFoundException, NoUserWorkflowFoundException {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledUserWorkflow.setPointerDate(null);
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();

    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(userWorkflow);
    when(scheduledUserWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(new ObjectId().toString());
    orchestratorService.updateScheduledUserWorkflow(scheduledUserWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void testUpdateScheduledUserWorkflow_BadContentException_nullScheduleFrequence()
      throws BadContentException, NoScheduledUserWorkflowFoundException, NoUserWorkflowFoundException {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledUserWorkflow.setScheduleFrequence(ScheduleFrequence.NULL);
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();

    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(userWorkflow);
    when(scheduledUserWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(new ObjectId().toString());
    orchestratorService.updateScheduledUserWorkflow(scheduledUserWorkflow);
  }

  @Test
  public void testDeleteScheduledUserWorkflow() {
    orchestratorService
        .deleteScheduledUserWorkflow(TestObjectFactory.DATASETNAME, TestObjectFactory.WORKFLOWOWNER,
            TestObjectFactory.WORKFLOWNAME);
    verify(scheduledUserWorkflowDao, times(1))
        .deleteScheduledUserWorkflow(anyString(), anyString(), anyString());
  }


}
