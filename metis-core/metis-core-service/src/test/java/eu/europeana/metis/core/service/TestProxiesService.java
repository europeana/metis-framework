package eu.europeana.metis.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.common.model.File;
import eu.europeana.cloud.common.model.Representation;
import eu.europeana.cloud.common.model.dps.StatisticsReport;
import eu.europeana.cloud.common.model.dps.SubTaskInfo;
import eu.europeana.cloud.common.model.dps.TaskErrorsInfo;
import eu.europeana.cloud.common.response.CloudTagsResponse;
import eu.europeana.cloud.common.response.ResultSlice;
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.cloud.mcs.driver.FileServiceClient;
import eu.europeana.cloud.mcs.driver.RecordServiceClient;
import eu.europeana.cloud.service.dps.exception.DpsException;
import eu.europeana.cloud.service.mcs.exception.MCSException;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException;
import eu.europeana.metis.core.rest.ListOfIds;
import eu.europeana.metis.core.rest.PaginatedRecordsResponse;
import eu.europeana.metis.core.rest.Record;
import eu.europeana.metis.core.rest.RecordsResponse;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.core.workflow.plugins.Topology;
import eu.europeana.metis.exception.ExternalTaskException;
import eu.europeana.metis.exception.GenericMetisException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-26
 */
class TestProxiesService {

  private static final long EXTERNAL_TASK_ID = 2070373127078497810L;

  private static ProxiesService proxiesService;
  private static WorkflowExecutionDao workflowExecutionDao;
  private static DpsClient dpsClient;
  private static DataSetServiceClient ecloudDataSetServiceClient;
  private static RecordServiceClient recordServiceClient;
  private static FileServiceClient fileServiceClient;
  private static Authorizer authorizer;

  @BeforeAll
  static void prepare() {
    workflowExecutionDao = mock(WorkflowExecutionDao.class);
    ecloudDataSetServiceClient = mock(DataSetServiceClient.class);
    recordServiceClient = mock(RecordServiceClient.class);
    fileServiceClient = mock(FileServiceClient.class);
    dpsClient = mock(DpsClient.class);
    authorizer = mock(Authorizer.class);

    proxiesService = spy(new ProxiesService(workflowExecutionDao, ecloudDataSetServiceClient,
        recordServiceClient, fileServiceClient, dpsClient, "ecloudProvider", authorizer));
  }

  @AfterEach
  void cleanUp() {
    reset(workflowExecutionDao);
    reset(ecloudDataSetServiceClient);
    reset(recordServiceClient);
    reset(fileServiceClient);
    reset(dpsClient);
    reset(authorizer);
    reset(proxiesService);
  }

  @Test
  void getExternalTaskLogs() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    List<SubTaskInfo> listOfSubTaskInfo = TestObjectFactory.createListOfSubTaskInfo();

