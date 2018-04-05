package eu.europeana.metis.core.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
import java.io.IOException;
import java.time.LocalDateTime;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-05
 */
public class TestScheduleWorkflowService {

  private static ScheduledWorkflowDao scheduledWorkflowDao;
  private static WorkflowDao workflowDao;
  private static DatasetDao datasetDao;
  private static ScheduleWorkflowService scheduleWorkflowService;

  @BeforeClass
  public static void prepare() throws IOException {
    workflowDao = Mockito.mock(WorkflowDao.class);
    scheduledWorkflowDao = Mockito.mock(ScheduledWorkflowDao.class);
    datasetDao = Mockito.mock(DatasetDao.class);

    scheduleWorkflowService = new ScheduleWorkflowService(scheduledWorkflowDao, workflowDao,
        datasetDao);
  }

  @After
  public void cleanUp() {
    Mockito.reset(workflowDao);
    Mockito.reset(scheduledWorkflowDao);
    Mockito.reset(datasetDao);
  }

  @Test
  public void getScheduledWorkflowByDatasetName() {
    scheduleWorkflowService.getScheduledWorkflowByDatasetId(TestObjectFactory.DATASETID);
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
    when(workflowDao.getWorkflow(TestObjectFactory.DATASETID)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(null);
    when(scheduledWorkflowDao.create(scheduledWorkflow))
        .thenReturn(new ObjectId().toString());
    scheduleWorkflowService.scheduleWorkflow(scheduledWorkflow);
  }

  @Test(expected = NoDatasetFoundException.class)
  public void scheduleWorkflow_NoDatasetFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(null);
    scheduleWorkflowService.scheduleWorkflow(scheduledWorkflow);
  }

  @Test(expected = NoWorkflowFoundException.class)
  public void scheduleWorkflow_NoWorkflowFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao.getWorkflow(TestObjectFactory.DATASETID)).thenReturn(null);
    scheduleWorkflowService.scheduleWorkflow(scheduledWorkflow);
  }

  @Test(expected = ScheduledWorkflowAlreadyExistsException.class)
  public void scheduleWorkflow_ScheduledWorkflowAlreadyExistsException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao.getWorkflow(TestObjectFactory.DATASETID)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(new ObjectId().toString());
    scheduleWorkflowService.scheduleWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void scheduleUserWorkflow_BadContentException_nullPointerDate() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflow.setPointerDate(null);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao.getWorkflow(TestObjectFactory.DATASETID)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(null);
    scheduleWorkflowService.scheduleWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void scheduleWorkflow_BadContentException_NULLScheduleFrequence() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflow.setScheduleFrequence(ScheduleFrequence.NULL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao.getWorkflow(TestObjectFactory.DATASETID)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(null);
    scheduleWorkflowService.scheduleWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void scheduleWorkflow_BadContentException_nullScheduleFrequence() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflow.setScheduleFrequence(null);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(datasetDao.getDatasetByDatasetId(TestObjectFactory.DATASETID)).thenReturn(dataset);
    when(workflowDao.getWorkflow(TestObjectFactory.DATASETID)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(null);
    scheduleWorkflowService.scheduleWorkflow(scheduledWorkflow);
  }

  @Test
  public void getAllScheduledWorkflows() {
    scheduleWorkflowService
        .getAllScheduledWorkflows(ScheduleFrequence.ONCE, 0);
    verify(scheduledWorkflowDao, times(1))
        .getAllScheduledWorkflows(any(ScheduleFrequence.class), anyInt());
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
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    Workflow workflow = TestObjectFactory.createWorkflowObject();

    when(workflowDao.getWorkflow(TestObjectFactory.DATASETID)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(new ObjectId().toString());
    when(scheduledWorkflowDao.update(scheduledWorkflow))
        .thenReturn(new ObjectId().toString());
    scheduleWorkflowService.updateScheduledWorkflow(scheduledWorkflow);
  }

  @Test(expected = NoWorkflowFoundException.class)
  public void updateScheduledUserWorkflow_NoUserWorkflowFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    when(workflowDao.getWorkflow(TestObjectFactory.DATASETID)).thenReturn(null);
    scheduleWorkflowService.updateScheduledWorkflow(scheduledWorkflow);
  }

  @Test(expected = NoScheduledWorkflowFoundException.class)
  public void updateScheduledWorkflow_NoScheduledWorkflowFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    Workflow workflow = TestObjectFactory.createWorkflowObject();

    when(workflowDao.getWorkflow(TestObjectFactory.DATASETID)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(null);
    scheduleWorkflowService.updateScheduledWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void updateScheduledWorkflow_BadContentException_nullPointerDate() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflow.setPointerDate(null);
    Workflow workflow = TestObjectFactory.createWorkflowObject();

    when(workflowDao.getWorkflow(TestObjectFactory.DATASETID)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(new ObjectId().toString());
    scheduleWorkflowService.updateScheduledWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void updateScheduledWorkflow_BadContentException_NULLScheduleFrequence()
      throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflow.setScheduleFrequence(ScheduleFrequence.NULL);
    Workflow workflow = TestObjectFactory.createWorkflowObject();

    when(workflowDao.getWorkflow(TestObjectFactory.DATASETID)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(new ObjectId().toString());
    scheduleWorkflowService.updateScheduledWorkflow(scheduledWorkflow);
  }

  @Test(expected = BadContentException.class)
  public void updateScheduledWorkflow_BadContentException_nullScheduleFrequence()
      throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduledWorkflow.setScheduleFrequence(null);
    Workflow workflow = TestObjectFactory.createWorkflowObject();

    when(workflowDao.getWorkflow(TestObjectFactory.DATASETID)).thenReturn(workflow);
    when(scheduledWorkflowDao.existsForDatasetId(TestObjectFactory.DATASETID))
        .thenReturn(new ObjectId().toString());
    scheduleWorkflowService.updateScheduledWorkflow(scheduledWorkflow);
  }

  @Test
  public void deleteScheduledWorkflow() {
    scheduleWorkflowService
        .deleteScheduledWorkflow(TestObjectFactory.DATASETID);
    verify(scheduledWorkflowDao, times(1))
        .deleteScheduledWorkflow(anyInt());
  }

  @Test
  public void getScheduledWorkflowsPerRequest() {
    scheduleWorkflowService.getScheduledWorkflowsPerRequest();
    verify(scheduledWorkflowDao, times(1)).getScheduledWorkflowPerRequest();
  }

}
