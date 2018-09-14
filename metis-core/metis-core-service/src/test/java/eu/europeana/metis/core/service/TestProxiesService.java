package eu.europeana.metis.core.service;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
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
import eu.europeana.metis.core.rest.RecordsResponse;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.core.workflow.plugins.Topology;
import eu.europeana.metis.exception.ExternalTaskException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-26
 */
public class TestProxiesService {

  private static final long EXTERNAL_TASK_ID = 2070373127078497810L;
  
  private static ProxiesService proxiesService;
  private static WorkflowExecutionDao workflowExecutionDao;
  private static DpsClient dpsClient;
  private static DataSetServiceClient ecloudDataSetServiceClient;
  private static RecordServiceClient recordServiceClient;
  private static FileServiceClient fileServiceClient;
  private static Authorizer authorizer;

  @BeforeClass
  public static void prepare() {
    workflowExecutionDao = mock(WorkflowExecutionDao.class);
    ecloudDataSetServiceClient = mock(DataSetServiceClient.class);
    recordServiceClient = mock(RecordServiceClient.class);
    fileServiceClient = mock(FileServiceClient.class);
    dpsClient = mock(DpsClient.class);
    authorizer = mock(Authorizer.class);

    proxiesService = new ProxiesService(workflowExecutionDao, ecloudDataSetServiceClient,
        recordServiceClient, fileServiceClient, dpsClient, "ecloudProvider", authorizer);
  }

  @After
  public void cleanUp() {
    reset(workflowExecutionDao);
    reset(ecloudDataSetServiceClient);
    reset(recordServiceClient);
    reset(fileServiceClient);
    reset(dpsClient);
    reset(authorizer);
  }

  @Test
  public void getExternalTaskLogs() throws Exception {
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
    verify(authorizer, times(1)).authorizeReadExistingDatasetById(metisUser, workflowExecution.getDatasetId());
    verifyNoMoreInteractions(authorizer);
    Assert.assertEquals(2, listOfSubTaskInfo.size());
    Assert.assertNull(listOfSubTaskInfo.get(0).getAdditionalInformations());
    Assert.assertNull(listOfSubTaskInfo.get(1).getAdditionalInformations());
  }

