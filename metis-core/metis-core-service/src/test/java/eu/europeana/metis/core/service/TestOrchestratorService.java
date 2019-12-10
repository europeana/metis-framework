package eu.europeana.metis.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import eu.europeana.metis.authentication.user.AccountRole;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.common.DaoFieldNames;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.DatasetXsltDao;
import eu.europeana.metis.core.dao.WorkflowDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao.ExecutionDatasetPair;
import eu.europeana.metis.core.dao.WorkflowExecutionDao.ExecutionIdAndStartedDatePair;
import eu.europeana.metis.core.dao.WorkflowExecutionDao.PluginWithExecutionId;
import eu.europeana.metis.core.dao.WorkflowExecutionDao.ResultList;
import eu.europeana.metis.core.dao.WorkflowUtils;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.dataset.DatasetExecutionInformation;
import eu.europeana.metis.core.dataset.DatasetXslt;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowFoundException;
import eu.europeana.metis.core.exceptions.PluginExecutionNotAllowed;
import eu.europeana.metis.core.exceptions.WorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.WorkflowExecutionAlreadyExistsException;
import eu.europeana.metis.core.execution.WorkflowExecutorManager;
import eu.europeana.metis.core.rest.ExecutionHistory;
import eu.europeana.metis.core.rest.PluginsWithDataAvailability;
import eu.europeana.metis.core.rest.VersionEvolution;
import eu.europeana.metis.core.rest.VersionEvolution.VersionEvolutionStep;
import eu.europeana.metis.core.rest.execution.overview.DatasetSummaryView;
import eu.europeana.metis.core.rest.execution.overview.ExecutionAndDatasetView;
import eu.europeana.metis.core.rest.execution.overview.ExecutionSummaryView;
import eu.europeana.metis.core.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.ValidationProperties;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePluginMetadata;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginFactory;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.ExecutionProgress;
import eu.europeana.metis.core.workflow.plugins.HTTPHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPreviewPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.IndexToPublishPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPlugin;
import eu.europeana.metis.core.workflow.plugins.OaipmhHarvestPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.core.workflow.plugins.ReindexToPreviewPlugin;
import eu.europeana.metis.core.workflow.plugins.ReindexToPreviewPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.TransformationPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationExternalPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.ValidationInternalPluginMetadata;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import eu.europeana.metis.utils.DateUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-06
 */
class TestOrchestratorService {

  private static final int SOLR_COMMIT_PERIOD_IN_MINS = 15;
  private static WorkflowExecutionDao workflowExecutionDao;
  private static WorkflowUtils workflowUtils;
  private static WorkflowDao workflowDao;
  private static DatasetDao datasetDao;
  private static DatasetXsltDao datasetXsltDao;
  private static WorkflowExecutorManager workflowExecutorManager;
  private static WorkflowExecutionFactory workflowExecutionFactory;
  private static OrchestratorService orchestratorService;
  private static RedissonClient redissonClient;
  private static Authorizer authorizer;

  @BeforeAll
  static void prepare() {
    workflowExecutionDao = mock(WorkflowExecutionDao.class);
    workflowUtils = mock(WorkflowUtils.class);
    workflowDao = mock(WorkflowDao.class);
    datasetDao = mock(DatasetDao.class);
    datasetXsltDao = mock(DatasetXsltDao.class);
    workflowExecutorManager = mock(WorkflowExecutorManager.class);
    redissonClient = mock(RedissonClient.class);
    authorizer = mock(Authorizer.class);

    workflowExecutionFactory = spy(new WorkflowExecutionFactory(datasetXsltDao));
    workflowExecutionFactory.setMetisCoreUrl("https://some.url.com");
    workflowExecutionFactory.setValidationExternalProperties(
        new ValidationProperties("url-ext", "schema-ext", "schematron-ext"));
    workflowExecutionFactory.setValidationInternalProperties(
        new ValidationProperties("url-int", "schema-int", "schematron-int"));

    orchestratorService = spy(new OrchestratorService(workflowExecutionFactory, workflowDao,
        workflowExecutionDao, workflowUtils, datasetDao, workflowExecutorManager, redissonClient,
        authorizer));
    orchestratorService.setSolrCommitPeriodInMins(SOLR_COMMIT_PERIOD_IN_MINS);
  }

  @AfterEach
  void cleanUp() {
    Mockito.reset(workflowExecutionDao);
    Mockito.reset(workflowUtils);
    Mockito.reset(workflowDao);
    Mockito.reset(datasetDao);
    Mockito.reset(workflowExecutorManager);
    Mockito.reset(redissonClient);
    Mockito.reset(authorizer);
    Mockito.reset(workflowExecutionFactory);
    Mockito.reset(orchestratorService);
  }

