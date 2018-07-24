package eu.europeana.metis.core.execution;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Date;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.common.model.dps.TaskState;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutionProgress;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.exception.ExternalTaskException;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-17
 */
public class TestWorkflowExecutor {

  private static WorkflowExecutionDao workflowExecutionDao;
  private static DpsClient dpsClient;
  private static WorkflowExecutionMonitor workflowExecutionMonitor;
  private static PersistenceProvider persistenceProvider;
  private static WorkflowExecutionSettings workflowExecutionSettings;

  @BeforeClass
  public static void prepare() {
    workflowExecutionDao = Mockito.mock(WorkflowExecutionDao.class);
    dpsClient = Mockito.mock(DpsClient.class);
    workflowExecutionMonitor = Mockito.mock(WorkflowExecutionMonitor.class);
    persistenceProvider =
        new PersistenceProvider(null, null, workflowExecutionDao, null, dpsClient);
    workflowExecutionSettings = Mockito.mock(WorkflowExecutionSettings.class);
  }

  @After
  public void cleanUp() {
    Mockito.reset(workflowExecutionDao);
    Mockito.reset(workflowExecutionMonitor);
    Mockito.reset(dpsClient);
    Mockito.reset(workflowExecutionSettings);
  }

  @Before
  public void setConstants() {
    when(workflowExecutionSettings.getDpsMonitorCheckIntervalInSecs()).thenReturn(0);
  }

  @Test
  public void call() {
    WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    workflowExecution.setId(new ObjectId());

    when(workflowExecutionMonitor.claimExecution(workflowExecution.getId().toString()))
        .thenReturn(workflowExecution);
    doNothing().when(workflowExecutionDao).updateMonitorInformation(workflowExecution);
    when(workflowExecutionDao.isCancelling(workflowExecution.getId())).thenReturn(false);
    doNothing().when(workflowExecutionDao).updateWorkflowPlugins(workflowExecution);
    when(workflowExecutionDao.update(workflowExecution))
        .thenReturn(workflowExecution.getId().toString());

    WorkflowExecutor workflowExecutor = new WorkflowExecutor(workflowExecution.getId().toString(),
        persistenceProvider, workflowExecutionSettings, workflowExecutionMonitor);
    workflowExecutor.call();

    Assert.assertEquals(WorkflowStatus.FINISHED, workflowExecution.getWorkflowStatus());
    Assert.assertNotNull(workflowExecution.getUpdatedDate());
    Assert.assertNotNull(workflowExecution.getFinishedDate());
  }

  @Test
  public void callNonMockedFieldValue() throws Exception {
    ExecutionProgress currentlyProcessingExecutionProgress = new ExecutionProgress();
    currentlyProcessingExecutionProgress.setStatus(TaskState.CURRENTLY_PROCESSING);
    ExecutionProgress processedExecutionProgress = new ExecutionProgress();
    processedExecutionProgress.setStatus(TaskState.PROCESSED);

    OaipmhHarvestPlugin oaipmhHarvestPlugin = Mockito.mock(OaipmhHarvestPlugin.class);
    OaipmhHarvestPluginMetadata oaipmhHarvestPluginMetadata = new OaipmhHarvestPluginMetadata();
    oaipmhHarvestPluginMetadata.setMocked(false);
    oaipmhHarvestPlugin.setPluginMetadata(oaipmhHarvestPluginMetadata);
    ArrayList<AbstractMetisPlugin> abstractMetisPlugins = new ArrayList<>();
    abstractMetisPlugins.add(oaipmhHarvestPlugin);

    WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    workflowExecution.setId(new ObjectId());
    workflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
    workflowExecution.setMetisPlugins(abstractMetisPlugins);

    when(oaipmhHarvestPlugin.getPluginMetadata()).thenReturn(oaipmhHarvestPluginMetadata);
    when(oaipmhHarvestPlugin.monitor(dpsClient)).thenReturn(currentlyProcessingExecutionProgress)
        .thenReturn(processedExecutionProgress);

    when(workflowExecutionMonitor.claimExecution(workflowExecution.getId().toString()))
        .thenReturn(workflowExecution);
    doNothing().when(workflowExecutionDao).updateMonitorInformation(workflowExecution);
    when(workflowExecutionDao.isCancelling(workflowExecution.getId())).thenReturn(false);

    doNothing().when(workflowExecutionDao).updateWorkflowPlugins(workflowExecution);
    when(workflowExecutionDao.update(workflowExecution))
        .thenReturn(workflowExecution.getId().toString());

    WorkflowExecutor workflowExecutor = new WorkflowExecutor(workflowExecution.getId().toString(),
        persistenceProvider, workflowExecutionSettings, workflowExecutionMonitor);
    workflowExecutor.call();

    verify(workflowExecutionDao, times(2)).updateMonitorInformation(workflowExecution);
    verify(workflowExecutionDao, times(1)).update(workflowExecution);
  }

