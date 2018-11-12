package eu.europeana.metis.core.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.ScheduledWorkflowDao;
import eu.europeana.metis.core.dao.WorkflowDao;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoScheduledWorkflowFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowFoundException;
import eu.europeana.metis.core.exceptions.ScheduledWorkflowAlreadyExistsException;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import java.time.LocalDateTime;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-05
 */
class TestScheduleWorkflowService {

  private static ScheduledWorkflowDao scheduledWorkflowDao;
  private static WorkflowDao workflowDao;
  private static DatasetDao datasetDao;
  private static ScheduleWorkflowService scheduleWorkflowService;
  private static Authorizer authorizer;

  @BeforeAll
  static void prepare() {
    workflowDao = mock(WorkflowDao.class);
    scheduledWorkflowDao = mock(ScheduledWorkflowDao.class);
    datasetDao = mock(DatasetDao.class);
    authorizer = mock(Authorizer.class);

    scheduleWorkflowService = new ScheduleWorkflowService(scheduledWorkflowDao, workflowDao,
        datasetDao, authorizer);
  }

  @AfterEach
  void cleanUp() {
    reset(workflowDao);
    reset(scheduledWorkflowDao);
    reset(datasetDao);
    reset(authorizer);
  }

  @Test
  void getScheduledWorkflowByDatasetId()
      throws UserUnauthorizedException, NoDatasetFoundException {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    scheduleWorkflowService.getScheduledWorkflowByDatasetId(metisUser, datasetId);
    verify(scheduledWorkflowDao, times(1)).getScheduledWorkflowByDatasetId(anyString());
    verifyNoMoreInteractions(scheduledWorkflowDao);
    verify(authorizer, times(1)).authorizeReadExistingDatasetById(metisUser, datasetId);
    verifyNoMoreInteractions(authorizer);
  }