  @Test(expected = NoWorkflowExecutionFoundException.class)
  public void getExternalTaskLogs_NoExecutionException() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(workflowExecutionDao.getByExternalTaskId(EXTERNAL_TASK_ID)).thenReturn(null);
    proxiesService.getExternalTaskLogs(metisUser, Topology.OAIPMH_HARVEST.getTopologyName(),
        EXTERNAL_TASK_ID, 1, 100);
  }

  @Test(expected = ExternalTaskException.class)
  public void getExternalTaskLogs_ExternalTaskException() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(dpsClient
        .getDetailedTaskReportBetweenChunks(Topology.OAIPMH_HARVEST.getTopologyName(),
            EXTERNAL_TASK_ID, 1, 100)).thenThrow(new DpsException());
    final WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    when(workflowExecutionDao.getByExternalTaskId(EXTERNAL_TASK_ID)).thenReturn(workflowExecution);
    proxiesService.getExternalTaskLogs(metisUser, Topology.OAIPMH_HARVEST.getTopologyName(),
        EXTERNAL_TASK_ID, 1, 100);
  }

  @Test
  public void getExternalTaskReport() throws Exception {
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
    verify(authorizer, times(1)).authorizeReadExistingDatasetById(metisUser, workflowExecution.getDatasetId());
    verifyNoMoreInteractions(authorizer);

    Assert.assertEquals(1, externalTaskReport.getErrors().size());
    Assert.assertFalse(externalTaskReport.getErrors().get(0).getErrorDetails().isEmpty());
  }

  @Test(expected = NoWorkflowExecutionFoundException.class)
  public void getExternalTaskReport_NoExecutionException() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(workflowExecutionDao.getByExternalTaskId(EXTERNAL_TASK_ID)).thenReturn(null);
    proxiesService.getExternalTaskReport(metisUser, Topology.OAIPMH_HARVEST.getTopologyName(),
        TestObjectFactory.EXTERNAL_TASK_ID, 10);
  }

  @Test(expected = ExternalTaskException.class)
  public void getExternalTaskReport_ExternalTaskException() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(dpsClient
        .getTaskErrorsReport(Topology.OAIPMH_HARVEST.getTopologyName(),
            TestObjectFactory.EXTERNAL_TASK_ID, null, 10))
        .thenThrow(new DpsException());
    final WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    when(workflowExecutionDao.getByExternalTaskId(EXTERNAL_TASK_ID)).thenReturn(workflowExecution);
    proxiesService.getExternalTaskReport(metisUser, Topology.OAIPMH_HARVEST.getTopologyName(),
        TestObjectFactory.EXTERNAL_TASK_ID, 10);
  }

  @Test
  public void getExternalTaskStatistics() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    final StatisticsReport taskStatistics = TestObjectFactory.createTaskStatisticsReport();
    when(dpsClient.getTaskStatisticsReport(Topology.OAIPMH_HARVEST.getTopologyName(),
        TestObjectFactory.EXTERNAL_TASK_ID)).thenReturn(taskStatistics);
    final WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    when(workflowExecutionDao.getByExternalTaskId(EXTERNAL_TASK_ID)).thenReturn(workflowExecution);
    final StatisticsReport externalTaskStatistics = proxiesService.getExternalTaskStatistics(metisUser, 
        Topology.OAIPMH_HARVEST.getTopologyName(), TestObjectFactory.EXTERNAL_TASK_ID);
    verify(authorizer, times(1)).authorizeReadExistingDatasetById(metisUser, workflowExecution.getDatasetId());
    verifyNoMoreInteractions(authorizer);
    assertNotNull(externalTaskStatistics);
    Assert.assertEquals(TestObjectFactory.EXTERNAL_TASK_ID, externalTaskStatistics.getTaskId());
    Assert.assertFalse(externalTaskStatistics.getNodeStatistics().isEmpty());
  }

  @Test(expected = NoWorkflowExecutionFoundException.class)
  public void getExternalTaskStatistics_NoExecutionException() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(workflowExecutionDao.getByExternalTaskId(EXTERNAL_TASK_ID)).thenReturn(null);
    proxiesService.getExternalTaskStatistics(metisUser, Topology.OAIPMH_HARVEST.getTopologyName(),
        TestObjectFactory.EXTERNAL_TASK_ID);
  }

  @Test(expected = ExternalTaskException.class)
  public void getExternalTaskStatistics_ExternalTaskException() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(dpsClient.getTaskStatisticsReport(Topology.OAIPMH_HARVEST.getTopologyName(),
        TestObjectFactory.EXTERNAL_TASK_ID)).thenThrow(new DpsException());
    final WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    when(workflowExecutionDao.getByExternalTaskId(EXTERNAL_TASK_ID)).thenReturn(workflowExecution);
    proxiesService.getExternalTaskStatistics(metisUser, Topology.OAIPMH_HARVEST.getTopologyName(),
        TestObjectFactory.EXTERNAL_TASK_ID);
  }

  @Test
  public void getListOfFileContentsFromPluginExecution() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    WorkflowExecution workflowExecutionObject = TestObjectFactory.createWorkflowExecutionObject();
    workflowExecutionObject.getMetisPlugins()
        .forEach(abstractMetisPlugin -> abstractMetisPlugin.setStartedDate(new Date()));
    when(workflowExecutionDao.getById(TestObjectFactory.EXECUTIONID))
        .thenReturn(workflowExecutionObject);

    ResultSlice<CloudTagsResponse> resultSlice = new ResultSlice<>();
    String ecloudId = "ECLOUDID1";
    CloudTagsResponse cloudTagsResponse = new CloudTagsResponse(ecloudId, false, false, false);
    resultSlice.setResults(Collections.singletonList(cloudTagsResponse));
    when(ecloudDataSetServiceClient
        .getDataSetRevisionsChunk(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), isNull(), anyInt())).thenReturn(resultSlice);

    Representation representation = mock(Representation.class);
    ArrayList<File> files = new ArrayList<>();
    File file = new File();
    file.setContentUri(URI.create("http://example.com"));
    files.add(file);
    when(representation.getFiles()).thenReturn(files);
    when(recordServiceClient.getRepresentationByRevision(anyString(), anyString(),
        anyString(), anyString(), anyString())).thenReturn(representation);

    String xmlRecord = "<rdf:RDF><edm:ProvidedCHO rdf:about=\"/some/path1\"></edm:ProvidedCHO></rdf:RDF>";
    InputStream stubInputStream = IOUtils.toInputStream(xmlRecord,
        StandardCharsets.UTF_8.name());
    when(fileServiceClient
        .getFile(representation.getFiles().get(0).getContentUri().toString()))
        .thenReturn(stubInputStream);

    RecordsResponse listOfFileContentsFromPluginExecution =
        proxiesService.getListOfFileContentsFromPluginExecution(metisUser,
            TestObjectFactory.EXECUTIONID, PluginType.OAIPMH_HARVEST, null, 5);
    verify(authorizer, times(1)).authorizeReadExistingDatasetById(metisUser, workflowExecutionObject.getDatasetId());
    verifyNoMoreInteractions(authorizer);

    Assert.assertEquals(xmlRecord, listOfFileContentsFromPluginExecution.getRecords().get(0).getXmlRecord());
    Assert.assertEquals(ecloudId, listOfFileContentsFromPluginExecution.getRecords().get(0).getEcloudId());
  }

  @Test(expected = NoWorkflowExecutionFoundException.class)
  public void getListOfFileContentsFromPluginExecution_NoExecutionException() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(workflowExecutionDao.getById(TestObjectFactory.EXECUTIONID)).thenReturn(null);
    proxiesService.getListOfFileContentsFromPluginExecution(metisUser,
        TestObjectFactory.EXECUTIONID, PluginType.OAIPMH_HARVEST, null, 5);
  }

  @Test(expected = ExternalTaskException.class)
  public void getListOfFileContentsFromPluginExecution_FileReadException() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    WorkflowExecution workflowExecutionObject = TestObjectFactory.createWorkflowExecutionObject();
    workflowExecutionObject.getMetisPlugins()
        .forEach(abstractMetisPlugin -> abstractMetisPlugin.setStartedDate(new Date()));
    when(workflowExecutionDao.getById(TestObjectFactory.EXECUTIONID))
        .thenReturn(workflowExecutionObject);

    ResultSlice<CloudTagsResponse> resultSlice = new ResultSlice<>();
    String ecloudId = "ECLOUDID1";
    CloudTagsResponse cloudTagsResponse = new CloudTagsResponse(ecloudId, false, false, false);
    resultSlice.setResults(Collections.singletonList(cloudTagsResponse));
    when(ecloudDataSetServiceClient
        .getDataSetRevisionsChunk(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), isNull(), anyInt())).thenReturn(resultSlice);

    Representation representation = mock(Representation.class);
    ArrayList<File> files = new ArrayList<>();
    File file = new File();
    file.setContentUri(URI.create("http://example.com"));
    files.add(file);
    when(representation.getFiles()).thenReturn(files);
    when(recordServiceClient.getRepresentationByRevision(anyString(), anyString(),
        anyString(), anyString(), anyString())).thenReturn(representation);

    when(fileServiceClient
        .getFile(representation.getFiles().get(0).getContentUri().toString()))
        .thenThrow(new IOException("Cannot read file"));

    proxiesService.getListOfFileContentsFromPluginExecution(metisUser, TestObjectFactory.EXECUTIONID,
            PluginType.OAIPMH_HARVEST, null, 5);
  }

  @Test(expected = ExternalTaskException.class)
  public void getListOfFileContentsFromPluginExecution_ExceptionRequestingRevisions() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    WorkflowExecution workflowExecutionObject = TestObjectFactory.createWorkflowExecutionObject();
    workflowExecutionObject.getMetisPlugins()
        .forEach(abstractMetisPlugin -> abstractMetisPlugin.setStartedDate(new Date()));
    when(workflowExecutionDao.getById(TestObjectFactory.EXECUTIONID))
        .thenReturn(workflowExecutionObject);

    ResultSlice<CloudTagsResponse> resultSlice = new ResultSlice<>();
    String ecloudId = "ECLOUDID1";
    CloudTagsResponse cloudTagsResponse = new CloudTagsResponse(ecloudId, false, false, false);
    resultSlice.setResults(Collections.singletonList(cloudTagsResponse));
    when(ecloudDataSetServiceClient
        .getDataSetRevisionsChunk(anyString(), anyString(), anyString(), anyString(),
            anyString(), anyString(), isNull(), anyInt())).thenThrow(new MCSException("Chunk cannot be retrieved"));


    proxiesService.getListOfFileContentsFromPluginExecution(metisUser, TestObjectFactory.EXECUTIONID,
        PluginType.OAIPMH_HARVEST, null, 5);
  }

}