  @Test
  public void callNonMockedFieldValue_ExceptionWhenExecuteIsCalled() throws Exception {

    OaipmhHarvestPlugin oaipmhHarvestPlugin = Mockito.mock(OaipmhHarvestPlugin.class);
    OaipmhHarvestPluginMetadata oaipmhHarvestPluginMetadata = new OaipmhHarvestPluginMetadata();
    oaipmhHarvestPluginMetadata.setMocked(false);
    oaipmhHarvestPlugin.setPluginMetadata(oaipmhHarvestPluginMetadata);
    ArrayList<AbstractMetisPlugin> abstractMetisPlugins = new ArrayList<>();
    abstractMetisPlugins.add(oaipmhHarvestPlugin);

    WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    workflowExecution.setId(new ObjectId());
    workflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
    workflowExecution.setMetisPlugins(abstractMetisPlugins);

    doThrow(new ExternalTaskException("Some error")).when(oaipmhHarvestPlugin).execute(
        any(DpsClient.class), isNull(), isNull(), eq(workflowExecution.getEcloudDatasetId()));
    when(oaipmhHarvestPlugin.getPluginStatus()).thenReturn(PluginStatus.FAILED);

    when(oaipmhHarvestPlugin.getPluginMetadata()).thenReturn(oaipmhHarvestPluginMetadata);

    when(workflowExecutionMonitor.claimExecution(workflowExecution.getId().toString()))
        .thenReturn(workflowExecution);
    doNothing().when(workflowExecutionDao).updateMonitorInformation(workflowExecution);
    when(workflowExecutionDao.isCancelling(workflowExecution.getId())).thenReturn(false);

    doNothing().when(workflowExecutionDao).updateWorkflowPlugins(workflowExecution);
    when(workflowExecutionDao.update(workflowExecution))
        .thenReturn(workflowExecution.getId().toString());

    WorkflowExecutor workflowExecutor = new WorkflowExecutor(workflowExecution.getId().toString(),
        persistenceProvider, workflowExecutionSettings, workflowExecutionMonitor);
    workflowExecutor.call();

    verify(workflowExecutionDao, times(1)).update(workflowExecution);
  }

  @Test
  public void callNonMockedFieldValue_DROPPEDExeternalTaskButNotCancelled() throws Exception {
    ExecutionProgress currentlyProcessingExecutionProgress = new ExecutionProgress();
    currentlyProcessingExecutionProgress.setStatus(TaskState.CURRENTLY_PROCESSING);
    ExecutionProgress droppedExecutionProgress = new ExecutionProgress();
    droppedExecutionProgress.setStatus(TaskState.DROPPED);

    OaipmhHarvestPlugin oaipmhHarvestPlugin = Mockito.mock(OaipmhHarvestPlugin.class);
    OaipmhHarvestPluginMetadata oaipmhHarvestPluginMetadata = new OaipmhHarvestPluginMetadata();
    oaipmhHarvestPluginMetadata.setMocked(false);
    oaipmhHarvestPlugin.setPluginMetadata(oaipmhHarvestPluginMetadata);
    ArrayList<AbstractMetisPlugin> abstractMetisPlugins = new ArrayList<>();
    abstractMetisPlugins.add(oaipmhHarvestPlugin);

    WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    workflowExecution.setId(new ObjectId());
    workflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
    workflowExecution.setMetisPlugins(abstractMetisPlugins);

    when(oaipmhHarvestPlugin.getPluginMetadata()).thenReturn(oaipmhHarvestPluginMetadata);
    when(oaipmhHarvestPlugin.monitor(dpsClient)).thenReturn(currentlyProcessingExecutionProgress)
        .thenReturn(droppedExecutionProgress);

    when(workflowExecutionMonitor.claimExecution(workflowExecution.getId().toString()))
        .thenReturn(workflowExecution);
    doNothing().when(workflowExecutionDao).updateMonitorInformation(workflowExecution);
    when(workflowExecutionDao.isCancelling(workflowExecution.getId())).thenReturn(false);

    doNothing().when(workflowExecutionDao).updateWorkflowPlugins(workflowExecution);
    when(workflowExecutionDao.update(workflowExecution))
        .thenReturn(workflowExecution.getId().toString());

    WorkflowExecutor workflowExecutor = new WorkflowExecutor(workflowExecution.getId().toString(),
        persistenceProvider, workflowExecutionSettings, workflowExecutionMonitor);
    workflowExecutor.call();

    verify(workflowExecutionDao, times(2)).updateMonitorInformation(workflowExecution);
    verify(workflowExecutionDao, times(1)).update(workflowExecution);
  }