    when(dpsClient
        .getDetailedTaskReportBetweenChunks(Topology.OAIPMH_HARVEST.getTopologyName(),
            EXTERNAL_TASK_ID,
            1, 100)).thenReturn(listOfSubTaskInfo);
    final WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    when(workflowExecutionDao.getByExternalTaskId(EXTERNAL_TASK_ID)).thenReturn(workflowExecution);
    proxiesService.getExternalTaskLogs(metisUser, Topology.OAIPMH_HARVEST.getTopologyName(),
        EXTERNAL_TASK_ID, 1, 100);
    verify(authorizer, times(1))
        .authorizeReadExistingDatasetById(metisUser, workflowExecution.getDatasetId());
    verifyNoMoreInteractions(authorizer);
    assertEquals(2, listOfSubTaskInfo.size());
    assertNull(listOfSubTaskInfo.get(0).getAdditionalInformations());
    assertNull(listOfSubTaskInfo.get(1).getAdditionalInformations());
  }

  @Test
  void getExternalTaskLogs_NoExecutionException() {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(workflowExecutionDao.getByExternalTaskId(EXTERNAL_TASK_ID)).thenReturn(null);
    assertThrows(NoWorkflowExecutionFoundException.class, () -> proxiesService
        .getExternalTaskLogs(metisUser, Topology.OAIPMH_HARVEST.getTopologyName(),
            EXTERNAL_TASK_ID, 1, 100));
  }

  @Test
  void getExternalTaskLogs_ExternalTaskException() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(dpsClient
        .getDetailedTaskReportBetweenChunks(Topology.OAIPMH_HARVEST.getTopologyName(),
            EXTERNAL_TASK_ID, 1, 100)).thenThrow(new DpsException());
    final WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    when(workflowExecutionDao.getByExternalTaskId(EXTERNAL_TASK_ID)).thenReturn(workflowExecution);
    assertThrows(ExternalTaskException.class, () -> proxiesService
        .getExternalTaskLogs(metisUser, Topology.OAIPMH_HARVEST.getTopologyName(),
            EXTERNAL_TASK_ID, 1, 100));
  }

  @Test
  void existsExternalTaskReport() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);

    when(dpsClient.checkIfErrorReportExists(Topology.OAIPMH_HARVEST.getTopologyName(),
        TestObjectFactory.EXTERNAL_TASK_ID)).thenReturn(true);
    final WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    when(workflowExecutionDao.getByExternalTaskId(EXTERNAL_TASK_ID)).thenReturn(workflowExecution);

    final boolean existsExternalTaskReport = proxiesService.existsExternalTaskReport(metisUser,
        Topology.OAIPMH_HARVEST.getTopologyName(), TestObjectFactory.EXTERNAL_TASK_ID);
    verify(authorizer, times(1))
        .authorizeReadExistingDatasetById(metisUser, workflowExecution.getDatasetId());
    verifyNoMoreInteractions(authorizer);

    assertTrue(existsExternalTaskReport);
  }

  @Test
  void getExternalTaskReport() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    TaskErrorsInfo taskErrorsInfo = TestObjectFactory.createTaskErrorsInfoListWithoutIdentifiers(2);
    TaskErrorsInfo taskErrorsInfoWithIdentifiers = TestObjectFactory
        .createTaskErrorsInfoWithIdentifiers(taskErrorsInfo.getErrors().get(0).getErrorType(),
            taskErrorsInfo.getErrors().get(0).getMessage());

    when(dpsClient
        .getTaskErrorsReport(Topology.OAIPMH_HARVEST.getTopologyName(),
            TestObjectFactory.EXTERNAL_TASK_ID, null, 10))
        .thenReturn(taskErrorsInfoWithIdentifiers);
    final WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    when(workflowExecutionDao.getByExternalTaskId(EXTERNAL_TASK_ID)).thenReturn(workflowExecution);

    TaskErrorsInfo externalTaskReport = proxiesService.getExternalTaskReport(metisUser,
        Topology.OAIPMH_HARVEST.getTopologyName(), TestObjectFactory.EXTERNAL_TASK_ID, 10);
    verify(authorizer, times(1))
        .authorizeReadExistingDatasetById(metisUser, workflowExecution.getDatasetId());
    verifyNoMoreInteractions(authorizer);

    assertEquals(1, externalTaskReport.getErrors().size());
    assertFalse(externalTaskReport.getErrors().get(0).getErrorDetails().isEmpty());
  }

  @Test
  void getExternalTaskReport_NoExecutionException() {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(workflowExecutionDao.getByExternalTaskId(EXTERNAL_TASK_ID)).thenReturn(null);
    assertThrows(NoWorkflowExecutionFoundException.class, () -> proxiesService
        .getExternalTaskReport(metisUser, Topology.OAIPMH_HARVEST.getTopologyName(),
            TestObjectFactory.EXTERNAL_TASK_ID, 10));
  }

  @Test
  void getExternalTaskReport_ExternalTaskException() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(dpsClient
        .getTaskErrorsReport(Topology.OAIPMH_HARVEST.getTopologyName(),
            TestObjectFactory.EXTERNAL_TASK_ID, null, 10))
        .thenThrow(new DpsException());
    final WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    when(workflowExecutionDao.getByExternalTaskId(EXTERNAL_TASK_ID)).thenReturn(workflowExecution);
    assertThrows(ExternalTaskException.class, () -> proxiesService
        .getExternalTaskReport(metisUser, Topology.OAIPMH_HARVEST.getTopologyName(),
            TestObjectFactory.EXTERNAL_TASK_ID, 10));
  }

  @Test
  void getExternalTaskStatistics() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final StatisticsReport taskStatistics = TestObjectFactory.createTaskStatisticsReport();
    when(dpsClient.getTaskStatisticsReport(Topology.OAIPMH_HARVEST.getTopologyName(),
        TestObjectFactory.EXTERNAL_TASK_ID)).thenReturn(taskStatistics);
    final WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    when(workflowExecutionDao.getByExternalTaskId(EXTERNAL_TASK_ID)).thenReturn(workflowExecution);
    final StatisticsReport externalTaskStatistics = proxiesService
        .getExternalTaskStatistics(metisUser,
            Topology.OAIPMH_HARVEST.getTopologyName(), TestObjectFactory.EXTERNAL_TASK_ID);
    verify(authorizer, times(1))
        .authorizeReadExistingDatasetById(metisUser, workflowExecution.getDatasetId());
    verifyNoMoreInteractions(authorizer);
    assertNotNull(externalTaskStatistics);
    assertEquals(TestObjectFactory.EXTERNAL_TASK_ID, externalTaskStatistics.getTaskId());
    assertFalse(externalTaskStatistics.getNodeStatistics().isEmpty());
  }

  @Test
  void getExternalTaskStatistics_NoExecutionException() {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(workflowExecutionDao.getByExternalTaskId(EXTERNAL_TASK_ID)).thenReturn(null);
    assertThrows(NoWorkflowExecutionFoundException.class, () -> proxiesService
        .getExternalTaskStatistics(metisUser, Topology.OAIPMH_HARVEST.getTopologyName(),
            TestObjectFactory.EXTERNAL_TASK_ID));
  }

  @Test
  void getExternalTaskStatistics_ExternalTaskException() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(dpsClient.getTaskStatisticsReport(Topology.OAIPMH_HARVEST.getTopologyName(),
        TestObjectFactory.EXTERNAL_TASK_ID)).thenThrow(new DpsException());
    final WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    when(workflowExecutionDao.getByExternalTaskId(EXTERNAL_TASK_ID)).thenReturn(workflowExecution);
    assertThrows(ExternalTaskException.class, () -> proxiesService
        .getExternalTaskStatistics(metisUser, Topology.OAIPMH_HARVEST.getTopologyName(),
            TestObjectFactory.EXTERNAL_TASK_ID));
  }

  @Test
  void getListOfFileContentsFromPluginExecution() throws Exception {

    // Create execution and plugin
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final WorkflowExecution execution = TestObjectFactory.createWorkflowExecutionObject();
    execution.getMetisPlugins()
        .forEach(abstractMetisPlugin -> abstractMetisPlugin.setStartedDate(new Date()));
    final AbstractMetisPlugin plugin = getUsedAndUnusedPluginType(execution).getLeft();
    doReturn(new ImmutablePair<>(execution, plugin)).when(proxiesService)
        .getExecutionAndPlugin(metisUser, TestObjectFactory.EXECUTIONID, plugin.getPluginType());
    when(workflowExecutionDao.getById(TestObjectFactory.EXECUTIONID)).thenReturn(execution);

    // Mock getting the records from eCloud.
    final ResultSlice<CloudTagsResponse> resultSlice = new ResultSlice<>();
    final String ecloudId = "ECLOUDID1";
    final CloudTagsResponse cloudTagsResponse = new CloudTagsResponse(ecloudId, false, false,
        false);
    resultSlice.setResults(Collections.singletonList(cloudTagsResponse));
    when(ecloudDataSetServiceClient
        .getDataSetRevisionsChunk(anyString(), anyString(), anyString(), anyString(), anyString(),
            anyString(), isNull(), anyInt())).thenReturn(resultSlice);

    // Mock obtaining the actual record.
    final Record record = new Record(ecloudId, "test content");
    doReturn(record).when(proxiesService).getRecord(plugin, ecloudId);

    // Execute the call.
    PaginatedRecordsResponse listOfFileContentsFromPluginExecution =
        proxiesService.getListOfFileContentsFromPluginExecution(metisUser,
            TestObjectFactory.EXECUTIONID, plugin.getPluginType(), null, 5);
    assertEquals(record.getXmlRecord(),
        listOfFileContentsFromPluginExecution.getRecords().get(0).getXmlRecord());
    assertEquals(ecloudId, listOfFileContentsFromPluginExecution.getRecords().get(0).getEcloudId());
  }

  @Test
  void getListOfFileContentsFromPluginExecution_ExceptionOfDataAvailability() throws GenericMetisException {

    // If there is no execution
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final PluginType pluginType = PluginType.OAIPMH_HARVEST;
    doThrow(NoWorkflowExecutionFoundException.class).when(proxiesService)
        .getExecutionAndPlugin(metisUser, TestObjectFactory.EXECUTIONID, pluginType);
    assertThrows(NoWorkflowExecutionFoundException.class, () -> proxiesService
        .getListOfFileContentsFromPluginExecution(metisUser, TestObjectFactory.EXECUTIONID,
            pluginType, null, 5));

    // If the user has no rights
    doThrow(UserUnauthorizedException.class).when(proxiesService)
        .getExecutionAndPlugin(metisUser, TestObjectFactory.EXECUTIONID, pluginType);
    assertThrows(UserUnauthorizedException.class, () -> proxiesService
        .getListOfFileContentsFromPluginExecution(metisUser, TestObjectFactory.EXECUTIONID,
            pluginType, null, 5));

    // If the execution does not have the plugin an empty result should be returned.
    doReturn(null).when(proxiesService)
        .getExecutionAndPlugin(metisUser, TestObjectFactory.EXECUTIONID, pluginType);
    final PaginatedRecordsResponse result = proxiesService
        .getListOfFileContentsFromPluginExecution(metisUser, TestObjectFactory.EXECUTIONID,
            pluginType, null, 5);
    assertNotNull(result);
    assertNotNull(result.getRecords());
    assertTrue(result.getRecords().isEmpty());
    assertNull(result.getNextPage());
  }

  @Test
  void getListOfFileContentsFromPluginExecution_ExceptionRequestingRevisions() throws Exception {

    // Create execution and plugin and mock relevant method getting them.
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final WorkflowExecution execution = TestObjectFactory.createWorkflowExecutionObject();
    execution.getMetisPlugins()
        .forEach(abstractMetisPlugin -> abstractMetisPlugin.setStartedDate(new Date()));
    final AbstractMetisPlugin plugin = getUsedAndUnusedPluginType(execution).getLeft();
    doReturn(new ImmutablePair<>(execution, plugin)).when(proxiesService)
        .getExecutionAndPlugin(metisUser, TestObjectFactory.EXECUTIONID, plugin.getPluginType());

    // Mock ecloud client method.
    when(ecloudDataSetServiceClient
        .getDataSetRevisionsChunk(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), isNull(), anyInt()))
        .thenThrow(new MCSException("Chunk cannot be retrieved"));

    // Check exception.
    assertThrows(ExternalTaskException.class, () -> proxiesService
        .getListOfFileContentsFromPluginExecution(metisUser, TestObjectFactory.EXECUTIONID,
            plugin.getPluginType(), null, 5));
  }

  @Test
  void testGetListOfFileContentsFromPluginExecution() throws GenericMetisException {

    // Create execution and plugin and mock relevant method getting them.
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final WorkflowExecution execution = TestObjectFactory.createWorkflowExecutionObject();
    execution.getMetisPlugins()
        .forEach(abstractMetisPlugin -> abstractMetisPlugin.setStartedDate(new Date()));
    final AbstractMetisPlugin plugin = getUsedAndUnusedPluginType(execution).getLeft();
    doReturn(new ImmutablePair<>(execution, plugin)).when(proxiesService)
        .getExecutionAndPlugin(metisUser, TestObjectFactory.EXECUTIONID, plugin.getPluginType());

    // Create the test records and the list of IDs.
    final Record record1 = new Record("ID 1", "test content 1");
    final Record record2 = new Record("ID 2", "test content 2");
    final Record record3 = new Record("ID 3", "test content 3");
    final List<String> idList = Stream.of(record1, record2, record3).map(Record::getEcloudId)
        .collect(Collectors.toList());

    // Mock the method for getting records
    doReturn(record1).when(proxiesService).getRecord(plugin, record1.getEcloudId());
    doReturn(record2).when(proxiesService).getRecord(plugin, record2.getEcloudId());
    doReturn(record3).when(proxiesService).getRecord(plugin, record3.getEcloudId());

    // Make the call - happy flow
    final ListOfIds input = new ListOfIds();
    input.setIds(idList);
    final RecordsResponse result = proxiesService
        .getListOfFileContentsFromPluginExecution(metisUser, TestObjectFactory.EXECUTIONID,
            plugin.getPluginType(), input);

    // Verify that the result contains the record in the right order
    assertNotNull(result);
    assertNotNull(result.getRecords());
    assertEquals(idList.size(),result.getRecords().size());
    assertEquals(idList,
        result.getRecords().stream().map(Record::getEcloudId).collect(Collectors.toList()));

    // Check that the call also works for an empty list
    input.setIds(Collections.emptyList());
    final RecordsResponse emptyResult = proxiesService
        .getListOfFileContentsFromPluginExecution(metisUser, TestObjectFactory.EXECUTIONID,
            plugin.getPluginType(), input);
    assertNotNull(emptyResult);
    assertNotNull(emptyResult.getRecords());
    assertTrue(emptyResult.getRecords().isEmpty());
    input.setIds(idList);

    // Check that if a record cannot be retrieved, the method fails.
    doThrow(ExternalTaskException.class).when(proxiesService)
        .getRecord(plugin, record3.getEcloudId());
    assertThrows(ExternalTaskException.class, () -> proxiesService
        .getListOfFileContentsFromPluginExecution(metisUser, TestObjectFactory.EXECUTIONID,
            plugin.getPluginType(), input));
  }

  @Test
  void testGetListOfFileContentsFromPluginExecution_ExceptionOfDataAvailability() throws GenericMetisException {

    // If there is no execution
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final PluginType pluginType = PluginType.OAIPMH_HARVEST;
    doThrow(NoWorkflowExecutionFoundException.class).when(proxiesService)
        .getExecutionAndPlugin(metisUser, TestObjectFactory.EXECUTIONID, pluginType);
    assertThrows(NoWorkflowExecutionFoundException.class, () -> proxiesService
        .getListOfFileContentsFromPluginExecution(metisUser, TestObjectFactory.EXECUTIONID,
            pluginType, new ListOfIds()));

    // If the user has no rights
    doThrow(UserUnauthorizedException.class).when(proxiesService)
        .getExecutionAndPlugin(metisUser, TestObjectFactory.EXECUTIONID, pluginType);
    assertThrows(UserUnauthorizedException.class, () -> proxiesService
        .getListOfFileContentsFromPluginExecution(metisUser, TestObjectFactory.EXECUTIONID,
            pluginType, new ListOfIds()));

    // If the execution does not have the plugin an empty result should be returned.
    doReturn(null).when(proxiesService)
        .getExecutionAndPlugin(metisUser, TestObjectFactory.EXECUTIONID, pluginType);
    assertThrows(NoWorkflowExecutionFoundException.class, () -> proxiesService
        .getListOfFileContentsFromPluginExecution(metisUser, TestObjectFactory.EXECUTIONID,
            pluginType, new ListOfIds()));
  }

  @Test
  void testGetExecutionAndPlugin() throws GenericMetisException {

    // Create a workflowExecution and get the plugin types
    final WorkflowExecution execution = TestObjectFactory.createWorkflowExecutionObject();
    final Pair<AbstractMetisPlugin, PluginType> pluginAndUnusedType = getUsedAndUnusedPluginType(execution);
    final PluginType unusedPluginType = pluginAndUnusedType.getRight();
    final AbstractMetisPlugin plugin = pluginAndUnusedType.getLeft();

    // Create a user and mock the dependency methods.
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(workflowExecutionDao.getById(TestObjectFactory.EXECUTIONID)).thenReturn(execution);
    doReturn(null).when(authorizer)
        .authorizeReadExistingDatasetById(metisUser, execution.getDatasetId());

    // Test happy flow with result
    final Pair<WorkflowExecution, AbstractMetisPlugin> result = proxiesService
        .getExecutionAndPlugin(metisUser, TestObjectFactory.EXECUTIONID, plugin.getPluginType());
    assertNotNull(result);
    assertEquals(execution, result.getLeft());
    assertNotNull(result.getRight());
    assertSame(plugin, result.getRight());
    assertTrue(execution.getMetisPlugins().contains(result.getRight()));
    verify(authorizer, times(1))
        .authorizeReadExistingDatasetById(metisUser, execution.getDatasetId());
    verifyNoMoreInteractions(authorizer);

    // Test happy flow without result
    assertNull(proxiesService
        .getExecutionAndPlugin(metisUser, TestObjectFactory.EXECUTIONID, unusedPluginType));

    // Test execution not found
    when(workflowExecutionDao.getById(TestObjectFactory.EXECUTIONID)).thenReturn(null);
    assertThrows(NoWorkflowExecutionFoundException.class, () -> proxiesService
        .getExecutionAndPlugin(metisUser, TestObjectFactory.EXECUTIONID, plugin.getPluginType()));
    when(workflowExecutionDao.getById(TestObjectFactory.EXECUTIONID)).thenReturn(execution);

    // Test unauthorized exception
    when(authorizer.authorizeReadExistingDatasetById(metisUser, execution.getDatasetId()))
        .thenThrow(UserUnauthorizedException.class);
    assertThrows(UserUnauthorizedException.class, () -> proxiesService
        .getExecutionAndPlugin(metisUser, TestObjectFactory.EXECUTIONID, plugin.getPluginType()));
    doReturn(null).when(authorizer)
        .authorizeReadExistingDatasetById(metisUser, execution.getDatasetId());
  }

  @Test
  void testGetRecord() throws MCSException, IOException, ExternalTaskException {

    // Create representation
    final Representation representation = mock(Representation.class);
    final String contentUri = "http://example.com";
    final File file = new File();
    file.setContentUri(URI.create(contentUri));
    when(representation.getFiles()).thenReturn(Collections.singletonList(file));

    // Create plugin
    final PluginType pluginType = PluginType.MEDIA_PROCESS;
    final AbstractMetisPlugin plugin = mock(AbstractMetisPlugin.class);
    when(plugin.getPluginType()).thenReturn(pluginType);
    when(plugin.getStartedDate()).thenReturn(new Date());

    // Configure mocks
    final String ecloudId = "ecloud ID";
    final String ecloudProvider = proxiesService.getEcloudProvider();
    final String dateString = proxiesService.pluginDateFormatForEcloud
        .format(plugin.getStartedDate());
    doReturn(representation).when(recordServiceClient).getRepresentationByRevision(
        eq(ecloudId), eq(AbstractMetisPlugin.getRepresentationName()),
        eq(pluginType.name()), eq(ecloudProvider), eq(dateString));
    final String testContent = "test content";
    when(fileServiceClient.getFile(contentUri))
        .thenReturn(new ByteArrayInputStream(testContent.getBytes(StandardCharsets.UTF_8)));

    // Test happy flow
    final Record result = proxiesService.getRecord(plugin, ecloudId);
    assertNotNull(result);
    assertEquals(ecloudId, result.getEcloudId());
    assertEquals(testContent, result.getXmlRecord());

    // When the file service client returns an exception
    when(fileServiceClient.getFile(contentUri)).thenThrow(new IOException("Cannot read file"));
    assertThrows(ExternalTaskException.class, () -> proxiesService.getRecord(plugin, ecloudId));
    doReturn(new ByteArrayInputStream(testContent.getBytes(StandardCharsets.UTF_8)))
        .when(fileServiceClient).getFile(contentUri);

    // When the record service client returns an exception
    when(recordServiceClient.getRepresentationByRevision(anyString(), anyString(),
        anyString(), anyString(), anyString())).thenThrow(MCSException.class);
    assertThrows(ExternalTaskException.class, () -> proxiesService.getRecord(plugin, ecloudId));
  }

  private Pair<AbstractMetisPlugin, PluginType> getUsedAndUnusedPluginType(
      WorkflowExecution execution) {
    final Set<PluginType> usedPluginTypes = execution.getMetisPlugins().stream()
        .map(AbstractMetisPlugin::getPluginType).collect(Collectors.toSet());
    final PluginType usedPluginType = usedPluginTypes.stream().findAny()
        .orElseThrow(IllegalStateException::new);
    final PluginType unusedPluginType = Stream.of(PluginType.values())
        .filter(type -> !usedPluginTypes.contains(type)).findAny()
        .orElseThrow(IllegalStateException::new);
    return new ImmutablePair<>(
        execution.getMetisPluginWithType(usedPluginType).orElseThrow(IllegalStateException::new),
        unusedPluginType);
  }
}
