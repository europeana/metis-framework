package eu.europeana.metis.core.rest;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.core.IsNull;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import eu.europeana.cloud.common.model.dps.StatisticsReport;
import eu.europeana.cloud.common.model.dps.SubTaskInfo;
import eu.europeana.cloud.common.model.dps.TaskErrorsInfo;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.authentication.rest.client.AuthenticationClient;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.rest.exception.RestResponseExceptionHandler;
import eu.europeana.metis.core.service.ProxiesService;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.plugins.PluginType;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-26
 */
public class TestProxiesController {

  private static ProxiesService proxiesService;
  private static AuthenticationClient authenticationClient;
  private static MockMvc proxiesControllerMock;

  @BeforeClass
  public static void setUp() {
    proxiesService = mock(ProxiesService.class);
    authenticationClient = mock(AuthenticationClient.class);
    ProxiesController proxiesController = new ProxiesController(proxiesService, authenticationClient);
    proxiesControllerMock = MockMvcBuilders
        .standaloneSetup(proxiesController)
        .setControllerAdvice(new RestResponseExceptionHandler())
        .build();
  }

  @Test
  public void getExternalTaskLogs() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    
    int from = 1;
    int to = 100;
    List<SubTaskInfo> listOfSubTaskInfo = TestObjectFactory.createListOfSubTaskInfo();
    for (SubTaskInfo subTaskInfo : listOfSubTaskInfo) {
      subTaskInfo.setAdditionalInformations(null);
    }
    when(proxiesService.getExternalTaskLogs(metisUser, TestObjectFactory.TOPOLOGY_NAME,
        TestObjectFactory.EXTERNAL_TASK_ID, from, to)).thenReturn(listOfSubTaskInfo);

    proxiesControllerMock.perform(
        get(RestEndpoints.ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_LOGS,
            TestObjectFactory.TOPOLOGY_NAME, TestObjectFactory.EXTERNAL_TASK_ID)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .param("from", Integer.toString(from))
            .param("to", Integer.toString(to))
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$[0].additionalInformations", is(IsNull.nullValue())))
        .andExpect(jsonPath("$[1].additionalInformations", is(IsNull.nullValue())));
  }

  @Test
  public void getExternalTaskReport() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    
    List<SubTaskInfo> listOfSubTaskInfo = TestObjectFactory.createListOfSubTaskInfo();
    for (SubTaskInfo subTaskInfo : listOfSubTaskInfo) {
      subTaskInfo.setAdditionalInformations(null);
    }

    TaskErrorsInfo taskErrorsInfo = TestObjectFactory.createTaskErrorsInfoListWithIdentifiers(2);
    when(proxiesService.getExternalTaskReport(metisUser, TestObjectFactory.TOPOLOGY_NAME,
        TestObjectFactory.EXTERNAL_TASK_ID, 10)).thenReturn(taskErrorsInfo);

    proxiesControllerMock.perform(
        get(RestEndpoints.ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_REPORT,
            TestObjectFactory.TOPOLOGY_NAME, TestObjectFactory.EXTERNAL_TASK_ID)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .param("idsPerError", "10")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.id", is(TestObjectFactory.EXTERNAL_TASK_ID)))
        .andExpect(jsonPath("$.errors", hasSize(taskErrorsInfo.getErrors().size())))
        .andExpect(jsonPath("$.errors[0].errorDetails",
            hasSize(taskErrorsInfo.getErrors().get(0).getErrorDetails().size())))
        .andExpect(jsonPath("$.errors[1].errorDetails",
            hasSize(taskErrorsInfo.getErrors().get(1).getErrorDetails().size())));
  }
  
  @Test
  public void getExternalTaskStatistics() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    
    final StatisticsReport taskStatistics = TestObjectFactory.createTaskStatisticsReport();
    when(proxiesService.getExternalTaskStatistics(metisUser, TestObjectFactory.TOPOLOGY_NAME,
        TestObjectFactory.EXTERNAL_TASK_ID)).thenReturn(taskStatistics);
    proxiesControllerMock.perform(
        get(RestEndpoints.ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_STATISTICS,
            TestObjectFactory.TOPOLOGY_NAME, TestObjectFactory.EXTERNAL_TASK_ID)
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8).content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.taskId", is(TestObjectFactory.EXTERNAL_TASK_ID)))
        .andExpect(jsonPath("$.nodeStatistics", hasSize(taskStatistics.getNodeStatistics().size())));
  }

  @Test
  public void getListOfFileContentsFromPluginExecution() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    
    ArrayList<Record> records = new ArrayList<>();
    Record record1 = new Record("ECLOUDID1",
        "<rdf:RDF><edm:ProvidedCHO rdf:about=\"/some/path1\"></edm:ProvidedCHO></rdf:RDF>");
    Record record2 = new Record("ECLOUDID2",
        "<rdf:RDF><edm:ProvidedCHO rdf:about=\"/some/path2\"></edm:ProvidedCHO></rdf:RDF>");
    records.add(record1);
    records.add(record2);
    RecordsResponse recordsResponse = new RecordsResponse(records, null);

    when(proxiesService.getListOfFileContentsFromPluginExecution(metisUser,
        TestObjectFactory.EXECUTIONID, PluginType.TRANSFORMATION, null, 5))
            .thenReturn(recordsResponse);

    proxiesControllerMock.perform(
        get(RestEndpoints.ORCHESTRATOR_PROXIES_RECORDS)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .param("workflowExecutionId", TestObjectFactory.EXECUTIONID)
            .param("pluginType", PluginType.TRANSFORMATION.name())
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.records[0].ecloudId", is(record1.getEcloudId())))
        .andExpect(jsonPath("$.records[0].xmlRecord", is(record1.getXmlRecord())))
        .andExpect(jsonPath("$.records[1].ecloudId", is(record2.getEcloudId())))
        .andExpect(jsonPath("$.records[1].xmlRecord", is(record2.getXmlRecord())));
  }

}