  @Test
  public void callNonMockedFieldValue_ConsecutiveMonitorFailures() throws Exception {

    OaipmhHarvestPlugin oaipmhHarvestPlugin = Mockito.mock(OaipmhHarvestPlugin.class);
    OaipmhHarvestPluginMetadata oaipmhHarvestPluginMetadata = new OaipmhHarvestPluginMetadata();
    oaipmhHarvestPluginMetadata.setMocked(false);
    oaipmhHarvestPlugin.setPluginMetadata(oaipmhHarvestPluginMetadata);
    ArrayList<AbstractMetisPlugin> abstractMetisPlugins = new ArrayList<>();
    abstractMetisPlugins.add(oaipmhHarvestPlugin);

    WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    workflowExecution.setId(new ObjectId());
    workflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
    workflowExecution.setMetisPlugins(abstractMetisPlugins);

    when(oaipmhHarvestPlugin.getPluginMetadata()).thenReturn(oaipmhHarvestPluginMetadata);
    when(oaipmhHarvestPlugin.monitor(dpsClient)).thenThrow(new ExternalTaskException("Some error"))
        .thenThrow(new ExternalTaskException("Some error"));

    when(workflowExecutionMonitor.claimExecution(workflowExecution.getId().toString()))
        .thenReturn(workflowExecution);
    doNothing().when(workflowExecutionDao).updateMonitorInformation(workflowExecution);
    when(workflowExecutionDao.isCancelling(workflowExecution.getId())).thenReturn(false);

    doNothing().when(workflowExecutionDao).updateWorkflowPlugins(workflowExecution);
    when(workflowExecutionDao.update(workflowExecution))
        .thenReturn(workflowExecution.getId().toString());

    WorkflowExecutor workflowExecutor = new WorkflowExecutor(workflowExecution.getId().toString(),
        persistenceProvider, workflowExecutionSettings, workflowExecutionMonitor);
    workflowExecutor.call();

    verify(workflowExecutionDao, times(1)).update(workflowExecution);
  }

  @Test
  public void callNonMockedFieldValueCancellingState() throws Exception {
    ExecutionProgress currentlyProcessingExecutionProgress = new ExecutionProgress();
    currentlyProcessingExecutionProgress.setStatus(TaskState.CURRENTLY_PROCESSING);
    ExecutionProgress processedExecutionProgress = new ExecutionProgress();
    processedExecutionProgress.setStatus(TaskState.PROCESSED);

    OaipmhHarvestPlugin oaipmhHarvestPlugin = Mockito.mock(OaipmhHarvestPlugin.class);
    OaipmhHarvestPluginMetadata oaipmhHarvestPluginMetadata = new OaipmhHarvestPluginMetadata();
    oaipmhHarvestPluginMetadata.setMocked(false);
    oaipmhHarvestPlugin.setPluginMetadata(oaipmhHarvestPluginMetadata);
    ArrayList<AbstractMetisPlugin> abstractMetisPlugins = new ArrayList<>();
    abstractMetisPlugins.add(oaipmhHarvestPlugin);

    WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    workflowExecution.setId(new ObjectId());
    workflowExecution.setWorkflowStatus(WorkflowStatus.INQUEUE);
    workflowExecution.setMetisPlugins(abstractMetisPlugins);

    when(oaipmhHarvestPlugin.getPluginMetadata()).thenReturn(oaipmhHarvestPluginMetadata);
    when(oaipmhHarvestPlugin.monitor(dpsClient)).thenReturn(currentlyProcessingExecutionProgress)
        .thenReturn(processedExecutionProgress);
    doNothing().when(oaipmhHarvestPlugin).cancel(dpsClient);

    when(workflowExecutionMonitor.claimExecution(workflowExecution.getId().toString()))
        .thenReturn(workflowExecution);
    doNothing().when(workflowExecutionDao).updateMonitorInformation(workflowExecution);
    when(workflowExecutionDao.isCancelling(workflowExecution.getId())).thenReturn(false)
        .thenReturn(true);

    doNothing().when(workflowExecutionDao).updateWorkflowPlugins(workflowExecution);
    when(workflowExecutionDao.update(workflowExecution))
        .thenReturn(workflowExecution.getId().toString());

    WorkflowExecutor workflowExecutor = new WorkflowExecutor(workflowExecution.getId().toString(),
        persistenceProvider, workflowExecutionSettings, workflowExecutionMonitor);
    workflowExecutor.call();

    verify(workflowExecutionDao, times(2)).updateMonitorInformation(workflowExecution);
    verify(workflowExecutionDao, times(1)).update(workflowExecution);
  }