  @Test
  void scheduleWorkflow() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(datasetId)).thenReturn(dataset);
    when(workflowDao.getWorkflow(datasetId)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(datasetId))
        .thenReturn(null);
    when(scheduledWorkflowDao.create(scheduledWorkflow))
        .thenReturn(new ObjectId().toString());
    scheduleWorkflowService.scheduleWorkflow(metisUser, scheduledWorkflow);
    verify(authorizer, times(1)).authorizeWriteExistingDatasetById(metisUser, datasetId);
    verifyNoMoreInteractions(authorizer);
  }

  @Test
  void scheduleWorkflow_NoDatasetFoundException() {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(datasetId)).thenReturn(null);
    assertThrows(NoDatasetFoundException.class, () -> scheduleWorkflowService.scheduleWorkflow(metisUser, scheduledWorkflow));
  }

  @Test
  void scheduleWorkflow_NoWorkflowFoundException() {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(datasetDao.getDatasetByDatasetId(datasetId)).thenReturn(dataset);
    when(workflowDao.getWorkflow(datasetId)).thenReturn(null);
    assertThrows(NoWorkflowFoundException.class, () -> scheduleWorkflowService.scheduleWorkflow(metisUser, scheduledWorkflow));
  }

  @Test
  void scheduleWorkflow_ScheduledWorkflowAlreadyExistsException() {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(datasetId)).thenReturn(dataset);
    when(workflowDao.getWorkflow(datasetId)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(datasetId))
        .thenReturn(new ObjectId().toString());
    assertThrows(ScheduledWorkflowAlreadyExistsException.class, () -> scheduleWorkflowService.scheduleWorkflow(metisUser, scheduledWorkflow));
  }

  @Test
  void scheduleUserWorkflow_BadContentException_nullPointerDate() {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflow.setPointerDate(null);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(datasetId)).thenReturn(dataset);
    when(workflowDao.getWorkflow(datasetId)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(datasetId))
        .thenReturn(null);
    assertThrows(BadContentException.class, () -> scheduleWorkflowService.scheduleWorkflow(metisUser, scheduledWorkflow));
  }

  @Test
  void scheduleWorkflow_BadContentException_NULLScheduleFrequence() {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflow.setScheduleFrequence(ScheduleFrequence.NULL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(datasetId)).thenReturn(dataset);
    when(workflowDao.getWorkflow(datasetId)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(datasetId))
        .thenReturn(null);
    assertThrows(BadContentException.class, () -> scheduleWorkflowService.scheduleWorkflow(metisUser, scheduledWorkflow));
  }

  @Test
  void scheduleWorkflow_BadContentException_nullScheduleFrequence() {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflow.setScheduleFrequence(null);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(datasetId)).thenReturn(dataset);
    when(workflowDao.getWorkflow(datasetId)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(datasetId))
        .thenReturn(null);
    assertThrows(BadContentException.class, () -> scheduleWorkflowService.scheduleWorkflow(metisUser, scheduledWorkflow));
  }

  @Test
  void getAllScheduledWorkflows() throws UserUnauthorizedException {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    scheduleWorkflowService.getAllScheduledWorkflows(metisUser, ScheduleFrequence.ONCE, 0);
    verify(scheduledWorkflowDao, times(1)).getAllScheduledWorkflows(any(ScheduleFrequence.class),
        anyInt());
    verify(authorizer, times(1)).authorizeReadAllDatasets(metisUser);
    verifyNoMoreInteractions(authorizer);
  }

  @Test
  void getAllScheduledUserWorkflowsByDateRangeONCE() {
    scheduleWorkflowService
        .getAllScheduledWorkflowsByDateRangeONCE(LocalDateTime.now(), LocalDateTime.now(),
            0);
    verify(scheduledWorkflowDao, times(1))
        .getAllScheduledWorkflowsByDateRangeONCE(any(LocalDateTime.class),
            any(LocalDateTime.class), anyInt());
  }

  @Test
  void updateScheduledWorkflow() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    Workflow workflow = TestObjectFactory.createWorkflowObject();

    when(workflowDao.getWorkflow(datasetId)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(datasetId))
        .thenReturn(new ObjectId().toString());
    when(scheduledWorkflowDao.update(scheduledWorkflow))
        .thenReturn(new ObjectId().toString());
    scheduleWorkflowService.updateScheduledWorkflow(metisUser, scheduledWorkflow);
    verify(authorizer, times(1)).authorizeWriteExistingDatasetById(metisUser, datasetId);
    verifyNoMoreInteractions(authorizer);
  }

  @Test
  void updateScheduledUserWorkflow_NoUserWorkflowFoundException() {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    when(workflowDao.getWorkflow(datasetId)).thenReturn(null);
    assertThrows(NoWorkflowFoundException.class, () -> scheduleWorkflowService.updateScheduledWorkflow(metisUser, scheduledWorkflow));
  }

  @Test
  void updateScheduledWorkflow_NoScheduledWorkflowFoundException() {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    Workflow workflow = TestObjectFactory.createWorkflowObject();

    when(workflowDao.getWorkflow(datasetId)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(datasetId))
        .thenReturn(null);
    assertThrows(NoScheduledWorkflowFoundException.class, () -> scheduleWorkflowService.updateScheduledWorkflow(metisUser, scheduledWorkflow));
  }

  @Test
  void updateScheduledWorkflow_BadContentException_nullPointerDate() {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflow.setPointerDate(null);
    Workflow workflow = TestObjectFactory.createWorkflowObject();

    when(workflowDao.getWorkflow(datasetId)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(datasetId))
        .thenReturn(new ObjectId().toString());
    assertThrows(BadContentException.class, () -> scheduleWorkflowService.updateScheduledWorkflow(metisUser, scheduledWorkflow));
  }

  @Test
  void updateScheduledWorkflow_BadContentException_NULLScheduleFrequence() {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflow.setScheduleFrequence(ScheduleFrequence.NULL);
    Workflow workflow = TestObjectFactory.createWorkflowObject();

    when(workflowDao.getWorkflow(datasetId)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(datasetId))
        .thenReturn(new ObjectId().toString());
    assertThrows(BadContentException.class, () -> scheduleWorkflowService.updateScheduledWorkflow(metisUser, scheduledWorkflow));
  }

  @Test
  void updateScheduledWorkflow_BadContentException_nullScheduleFrequence() {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflow.setScheduleFrequence(null);
    Workflow workflow = TestObjectFactory.createWorkflowObject();

    when(workflowDao.getWorkflow(datasetId)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(datasetId))
        .thenReturn(new ObjectId().toString());
    assertThrows(BadContentException.class, () -> scheduleWorkflowService.updateScheduledWorkflow(metisUser, scheduledWorkflow));
  }

  @Test
  void deleteScheduledWorkflow() throws UserUnauthorizedException, NoDatasetFoundException {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    scheduleWorkflowService.deleteScheduledWorkflow(metisUser, datasetId);
    verify(scheduledWorkflowDao, times(1)).deleteScheduledWorkflow(anyString());
    verify(authorizer, times(1)).authorizeWriteExistingDatasetById(metisUser, datasetId);
    verifyNoMoreInteractions(authorizer);
  }

  @Test
  void getScheduledWorkflowsPerRequest() {
    scheduleWorkflowService.getScheduledWorkflowsPerRequest();
    verify(scheduledWorkflowDao, times(1)).getScheduledWorkflowPerRequest();
  }

}
