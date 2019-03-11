package eu.europeana.metis.core.rest;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.cloud.common.model.dps.StatisticsReport;
import eu.europeana.cloud.common.model.dps.SubTaskInfo;
import eu.europeana.cloud.common.model.dps.TaskErrorsInfo;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.authentication.rest.client.AuthenticationClient;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException;
import eu.europeana.metis.core.rest.VersionEvolution.VersionEvolutionStep;
import eu.europeana.metis.core.rest.exception.RestResponseExceptionHandler;
import eu.europeana.metis.core.service.ProxiesService;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.exception.UserUnauthorizedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-26
 */
class TestProxiesController {

  private static ProxiesService proxiesService;
  private static AuthenticationClient authenticationClient;
  private static MockMvc proxiesControllerMock;

  @BeforeAll
  static void setUp() {
    proxiesService = mock(ProxiesService.class);
    authenticationClient = mock(AuthenticationClient.class);
    ProxiesController proxiesController = new ProxiesController(proxiesService,
        authenticationClient);
    proxiesControllerMock = MockMvcBuilders
        .standaloneSetup(proxiesController)
        .setControllerAdvice(new RestResponseExceptionHandler())
        .build();
  }

  @Test
  void getExternalTaskLogs() throws Exception {
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
  void existsExternalTaskReport() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);

    when(proxiesService.existsExternalTaskReport(metisUser, TestObjectFactory.TOPOLOGY_NAME,
        TestObjectFactory.EXTERNAL_TASK_ID)).thenReturn(true);

    proxiesControllerMock.perform(
        get(RestEndpoints.ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_REPORT_EXISTS,
            TestObjectFactory.TOPOLOGY_NAME, TestObjectFactory.EXTERNAL_TASK_ID)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.existsExternalTaskReport", is(true)));
  }

  @Test
  void getExternalTaskReport() throws Exception {
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
  void getExternalTaskStatistics() throws Exception {
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
        .andExpect(
            jsonPath("$.nodeStatistics", hasSize(taskStatistics.getNodeStatistics().size())));
  }

  @Test
  void getListOfFileContentsFromPluginExecution() throws Exception {
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
    PaginatedRecordsResponse recordsResponse = new PaginatedRecordsResponse(records, null);

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

  @Test
  void testGetRecordEvolutionForVersion() throws Exception {

    // Get the user
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);

    // Create nonempty ID list and result list.
    final Record record1 = new Record("ID 1", "content 1");
    final Record record2 = new Record("ID 2", "content 2");
    final RecordsResponse output = new RecordsResponse(Arrays.asList(record1, record2));
    final List<String> expectedInput = output.getRecords().stream().map(Record::getEcloudId)
        .collect(Collectors.toList());

    // Test happy flow with non-empty ID list
    final PluginType pluginType = PluginType.MEDIA_PROCESS;
    doAnswer(invocation -> {
      final ListOfIds input = invocation.getArgument(3);
      assertEquals(expectedInput, input.getIds());
      return output;
    }).when(proxiesService).getListOfFileContentsFromPluginExecution(same(metisUser),
        eq(TestObjectFactory.EXECUTIONID), eq(pluginType), any());
    proxiesControllerMock
        .perform(post(RestEndpoints.ORCHESTRATOR_PROXIES_RECORDS_BY_IDS)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .param("workflowExecutionId", TestObjectFactory.EXECUTIONID)
            .param("pluginType", pluginType.name())
            .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content("{\"ids\":[\"" + String.join("\",\"", expectedInput) + "\"]}"))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.records", hasSize(2)))
        .andExpect(jsonPath("$.records[0].ecloudId", is(record1.getEcloudId())))
        .andExpect(jsonPath("$.records[0].xmlRecord", is(record1.getXmlRecord())))
        .andExpect(jsonPath("$.records[1].ecloudId", is(record2.getEcloudId())))
        .andExpect(jsonPath("$.records[1].xmlRecord", is(record2.getXmlRecord())));

    // Test happy flow with empty ID list
    final RecordsResponse emptyOutput = new RecordsResponse(Collections.emptyList());
    doAnswer(invocation -> {
      final ListOfIds input = invocation.getArgument(3);
      assertTrue(input.getIds().isEmpty());
      return emptyOutput;
    }).when(proxiesService).getListOfFileContentsFromPluginExecution(same(metisUser),
        eq(TestObjectFactory.EXECUTIONID), eq(pluginType), any());
    proxiesControllerMock
        .perform(post(RestEndpoints.ORCHESTRATOR_PROXIES_RECORDS_BY_IDS)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .param("workflowExecutionId", TestObjectFactory.EXECUTIONID)
            .param("pluginType", pluginType.name())
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content("{\"ids\":[]}"))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.records", hasSize(0)));
    proxiesControllerMock
        .perform(post(RestEndpoints.ORCHESTRATOR_PROXIES_RECORDS_BY_IDS)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .param("workflowExecutionId", TestObjectFactory.EXECUTIONID)
            .param("pluginType", pluginType.name())
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content("{}"))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.records", hasSize(0)));

    // Test for bad input
    when(proxiesService.getListOfFileContentsFromPluginExecution(same(metisUser),
        eq(TestObjectFactory.EXECUTIONID), eq(pluginType), any()))
        .thenThrow(new NoWorkflowExecutionFoundException(""));
    proxiesControllerMock
        .perform(post(RestEndpoints.ORCHESTRATOR_PROXIES_RECORDS_BY_IDS)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .param("workflowExecutionId", TestObjectFactory.EXECUTIONID)
            .param("pluginType", pluginType.name())
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content("{}"))
        .andExpect(status().is(404));

    // Test for unauthorized user
    doThrow(new UserUnauthorizedException("")).when(proxiesService)
        .getListOfFileContentsFromPluginExecution(same(metisUser),
            eq(TestObjectFactory.EXECUTIONID), eq(pluginType), any());
    proxiesControllerMock
        .perform(post(RestEndpoints.ORCHESTRATOR_PROXIES_RECORDS_BY_IDS)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .param("workflowExecutionId", TestObjectFactory.EXECUTIONID)
            .param("pluginType", pluginType.name())
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content("{}"))
        .andExpect(status().is(401));
  }
}
