package eu.europeana.metis.core.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.time.LocalDateTime;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
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

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-05
 */
public class TestScheduleWorkflowService {

  private static ScheduledWorkflowDao scheduledWorkflowDao;
  private static WorkflowDao workflowDao;
  private static DatasetDao datasetDao;
  private static ScheduleWorkflowService scheduleWorkflowService;
  private static Authorizer authorizer;

  @BeforeClass
  public static void prepare() throws IOException {
    workflowDao = mock(WorkflowDao.class);
    scheduledWorkflowDao = mock(ScheduledWorkflowDao.class);
    datasetDao = mock(DatasetDao.class);
    authorizer = mock(Authorizer.class);

    scheduleWorkflowService = new ScheduleWorkflowService(scheduledWorkflowDao, workflowDao,
        datasetDao, authorizer);
  }

  @After
  public void cleanUp() {
    reset(workflowDao);
    reset(scheduledWorkflowDao);
    reset(datasetDao);
    reset(authorizer);
  }

  @Test
  public void getScheduledWorkflowByDatasetId()
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
  public void scheduleWorkflow() throws Exception {
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

  @Test(expected = NoDatasetFoundException.class)
  public void scheduleWorkflow_NoDatasetFoundException() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(datasetId)).thenReturn(null);
    scheduleWorkflowService.scheduleWorkflow(metisUser, scheduledWorkflow);
  }

  @Test(expected = NoWorkflowFoundException.class)
  public void scheduleWorkflow_NoWorkflowFoundException() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(datasetDao.getDatasetByDatasetId(datasetId)).thenReturn(dataset);
    when(workflowDao.getWorkflow(datasetId)).thenReturn(null);
    scheduleWorkflowService.scheduleWorkflow(metisUser, scheduledWorkflow);
  }

  @Test(expected = ScheduledWorkflowAlreadyExistsException.class)
  public void scheduleWorkflow_ScheduledWorkflowAlreadyExistsException() throws Exception {
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
    scheduleWorkflowService.scheduleWorkflow(metisUser, scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void scheduleUserWorkflow_BadContentException_nullPointerDate() throws Exception {
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
    scheduleWorkflowService.scheduleWorkflow(metisUser, scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void scheduleWorkflow_BadContentException_NULLScheduleFrequence() throws Exception {
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
    scheduleWorkflowService.scheduleWorkflow(metisUser, scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void scheduleWorkflow_BadContentException_nullScheduleFrequence() throws Exception {
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
    scheduleWorkflowService.scheduleWorkflow(metisUser, scheduledWorkflow);
  }

  @Test
  public void getAllScheduledWorkflows() throws UserUnauthorizedException {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    scheduleWorkflowService.getAllScheduledWorkflows(metisUser, ScheduleFrequence.ONCE, 0);
    verify(scheduledWorkflowDao, times(1)).getAllScheduledWorkflows(any(ScheduleFrequence.class),
        anyInt());
    verify(authorizer, times(1)).authorizeReadAllDatasets(metisUser);
    verifyNoMoreInteractions(authorizer);
  }

  @Test
  public void getAllScheduledUserWorkflowsByDateRangeONCE() {
    scheduleWorkflowService
        .getAllScheduledWorkflowsByDateRangeONCE(LocalDateTime.now(), LocalDateTime.now(),
            0);
    verify(scheduledWorkflowDao, times(1))
        .getAllScheduledWorkflowsByDateRangeONCE(any(LocalDateTime.class),
            any(LocalDateTime.class), anyInt());
  }

  @Test
  public void updateScheduledWorkflow() throws Exception {
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

  @Test(expected = NoWorkflowFoundException.class)
  public void updateScheduledUserWorkflow_NoUserWorkflowFoundException() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    when(workflowDao.getWorkflow(datasetId)).thenReturn(null);
    scheduleWorkflowService.updateScheduledWorkflow(metisUser, scheduledWorkflow);
  }

  @Test(expected = NoScheduledWorkflowFoundException.class)
  public void updateScheduledWorkflow_NoScheduledWorkflowFoundException() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    Workflow workflow = TestObjectFactory.createWorkflowObject();

    when(workflowDao.getWorkflow(datasetId)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(datasetId))
        .thenReturn(null);
    scheduleWorkflowService.updateScheduledWorkflow(metisUser, scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void updateScheduledWorkflow_BadContentException_nullPointerDate() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflow.setPointerDate(null);
    Workflow workflow = TestObjectFactory.createWorkflowObject();

    when(workflowDao.getWorkflow(datasetId)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(datasetId))
        .thenReturn(new ObjectId().toString());
    scheduleWorkflowService.updateScheduledWorkflow(metisUser, scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void updateScheduledWorkflow_BadContentException_NULLScheduleFrequence()
      throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflow.setScheduleFrequence(ScheduleFrequence.NULL);
    Workflow workflow = TestObjectFactory.createWorkflowObject();

    when(workflowDao.getWorkflow(datasetId)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(datasetId))
        .thenReturn(new ObjectId().toString());
    scheduleWorkflowService.updateScheduledWorkflow(metisUser, scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void updateScheduledWorkflow_BadContentException_nullScheduleFrequence()
      throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflow.setScheduleFrequence(null);
    Workflow workflow = TestObjectFactory.createWorkflowObject();

    when(workflowDao.getWorkflow(datasetId)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(datasetId))
        .thenReturn(new ObjectId().toString());
    scheduleWorkflowService.updateScheduledWorkflow(metisUser, scheduledWorkflow);
  }

  @Test
  public void deleteScheduledWorkflow() throws UserUnauthorizedException, NoDatasetFoundException {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    scheduleWorkflowService.deleteScheduledWorkflow(metisUser, datasetId);
    verify(scheduledWorkflowDao, times(1)).deleteScheduledWorkflow(anyString());
    verify(authorizer, times(1)).authorizeWriteExistingDatasetById(metisUser, datasetId);
    verifyNoMoreInteractions(authorizer);
  }

  @Test
  public void getScheduledWorkflowsPerRequest() {
    scheduleWorkflowService.getScheduledWorkflowsPerRequest();
    verify(scheduledWorkflowDao, times(1)).getScheduledWorkflowPerRequest();
  }

}
