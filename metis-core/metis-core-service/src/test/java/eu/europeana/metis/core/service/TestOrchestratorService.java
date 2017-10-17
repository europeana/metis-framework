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
  public static void prepare() {
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
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    orchestratorService.createUserWorkflow(userWorkflow);

    InOrder inOrder = Mockito.inOrder(userWorkflowDao);
    inOrder.verify(userWorkflowDao, times(1)).exists(userWorkflow);
    inOrder.verify(userWorkflowDao, times(1)).create(userWorkflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test(expected = UserWorkflowAlreadyExistsException.class)
  public void createUserWorkflow_AlreadyExists() throws Exception {
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    when(userWorkflowDao.exists(userWorkflow)).thenReturn(new ObjectId().toString());

    orchestratorService.createUserWorkflow(userWorkflow);

    InOrder inOrder = Mockito.inOrder(userWorkflowDao);
    inOrder.verify(userWorkflowDao, times(1)).exists(userWorkflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void updateUserWorkflow() throws Exception {
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    when(userWorkflowDao.exists(userWorkflow)).thenReturn(new ObjectId().toString());
    orchestratorService.updateUserWorkflow(userWorkflow);
    InOrder inOrder = Mockito.inOrder(userWorkflowDao);
    inOrder.verify(userWorkflowDao, times(1)).exists(userWorkflow);
    inOrder.verify(userWorkflowDao, times(1)).update(userWorkflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test(expected = NoUserWorkflowFoundException.class)
  public void updateUserWorkflow_NoUserWorkflowFound() throws Exception {
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    orchestratorService.updateUserWorkflow(userWorkflow);
    InOrder inOrder = Mockito.inOrder(userWorkflowDao);
    inOrder.verify(userWorkflowDao, times(1)).exists(userWorkflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void deleteUserWorkflow() {
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
  public void getUserWorkflow() {
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    when(userWorkflowDao
        .getUserWorkflow(userWorkflow.getWorkflowOwner(), userWorkflow.getWorkflowName()))
        .thenReturn(userWorkflow);

    UserWorkflow retrievedUserWorkflow = orchestratorService
        .getUserWorkflow(userWorkflow.getWorkflowOwner(), userWorkflow.getWorkflowName());
    Assert.assertSame(userWorkflow, retrievedUserWorkflow);
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
  public void addUserWorkflowInQueueOfUserWorkflowExecutionsByUserWorkflow()
      throws Exception {
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
  public void addUserWorkflowInQueueOfUserWorkflowExecutionsByUserWorkflow_NoDatasetFoundException()
      throws Exception {
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(null);
    orchestratorService
        .addUserWorkflowInQueueOfUserWorkflowExecutions(TestObjectFactory.DATASETNAME, userWorkflow,
            0);
  }

  @Test(expected = UserWorkflowAlreadyExistsException.class)
  public void addUserWorkflowInQueueOfUserWorkflowExecutionsByUserWorkflow_UserWorkflowAlreadyExistsException()
      throws Exception {

    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(dataset);
    when(userWorkflowDao.exists(userWorkflow)).thenReturn(new ObjectId().toString());
    orchestratorService
        .addUserWorkflowInQueueOfUserWorkflowExecutions(TestObjectFactory.DATASETNAME,
            userWorkflow, 0);
  }

  @Test(expected = UserWorkflowExecutionAlreadyExistsException.class)
  public void addUserWorkflowInQueueOfUserWorkflowExecutionsByUserWorkflow_UserWorkflowExecutionAlreadyExistsException()
      throws Exception {
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
  public void cancelUserWorkflowExecution() throws Exception {
    UserWorkflowExecution userWorkflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    when(userWorkflowExecutionDao.getRunningOrInQueueExecution(TestObjectFactory.DATASETNAME))
        .thenReturn(userWorkflowExecution);
    doNothing().when(userWorkflowExecutorManager)
        .cancelUserWorkflowExecution(userWorkflowExecution);
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
  public void scheduleUserWorkflow_NoDatasetFoundException() throws Exception {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    when(datasetDao.getDatasetByDatasetName(TestObjectFactory.DATASETNAME)).thenReturn(null);
    orchestratorService.scheduleUserWorkflow(scheduledUserWorkflow);
  }

  @Test(expected = NoUserWorkflowFoundException.class)
  public void scheduleUserWorkflow_NoUserWorkflowFoundException() throws Exception {
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
  public void scheduleUserWorkflow_ScheduledUserWorkflowAlreadyExistsException() throws Exception {
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
  public void scheduleUserWorkflow_BadContentException_nullPointerDate() throws Exception {
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
  public void scheduleUserWorkflow_BadContentException_NULLScheduleFrequence() throws Exception {
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

  @Test(expected = BadContentException.class)
  public void scheduleUserWorkflow_BadContentException_nullScheduleFrequence() throws Exception {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledUserWorkflow.setScheduleFrequence(null);
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
  public void updateScheduledUserWorkflow_NoUserWorkflowFoundException() throws Exception {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(null);
    orchestratorService.updateScheduledUserWorkflow(scheduledUserWorkflow);
  }

  @Test(expected = NoScheduledUserWorkflowFoundException.class)
  public void updateScheduledUserWorkflow_NoScheduledUserWorkflowFoundException() throws Exception {
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
  public void updateScheduledUserWorkflow_BadContentException_nullPointerDate() throws Exception {
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
  public void updateScheduledUserWorkflow_BadContentException_NULLScheduleFrequence()
      throws Exception {
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

  @Test(expected = BadContentException.class)
  public void updateScheduledUserWorkflow_BadContentException_nullScheduleFrequence()
      throws Exception {
    ScheduledUserWorkflow scheduledUserWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    scheduledUserWorkflow.setScheduleFrequence(null);
    UserWorkflow userWorkflow = TestObjectFactory.createUserWorkflowObject();

    when(userWorkflowDao
        .getUserWorkflow(TestObjectFactory.WORKFLOWOWNER, TestObjectFactory.WORKFLOWNAME))
        .thenReturn(userWorkflow);
    when(scheduledUserWorkflowDao.existsForDatasetName(TestObjectFactory.DATASETNAME))
        .thenReturn(new ObjectId().toString());
    orchestratorService.updateScheduledUserWorkflow(scheduledUserWorkflow);
  }

  @Test
  public void deleteScheduledUserWorkflow() {
    orchestratorService
        .deleteScheduledUserWorkflow(TestObjectFactory.DATASETNAME);
    verify(scheduledUserWorkflowDao, times(1))
        .deleteScheduledUserWorkflow(anyString());
  }
}