  @Test
  void createWorkflow() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset("datasetName");
    workflow.setDatasetId(dataset.getDatasetId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    orchestratorService.createWorkflow(metisUser, workflow.getDatasetId(), workflow, null);

    verify(authorizer, times(1))
        .authorizeWriteExistingDatasetById(metisUser, workflow.getDatasetId());
    verifyNoMoreInteractions(authorizer);
    InOrder inOrder = Mockito.inOrder(workflowDao);
    inOrder.verify(workflowDao, times(1)).workflowExistsForDataset(workflow.getDatasetId());
    inOrder.verify(workflowDao, times(1)).create(workflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void createWorkflowOrderOfPluginsNotAllowed() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset("datasetName");
    workflow.setDatasetId(dataset.getDatasetId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    doThrow(PluginExecutionNotAllowed.class).when(workflowUtils).validateWorkflowPlugins(workflow, null);
    assertThrows(PluginExecutionNotAllowed.class,
        () -> orchestratorService.createWorkflow(metisUser, workflow.getDatasetId(), workflow, null));

    verify(authorizer, times(1))
        .authorizeWriteExistingDatasetById(metisUser, workflow.getDatasetId());
    verifyNoMoreInteractions(authorizer);
    verify(workflowDao, times(1)).workflowExistsForDataset(workflow.getDatasetId());
    verifyNoMoreInteractions(workflowDao);
  }

  @Test
  void createWorkflow_AlreadyExists() {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset("datasetName");
    workflow.setDatasetId(dataset.getDatasetId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao.workflowExistsForDataset(workflow.getDatasetId())).thenReturn(true);

    assertThrows(WorkflowAlreadyExistsException.class,
        () -> orchestratorService.createWorkflow(metisUser, workflow.getDatasetId(), workflow, null));

    InOrder inOrder = Mockito.inOrder(workflowDao);
    inOrder.verify(workflowDao, times(1)).workflowExistsForDataset(workflow.getDatasetId());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void updateWorkflow() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset("datasetName");
    workflow.setDatasetId(dataset.getDatasetId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao.getWorkflow(dataset.getDatasetId())).thenReturn(workflow);
    orchestratorService.updateWorkflow(metisUser, workflow.getDatasetId(), workflow, null);
    verify(authorizer, times(1))
        .authorizeWriteExistingDatasetById(metisUser, workflow.getDatasetId());
    verifyNoMoreInteractions(authorizer);
    InOrder inOrder = Mockito.inOrder(workflowDao);
    inOrder.verify(workflowDao, times(1)).getWorkflow(dataset.getDatasetId());
    inOrder.verify(workflowDao, times(1)).update(workflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void updateUserWorkflow_NoUserWorkflowFound() {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset("datasetName");
    workflow.setDatasetId(dataset.getDatasetId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    assertThrows(NoWorkflowFoundException.class,
        () -> orchestratorService.updateWorkflow(metisUser, workflow.getDatasetId(), workflow, null));
    InOrder inOrder = Mockito.inOrder(workflowDao);
    inOrder.verify(workflowDao, times(1)).getWorkflow(anyString());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void deleteWorkflow() throws GenericMetisException {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    orchestratorService.deleteWorkflow(metisUser, workflow.getDatasetId());
    verify(authorizer, times(1))
        .authorizeWriteExistingDatasetById(metisUser, workflow.getDatasetId());
    verifyNoMoreInteractions(authorizer);
    ArgumentCaptor<String> workflowDatasetIdArgumentCaptor = ArgumentCaptor
        .forClass(String.class);
    verify(workflowDao, times(1)).deleteWorkflow(workflowDatasetIdArgumentCaptor.capture());
    assertEquals(workflow.getDatasetId(),
        workflowDatasetIdArgumentCaptor.getValue());
  }

  @Test
  void getWorkflow() throws GenericMetisException {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);

    Workflow retrievedWorkflow = orchestratorService
        .getWorkflow(metisUser, workflow.getDatasetId());
    verify(authorizer, times(1))
        .authorizeReadExistingDatasetById(metisUser, workflow.getDatasetId());
    verifyNoMoreInteractions(authorizer);
    assertSame(workflow, retrievedWorkflow);
  }

  @Test
  void getWorkflowExecutionByExecutionId() throws GenericMetisException {

    // Create some objects
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String workflowExecutionId = "workflow execution ID";
    final WorkflowExecution workflowExecution = mock(WorkflowExecution.class);
    final String datasetId = "dataset ID";
    when(workflowExecution.getDatasetId()).thenReturn(datasetId);

    // Test the happy flow
    when(workflowExecutionDao.getById(workflowExecutionId)).thenReturn(workflowExecution);
    when(authorizer.authorizeReadExistingDatasetById(metisUser, datasetId)).thenReturn(null);
    assertSame(workflowExecution,
        orchestratorService.getWorkflowExecutionByExecutionId(metisUser, workflowExecutionId));

    // Test when the workflow execution does not exist
    when(workflowExecutionDao.getById(eq(workflowExecutionId))).thenReturn(null);
    assertNull(
        orchestratorService.getWorkflowExecutionByExecutionId(metisUser, workflowExecutionId));
    when(workflowExecutionDao.getById(eq(workflowExecutionId))).thenReturn(workflowExecution);
    assertSame(workflowExecution,
        orchestratorService.getWorkflowExecutionByExecutionId(metisUser, workflowExecutionId));

    // Test when the user is not allowed
    when(authorizer.authorizeReadExistingDatasetById(metisUser, datasetId))
        .thenThrow(new UserUnauthorizedException(""));
    assertThrows(UserUnauthorizedException.class, () -> orchestratorService
        .getWorkflowExecutionByExecutionId(metisUser, workflowExecutionId));
    doReturn(null).when(authorizer).authorizeReadExistingDatasetById(metisUser, datasetId);
    assertSame(workflowExecution,
        orchestratorService.getWorkflowExecutionByExecutionId(metisUser, workflowExecutionId));
  }

  @Test
  void getWorkflowExecutionByExecutionId_NonExistingWorkflowExecution()
      throws GenericMetisException {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String workflowExecutionId = "workflow execution id";
    when(workflowExecutionDao.getById(workflowExecutionId)).thenReturn(null);
    orchestratorService.getWorkflowExecutionByExecutionId(metisUser, workflowExecutionId);
    verifyNoMoreInteractions(authorizer);
    InOrder inOrder = Mockito.inOrder(workflowExecutionDao);
    inOrder.verify(workflowExecutionDao, times(1)).getById(workflowExecutionId);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions() throws Exception {

    // Create the test objects
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(authorizer.authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(anyString())).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    DatasetXslt datasetXslt = TestObjectFactory.createXslt(dataset);
    datasetXslt.setId(new ObjectId(TestObjectFactory.XSLTID));
    when(datasetXsltDao.getLatestDefaultXslt()).thenReturn(datasetXslt);
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(rlock).unlock();
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);

    // Add the workflow
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUser, dataset.getDatasetId(), null, 0);
    verify(authorizer, times(1))
        .authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId());
    verifyNoMoreInteractions(authorizer);

    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutionsWithoutAuthorization(dataset.getDatasetId(), null,
            0);
    verifyNoMoreInteractions(authorizer);

    // Verify the validation parameters
    final Map<ExecutablePluginType, AbstractExecutablePluginMetadata> pluginsByType = workflow
        .getMetisPluginsMetadata().stream().collect(Collectors
            .toMap(AbstractExecutablePluginMetadata::getExecutablePluginType, Function.identity(),
                (m1, m2) -> m1));
    final ValidationInternalPluginMetadata metadataInternal =
        (ValidationInternalPluginMetadata) pluginsByType.get(ExecutablePluginType.VALIDATION_INTERNAL);
    assertEquals(workflowExecutionFactory.getValidationInternalProperties().getUrlOfSchemasZip(),
        metadataInternal.getUrlOfSchemasZip());
    assertEquals(workflowExecutionFactory.getValidationInternalProperties().getSchemaRootPath(),
        metadataInternal.getSchemaRootPath());
    assertEquals(workflowExecutionFactory.getValidationInternalProperties().getSchematronRootPath(),
        metadataInternal.getSchematronRootPath());
    final ValidationExternalPluginMetadata metadataExternal =
        (ValidationExternalPluginMetadata) pluginsByType.get(ExecutablePluginType.VALIDATION_EXTERNAL);
    assertEquals(workflowExecutionFactory.getValidationExternalProperties().getUrlOfSchemasZip(),
        metadataExternal.getUrlOfSchemasZip());
    assertEquals(workflowExecutionFactory.getValidationExternalProperties().getSchemaRootPath(),
        metadataExternal.getSchemaRootPath());
    assertEquals(workflowExecutionFactory.getValidationExternalProperties().getSchematronRootPath(),
        metadataExternal.getSchematronRootPath());
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_TransformationUsesCustomXslt()
      throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    workflow.getMetisPluginsMetadata().forEach(abstractMetisPluginMetadata -> {
      if (abstractMetisPluginMetadata instanceof TransformationPluginMetadata) {
        ((TransformationPluginMetadata) abstractMetisPluginMetadata).setCustomXslt(true);
      }
    });
    when(authorizer.authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(anyString())).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    DatasetXslt datasetXslt = TestObjectFactory.createXslt(dataset);
    datasetXslt.setId(new ObjectId(TestObjectFactory.XSLTID));
    dataset.setXsltId(datasetXslt.getId());
    when(datasetXsltDao.getById(dataset.getXsltId().toString())).thenReturn(datasetXslt);
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(rlock).unlock();
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUser, dataset.getDatasetId(), null, 0);
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_AddHTTPHarvest()
      throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    HTTPHarvestPluginMetadata httpHarvestPluginMetadata = new HTTPHarvestPluginMetadata();
    httpHarvestPluginMetadata.setUrl("http://harvest.url.org");
    httpHarvestPluginMetadata.setEnabled(true);
    workflow.getMetisPluginsMetadata().set(0, httpHarvestPluginMetadata);
    when(authorizer.authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    when(redissonClient.getFairLock(anyString())).thenReturn(Mockito.mock(RLock.class));
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUser, dataset.getDatasetId(), null, 0);
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_NoHarvestPlugin() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    workflow.getMetisPluginsMetadata().remove(0);

    when(authorizer.authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    OaipmhHarvestPlugin oaipmhHarvestPlugin = (OaipmhHarvestPlugin) ExecutablePluginFactory
        .createPlugin(new OaipmhHarvestPluginMetadata());
    oaipmhHarvestPlugin.setPluginMetadata(new OaipmhHarvestPluginMetadata());
    oaipmhHarvestPlugin.setStartedDate(new Date());
    ExecutionProgress executionProgress = new ExecutionProgress();
    executionProgress.setProcessedRecords(5);
    oaipmhHarvestPlugin.setExecutionProgress(executionProgress);
    when(workflowUtils.validateWorkflowPlugins(workflow, null)).thenReturn(oaipmhHarvestPlugin);
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(anyString())).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(rlock).unlock();
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUser, dataset.getDatasetId(), null, 0);
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_NoHarvestPlugin_NoProcessPlugin()
      throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(authorizer.authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    when(redissonClient.getFairLock(anyString())).thenReturn(Mockito.mock(RLock.class));
    when(workflowUtils.validateWorkflowPlugins(workflow, null))
        .thenThrow(new PluginExecutionNotAllowed(""));
    assertThrows(PluginExecutionNotAllowed.class, () -> orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUser, dataset.getDatasetId(), null, 0));
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_EcloudDatasetAlreadyGenerated()
      throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setEcloudDatasetId("f525f64c-fea0-44bf-8c56-88f30962734c");
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(authorizer.authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    when(redissonClient.getFairLock(anyString())).thenReturn(Mockito.mock(RLock.class));
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUser, dataset.getDatasetId(), null, 0);
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_EcloudDatasetAlreadyExistsInEcloud()
      throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(authorizer.authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    when(datasetDao.checkAndCreateDatasetInEcloud(any(Dataset.class)))
        .thenReturn(UUID.randomUUID().toString());
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(anyString())).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(rlock).unlock();
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUser, dataset.getDatasetId(), null, 0);
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_EcloudDatasetCreationFails()
      throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(authorizer.authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    when(datasetDao.checkAndCreateDatasetInEcloud(any(Dataset.class)))
        .thenReturn(UUID.randomUUID().toString());
    when(redissonClient.getFairLock(anyString())).thenReturn(Mockito.mock(RLock.class));
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    String objectId = new ObjectId().toString();
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(objectId);
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId, 0);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUser, dataset.getDatasetId(), null, 0);
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_NoDatasetFoundException()
      throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    when(authorizer.authorizeWriteExistingDatasetById(metisUser, datasetId))
        .thenThrow(NoDatasetFoundException.class);
    assertThrows(NoDatasetFoundException.class, () -> orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUser, datasetId, null, 0));
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_NoDatasetFoundException_Unauthorized() {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    when(datasetDao.getDatasetByDatasetId(datasetId)).thenReturn(null);
    assertThrows(NoDatasetFoundException.class, () -> orchestratorService
        .addWorkflowInQueueOfWorkflowExecutionsWithoutAuthorization(datasetId, null, 0));
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_NoWorkflowFoundException()
      throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(authorizer.authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowDao.getWorkflow(dataset.getDatasetId())).thenReturn(null);
    assertThrows(NoWorkflowFoundException.class, () -> orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUser, dataset.getDatasetId(), null, 0));
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_WorkflowIsEmpty() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = new Workflow();
    workflow.setDatasetId(dataset.getDatasetId());
    when(authorizer.authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowDao.getWorkflow(dataset.getDatasetId())).thenReturn(workflow);
    when(workflowUtils.validateWorkflowPlugins(workflow, null))
        .thenThrow(new BadContentException(""));
    assertThrows(BadContentException.class, () -> orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUser, dataset.getDatasetId(), null, 0));
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_WorkflowExecutionAlreadyExistsException()
      throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(authorizer.authorizeWriteExistingDatasetById(metisUser, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowDao.getWorkflow(dataset.getDatasetId())).thenReturn(workflow);
    when(redissonClient.getFairLock(anyString())).thenReturn(Mockito.mock(RLock.class));
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId()))
        .thenReturn(new ObjectId().toString());
    assertThrows(WorkflowExecutionAlreadyExistsException.class, () -> orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUser, dataset.getDatasetId(), null, 0));
  }

  @Test
  void cancelWorkflowExecution() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    when(workflowExecutionDao.getById(TestObjectFactory.EXECUTIONID)).thenReturn(workflowExecution);
    doNothing().when(workflowExecutionDao).setCancellingState(workflowExecution, null);
    orchestratorService.cancelWorkflowExecution(metisUser, TestObjectFactory.EXECUTIONID);
    verify(authorizer, times(1))
        .authorizeWriteExistingDatasetById(metisUser, workflowExecution.getDatasetId());
    verifyNoMoreInteractions(authorizer);
  }

  @Test
  void cancelWorkflowExecution_NoWorkflowExecutionFoundException() {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(workflowExecutionDao.getById(TestObjectFactory.EXECUTIONID)).thenReturn(null);
    assertThrows(NoWorkflowExecutionFoundException.class, () -> orchestratorService
        .cancelWorkflowExecution(metisUser, TestObjectFactory.EXECUTIONID));
    verifyNoMoreInteractions(workflowExecutorManager);
  }

  @Test
  void getWorkflowExecutionsPerRequest() {
    orchestratorService.getWorkflowExecutionsPerRequest();
    verify(workflowExecutionDao, times(1)).getWorkflowExecutionsPerRequest();
  }

  @Test
  void getWorkflowsPerRequest() {
    orchestratorService.getWorkflowsPerRequest();
    verify(workflowDao, times(1)).getWorkflowsPerRequest();
  }

  @Test
  void getLatestSuccessfulFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution_ProcessPlugin()
      throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final AbstractExecutablePlugin oaipmhHarvestPlugin = ExecutablePluginFactory
        .createPlugin(new OaipmhHarvestPluginMetadata());
    when(authorizer.authorizeReadExistingDatasetById(metisUser, datasetId)).thenReturn(null);
    when(workflowUtils.computePredecessorPlugin(ExecutablePluginType.VALIDATION_EXTERNAL, null,
        datasetId)).thenReturn(oaipmhHarvestPlugin);
    assertSame(oaipmhHarvestPlugin, orchestratorService
        .getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(metisUser, datasetId,
            ExecutablePluginType.VALIDATION_EXTERNAL, null));
    verify(authorizer, times(1)).authorizeReadExistingDatasetById(metisUser, datasetId);
    verifyNoMoreInteractions(authorizer);
  }

  @Test
  void getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution_PluginExecutionNotAllowed()
      throws NoDatasetFoundException, UserUnauthorizedException, PluginExecutionNotAllowed {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    when(authorizer.authorizeReadExistingDatasetById(metisUser, datasetId)).thenReturn(null);
    when(workflowUtils.computePredecessorPlugin(ExecutablePluginType.VALIDATION_EXTERNAL, null,
        datasetId)).thenThrow(new PluginExecutionNotAllowed(""));
    assertThrows(PluginExecutionNotAllowed.class, () -> orchestratorService
        .getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(metisUser,
            datasetId, ExecutablePluginType.VALIDATION_EXTERNAL, null));
  }

  @Test
  void getAllWorkflowExecutionsByDatasetId() throws GenericMetisException {

    // Define some constants
    final int nextPage = 1;
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<WorkflowStatus> workflowStatuses = Collections.singleton(WorkflowStatus.INQUEUE);

    // Check with specific dataset ID: should query only that dataset.
    doReturn(new ResultList<>(Collections.emptyList(), false)).when(workflowExecutionDao)
        .getAllWorkflowExecutions(any(), any(), any(), anyBoolean(), anyInt(), anyBoolean());
    orchestratorService.getAllWorkflowExecutions(metisUser, datasetId, workflowStatuses,
        DaoFieldNames.ID, false, nextPage);
    verify(authorizer, times(1)).authorizeReadExistingDatasetById(metisUser, datasetId);
    verifyNoMoreInteractions(authorizer);
    verify(workflowExecutionDao, times(1)).getAllWorkflowExecutions(
        eq(Collections.singleton(datasetId)), eq(workflowStatuses), eq(DaoFieldNames.ID), eq(false),
        eq(nextPage), eq(false));
    verifyNoMoreInteractions(workflowExecutionDao);
  }

  @Test
  void getAllWorkflowExecutionsForRegularUser() throws GenericMetisException {

    // Define some constants
    final int nextPage = 1;
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final Set<String> datasetIds = new HashSet<>(Arrays.asList("A", "B", "C"));
    final List<Dataset> datasets = datasetIds.stream().map(id -> {
      final Dataset result = new Dataset();
      result.setDatasetId(id);
      return result;
    }).collect(Collectors.toList());
    final Set<WorkflowStatus> workflowStatuses = Collections.singleton(WorkflowStatus.INQUEUE);

    // Check for all datasets and for regular user: should query all datasets to which that user's
    // organization has rights.
    when(datasetDao.getAllDatasetsByOrganizationId(metisUser.getOrganizationId()))
        .thenReturn(datasets);
    doReturn(new ResultList<>(Collections.emptyList(), false)).when(workflowExecutionDao)
        .getAllWorkflowExecutions(any(), any(), any(), anyBoolean(), anyInt(), anyBoolean());
    orchestratorService.getAllWorkflowExecutions(metisUser, null, workflowStatuses,
        DaoFieldNames.CREATED_DATE, false, nextPage);
    verify(authorizer, times(1)).authorizeReadAllDatasets(metisUser);
    verifyNoMoreInteractions(authorizer);
    verify(workflowExecutionDao, times(1)).getAllWorkflowExecutions(eq(datasetIds),
        eq(workflowStatuses), eq(DaoFieldNames.CREATED_DATE), eq(false), eq(nextPage), eq(false));
    verifyNoMoreInteractions(workflowExecutionDao);
  }

  @Test
  void getAllWorkflowExecutionsForAdmin() throws GenericMetisException {

    // Define some constants
    final int nextPage = 1;
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final Set<WorkflowStatus> workflowStatuses = Collections.singleton(WorkflowStatus.INQUEUE);

    // Check for all datasets and for admin user: should query all datasets.
    metisUser.setAccountRole(AccountRole.METIS_ADMIN);
    doReturn(new ResultList<>(Collections.emptyList(), false)).when(workflowExecutionDao)
        .getAllWorkflowExecutions(any(), any(), any(), anyBoolean(), anyInt(), anyBoolean());
    orchestratorService.getAllWorkflowExecutions(metisUser, null, workflowStatuses,
        DaoFieldNames.CREATED_DATE, true, nextPage);
    verify(authorizer, times(1)).authorizeReadAllDatasets(metisUser);
    verifyNoMoreInteractions(authorizer);
    verify(workflowExecutionDao, times(1)).getAllWorkflowExecutions(isNull(), eq(workflowStatuses),
        eq(DaoFieldNames.CREATED_DATE), eq(true), eq(nextPage), eq(false));
    verifyNoMoreInteractions(workflowExecutionDao);
  }

  @Test
  void getWorkflowExecutionOverviewForRegularUser() throws GenericMetisException {

    // Define some constants
    final int nextPage = 1;
    final int pageCount = 2;
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final Set<String> datasetIds = new HashSet<>(Arrays.asList("A", "B", "C"));
    final List<Dataset> datasets = datasetIds.stream().map(id -> {
      final Dataset result = new Dataset();
      result.setDatasetId(id);
      return result;
    }).collect(Collectors.toList());
    final List<ExecutionDatasetPair> data = TestObjectFactory.createExecutionsWithDatasets(4);

    // Check for all datasets and for regular user: should query all datasets to which that user's
    // organization has rights.
    when(datasetDao.getAllDatasetsByOrganizationId(metisUser.getOrganizationId()))
        .thenReturn(datasets);
    when(workflowExecutionDao
        .getWorkflowExecutionsOverview(eq(datasetIds), isNull(), isNull(), isNull(), isNull(),
            eq(nextPage), eq(pageCount)))
        .thenReturn(new ResultList<>(data, false));
    final List<ExecutionAndDatasetView> result = orchestratorService
        .getWorkflowExecutionsOverview(metisUser, null, null, null, null, nextPage, pageCount);
    verify(authorizer, times(1)).authorizeReadAllDatasets(metisUser);
    verifyNoMoreInteractions(authorizer);
    verify(workflowExecutionDao, times(1))
        .getWorkflowExecutionsOverview(eq(datasetIds), isNull(), isNull(), isNull(), isNull(),
            eq(nextPage), eq(pageCount));
    verifyNoMoreInteractions(workflowExecutionDao);
    assertEquals(data.size(), result.size());
    assertEquals(data.stream().map(ExecutionDatasetPair::getDataset).map(Dataset::getDatasetId)
            .collect(Collectors.toList()),
        result.stream().map(ExecutionAndDatasetView::getDataset)
            .map(DatasetSummaryView::getDatasetId)
            .collect(Collectors.toList()));
    assertEquals(data.stream().map(ExecutionDatasetPair::getExecution).map(WorkflowExecution::getId)
            .map(ObjectId::toString).collect(Collectors.toList()),
        result.stream().map(ExecutionAndDatasetView::getExecution)
            .map(ExecutionSummaryView::getId).collect(Collectors.toList()));
  }

  @Test
  void getWorkflowExecutionOverviewForAdmin() throws GenericMetisException {

    // Define some constants
    final int nextPage = 1;
    final int pageCount = 2;
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final List<ExecutionDatasetPair> data = TestObjectFactory.createExecutionsWithDatasets(4);

    // Check for all datasets and for admin user: should query all datasets.
    metisUser.setAccountRole(AccountRole.METIS_ADMIN);
    when(workflowExecutionDao
        .getWorkflowExecutionsOverview(isNull(), isNull(), isNull(), isNull(), isNull(),
            eq(nextPage), eq(pageCount)))
        .thenReturn(new ResultList<>(data, false));
    final List<ExecutionAndDatasetView> result = orchestratorService
        .getWorkflowExecutionsOverview(metisUser, null, null, null, null, nextPage, pageCount);
    verify(authorizer, times(1)).authorizeReadAllDatasets(metisUser);
    verifyNoMoreInteractions(authorizer);
    verify(workflowExecutionDao, times(1))
        .getWorkflowExecutionsOverview(isNull(), isNull(), isNull(), isNull(), isNull(),
            eq(nextPage), eq(pageCount));
    verifyNoMoreInteractions(workflowExecutionDao);
    assertEquals(data.size(), result.size());
    assertEquals(data.stream().map(ExecutionDatasetPair::getDataset).map(Dataset::getDatasetId)
            .collect(Collectors.toList()),
        result.stream().map(ExecutionAndDatasetView::getDataset)
            .map(DatasetSummaryView::getDatasetId)
            .collect(Collectors.toList()));
    assertEquals(data.stream().map(ExecutionDatasetPair::getExecution).map(WorkflowExecution::getId)
            .map(ObjectId::toString).collect(Collectors.toList()),
        result.stream().map(ExecutionAndDatasetView::getExecution)
            .map(ExecutionSummaryView::getId).collect(Collectors.toList()));
  }

  @Test
  void getDatasetExecutionInformation() throws GenericMetisException {

    // Create execution progress object
    ExecutionProgress executionProgress = new ExecutionProgress();
    executionProgress.setProcessedRecords(100);
    executionProgress.setErrors(20);

    // Create harvest plugin.
    AbstractExecutablePlugin oaipmhHarvestPlugin = ExecutablePluginFactory
        .createPlugin(new OaipmhHarvestPluginMetadata());
    oaipmhHarvestPlugin.setFinishedDate(
        DateUtils.modifyDateByTimeUnitAmount(new Date(), -(SOLR_COMMIT_PERIOD_IN_MINS + 5),
            TimeUnit.MINUTES));
    oaipmhHarvestPlugin.setDataStatus(null); // Is default status, means valid.
    oaipmhHarvestPlugin.setExecutionProgress(executionProgress);

    // Create first publish plugin
    AbstractExecutablePlugin firstPublishPlugin = ExecutablePluginFactory
        .createPlugin(new IndexToPublishPluginMetadata());
    firstPublishPlugin.setFinishedDate(
        DateUtils.modifyDateByTimeUnitAmount(new Date(), -(SOLR_COMMIT_PERIOD_IN_MINS + 4),
            TimeUnit.MINUTES));
    firstPublishPlugin.setDataStatus(null); // Is default status, means valid.
    firstPublishPlugin.setExecutionProgress(executionProgress);

    // Create preview plugin
    AbstractExecutablePlugin previewPlugin = ExecutablePluginFactory
        .createPlugin(new IndexToPreviewPluginMetadata());
    previewPlugin.setFinishedDate(
        DateUtils.modifyDateByTimeUnitAmount(new Date(), -(SOLR_COMMIT_PERIOD_IN_MINS + 3),
            TimeUnit.MINUTES));
    previewPlugin.setDataStatus(null); // Is default status, means valid.
    previewPlugin.setExecutionProgress(executionProgress);

    // Create second publish plugin
    AbstractExecutablePlugin lastPublishPlugin = ExecutablePluginFactory
        .createPlugin(new IndexToPublishPluginMetadata());
    lastPublishPlugin.setFinishedDate(
        DateUtils.modifyDateByTimeUnitAmount(new Date(), -(SOLR_COMMIT_PERIOD_IN_MINS + 2),
            TimeUnit.MINUTES));
    lastPublishPlugin.setDataStatus(null); // Is default status, means valid.
    lastPublishPlugin.setExecutionProgress(executionProgress);

    // Create reindex to preview plugin
    AbstractMetisPlugin reindexToPreviewPlugin = new ReindexToPreviewPlugin(
        new ReindexToPreviewPluginMetadata());
    reindexToPreviewPlugin.setFinishedDate(
        DateUtils.modifyDateByTimeUnitAmount(new Date(), -(SOLR_COMMIT_PERIOD_IN_MINS + 1),
            TimeUnit.MINUTES));

    // Create execution in progress with a publish plugin
    final WorkflowExecution workflowExecutionObject = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecutionObject.setWorkflowStatus(WorkflowStatus.RUNNING);
    final List<AbstractMetisPlugin> metisPlugins = workflowExecutionObject.getMetisPlugins();
    final AbstractExecutablePlugin cleaningPublishPlugin = ExecutablePluginFactory
        .createPlugin(new IndexToPublishPluginMetadata());
    cleaningPublishPlugin.setPluginStatus(PluginStatus.CLEANING);
    metisPlugins.add(cleaningPublishPlugin);
    workflowExecutionObject.setMetisPlugins(metisPlugins);

    // Mock the workflow execution
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
        EnumSet.of(ExecutablePluginType.HTTP_HARVEST, ExecutablePluginType.OAIPMH_HARVEST), false))
            .thenReturn(new PluginWithExecutionId<>("", oaipmhHarvestPlugin));
    when(workflowExecutionDao.getFirstSuccessfulPlugin(datasetId,
        EnumSet.of(PluginType.PUBLISH, PluginType.REINDEX_TO_PUBLISH)))
            .thenReturn(firstPublishPlugin);
    when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
        EnumSet.of(ExecutablePluginType.PREVIEW), false))
            .thenReturn(new PluginWithExecutionId<>("", previewPlugin));
    when(workflowExecutionDao.getLatestSuccessfulPlugin(datasetId,
        EnumSet.of(PluginType.PREVIEW, PluginType.REINDEX_TO_PREVIEW)))
            .thenReturn(reindexToPreviewPlugin);
    when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
        EnumSet.of(ExecutablePluginType.PUBLISH), false))
            .thenReturn(new PluginWithExecutionId<>("", lastPublishPlugin));
    when(workflowExecutionDao.getLatestSuccessfulPlugin(datasetId,
        EnumSet.of(PluginType.PUBLISH, PluginType.REINDEX_TO_PUBLISH)))
            .thenReturn(lastPublishPlugin);
    when(workflowExecutionDao.getRunningOrInQueueExecution(datasetId))
        .thenReturn(workflowExecutionObject);

    DatasetExecutionInformation executionInfo = orchestratorService
        .getDatasetExecutionInformation(metisUser, datasetId);

    verify(authorizer, times(1)).authorizeReadExistingDatasetById(metisUser, datasetId);
    verifyNoMoreInteractions(authorizer);

    assertEquals(oaipmhHarvestPlugin.getFinishedDate(), executionInfo.getLastHarvestedDate());
    assertEquals(reindexToPreviewPlugin.getFinishedDate(), executionInfo.getLastPreviewDate());
    assertEquals(firstPublishPlugin.getFinishedDate(), executionInfo.getFirstPublishedDate());
    assertEquals(lastPublishPlugin.getFinishedDate(), executionInfo.getLastPublishedDate());

    assertEquals(
        oaipmhHarvestPlugin.getExecutionProgress().getProcessedRecords() - oaipmhHarvestPlugin
            .getExecutionProgress().getErrors(), executionInfo.getLastHarvestedRecords());
    assertEquals(
        previewPlugin.getExecutionProgress().getProcessedRecords() - previewPlugin
            .getExecutionProgress().getErrors(), executionInfo.getLastPreviewRecords());
    assertEquals(
        lastPublishPlugin.getExecutionProgress().getProcessedRecords() - lastPublishPlugin
            .getExecutionProgress().getErrors(), executionInfo.getLastPublishedRecords());

    assertTrue(executionInfo.isLastPreviewRecordsReadyForViewing());
    assertFalse(executionInfo.isLastPublishedRecordsReadyForViewing());
  }

  @Test
  void testGetDatasetExecutionHistory() throws GenericMetisException {

    // Create some objects
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = "dataset ID";
    when(authorizer.authorizeReadExistingDatasetById(metisUser, datasetId)).thenReturn(null);

    // Create the history
    final ExecutionIdAndStartedDatePair pair1 = new ExecutionIdAndStartedDatePair(
        new ObjectId(), new Date(1));
    final ExecutionIdAndStartedDatePair pair2 = new ExecutionIdAndStartedDatePair(
        new ObjectId(), new Date(2));
    final ExecutionIdAndStartedDatePair pair3 = new ExecutionIdAndStartedDatePair(
        new ObjectId(), new Date(3));
    doReturn(new ResultList<>(Arrays.asList(pair1, pair2, pair3), false)).when(workflowExecutionDao)
        .getAllExecutionStartDates(datasetId);

    // Test the happy flow
    final ExecutionHistory result = orchestratorService
        .getDatasetExecutionHistory(metisUser, datasetId);
    assertNotNull(result);
    assertNotNull(result.getExecutions());
    assertEquals(3, result.getExecutions().size());
    assertEquals(pair1.getExecutionIdAsString(), result.getExecutions().get(0).getWorkflowExecutionId());
    assertEquals(pair1.getStartedDate(), result.getExecutions().get(0).getStartedDate());
    assertEquals(pair2.getExecutionIdAsString(), result.getExecutions().get(1).getWorkflowExecutionId());
    assertEquals(pair2.getStartedDate(), result.getExecutions().get(1).getStartedDate());
    assertEquals(pair3.getExecutionIdAsString(), result.getExecutions().get(2).getWorkflowExecutionId());
    assertEquals(pair3.getStartedDate(), result.getExecutions().get(2).getStartedDate());

    // Test when the user is not allowed
    when(authorizer.authorizeReadExistingDatasetById(metisUser, datasetId)).thenThrow(new UserUnauthorizedException(""));
    assertThrows(UserUnauthorizedException.class, () -> orchestratorService
        .getDatasetExecutionHistory(metisUser, datasetId));
  }

  @Test
  void testGetExecutablePluginsWithDataAvailability() throws GenericMetisException {

    // Create plugins
    final AbstractExecutablePlugin plugin1 = mock(AbstractExecutablePlugin.class);
    when(plugin1.getPluginType()).thenReturn(PluginType.OAIPMH_HARVEST);
    final ExecutionProgress progress1 = new ExecutionProgress();
    progress1.setProcessedRecords(10);
    progress1.setErrors(1);
    when(plugin1.getExecutionProgress()).thenReturn(progress1);
    final AbstractExecutablePlugin plugin2 = mock(AbstractExecutablePlugin.class);
    final ExecutionProgress progress2 = new ExecutionProgress();
    when(plugin2.getPluginType()).thenReturn(PluginType.TRANSFORMATION);
    progress2.setProcessedRecords(10);
    progress2.setErrors(10);
    when(plugin2.getExecutionProgress()).thenReturn(progress2);
    final AbstractExecutablePlugin plugin3 = mock(AbstractExecutablePlugin.class);
    when(plugin3.getPluginType()).thenReturn(PluginType.MEDIA_PROCESS);
    when(plugin3.getExecutionProgress()).thenReturn(null);
    final ReindexToPreviewPlugin plugin4 = mock(ReindexToPreviewPlugin.class);
    when(plugin4.getPluginType()).thenReturn(PluginType.REINDEX_TO_PUBLISH);
    when(plugin4.getFinishedDate()).thenReturn(new Date(4));

    // Create other objects
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = "dataset ID";
    final WorkflowExecution execution = createWorkflowExecution(metisUser, datasetId, plugin1,
        plugin2, plugin3, plugin4);
    final String workflowExecutionId = execution.getId().toString();

    // Test happy flow
    final PluginsWithDataAvailability result = orchestratorService
        .getExecutablePluginsWithDataAvailability(metisUser, workflowExecutionId);
    assertNotNull(result);
    assertNotNull(result.getPlugins());
    assertEquals(3, result.getPlugins().size());
    assertEquals(plugin1.getPluginType(), result.getPlugins().get(0).getPluginType());
    assertTrue(result.getPlugins().get(0).isHasSuccessfulData());
    assertEquals(plugin2.getPluginType(), result.getPlugins().get(1).getPluginType());
    assertFalse(result.getPlugins().get(1).isHasSuccessfulData());
    assertEquals(plugin3.getPluginType(), result.getPlugins().get(2).getPluginType());
    assertFalse(result.getPlugins().get(2).isHasSuccessfulData());

    // Test when the workflow execution does not exist
    doReturn(null).when(orchestratorService)
        .getWorkflowExecutionByExecutionId(metisUser, workflowExecutionId);
    assertThrows(NoWorkflowExecutionFoundException.class, () -> orchestratorService
        .getExecutablePluginsWithDataAvailability(metisUser, workflowExecutionId));

    // Test when the user is not allowed
    when(orchestratorService.getWorkflowExecutionByExecutionId(metisUser, workflowExecutionId))
        .thenAnswer(invocation -> {
          throw new UserUnauthorizedException("");
        });
    assertThrows(UserUnauthorizedException.class, () -> orchestratorService
        .getExecutablePluginsWithDataAvailability(metisUser, workflowExecutionId));
  }

  @Test
  void testGetRecordEvolutionForVersionExceptions() throws GenericMetisException {

    // Create some objects
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String workflowExecutionId = "workflow execution ID";
    final PluginType pluginType = PluginType.MEDIA_PROCESS;
    final WorkflowExecution workflowExecution = mock(WorkflowExecution.class);

    // Test when the workflow execution does not exist
    when(orchestratorService.getWorkflowExecutionByExecutionId(metisUser, workflowExecutionId))
        .thenReturn(null);
    assertThrows(NoWorkflowExecutionFoundException.class, () -> orchestratorService
        .getRecordEvolutionForVersion(metisUser, workflowExecutionId, pluginType));

    // Test when the user is not allowed
    when(orchestratorService.getWorkflowExecutionByExecutionId(metisUser, workflowExecutionId))
        .thenAnswer(invocation -> {
          throw new UserUnauthorizedException("");
        });
    assertThrows(UserUnauthorizedException.class, () -> orchestratorService
        .getRecordEvolutionForVersion(metisUser, workflowExecutionId, pluginType));

    // Test when the workflow execution does not have a plugin of the right type
    doReturn(workflowExecution).when(orchestratorService)
        .getWorkflowExecutionByExecutionId(metisUser, workflowExecutionId);
    when(workflowExecution.getMetisPluginWithType(pluginType)).thenReturn(Optional.empty());
    assertThrows(NoWorkflowExecutionFoundException.class, () -> orchestratorService
        .getRecordEvolutionForVersion(metisUser, workflowExecutionId, pluginType));
  }

  @Test
  void testGetRecordEvolutionForVersionHappyFlow() throws GenericMetisException {

    // Create two workflow executions with three plugins and link them together
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = "dataset ID";
    final AbstractExecutablePlugin plugin1 = createMetisPlugin(ExecutablePluginType.OAIPMH_HARVEST,
        new Date(1));
    final AbstractExecutablePlugin plugin2 = createMetisPlugin(ExecutablePluginType.TRANSFORMATION,
        new Date(2));
    final AbstractExecutablePlugin plugin3 = createMetisPlugin(ExecutablePluginType.MEDIA_PROCESS,
        new Date(3));
    final WorkflowExecution execution1 = createWorkflowExecution(metisUser, datasetId, plugin1);
    final WorkflowExecution execution2 = createWorkflowExecution(metisUser, datasetId, plugin2,
        plugin3);

    // Mock the methods in workflow utils.
    final List<Pair<AbstractExecutablePlugin, WorkflowExecution>> evolutionWithContent = Arrays.asList(
        ImmutablePair.of(plugin1, execution1), ImmutablePair.of(plugin2, execution2));
    doReturn(evolutionWithContent).when(workflowUtils).compileVersionEvolution(plugin3, execution2);
    doReturn(new ArrayList<>()).when(workflowUtils).compileVersionEvolution(plugin1, execution1);

    // Execute the call and expect an evolution with content.
    final VersionEvolution resultForThree = orchestratorService.getRecordEvolutionForVersion(
        metisUser, execution2.getId().toString(), plugin3.getPluginType());
    assertNotNull(resultForThree);
    assertNotNull(resultForThree.getEvolutionSteps());
    assertEquals(2, resultForThree.getEvolutionSteps().size());
    assertEvolutionStepEquals(resultForThree.getEvolutionSteps().get(0), execution1, plugin1);
    assertEvolutionStepEquals(resultForThree.getEvolutionSteps().get(1), execution2, plugin2);

    // Execute the call and expect an evolution without content.
    final VersionEvolution resultForOne = orchestratorService.getRecordEvolutionForVersion(
        metisUser, execution1.getId().toString(), plugin1.getPluginType());
    assertNotNull(resultForOne);
    assertNotNull(resultForOne.getEvolutionSteps());
    assertTrue(resultForOne.getEvolutionSteps().isEmpty());
  }

  private void assertEvolutionStepEquals(VersionEvolutionStep evolutionStep,
      WorkflowExecution execution, AbstractExecutablePlugin<?> plugin) {
    assertNotNull(evolutionStep);
    assertEquals(plugin.getFinishedDate(), evolutionStep.getFinishedTime());
    assertEquals(plugin.getPluginMetadata().getExecutablePluginType(),
        evolutionStep.getPluginType());
    assertEquals(execution.getId().toString(), evolutionStep.getWorkflowExecutionId());
  }

  private WorkflowExecution createWorkflowExecution(MetisUser metisUser, String datasetId,
      AbstractMetisPlugin... plugins) throws GenericMetisException {
    final WorkflowExecution result = new WorkflowExecution();
    result.setId(new ObjectId());
    result.setDatasetId(datasetId);
    result.setMetisPlugins(Arrays.asList(plugins));
    when(orchestratorService.getWorkflowExecutionByExecutionId(metisUser,
        result.getId().toString())).thenReturn(result);
    return result;
  }

  private AbstractExecutablePlugin createMetisPlugin(ExecutablePluginType type, Date date) {
    AbstractExecutablePlugin<AbstractExecutablePluginMetadata> result = mock(
        AbstractExecutablePlugin.class);
    AbstractExecutablePluginMetadata metadata = mock(AbstractExecutablePluginMetadata.class);
    when(metadata.getExecutablePluginType()).thenReturn(type);
    when(result.getPluginType()).thenReturn(type.toPluginType());
    when(result.getPluginMetadata()).thenReturn(metadata);
    when(result.getFinishedDate()).thenReturn(date);
    return result;
  }
}
