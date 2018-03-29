package eu.europeana.metis.core.service;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
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
import org.mockito.Mockito;
import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.common.model.File;
import eu.europeana.cloud.common.model.dps.StatisticsReport;
import eu.europeana.cloud.common.model.dps.SubTaskInfo;
import eu.europeana.cloud.common.model.dps.TaskErrorsInfo;
import eu.europeana.cloud.common.response.CloudTagsResponse;
import eu.europeana.cloud.common.response.RepresentationRevisionResponse;
import eu.europeana.cloud.common.response.ResultSlice;
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.cloud.mcs.driver.FileServiceClient;
import eu.europeana.cloud.mcs.driver.RecordServiceClient;
import eu.europeana.cloud.service.dps.exception.DpsException;
import eu.europeana.cloud.service.mcs.exception.MCSException;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.rest.RecordsResponse;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.core.workflow.plugins.Topology;
import eu.europeana.metis.exception.ExternalTaskException;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-26
 */
public class TestProxiesService {

  private static ProxiesService proxiesService;
  private static WorkflowExecutionDao workflowExecutionDao;
  private static DpsClient dpsClient;
  private static DataSetServiceClient ecloudDataSetServiceClient;
  private static RecordServiceClient recordServiceClient;
  private static FileServiceClient fileServiceClient;

  @BeforeClass
  public static void prepare() {
    workflowExecutionDao = Mockito.mock(WorkflowExecutionDao.class);
    ecloudDataSetServiceClient = Mockito.mock(DataSetServiceClient.class);
    recordServiceClient = Mockito.mock(RecordServiceClient.class);
    fileServiceClient = Mockito.mock(FileServiceClient.class);
    dpsClient = Mockito.mock(DpsClient.class);

    proxiesService = new ProxiesService(workflowExecutionDao, ecloudDataSetServiceClient,
        recordServiceClient, fileServiceClient, dpsClient, "ecloudProvider");
  }

  @After
  public void cleanUp() {
    Mockito.reset(workflowExecutionDao);
    Mockito.reset(ecloudDataSetServiceClient);
    Mockito.reset(recordServiceClient);
    Mockito.reset(fileServiceClient);
    Mockito.reset(dpsClient);
  }

  @Test
  public void getExternalTaskLogs() throws Exception {
    List<SubTaskInfo> listOfSubTaskInfo = TestObjectFactory.createListOfSubTaskInfo();

    when(dpsClient
        .getDetailedTaskReportBetweenChunks(Topology.OAIPMH_HARVEST.getTopologyName(),
            2070373127078497810L,
            1, 100)).thenReturn(listOfSubTaskInfo);
    proxiesService
        .getExternalTaskLogs(Topology.OAIPMH_HARVEST.getTopologyName(), 2070373127078497810L, 1,
            100);
    Assert.assertEquals(2, listOfSubTaskInfo.size());
    Assert.assertTrue(listOfSubTaskInfo.get(0).getAdditionalInformations() == null);
    Assert.assertTrue(listOfSubTaskInfo.get(1).getAdditionalInformations() == null);
  }

  @Test(expected = ExternalTaskException.class)
  public void getExternalTaskLogs_ExternalTaskException() throws Exception {
    when(dpsClient
        .getDetailedTaskReportBetweenChunks(Topology.OAIPMH_HARVEST.getTopologyName(),
            2070373127078497810L, 1, 100)).thenThrow(new DpsException());
    proxiesService
        .getExternalTaskLogs(Topology.OAIPMH_HARVEST.getTopologyName(), 2070373127078497810L, 1,
            100);
  }

  @Test
  public void getExternalTaskReport() throws Exception {
    TaskErrorsInfo taskErrorsInfo = TestObjectFactory.createTaskErrorsInfoListWithoutIdentifiers(2);
    TaskErrorsInfo taskErrorsInfoWithIdentifiers = TestObjectFactory
        .createTaskErrorsInfoWithIdentifiers(taskErrorsInfo.getErrors().get(0).getErrorType(),
            taskErrorsInfo.getErrors().get(0).getMessage());

    when(dpsClient
        .getTaskErrorsReport(Topology.OAIPMH_HARVEST.getTopologyName(),
            TestObjectFactory.EXTERNAL_TASK_ID, null, 10))
        .thenReturn(taskErrorsInfoWithIdentifiers);

    TaskErrorsInfo externalTaskReport = proxiesService
        .getExternalTaskReport(Topology.OAIPMH_HARVEST.getTopologyName(),
            TestObjectFactory.EXTERNAL_TASK_ID, 10);

    Assert.assertEquals(1, externalTaskReport.getErrors().size());
    Assert.assertTrue(externalTaskReport.getErrors().get(0).getErrorDetails().size() != 0);
  }