  @Test
  public void callExecutionInRUNNINGState() {
    WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    workflowExecution.setId(new ObjectId());
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    workflowExecution.setStartedDate(new Date());
    AbstractMetisPlugin metisPlugin = workflowExecution.getMetisPlugins().get(0);
    metisPlugin.setPluginStatus(PluginStatus.FINISHED);

    when(workflowExecutionMonitor.claimExecution(workflowExecution.getId().toString()))
        .thenReturn(workflowExecution);
    doNothing().when(workflowExecutionDao).updateMonitorInformation(workflowExecution);
    when(workflowExecutionDao.isCancelling(workflowExecution.getId())).thenReturn(false);
    doNothing().when(workflowExecutionDao).updateWorkflowPlugins(workflowExecution);
    when(workflowExecutionDao.update(workflowExecution))
        .thenReturn(workflowExecution.getId().toString());

    WorkflowExecutor workflowExecutor = new WorkflowExecutor(workflowExecution.getId().toString(),
        persistenceProvider, workflowExecutionSettings, workflowExecutionMonitor);
    workflowExecutor.call();

    Assert.assertEquals(WorkflowStatus.FINISHED, workflowExecution.getWorkflowStatus());
    Assert.assertNotNull(workflowExecution.getStartedDate());
    Assert.assertNotNull(workflowExecution.getUpdatedDate());
    Assert.assertNotNull(workflowExecution.getFinishedDate());
    Assert.assertNull(workflowExecution.getMetisPlugins().get(0).getFinishedDate());
  }

  @Test
  public void callExecutionThatMayNotBeClaimed() {
    when(workflowExecutionMonitor.claimExecution(any())).thenReturn(null);

    WorkflowExecutor workflowExecutor = new WorkflowExecutor("testId", persistenceProvider,
        workflowExecutionSettings, workflowExecutionMonitor);
    workflowExecutor.call();

    verify(workflowExecutionMonitor, times(1)).claimExecution(any());
    verifyNoMoreInteractions(workflowExecutionDao);
  }

  @Test
  public void callCancellingStateINQUEUE() {
    WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    workflowExecution.setId(new ObjectId());

    when(workflowExecutionMonitor.claimExecution(workflowExecution.getId().toString()))
        .thenReturn(workflowExecution);
    when(workflowExecutionDao.isCancelling(workflowExecution.getId())).thenReturn(false)
        .thenReturn(true);

    WorkflowExecutor workflowExecutor = new WorkflowExecutor(workflowExecution.getId().toString(),
        persistenceProvider, workflowExecutionSettings, workflowExecutionMonitor);
    workflowExecutor.call();

    Assert.assertEquals(WorkflowStatus.CANCELLED, workflowExecution.getWorkflowStatus());
  }

  @Test
  public void callCancellingStateRUNNING() {
    WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    workflowExecution.setId(new ObjectId());
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    workflowExecution.setStartedDate(new Date());

    when(workflowExecutionMonitor.claimExecution(workflowExecution.getId().toString()))
        .thenReturn(workflowExecution);
    when(workflowExecutionDao.isCancelling(workflowExecution.getId())).thenReturn(false)
        .thenReturn(true);

    WorkflowExecutor workflowExecutor = new WorkflowExecutor(workflowExecution.getId().toString(),
        persistenceProvider, workflowExecutionSettings, workflowExecutionMonitor);
    workflowExecutor.call();

    Assert.assertEquals(WorkflowStatus.CANCELLED, workflowExecution.getWorkflowStatus());
  }
}
