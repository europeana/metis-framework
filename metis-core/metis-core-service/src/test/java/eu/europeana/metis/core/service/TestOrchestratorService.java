package eu.europeana.metis.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import eu.europeana.metis.authentication.user.MetisUserView;
import eu.europeana.metis.core.common.DaoFieldNames;
import eu.europeana.metis.core.dao.DataEvolutionUtils;
import eu.europeana.metis.core.dao.DatasetDao;
import eu.europeana.metis.core.dao.DatasetXsltDao;
import eu.europeana.metis.core.dao.DepublishRecordIdDao;
import eu.europeana.metis.core.dao.PluginWithExecutionId;
import eu.europeana.metis.core.dao.WorkflowDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.dao.WorkflowExecutionDao.ExecutionDatasetPair;
import eu.europeana.metis.core.dao.WorkflowExecutionDao.ResultList;
import eu.europeana.metis.core.dao.WorkflowValidationUtils;
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
import eu.europeana.metis.core.workflow.plugins.MediaProcessPluginMetadata;
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
import org.jetbrains.annotations.NotNull;
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
  private static DataEvolutionUtils dataEvolutionUtils;
  private static WorkflowValidationUtils validationUtils;
  private static WorkflowDao workflowDao;
  private static DatasetDao datasetDao;
  private static DatasetXsltDao datasetXsltDao;
  private static DepublishRecordIdDao depublishRecordIdDao;
  private static WorkflowExecutorManager workflowExecutorManager;
  private static WorkflowExecutionFactory workflowExecutionFactory;
  private static OrchestratorService orchestratorService;
  private static RedissonClient redissonClient;
  private static Authorizer authorizer;

  @BeforeAll
  static void prepare() {
    workflowExecutionDao = mock(WorkflowExecutionDao.class);
    dataEvolutionUtils = mock(DataEvolutionUtils.class);
    validationUtils = mock(WorkflowValidationUtils.class);
    workflowDao = mock(WorkflowDao.class);
    datasetDao = mock(DatasetDao.class);
    datasetXsltDao = mock(DatasetXsltDao.class);
    depublishRecordIdDao = mock(DepublishRecordIdDao.class);
    workflowExecutorManager = mock(WorkflowExecutorManager.class);
    redissonClient = mock(RedissonClient.class);
    authorizer = mock(Authorizer.class);

    workflowExecutionFactory = spy(new WorkflowExecutionFactory(datasetXsltDao,
        depublishRecordIdDao, workflowExecutionDao, dataEvolutionUtils));
    workflowExecutionFactory.setValidationExternalProperties(
        new ValidationProperties("url-ext", "schema-ext", "schematron-ext"));
    workflowExecutionFactory.setValidationInternalProperties(
        new ValidationProperties("url-int", "schema-int", "schematron-int"));

    orchestratorService = spy(new OrchestratorService(workflowExecutionFactory, workflowDao,
        workflowExecutionDao, validationUtils, dataEvolutionUtils, datasetDao,
        workflowExecutorManager, redissonClient, authorizer, depublishRecordIdDao));
    orchestratorService.setSolrCommitPeriodInMins(SOLR_COMMIT_PERIOD_IN_MINS);
  }

  @AfterEach
  void cleanUp() {
    Mockito.reset(workflowExecutionDao);
    Mockito.reset(validationUtils);
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
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset("datasetName");
    workflow.setDatasetId(dataset.getDatasetId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    orchestratorService.createWorkflow(metisUserView, workflow.getDatasetId(), workflow, null);

    verify(authorizer, times(1))
        .authorizeWriteExistingDatasetById(metisUserView, workflow.getDatasetId());
    verifyNoMoreInteractions(authorizer);
    InOrder inOrder = Mockito.inOrder(workflowDao);
    inOrder.verify(workflowDao, times(1)).workflowExistsForDataset(workflow.getDatasetId());
    inOrder.verify(workflowDao, times(1)).create(workflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void createWorkflowOrderOfPluginsNotAllowed() throws Exception {
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset("datasetName");
    workflow.setDatasetId(dataset.getDatasetId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    doThrow(PluginExecutionNotAllowed.class).when(validationUtils)
        .validateWorkflowPlugins(workflow, null);
    assertThrows(PluginExecutionNotAllowed.class,
        () -> orchestratorService
            .createWorkflow(metisUserView, workflow.getDatasetId(), workflow, null));

    verify(authorizer, times(1))
        .authorizeWriteExistingDatasetById(metisUserView, workflow.getDatasetId());
    verifyNoMoreInteractions(authorizer);
    verify(workflowDao, times(1)).workflowExistsForDataset(workflow.getDatasetId());
    verifyNoMoreInteractions(workflowDao);
  }

  @Test
  void createWorkflow_AlreadyExists() {
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset("datasetName");
    workflow.setDatasetId(dataset.getDatasetId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao.workflowExistsForDataset(workflow.getDatasetId())).thenReturn(true);

    assertThrows(WorkflowAlreadyExistsException.class,
        () -> orchestratorService
            .createWorkflow(metisUserView, workflow.getDatasetId(), workflow, null));

    InOrder inOrder = Mockito.inOrder(workflowDao);
    inOrder.verify(workflowDao, times(1)).workflowExistsForDataset(workflow.getDatasetId());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void updateWorkflow() throws Exception {
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset("datasetName");
    workflow.setDatasetId(dataset.getDatasetId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao.getWorkflow(dataset.getDatasetId())).thenReturn(workflow);
    orchestratorService.updateWorkflow(metisUserView, workflow.getDatasetId(), workflow, null);
    verify(authorizer, times(1))
        .authorizeWriteExistingDatasetById(metisUserView, workflow.getDatasetId());
    verifyNoMoreInteractions(authorizer);
    InOrder inOrder = Mockito.inOrder(workflowDao);
    inOrder.verify(workflowDao, times(1)).getWorkflow(dataset.getDatasetId());
    inOrder.verify(workflowDao, times(1)).update(workflow);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void updateUserWorkflow_NoUserWorkflowFound() {
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    Dataset dataset = TestObjectFactory.createDataset("datasetName");
    workflow.setDatasetId(dataset.getDatasetId());
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    assertThrows(NoWorkflowFoundException.class,
        () -> orchestratorService
            .updateWorkflow(metisUserView, workflow.getDatasetId(), workflow, null));
    InOrder inOrder = Mockito.inOrder(workflowDao);
    inOrder.verify(workflowDao, times(1)).getWorkflow(anyString());
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void deleteWorkflow() throws GenericMetisException {
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    orchestratorService.deleteWorkflow(metisUserView, workflow.getDatasetId());
    verify(authorizer, times(1))
        .authorizeWriteExistingDatasetById(metisUserView, workflow.getDatasetId());
    verifyNoMoreInteractions(authorizer);
    ArgumentCaptor<String> workflowDatasetIdArgumentCaptor = ArgumentCaptor
        .forClass(String.class);
    verify(workflowDao, times(1)).deleteWorkflow(workflowDatasetIdArgumentCaptor.capture());
    assertEquals(workflow.getDatasetId(),
        workflowDatasetIdArgumentCaptor.getValue());
  }

  @Test
  void getWorkflow() throws GenericMetisException {
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);

    Workflow retrievedWorkflow = orchestratorService
        .getWorkflow(metisUserView, workflow.getDatasetId());
    verify(authorizer, times(1))
        .authorizeReadExistingDatasetById(metisUserView, workflow.getDatasetId());
    verifyNoMoreInteractions(authorizer);
    assertSame(workflow, retrievedWorkflow);
  }

  @Test
  void getWorkflowExecutionByExecutionId() throws GenericMetisException {

    // Create some objects
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String workflowExecutionId = "workflow execution ID";
    final WorkflowExecution workflowExecution = mock(WorkflowExecution.class);
    final String datasetId = "dataset ID";
    when(workflowExecution.getDatasetId()).thenReturn(datasetId);

    // Test the happy flow
    when(workflowExecutionDao.getById(workflowExecutionId)).thenReturn(workflowExecution);
    when(authorizer.authorizeReadExistingDatasetById(metisUserView, datasetId)).thenReturn(null);
    assertSame(workflowExecution,
        orchestratorService.getWorkflowExecutionByExecutionId(metisUserView, workflowExecutionId));

    // Test when the workflow execution does not exist
    when(workflowExecutionDao.getById(eq(workflowExecutionId))).thenReturn(null);
    assertNull(
        orchestratorService.getWorkflowExecutionByExecutionId(metisUserView, workflowExecutionId));
    when(workflowExecutionDao.getById(eq(workflowExecutionId))).thenReturn(workflowExecution);
    assertSame(workflowExecution,
        orchestratorService.getWorkflowExecutionByExecutionId(metisUserView, workflowExecutionId));

    // Test when the user is not allowed
    when(authorizer.authorizeReadExistingDatasetById(metisUserView, datasetId))
        .thenThrow(new UserUnauthorizedException(""));
    assertThrows(UserUnauthorizedException.class, () -> orchestratorService
        .getWorkflowExecutionByExecutionId(metisUserView, workflowExecutionId));
    doReturn(null).when(authorizer).authorizeReadExistingDatasetById(metisUserView, datasetId);
    assertSame(workflowExecution,
        orchestratorService.getWorkflowExecutionByExecutionId(metisUserView, workflowExecutionId));
  }

  @Test
  void getWorkflowExecutionByExecutionId_NonExistingWorkflowExecution()
      throws GenericMetisException {
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String workflowExecutionId = "workflow execution id";
    when(workflowExecutionDao.getById(workflowExecutionId)).thenReturn(null);
    orchestratorService.getWorkflowExecutionByExecutionId(metisUserView, workflowExecutionId);
    verifyNoMoreInteractions(authorizer);
    InOrder inOrder = Mockito.inOrder(workflowExecutionDao);
    inOrder.verify(workflowExecutionDao, times(1)).getById(workflowExecutionId);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions() throws Exception {

    // Create the test objects
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(authorizer.authorizeWriteExistingDatasetById(metisUserView, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(datasetDao.getDatasetByDatasetId(dataset.getDatasetId())).thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(anyString())).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    ObjectId objectId = new ObjectId();
    DatasetXslt datasetXslt = TestObjectFactory.createXslt(dataset);
    datasetXslt.setId(TestObjectFactory.DATASET_XSLT.getId());
    when(datasetXsltDao.getLatestDefaultXslt()).thenReturn(datasetXslt);
    WorkflowExecution workflowExecutionTest = new WorkflowExecution(dataset, new ArrayList<>(), 0);
    workflowExecutionTest.setId(objectId);
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(workflowExecutionTest);
    doNothing().when(rlock).unlock();
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId.toString(), 0);

    // Add the workflow
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUserView, dataset.getDatasetId(), null, null, 0);
    verify(authorizer, times(1))
        .authorizeWriteExistingDatasetById(metisUserView, dataset.getDatasetId());
    verifyNoMoreInteractions(authorizer);

    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutionsWithoutAuthorization(dataset.getDatasetId(), null,
            null,
            0);
    verifyNoMoreInteractions(authorizer);

    // Verify the validation parameters
    final Map<ExecutablePluginType, AbstractExecutablePluginMetadata> pluginsByType = workflow
        .getMetisPluginsMetadata().stream().collect(Collectors
            .toMap(AbstractExecutablePluginMetadata::getExecutablePluginType, Function.identity(),
                (m1, m2) -> m1));
    final ValidationInternalPluginMetadata metadataInternal =
        (ValidationInternalPluginMetadata) pluginsByType
            .get(ExecutablePluginType.VALIDATION_INTERNAL);
    assertEquals(workflowExecutionFactory.getValidationInternalProperties().getUrlOfSchemasZip(),
        metadataInternal.getUrlOfSchemasZip());
    assertEquals(workflowExecutionFactory.getValidationInternalProperties().getSchemaRootPath(),
        metadataInternal.getSchemaRootPath());
    assertEquals(workflowExecutionFactory.getValidationInternalProperties().getSchematronRootPath(),
        metadataInternal.getSchematronRootPath());
    final ValidationExternalPluginMetadata metadataExternal =
        (ValidationExternalPluginMetadata) pluginsByType
            .get(ExecutablePluginType.VALIDATION_EXTERNAL);
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
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    workflow.getMetisPluginsMetadata().forEach(abstractMetisPluginMetadata -> {
      if (abstractMetisPluginMetadata instanceof TransformationPluginMetadata) {
        ((TransformationPluginMetadata) abstractMetisPluginMetadata).setCustomXslt(true);
      }
    });
    when(authorizer.authorizeWriteExistingDatasetById(metisUserView, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(anyString())).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    ObjectId objectId = new ObjectId();
    DatasetXslt datasetXslt = TestObjectFactory.createXslt(dataset);
    datasetXslt.setId(TestObjectFactory.DATASET_XSLT.getId());
    dataset.setXsltId(datasetXslt.getId());
    WorkflowExecution workflowExecutionTest = new WorkflowExecution(dataset, new ArrayList<>(), 0);
    workflowExecutionTest.setId(objectId);
    when(datasetXsltDao.getById(dataset.getXsltId().toString())).thenReturn(datasetXslt);
    when(workflowExecutionDao.create(any(WorkflowExecution.class)))
        .thenReturn(workflowExecutionTest);
    doNothing().when(rlock).unlock();
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId.toString(), 0);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUserView, dataset.getDatasetId(), null, null, 0);
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_AddHTTPHarvest()
      throws Exception {
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    HTTPHarvestPluginMetadata httpHarvestPluginMetadata = new HTTPHarvestPluginMetadata();
    httpHarvestPluginMetadata.setUrl("http://harvest.url.org");
    httpHarvestPluginMetadata.setEnabled(true);
    workflow.getMetisPluginsMetadata().set(0, httpHarvestPluginMetadata);
    when(authorizer.authorizeWriteExistingDatasetById(metisUserView, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    when(redissonClient.getFairLock(anyString())).thenReturn(Mockito.mock(RLock.class));
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    ObjectId objectId = new ObjectId();
    WorkflowExecution workflowExecutionTest = new WorkflowExecution(dataset, new ArrayList<>(), 0);
    workflowExecutionTest.setId(objectId);
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(workflowExecutionTest);
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId.toString(), 0);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUserView, dataset.getDatasetId(), null, null, 0);
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_NoHarvestPlugin() throws Exception {
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    workflow.getMetisPluginsMetadata().remove(0);

    when(authorizer.authorizeWriteExistingDatasetById(metisUserView, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    OaipmhHarvestPlugin oaipmhHarvestPlugin = (OaipmhHarvestPlugin) ExecutablePluginFactory
        .createPlugin(new OaipmhHarvestPluginMetadata());
    oaipmhHarvestPlugin.setPluginMetadata(new OaipmhHarvestPluginMetadata());
    oaipmhHarvestPlugin.setStartedDate(new Date());
    ExecutionProgress executionProgress = new ExecutionProgress();
    executionProgress.setProcessedRecords(5);
    oaipmhHarvestPlugin.setExecutionProgress(executionProgress);
    when(validationUtils.validateWorkflowPlugins(workflow, null))
        .thenReturn(new PluginWithExecutionId<>("execution id", oaipmhHarvestPlugin));
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(anyString())).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    ObjectId objectId = new ObjectId();
    WorkflowExecution workflowExecutionTest = new WorkflowExecution(dataset, new ArrayList<>(), 0);
    workflowExecutionTest.setId(objectId);
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(workflowExecutionTest);
    doNothing().when(rlock).unlock();
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId.toString(), 0);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUserView, dataset.getDatasetId(), null, null, 0);
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_NoHarvestPlugin_NoProcessPlugin()
      throws Exception {
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(authorizer.authorizeWriteExistingDatasetById(metisUserView, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    when(redissonClient.getFairLock(anyString())).thenReturn(Mockito.mock(RLock.class));
    when(validationUtils.validateWorkflowPlugins(workflow, null))
        .thenThrow(new PluginExecutionNotAllowed(""));
    assertThrows(PluginExecutionNotAllowed.class, () -> orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUserView, dataset.getDatasetId(), null, null, 0));
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_EcloudDatasetAlreadyGenerated()
      throws Exception {
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    dataset.setEcloudDatasetId("f525f64c-fea0-44bf-8c56-88f30962734c");
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(authorizer.authorizeWriteExistingDatasetById(metisUserView, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    when(redissonClient.getFairLock(anyString())).thenReturn(Mockito.mock(RLock.class));
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    ObjectId objectId = new ObjectId();
    WorkflowExecution workflowExecutionTest = new WorkflowExecution(dataset, new ArrayList<>(), 0);
    workflowExecutionTest.setId(objectId);
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(workflowExecutionTest);
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId.toString(), 0);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUserView, dataset.getDatasetId(), null, null, 0);
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_EcloudDatasetAlreadyExistsInEcloud()
      throws Exception {
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(authorizer.authorizeWriteExistingDatasetById(metisUserView, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    when(datasetDao.checkAndCreateDatasetInEcloud(any(Dataset.class)))
        .thenReturn(UUID.randomUUID().toString());
    RLock rlock = mock(RLock.class);
    when(redissonClient.getFairLock(anyString())).thenReturn(rlock);
    doNothing().when(rlock).lock();
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    ObjectId objectId = new ObjectId();
    WorkflowExecution workflowExecutionTest = new WorkflowExecution(dataset, new ArrayList<>(), 0);
    workflowExecutionTest.setId(objectId);
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(workflowExecutionTest);
    doNothing().when(rlock).unlock();
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId.toString(), 0);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUserView, dataset.getDatasetId(), null, null, 0);
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_EcloudDatasetCreationFails()
      throws Exception {
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(authorizer.authorizeWriteExistingDatasetById(metisUserView, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowDao.getWorkflow(workflow.getDatasetId())).thenReturn(workflow);
    when(datasetDao.checkAndCreateDatasetInEcloud(any(Dataset.class)))
        .thenReturn(UUID.randomUUID().toString());
    when(redissonClient.getFairLock(anyString())).thenReturn(Mockito.mock(RLock.class));
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId())).thenReturn(null);
    ObjectId objectId = new ObjectId();
    WorkflowExecution workflowExecutionTest = new WorkflowExecution(dataset, new ArrayList<>(), 0);
    workflowExecutionTest.setId(objectId);
    when(workflowExecutionDao.create(any(WorkflowExecution.class))).thenReturn(workflowExecutionTest);
    doNothing().when(workflowExecutorManager).addWorkflowExecutionToQueue(objectId.toString(), 0);
    orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUserView, dataset.getDatasetId(), null, null, 0);
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_NoDatasetFoundException()
      throws Exception {
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    when(authorizer.authorizeWriteExistingDatasetById(metisUserView, datasetId))
        .thenThrow(NoDatasetFoundException.class);
    assertThrows(NoDatasetFoundException.class, () -> orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUserView, datasetId, null, null, 0));
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_NoDatasetFoundException_Unauthorized() {
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    when(datasetDao.getDatasetByDatasetId(datasetId)).thenReturn(null);
    assertThrows(NoDatasetFoundException.class, () -> orchestratorService
        .addWorkflowInQueueOfWorkflowExecutionsWithoutAuthorization(datasetId, null, null, 0));
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_NoWorkflowFoundException()
      throws Exception {
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    when(authorizer.authorizeWriteExistingDatasetById(metisUserView, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowDao.getWorkflow(dataset.getDatasetId())).thenReturn(null);
    assertThrows(NoWorkflowFoundException.class, () -> orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUserView, dataset.getDatasetId(), null, null, 0));
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_WorkflowIsEmpty() throws Exception {
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = new Workflow();
    workflow.setDatasetId(dataset.getDatasetId());
    when(authorizer.authorizeWriteExistingDatasetById(metisUserView, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowDao.getWorkflow(dataset.getDatasetId())).thenReturn(workflow);
    when(validationUtils.validateWorkflowPlugins(workflow, null))
        .thenThrow(new BadContentException(""));
    assertThrows(BadContentException.class, () -> orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUserView, dataset.getDatasetId(), null, null, 0));
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_WorkflowExecutionAlreadyExistsException()
      throws Exception {
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    Dataset dataset = TestObjectFactory.createDataset(TestObjectFactory.DATASETNAME);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(authorizer.authorizeWriteExistingDatasetById(metisUserView, dataset.getDatasetId()))
        .thenReturn(dataset);
    when(workflowDao.getWorkflow(dataset.getDatasetId())).thenReturn(workflow);
    when(redissonClient.getFairLock(anyString())).thenReturn(Mockito.mock(RLock.class));
    when(workflowExecutionDao.existsAndNotCompleted(dataset.getDatasetId()))
        .thenReturn(new ObjectId().toString());
    assertThrows(WorkflowExecutionAlreadyExistsException.class, () -> orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUserView, dataset.getDatasetId(), null, null, 0));
  }

  @Test
  void cancelWorkflowExecution() throws Exception {
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    when(workflowExecutionDao.getById(TestObjectFactory.EXECUTIONID)).thenReturn(workflowExecution);
    doNothing().when(workflowExecutionDao).setCancellingState(workflowExecution, null);
    orchestratorService.cancelWorkflowExecution(metisUserView, TestObjectFactory.EXECUTIONID);
    verify(authorizer, times(1))
        .authorizeWriteExistingDatasetById(metisUserView, workflowExecution.getDatasetId());
    verifyNoMoreInteractions(authorizer);
  }

  @Test
  void cancelWorkflowExecution_NoWorkflowExecutionFoundException() {
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(workflowExecutionDao.getById(TestObjectFactory.EXECUTIONID)).thenReturn(null);
    assertThrows(NoWorkflowExecutionFoundException.class, () -> orchestratorService
        .cancelWorkflowExecution(metisUserView, TestObjectFactory.EXECUTIONID));
    verifyNoMoreInteractions(workflowExecutorManager);
  }

  @Test
  void getWorkflowExecutionsPerRequest() {
    orchestratorService.getWorkflowExecutionsPerRequest();
    verify(workflowExecutionDao, times(1)).getWorkflowExecutionsPerRequest();
  }

  @Test
  void getLatestSuccessfulFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution_ProcessPlugin()
      throws Exception {
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final AbstractExecutablePlugin oaipmhHarvestPlugin = ExecutablePluginFactory
        .createPlugin(new OaipmhHarvestPluginMetadata());
    when(authorizer.authorizeReadExistingDatasetById(metisUserView, datasetId)).thenReturn(null);
    doReturn(new PluginWithExecutionId<>("execution ID", oaipmhHarvestPlugin))
        .when(dataEvolutionUtils)
        .computePredecessorPlugin(ExecutablePluginType.VALIDATION_EXTERNAL, null, datasetId);
    assertSame(oaipmhHarvestPlugin, orchestratorService
        .getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(metisUserView, datasetId,
            ExecutablePluginType.VALIDATION_EXTERNAL, null));
    verify(authorizer, times(1)).authorizeReadExistingDatasetById(metisUserView, datasetId);
    verifyNoMoreInteractions(authorizer);
  }

  @Test
  void getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution_PluginExecutionNotAllowed()
      throws NoDatasetFoundException, UserUnauthorizedException, PluginExecutionNotAllowed {
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    when(authorizer.authorizeReadExistingDatasetById(metisUserView, datasetId)).thenReturn(null);
    when(dataEvolutionUtils.computePredecessorPlugin(ExecutablePluginType.VALIDATION_EXTERNAL, null,
        datasetId)).thenThrow(new PluginExecutionNotAllowed(""));
    assertThrows(PluginExecutionNotAllowed.class, () -> orchestratorService
        .getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(metisUserView,
            datasetId, ExecutablePluginType.VALIDATION_EXTERNAL, null));
  }

  @Test
  void getAllWorkflowExecutionsByDatasetId() throws GenericMetisException {

    // Define some constants
    final int nextPage = 1;
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    final Set<WorkflowStatus> workflowStatuses = Collections.singleton(WorkflowStatus.INQUEUE);

    // Check with specific dataset ID: should query only that dataset.
    doReturn(new ResultList<>(Collections.emptyList(), false)).when(workflowExecutionDao)
        .getAllWorkflowExecutions(any(), any(), any(), anyBoolean(), anyInt(), anyInt(), anyBoolean());
    orchestratorService.getAllWorkflowExecutions(metisUserView, datasetId, workflowStatuses,
        DaoFieldNames.ID, false, nextPage);
    verify(authorizer, times(1)).authorizeReadExistingDatasetById(metisUserView, datasetId);
    verifyNoMoreInteractions(authorizer);
    verify(workflowExecutionDao, times(1)).getAllWorkflowExecutions(
        eq(Collections.singleton(datasetId)), eq(workflowStatuses), eq(DaoFieldNames.ID), eq(false),
        eq(nextPage), eq(1), eq(false));
    verify(workflowExecutionDao, times(1)).getWorkflowExecutionsPerRequest();
    verifyNoMoreInteractions(workflowExecutionDao);
  }

  @Test
  void getAllWorkflowExecutionsForRegularUser() throws GenericMetisException {

    // Define some constants
    final int nextPage = 1;
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final Set<String> datasetIds = new HashSet<>(Arrays.asList("A", "B", "C"));
    final List<Dataset> datasets = datasetIds.stream().map(id -> {
      final Dataset result = new Dataset();
      result.setDatasetId(id);
      return result;
    }).toList();
    final Set<WorkflowStatus> workflowStatuses = Collections.singleton(WorkflowStatus.INQUEUE);

    // Check for all datasets and for regular user: should query all datasets to which that user's
    // organization has rights.
    when(datasetDao.getAllDatasetsByOrganizationId(metisUserView.getOrganizationId()))
        .thenReturn(datasets);
    doReturn(new ResultList<>(Collections.emptyList(), false)).when(workflowExecutionDao)
        .getAllWorkflowExecutions(any(), any(), any(), anyBoolean(), anyInt(), anyInt(), anyBoolean());
    orchestratorService.getAllWorkflowExecutions(metisUserView, null, workflowStatuses,
        DaoFieldNames.CREATED_DATE, false, nextPage);
    verify(authorizer, times(1)).authorizeReadAllDatasets(metisUserView);
    verifyNoMoreInteractions(authorizer);
    verify(workflowExecutionDao, times(1)).getAllWorkflowExecutions(eq(datasetIds),
        eq(workflowStatuses), eq(DaoFieldNames.CREATED_DATE), eq(false), eq(nextPage), eq(1), eq(false));
    verify(workflowExecutionDao, times(1)).getWorkflowExecutionsPerRequest();
    verifyNoMoreInteractions(workflowExecutionDao);
  }

  @Test
  void getAllWorkflowExecutionsForAdmin() throws GenericMetisException {

    // Define some constants
    final int nextPage = 1;
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final Set<WorkflowStatus> workflowStatuses = Collections.singleton(WorkflowStatus.INQUEUE);

    // Check for all datasets and for admin user: should query all datasets.
    doReturn(AccountRole.METIS_ADMIN).when(metisUserView).getAccountRole();
    doReturn(new ResultList<>(Collections.emptyList(), false)).when(workflowExecutionDao)
        .getAllWorkflowExecutions(any(), any(), any(), anyBoolean(), anyInt(), anyInt(), anyBoolean());
    orchestratorService.getAllWorkflowExecutions(metisUserView, null, workflowStatuses,
        DaoFieldNames.CREATED_DATE, true, nextPage);
    verify(authorizer, times(1)).authorizeReadAllDatasets(metisUserView);
    verifyNoMoreInteractions(authorizer);
    verify(workflowExecutionDao, times(1)).getAllWorkflowExecutions(isNull(), eq(workflowStatuses),
        eq(DaoFieldNames.CREATED_DATE), eq(true), eq(nextPage), eq(1), eq(false));
    verify(workflowExecutionDao, times(1)).getWorkflowExecutionsPerRequest();
    verifyNoMoreInteractions(workflowExecutionDao);
  }

  @Test
  void getWorkflowExecutionOverviewForRegularUser() throws GenericMetisException {

    // Define some constants
    final int nextPage = 1;
    final int pageCount = 2;
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final Set<String> datasetIds = new HashSet<>(Arrays.asList("A", "B", "C"));
    final List<Dataset> datasets = datasetIds.stream().map(id -> {
      final Dataset result = new Dataset();
      result.setDatasetId(id);
      return result;
    }).toList();
    final List<ExecutionDatasetPair> data = TestObjectFactory.createExecutionsWithDatasets(4);

    // Check for all datasets and for regular user: should query all datasets to which that user's
    // organization has rights.
    when(datasetDao.getAllDatasetsByOrganizationId(metisUserView.getOrganizationId()))
        .thenReturn(datasets);
    when(workflowExecutionDao
        .getWorkflowExecutionsOverview(eq(datasetIds), isNull(), isNull(), isNull(), isNull(),
            eq(nextPage), eq(pageCount)))
        .thenReturn(new ResultList<>(data, false));
    final List<ExecutionAndDatasetView> result = orchestratorService
        .getWorkflowExecutionsOverview(metisUserView, null, null, null, null, nextPage, pageCount)
        .getResults();
    verify(authorizer, times(1)).authorizeReadAllDatasets(metisUserView);
    verifyNoMoreInteractions(authorizer);
    verify(workflowExecutionDao, times(1))
        .getWorkflowExecutionsOverview(eq(datasetIds), isNull(), isNull(), isNull(), isNull(),
            eq(nextPage), eq(pageCount));
    verify(workflowExecutionDao, times(1)).getWorkflowExecutionsPerRequest();
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
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final List<ExecutionDatasetPair> data = TestObjectFactory.createExecutionsWithDatasets(4);

    // Check for all datasets and for admin user: should query all datasets.
    doReturn(AccountRole.METIS_ADMIN).when(metisUserView).getAccountRole();
    when(workflowExecutionDao
        .getWorkflowExecutionsOverview(isNull(), isNull(), isNull(), isNull(), isNull(),
            eq(nextPage), eq(pageCount)))
        .thenReturn(new ResultList<>(data, false));
    final List<ExecutionAndDatasetView> result = orchestratorService
        .getWorkflowExecutionsOverview(metisUserView, null, null, null, null, nextPage, pageCount)
        .getResults();
    verify(authorizer, times(1)).authorizeReadAllDatasets(metisUserView);
    verifyNoMoreInteractions(authorizer);
    verify(workflowExecutionDao, times(1))
        .getWorkflowExecutionsOverview(isNull(), isNull(), isNull(), isNull(), isNull(),
            eq(nextPage), eq(pageCount));
    verify(workflowExecutionDao, times(1)).getWorkflowExecutionsPerRequest();
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
    ExecutionProgress executionProgress = getExecutionProgress(100, 20);
    final Date longEnoughToBeValidDate = DateUtils.modifyDateByTimeUnitAmount(new Date(), -(SOLR_COMMIT_PERIOD_IN_MINS + 3),
        TimeUnit.MINUTES);
    final Date notLongEnoughToBeValidDate = DateUtils.modifyDateByTimeUnitAmount(new Date(), -(SOLR_COMMIT_PERIOD_IN_MINS + 2),
        TimeUnit.MINUTES);


    // Create preview plugin
    AbstractExecutablePlugin<IndexToPreviewPluginMetadata> previewPlugin = ExecutablePluginFactory
        .createPlugin(new IndexToPreviewPluginMetadata());
    previewPlugin.setFinishedDate(longEnoughToBeValidDate);
    previewPlugin.setDataStatus(null); // Is default status, means valid.
    previewPlugin.setExecutionProgress(executionProgress);

    // Create second publish plugin
    AbstractExecutablePlugin<IndexToPublishPluginMetadata> lastPublishPlugin = ExecutablePluginFactory
        .createPlugin(new IndexToPublishPluginMetadata());
    lastPublishPlugin.setFinishedDate(notLongEnoughToBeValidDate);
    lastPublishPlugin.setDataStatus(null); // Is default status, means valid.
    lastPublishPlugin.setExecutionProgress(executionProgress);

    boolean enableRunningPublish = true;
    getDatasetExecutionInformation(previewPlugin, lastPublishPlugin, enableRunningPublish, true, false);
    previewPlugin.getExecutionProgress().setTotalDatabaseRecords(100);
    lastPublishPlugin.getExecutionProgress().setTotalDatabaseRecords(100);
    lastPublishPlugin.setFinishedDate(longEnoughToBeValidDate);
    enableRunningPublish = false;
    getDatasetExecutionInformation(previewPlugin, lastPublishPlugin, enableRunningPublish, true, true);
    previewPlugin.getExecutionProgress().setTotalDatabaseRecords(0);
    lastPublishPlugin.getExecutionProgress().setTotalDatabaseRecords(0);
    getDatasetExecutionInformation(previewPlugin, lastPublishPlugin, enableRunningPublish, false, false);
  }

  private void getDatasetExecutionInformation(
      AbstractExecutablePlugin<IndexToPreviewPluginMetadata> previewPlugin,
      AbstractExecutablePlugin<IndexToPublishPluginMetadata> lastPublishPlugin, boolean enableRunningPublish,
      boolean previewReadyForViewing,
      boolean publishReadyForViewing) throws GenericMetisException {
    ExecutionProgress executionProgress = getExecutionProgress(100, 20);

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
    final WorkflowExecution executionWithFirstPublishPlugin = TestObjectFactory
        .createWorkflowExecutionObject();
    final List<AbstractMetisPlugin> metisPluginsFirstPublish = executionWithFirstPublishPlugin
        .getMetisPlugins();
    metisPluginsFirstPublish.add(firstPublishPlugin);
    executionWithFirstPublishPlugin.setMetisPlugins(metisPluginsFirstPublish);

    final WorkflowExecution executionWithLastPublishPlugin = TestObjectFactory
        .createWorkflowExecutionObject();
    final List<AbstractMetisPlugin> metisPluginsLastPublish = executionWithLastPublishPlugin
        .getMetisPlugins();
    metisPluginsLastPublish.add(lastPublishPlugin);
    executionWithLastPublishPlugin.setMetisPlugins(metisPluginsLastPublish);

    // Create reindex to preview plugin
    AbstractMetisPlugin reindexToPreviewPlugin = new ReindexToPreviewPlugin(
        new ReindexToPreviewPluginMetadata());
    reindexToPreviewPlugin.setFinishedDate(
        DateUtils.modifyDateByTimeUnitAmount(new Date(), -(SOLR_COMMIT_PERIOD_IN_MINS + 1),
            TimeUnit.MINUTES));
    final WorkflowExecution executionWithReindexToPreview = TestObjectFactory
        .createWorkflowExecutionObject();
    executionWithReindexToPreview.setMetisPlugins(List.of(reindexToPreviewPlugin));

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
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = Integer.toString(TestObjectFactory.DATASETID);
    when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
        EnumSet.of(ExecutablePluginType.HTTP_HARVEST, ExecutablePluginType.OAIPMH_HARVEST), false))
        .thenReturn(new PluginWithExecutionId<>("", oaipmhHarvestPlugin));
    when(workflowExecutionDao.getFirstSuccessfulPlugin(datasetId,
        EnumSet.of(PluginType.PUBLISH, PluginType.REINDEX_TO_PUBLISH)))
        .thenReturn(new PluginWithExecutionId<>(
            executionWithFirstPublishPlugin.getId().toString(), firstPublishPlugin));
    when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
        EnumSet.of(ExecutablePluginType.PREVIEW), false))
        .thenReturn(new PluginWithExecutionId<>("", previewPlugin));
    when(workflowExecutionDao.getLatestSuccessfulPlugin(datasetId,
        EnumSet.of(PluginType.PREVIEW, PluginType.REINDEX_TO_PREVIEW)))
        .thenReturn(
            new PluginWithExecutionId<>(executionWithReindexToPreview.getId().toString(),
                reindexToPreviewPlugin));
    when(workflowExecutionDao.getLatestSuccessfulExecutablePlugin(datasetId,
        EnumSet.of(ExecutablePluginType.PUBLISH), false))
        .thenReturn(new PluginWithExecutionId<>("", lastPublishPlugin));
    when(workflowExecutionDao.getLatestSuccessfulPlugin(datasetId,
        EnumSet.of(PluginType.PUBLISH, PluginType.REINDEX_TO_PUBLISH)))
        .thenReturn(new PluginWithExecutionId<>(
            executionWithLastPublishPlugin.getId().toString(), lastPublishPlugin));
    if (enableRunningPublish) {
      when(workflowExecutionDao.getRunningOrInQueueExecution(datasetId))
          .thenReturn(workflowExecutionObject);
    } else {
      when(workflowExecutionDao.getRunningOrInQueueExecution(datasetId)).thenReturn(null);
    }

    DatasetExecutionInformation executionInfo = orchestratorService
        .getDatasetExecutionInformation(metisUserView, datasetId);

    verify(authorizer, times(1)).authorizeReadExistingDatasetById(metisUserView, datasetId);
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

    assertEquals(previewReadyForViewing, executionInfo.isLastPreviewRecordsReadyForViewing());
    assertEquals(publishReadyForViewing, executionInfo.isLastPublishedRecordsReadyForViewing());
  }

  @NotNull
  private ExecutionProgress getExecutionProgress(int processedRecords, int errors) {
    // Create execution progress object
    ExecutionProgress executionProgress = new ExecutionProgress();
    executionProgress.setProcessedRecords(processedRecords);
    executionProgress.setErrors(errors);
    return executionProgress;
  }

  @Test
  void testGetDatasetExecutionHistory() throws GenericMetisException {

    // Create plugins
    final AbstractExecutablePlugin plugin1 = mock(AbstractExecutablePlugin.class);
    when(plugin1.getPluginType()).thenReturn(PluginType.OAIPMH_HARVEST);
    when(plugin1.getPluginMetadata()).thenReturn(new HTTPHarvestPluginMetadata());
    final ExecutionProgress progress1 = getExecutionProgress(10, 1);
    when(plugin1.getExecutionProgress()).thenReturn(progress1);
    final AbstractExecutablePlugin plugin2 = mock(AbstractExecutablePlugin.class);
    final ExecutionProgress progress2 = new ExecutionProgress();
    when(plugin2.getPluginType()).thenReturn(PluginType.TRANSFORMATION);
    when(plugin2.getPluginMetadata()).thenReturn(new TransformationPluginMetadata());
    progress2.setProcessedRecords(10);
    progress2.setErrors(10);
    when(plugin2.getExecutionProgress()).thenReturn(progress2);
    final AbstractExecutablePlugin plugin3 = mock(AbstractExecutablePlugin.class);
    when(plugin3.getPluginType()).thenReturn(PluginType.MEDIA_PROCESS);
    when(plugin2.getPluginMetadata()).thenReturn(new MediaProcessPluginMetadata());
    when(plugin3.getExecutionProgress()).thenReturn(null);
    final ReindexToPreviewPlugin plugin4 = mock(ReindexToPreviewPlugin.class);
    when(plugin4.getPluginType()).thenReturn(PluginType.REINDEX_TO_PUBLISH);
    when(plugin4.getFinishedDate()).thenReturn(new Date(4));

    // Create other objects
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = "dataset ID";
    final WorkflowExecution execution1 = createWorkflowExecution(metisUserView, datasetId, plugin1,
        plugin2);
    execution1.setStartedDate(new Date(12345));
    final WorkflowExecution execution2 = createWorkflowExecution(metisUserView, datasetId, plugin3);
    final WorkflowExecution execution3 = createWorkflowExecution(metisUserView, datasetId, plugin4);

    // Mock the dao and call the method.
    doReturn(new ResultList<>(List.of(execution1, execution2, execution3), false))
        .when(workflowExecutionDao).getAllWorkflowExecutions(any(), any(), any(), anyBoolean(),
            anyInt(), any(), anyBoolean());
    final ExecutionHistory result = orchestratorService.getDatasetExecutionHistory(metisUserView, datasetId);

    // Verify the interactions
    verify(authorizer, times(1)).authorizeReadExistingDatasetById(metisUserView, datasetId);
    verifyNoMoreInteractions(authorizer);
    verify(workflowExecutionDao, times(1)).getAllWorkflowExecutions(
        eq(Collections.singleton(datasetId)), isNull(), eq(DaoFieldNames.STARTED_DATE), eq(false),
        eq(0), isNull(), eq(false));
    verifyNoMoreInteractions(workflowExecutionDao);

    // Verify the result
    assertEquals(1, result.getExecutions().size());
    assertEquals(execution1.getId().toString(),
        result.getExecutions().get(0).getWorkflowExecutionId());
    assertEquals(execution1.getStartedDate(), result.getExecutions().get(0).getStartedDate());
  }

  @Test
  void testGetExecutablePluginsWithDataAvailability() throws GenericMetisException {

    // Create plugins
    final AbstractExecutablePlugin plugin1 = mock(AbstractExecutablePlugin.class);
    when(plugin1.getPluginType()).thenReturn(PluginType.OAIPMH_HARVEST);
    when(plugin1.getPluginMetadata()).thenReturn(new HTTPHarvestPluginMetadata());
    final ExecutionProgress progress1 = getExecutionProgress(10, 1);
    when(plugin1.getExecutionProgress()).thenReturn(progress1);
    final AbstractExecutablePlugin plugin2 = mock(AbstractExecutablePlugin.class);
    final ExecutionProgress progress2 = new ExecutionProgress();
    when(plugin2.getPluginType()).thenReturn(PluginType.TRANSFORMATION);
    when(plugin2.getPluginMetadata()).thenReturn(new TransformationPluginMetadata());
    progress2.setProcessedRecords(10);
    progress2.setErrors(10);
    when(plugin2.getExecutionProgress()).thenReturn(progress2);
    final AbstractExecutablePlugin plugin3 = mock(AbstractExecutablePlugin.class);
    when(plugin3.getPluginType()).thenReturn(PluginType.MEDIA_PROCESS);
    when(plugin2.getPluginMetadata()).thenReturn(new MediaProcessPluginMetadata());
    when(plugin3.getExecutionProgress()).thenReturn(null);
    final ReindexToPreviewPlugin plugin4 = mock(ReindexToPreviewPlugin.class);
    when(plugin4.getPluginType()).thenReturn(PluginType.REINDEX_TO_PUBLISH);
    when(plugin4.getFinishedDate()).thenReturn(new Date(4));

    // Create other objects
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = "dataset ID";
    final WorkflowExecution execution = createWorkflowExecution(metisUserView, datasetId, plugin1,
        plugin2, plugin3, plugin4);
    final String workflowExecutionId = execution.getId().toString();

    // Test happy flow
    final PluginsWithDataAvailability result = orchestratorService
        .getExecutablePluginsWithDataAvailability(metisUserView, workflowExecutionId);
    assertNotNull(result);
    assertNotNull(result.getPlugins());
    assertEquals(1, result.getPlugins().size());
    assertEquals(plugin1.getPluginType(), result.getPlugins().get(0).getPluginType());
    assertTrue(result.getPlugins().get(0).isCanDisplayRawXml());

    // Test when the workflow execution does not exist
    doReturn(null).when(orchestratorService)
        .getWorkflowExecutionByExecutionId(metisUserView, workflowExecutionId);
    assertThrows(NoWorkflowExecutionFoundException.class, () -> orchestratorService
        .getExecutablePluginsWithDataAvailability(metisUserView, workflowExecutionId));

    // Test when the user is not allowed
    when(orchestratorService.getWorkflowExecutionByExecutionId(metisUserView, workflowExecutionId))
        .thenAnswer(invocation -> {
          throw new UserUnauthorizedException("");
        });
    assertThrows(UserUnauthorizedException.class, () -> orchestratorService
        .getExecutablePluginsWithDataAvailability(metisUserView, workflowExecutionId));
  }

  @Test
  void testGetRecordEvolutionForVersionExceptions() throws GenericMetisException {

    // Create some objects
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String workflowExecutionId = "workflow execution ID";
    final PluginType pluginType = PluginType.MEDIA_PROCESS;
    final WorkflowExecution workflowExecution = mock(WorkflowExecution.class);

    // Test when the workflow execution does not exist
    when(orchestratorService.getWorkflowExecutionByExecutionId(metisUserView, workflowExecutionId))
        .thenReturn(null);
    assertThrows(NoWorkflowExecutionFoundException.class, () -> orchestratorService
        .getRecordEvolutionForVersion(metisUserView, workflowExecutionId, pluginType));

    // Test when the user is not allowed
    when(orchestratorService.getWorkflowExecutionByExecutionId(metisUserView, workflowExecutionId))
        .thenAnswer(invocation -> {
          throw new UserUnauthorizedException("");
        });
    assertThrows(UserUnauthorizedException.class, () -> orchestratorService
        .getRecordEvolutionForVersion(metisUserView, workflowExecutionId, pluginType));

    // Test when the workflow execution does not have a plugin of the right type
    doReturn(workflowExecution).when(orchestratorService)
        .getWorkflowExecutionByExecutionId(metisUserView, workflowExecutionId);
    when(workflowExecution.getMetisPluginWithType(pluginType)).thenReturn(Optional.empty());
    assertThrows(NoWorkflowExecutionFoundException.class, () -> orchestratorService
        .getRecordEvolutionForVersion(metisUserView, workflowExecutionId, pluginType));
  }

  @Test
  void testGetRecordEvolutionForVersionHappyFlow() throws GenericMetisException {

    // Create two workflow executions with three plugins and link them together
    final MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final String datasetId = "dataset ID";
    final AbstractExecutablePlugin plugin1 = createMetisPlugin(ExecutablePluginType.OAIPMH_HARVEST,
        new Date(1));
    final AbstractExecutablePlugin plugin2 = createMetisPlugin(ExecutablePluginType.TRANSFORMATION,
        new Date(2));
    final AbstractExecutablePlugin plugin3 = createMetisPlugin(ExecutablePluginType.MEDIA_PROCESS,
        new Date(3));
    final WorkflowExecution execution1 = createWorkflowExecution(metisUserView, datasetId, plugin1);
    final WorkflowExecution execution2 = createWorkflowExecution(metisUserView, datasetId, plugin2,
        plugin3);

    // Mock the methods in workflow utils.
    final List<Pair<AbstractExecutablePlugin, WorkflowExecution>> evolutionWithContent = Arrays
        .asList(
            ImmutablePair.of(plugin1, execution1), ImmutablePair.of(plugin2, execution2));
    doReturn(evolutionWithContent).when(dataEvolutionUtils).compileVersionEvolution(plugin3, execution2);
    doReturn(new ArrayList<>()).when(dataEvolutionUtils).compileVersionEvolution(plugin1, execution1);

    // Execute the call and expect an evolution with content.
    final VersionEvolution resultForThree = orchestratorService.getRecordEvolutionForVersion(
        metisUserView, execution2.getId().toString(), plugin3.getPluginType());
    assertNotNull(resultForThree);
    assertNotNull(resultForThree.getEvolutionSteps());
    assertEquals(2, resultForThree.getEvolutionSteps().size());
    assertEvolutionStepEquals(resultForThree.getEvolutionSteps().get(0), execution1, plugin1);
    assertEvolutionStepEquals(resultForThree.getEvolutionSteps().get(1), execution2, plugin2);

    // Execute the call and expect an evolution without content.
    final VersionEvolution resultForOne = orchestratorService.getRecordEvolutionForVersion(
        metisUserView, execution1.getId().toString(), plugin1.getPluginType());
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

  private WorkflowExecution createWorkflowExecution(MetisUserView metisUserView, String datasetId,
                                                    AbstractMetisPlugin... plugins) throws GenericMetisException {
    final WorkflowExecution result = new WorkflowExecution();
    result.setId(new ObjectId());
    result.setDatasetId(datasetId);
    result.setMetisPlugins(Arrays.asList(plugins));
    when(orchestratorService.getWorkflowExecutionByExecutionId(metisUserView,
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