  @Test(expected = ExternalTaskException.class)
  public void getExternalTaskReport_ExternalTaskException() throws Exception {
    when(dpsClient
        .getTaskErrorsReport(Topology.OAIPMH_HARVEST.getTopologyName(),
            TestObjectFactory.EXTERNAL_TASK_ID, null, 10))
        .thenThrow(new DpsException());
    proxiesService
        .getExternalTaskReport(Topology.OAIPMH_HARVEST.getTopologyName(),
            TestObjectFactory.EXTERNAL_TASK_ID, 10);
  }

  @Test
  public void getExternalTaskStatistics() throws Exception {
    final StatisticsReport taskStatistics = TestObjectFactory.createTaskStatisticsReport();
    when(dpsClient.getTaskStatisticsReport(Topology.OAIPMH_HARVEST.getTopologyName(),
        TestObjectFactory.EXTERNAL_TASK_ID)).thenReturn(taskStatistics);
    final StatisticsReport externalTaskStatistics = proxiesService.getExternalTaskStatistics(
        Topology.OAIPMH_HARVEST.getTopologyName(), TestObjectFactory.EXTERNAL_TASK_ID);
    assertNotNull(externalTaskStatistics);
    Assert.assertEquals(TestObjectFactory.EXTERNAL_TASK_ID, externalTaskStatistics.getTaskId());
    Assert.assertTrue(externalTaskStatistics.getNodeStatistics().size() != 0);
  }

  @Test(expected = ExternalTaskException.class)
  public void getExternalTaskStatistics_ExternalTaskException() throws Exception {
    when(dpsClient.getTaskStatisticsReport(Topology.OAIPMH_HARVEST.getTopologyName(),
        TestObjectFactory.EXTERNAL_TASK_ID)).thenThrow(new DpsException());
    proxiesService.getExternalTaskStatistics(Topology.OAIPMH_HARVEST.getTopologyName(),
        TestObjectFactory.EXTERNAL_TASK_ID);
  }

  @Test
  public void getListOfFileContentsFromPluginExecution() throws Exception {
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

    RepresentationRevisionResponse representationRevisionResponse = Mockito
        .mock(RepresentationRevisionResponse.class);
    ArrayList<File> files = new ArrayList<>();
    File file = new File();
    file.setContentUri(URI.create("http://example.com"));
    files.add(file);
    when(representationRevisionResponse.getFiles()).thenReturn(files);
    when(recordServiceClient.getRepresentationRevision(anyString(), anyString(),
        anyString(), anyString(), anyString())).thenReturn(representationRevisionResponse);

    String xmlRecord = "<rdf:RDF><edm:ProvidedCHO rdf:about=\"/some/path1\"></edm:ProvidedCHO></rdf:RDF>";
    InputStream stubInputStream = IOUtils.toInputStream(xmlRecord,
        StandardCharsets.UTF_8.name());
    when(fileServiceClient
        .getFile(representationRevisionResponse.getFiles().get(0).getContentUri().toString()))
        .thenReturn(stubInputStream);

    RecordsResponse listOfFileContentsFromPluginExecution = proxiesService
        .getListOfFileContentsFromPluginExecution(TestObjectFactory.EXECUTIONID,
            PluginType.OAIPMH_HARVEST, null, 5);

    Assert.assertEquals(xmlRecord, listOfFileContentsFromPluginExecution.getRecords().get(0).getXmlRecord());
    Assert.assertEquals(ecloudId, listOfFileContentsFromPluginExecution.getRecords().get(0).getEcloudId());
  }


  @Test(expected = ExternalTaskException.class)
  public void getListOfFileContentsFromPluginExecution_FileReadException() throws Exception {
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

    RepresentationRevisionResponse representationRevisionResponse = Mockito
        .mock(RepresentationRevisionResponse.class);
    ArrayList<File> files = new ArrayList<>();
    File file = new File();
    file.setContentUri(URI.create("http://example.com"));
    files.add(file);
    when(representationRevisionResponse.getFiles()).thenReturn(files);
    when(recordServiceClient.getRepresentationRevision(anyString(), anyString(),
        anyString(), anyString(), anyString())).thenReturn(representationRevisionResponse);

    when(fileServiceClient
        .getFile(representationRevisionResponse.getFiles().get(0).getContentUri().toString()))
        .thenThrow(new IOException("Cannot read file"));

    proxiesService.getListOfFileContentsFromPluginExecution(TestObjectFactory.EXECUTIONID,
            PluginType.OAIPMH_HARVEST, null, 5);
  }

  @Test(expected = ExternalTaskException.class)
  public void getListOfFileContentsFromPluginExecution_ExceptionRequestingRevisions() throws Exception {
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


    proxiesService.getListOfFileContentsFromPluginExecution(TestObjectFactory.EXECUTIONID,
        PluginType.OAIPMH_HARVEST, null, 5);
  }

}
