package eu.europeana.metis.core.rest;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.authentication.rest.client.AuthenticationClient;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.common.DaoFieldNames;
import eu.europeana.metis.core.dataset.DatasetExecutionInformation;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowFoundException;
import eu.europeana.metis.core.exceptions.WorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.WorkflowExecutionAlreadyExistsException;
import eu.europeana.metis.core.rest.ExecutionHistory.Execution;
import eu.europeana.metis.core.rest.PluginsWithDataAvailability.PluginWithDataAvailability;
import eu.europeana.metis.core.rest.VersionEvolution.VersionEvolutionStep;
import eu.europeana.metis.core.rest.exception.RestResponseExceptionHandler;
import eu.europeana.metis.core.rest.execution.overview.ExecutionAndDatasetView;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.utils.TestObjectFactory;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginFactory;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.core.workflow.plugins.ValidationExternalPluginMetadata;
import eu.europeana.metis.exception.UserUnauthorizedException;
import eu.europeana.metis.utils.TestUtils;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-06
 */
class TestOrchestratorController {

  private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
  static {
    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  private static OrchestratorService orchestratorService;
  private static MockMvc orchestratorControllerMock;
  private static AuthenticationClient authenticationClient;

  @BeforeAll
  static void setUp() {
    orchestratorService = mock(OrchestratorService.class);
    authenticationClient = mock(AuthenticationClient.class);
    OrchestratorController orchestratorController =
        new OrchestratorController(orchestratorService, authenticationClient);
    orchestratorControllerMock = MockMvcBuilders
        .standaloneSetup(orchestratorController)
        .setControllerAdvice(new RestResponseExceptionHandler())
        .setMessageConverters(new MappingJackson2HttpMessageConverter(),
            new MappingJackson2XmlHttpMessageConverter(),
            new StringHttpMessageConverter(StandardCharsets.UTF_8))
        .build();
  }

  @AfterEach
  void cleanUp() {
    Mockito.reset(orchestratorService, authenticationClient);
  }

  @Test
  void createWorkflow() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID,
        Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(201))
        .andExpect(content().string(""));

    verify(orchestratorService, times(1))
        .createWorkflow(eq(metisUser), anyString(), any(Workflow.class), isNull());
  }

  @Test
  void createWorkflow_Unauthenticated() throws Exception {
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID,
        Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));

    verify(orchestratorService, never()).createWorkflow(any(), anyString(), any(Workflow.class), any());
  }

  @Test
  void createWorkflow_Unauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    doThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED))
        .when(orchestratorService).createWorkflow(eq(metisUser), any(), any(), isNull());
    orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID,
        Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
  }

  @Test
  void createWorkflow_WorkflowAlreadyExistsException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    doThrow(new WorkflowAlreadyExistsException("Some error")).when(orchestratorService)
        .createWorkflow(any(MetisUser.class), anyString(), any(Workflow.class), any());
    orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID,
        Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(409))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));

    verify(orchestratorService, times(1))
        .createWorkflow(eq(metisUser), anyString(), any(Workflow.class), isNull());
  }

  @Test
  void updateWorkflow() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    orchestratorControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID,
        Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(204))
        .andExpect(content().string(""));

    verify(orchestratorService, times(1))
        .updateWorkflow(eq(metisUser), anyString(), any(Workflow.class), isNull());
  }

  @Test
  void updateWorkflow_Unauthenticated() throws Exception {
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    orchestratorControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID,
        Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));

    verify(orchestratorService, never()).updateWorkflow(any(), anyString(), any(Workflow.class), any());
  }

  @Test
  void updateWorkflow_Unauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    doThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED))
        .when(orchestratorService).updateWorkflow(eq(metisUser), anyString(), any(Workflow.class), isNull());
    orchestratorControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID,
        Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
  }

  @Test
  void updateWorkflow_NoWorkflowFoundException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    doThrow(new NoWorkflowFoundException("Some error")).when(orchestratorService)
        .updateWorkflow(eq(metisUser), anyString(), any(Workflow.class), isNull());
    orchestratorControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID,
        Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));

    verify(orchestratorService, times(1))
        .updateWorkflow(eq(metisUser), anyString(), any(Workflow.class), isNull());
  }

  @Test
  void deleteWorkflow() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    orchestratorControllerMock.perform(delete(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID,
        Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(""))
        .andExpect(status().is(204))
        .andExpect(content().string(""));
    verify(orchestratorService, times(1)).deleteWorkflow(eq(metisUser), anyString());
  }

  @Test
  void deleteWorkflow_Unauthenticated() throws Exception {
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));
    orchestratorControllerMock.perform(delete(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID,
        Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(""))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
    verify(orchestratorService, never()).deleteWorkflow(any(), anyString());
  }

  @Test
  void deleteWorkflow_Unauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    doThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED))
        .when(orchestratorService).deleteWorkflow(eq(metisUser), any());
    orchestratorControllerMock.perform(delete(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID,
        Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(""))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
  }

  @Test
  void getWorkflow() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(orchestratorService.getWorkflow(eq(metisUser), anyString())).thenReturn(workflow);
    orchestratorControllerMock.perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID,
        Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.datasetId", is(workflow.getDatasetId())));

    verify(orchestratorService, times(1)).getWorkflow(eq(metisUser), anyString());
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    WorkflowExecution workflowExecution = TestObjectFactory.createWorkflowExecutionObject();
    when(orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(eq(metisUser), anyString(), isNull(), anyInt()))
        .thenReturn(workflowExecution);
    orchestratorControllerMock.perform(
        post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE,
            Integer.toString(TestObjectFactory.DATASETID))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(201))
        .andExpect(jsonPath("$.workflowStatus", is(WorkflowStatus.INQUEUE.name())));
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_Unauthenticated() throws Exception {
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));
    orchestratorControllerMock.perform(
        post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE,
            Integer.toString(TestObjectFactory.DATASETID))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_Unauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    when(orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(eq(metisUser), anyString(), isNull(), anyInt()))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));
    orchestratorControllerMock.perform(
        post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE,
            Integer.toString(TestObjectFactory.DATASETID))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_WorkflowExecutionAlreadyExistsException()
      throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    doThrow(new WorkflowExecutionAlreadyExistsException("Some error")).when(orchestratorService)
        .addWorkflowInQueueOfWorkflowExecutions(eq(metisUser), anyString(), isNull(), anyInt());
    orchestratorControllerMock.perform(
        post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE,
            Integer.toString(TestObjectFactory.DATASETID))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(409))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_NoDatasetFoundException()
      throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    doThrow(new NoDatasetFoundException("Some error")).when(orchestratorService)
        .addWorkflowInQueueOfWorkflowExecutions(eq(metisUser), anyString(), isNull(), anyInt());
    orchestratorControllerMock.perform(
        post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE,
            Integer.toString(TestObjectFactory.DATASETID))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  void addWorkflowInQueueOfWorkflowExecutions_NoWorkflowFoundException()
      throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    doThrow(new NoWorkflowFoundException("Some error")).when(orchestratorService)
        .addWorkflowInQueueOfWorkflowExecutions(eq(metisUser), anyString(), isNull(), anyInt());
    orchestratorControllerMock.perform(
        post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE,
            Integer.toString(TestObjectFactory.DATASETID))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  void cancelWorkflowExecution() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    doNothing().when(orchestratorService).cancelWorkflowExecution(eq(metisUser), anyString());
    orchestratorControllerMock.perform(
        delete(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID,
            TestObjectFactory.EXECUTIONID)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(204))
        .andExpect(content().string(""));
  }

  @Test
  void cancelWorkflowExecution_Unauthenticated() throws Exception {
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));
    orchestratorControllerMock.perform(
        delete(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID,
            TestObjectFactory.EXECUTIONID)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
  }

  @Test
  void cancelWorkflowExecution_Unauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    doThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED))
        .when(orchestratorService).cancelWorkflowExecution(eq(metisUser), anyString());
    orchestratorControllerMock.perform(
        delete(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID,
            TestObjectFactory.EXECUTIONID)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
  }

  @Test
  void cancelWorkflowExecution_NoWorkflowExecutionFoundException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    doThrow(new NoWorkflowExecutionFoundException("Some error")).when(orchestratorService)
        .cancelWorkflowExecution(eq(metisUser), anyString());
    orchestratorControllerMock.perform(
        delete(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID,
            TestObjectFactory.EXECUTIONID)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  void getWorkflowExecutionByExecutionId() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    when(orchestratorService.getWorkflowExecutionByExecutionId(eq(metisUser), anyString()))
        .thenReturn(workflowExecution);
    orchestratorControllerMock.perform(
        get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID,
            TestObjectFactory.EXECUTIONID)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.workflowStatus", is(WorkflowStatus.RUNNING.name())));
  }

  @Test
  void getLatestFinishedPluginWorkflowExecutionByDatasetIdIfPluginTypeAllowedForExecution()
      throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    AbstractExecutablePlugin plugin = ExecutablePluginFactory
        .createPlugin(new ValidationExternalPluginMetadata());
    plugin.setId("validation_external_id");
    when(orchestratorService.getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(
        metisUser, Integer.toString(TestObjectFactory.DATASETID), ExecutablePluginType.VALIDATION_EXTERNAL,
        null))
        .thenReturn(plugin);

    orchestratorControllerMock.perform(
        get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_ALLOWED_PLUGIN,
            Integer.toString(TestObjectFactory.DATASETID))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .param("pluginType", "VALIDATION_EXTERNAL")
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.pluginType", is(PluginType.VALIDATION_EXTERNAL.name())));
  }

  @Test
  void getLatestFinishedPluginWorkflowExecutionByDatasetIdIfPluginTypeAllowedForExecution_HarvestingPlugin()
      throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    when(orchestratorService
        .getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(metisUser,
            Integer.toString(TestObjectFactory.DATASETID), ExecutablePluginType.OAIPMH_HARVEST, null))
        .thenReturn(null);

    orchestratorControllerMock.perform(
        get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_ALLOWED_PLUGIN,
            Integer.toString(TestObjectFactory.DATASETID))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .param("pluginType", "OAIPMH_HARVEST")
            .content(""))
        .andExpect(status().is(200));
  }

  @Test
  void getDatasetExecutionInformation() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    DatasetExecutionInformation datasetExecutionInformation = new DatasetExecutionInformation();
    datasetExecutionInformation.setLastHarvestedDate(new Date(1000));
    datasetExecutionInformation.setLastHarvestedRecords(100);
    datasetExecutionInformation.setFirstPublishedDate(new Date(2000));
    datasetExecutionInformation.setLastPublishedDate(new Date(3000));
    datasetExecutionInformation.setLastPublishedRecords(100);
    when(orchestratorService
        .getDatasetExecutionInformation(metisUser, Integer.toString(TestObjectFactory.DATASETID)))
        .thenReturn(datasetExecutionInformation);

    orchestratorControllerMock.perform(
        get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_INFORMATION,
            Integer.toString(TestObjectFactory.DATASETID))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.lastHarvestedDate",
            is(simpleDateFormat.format(datasetExecutionInformation.getLastHarvestedDate()))))
        .andExpect(jsonPath("$.lastHarvestedRecords",
            is(datasetExecutionInformation.getLastHarvestedRecords())))
        .andExpect(jsonPath("$.firstPublishedDate",
            is(simpleDateFormat.format(datasetExecutionInformation.getFirstPublishedDate()))))
        .andExpect(jsonPath("$.lastPublishedDate",
            is(simpleDateFormat.format(datasetExecutionInformation.getLastPublishedDate()))))
        .andExpect(jsonPath("$.lastPublishedRecords",
            is(datasetExecutionInformation.getLastPublishedRecords())));
  }

  @Test
  void getAllWorkflowExecutionsByDatasetId() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    int listSize = 2;
    List<WorkflowExecution> listOfWorkflowExecutions = TestObjectFactory
        .createListOfWorkflowExecutions(listSize + 1); //To get the effect of next page

    when(orchestratorService.getWorkflowExecutionsPerRequest()).thenReturn(listSize);
    when(orchestratorService.getAllWorkflowExecutions(eq(metisUser), anyString(),
        ArgumentMatchers.anySet(), any(DaoFieldNames.class), anyBoolean(), anyInt()))
        .thenReturn(listOfWorkflowExecutions);
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID,
            Integer.toString(TestObjectFactory.DATASETID))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .param("workflowStatus", WorkflowStatus.INQUEUE.name())
            .param("nextPage", "")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.results", hasSize(listSize + 1)))
        .andExpect(
            jsonPath("$.results[0].datasetId", is(Integer.toString(TestObjectFactory.DATASETID))))
        .andExpect(jsonPath("$.results[0].workflowStatus", is(WorkflowStatus.INQUEUE.name())))
        .andExpect(jsonPath("$.results[1].datasetId",
            is(Integer.toString(TestObjectFactory.DATASETID + 1))))
        .andExpect(jsonPath("$.results[1].workflowStatus", is(WorkflowStatus.INQUEUE.name())))
        .andExpect(jsonPath("$.nextPage").isNotEmpty());
  }

  @Test
  void getAllWorkflowExecutionsByDatasetIdNegativeNextPage() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID,
            Integer.toString(TestObjectFactory.DATASETID))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .param("workflowStatus", WorkflowStatus.INQUEUE.name())
            .param("nextPage", "-1")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(406));
  }

  @Test
  void getAllWorkflowExecutions() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    int listSize = 2;
    List<WorkflowExecution> listOfWorkflowExecutions = TestObjectFactory
        .createListOfWorkflowExecutions(listSize + 1); //To get the effect of next page

    when(orchestratorService.getWorkflowExecutionsPerRequest()).thenReturn(listSize);
    when(orchestratorService.getAllWorkflowExecutions(eq(metisUser), isNull(),
        ArgumentMatchers.anySet(), any(DaoFieldNames.class), anyBoolean(), anyInt()))
        .thenReturn(listOfWorkflowExecutions);
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .param("workflowStatus", WorkflowStatus.INQUEUE.name())
            .param("nextPage", "")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.results", hasSize(listSize + 1)))
        .andExpect(
            jsonPath("$.results[0].datasetId", is(Integer.toString(TestObjectFactory.DATASETID))))
        .andExpect(jsonPath("$.results[0].workflowStatus", is(WorkflowStatus.INQUEUE.name())))
        .andExpect(jsonPath("$.results[1].datasetId",
            is(Integer.toString(TestObjectFactory.DATASETID + 1))))
        .andExpect(jsonPath("$.results[1].workflowStatus", is(WorkflowStatus.INQUEUE.name())))
        .andExpect(jsonPath("$.nextPage").isNotEmpty());
  }

  @Test
  void getAllWorkflowExecutionsNegativeNextPage() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .param("workflowStatus", WorkflowStatus.INQUEUE.name())
            .param("nextPage", "-1")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(406));
  }

  @Test
  void getWorkflowExecutionsOverview() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    final int pageSize = 2;
    final int nextPage = 5;
    final int pageCount = 3;
    final List<ExecutionAndDatasetView> listOfWorkflowExecutionAndDatasetViews = TestObjectFactory
        .createListOfExecutionOverviews(pageSize * pageCount);

    when(orchestratorService.getWorkflowExecutionsPerRequest()).thenReturn(pageSize);
    when(orchestratorService
        .getWorkflowExecutionsOverview(eq(metisUser), isNull(), isNull(), isNull(), isNull(),
            eq(nextPage), eq(pageCount)))
        .thenReturn(listOfWorkflowExecutionAndDatasetViews);
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_OVERVIEW)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .param("nextPage", "" + nextPage)
            .param("pageCount", "" + pageCount)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.results", hasSize(pageSize * pageCount)))
        .andExpect(
            jsonPath("$.results[0].dataset.datasetId",
                is(Integer.toString(TestObjectFactory.DATASETID))))
        .andExpect(
            jsonPath("$.results[0].execution.workflowStatus", is(WorkflowStatus.INQUEUE.name())))
        .andExpect(jsonPath("$.results[1].dataset.datasetId",
            is(Integer.toString(TestObjectFactory.DATASETID + 1))))
        .andExpect(
            jsonPath("$.results[1].execution.workflowStatus", is(WorkflowStatus.INQUEUE.name())))
        .andExpect(jsonPath("$.nextPage", is(nextPage + pageCount)))
        .andExpect(jsonPath("$.listSize", is(pageSize * pageCount)));
  }

  @Test
  void getWorkflowExecutionsOverviewBadPaginationArguments() throws Exception {
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_OVERVIEW)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .param("nextPage", "-1")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(406));
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_OVERVIEW)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .param("pageCount", "0")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(406));
  }

  @Test
  void testGetDatasetExecutionHistory() throws Exception {

    // Get the user
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);

    // Create nonempty history
    final Execution execution1 = new Execution();
    execution1.setWorkflowExecutionId("execution 1");
    execution1.setStartedDate(new Date(1));
    final Execution execution2 = new Execution();
    execution2.setWorkflowExecutionId("execution 2");
    execution2.setStartedDate(new Date(2));
    final ExecutionHistory resultNonEmpty = new ExecutionHistory();
    resultNonEmpty.setExecutions(Arrays.asList(execution1, execution2));

    // Test happy flow with non-empty evolution
    when(orchestratorService
        .getDatasetExecutionHistory(metisUser, "" + TestObjectFactory.DATASETID))
        .thenReturn(resultNonEmpty);
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_HISTORY,
            TestObjectFactory.DATASETID)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.executions", hasSize(2)))
        .andExpect(jsonPath("$.executions[0].workflowExecutionId",
            is(execution1.getWorkflowExecutionId())))
        .andExpect(jsonPath("$.executions[0].startedDate",
            is(simpleDateFormat.format(execution1.getStartedDate()))))
        .andExpect(jsonPath("$.executions[1].workflowExecutionId",
            is(execution2.getWorkflowExecutionId())))
        .andExpect(jsonPath("$.executions[1].startedDate",
            is(simpleDateFormat.format(execution2.getStartedDate()))));

    // Test happy flow with empty evolution
    final ExecutionHistory resultEmpty = new ExecutionHistory();
    resultEmpty.setExecutions(Collections.emptyList());
    when(orchestratorService
        .getDatasetExecutionHistory(metisUser, "" + TestObjectFactory.DATASETID))
        .thenReturn(resultEmpty);
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_HISTORY,
            TestObjectFactory.DATASETID)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.executions", hasSize(0)));

    // Test for bad input
    when(orchestratorService
        .getDatasetExecutionHistory(metisUser, "" + TestObjectFactory.DATASETID))
        .thenThrow(new NoDatasetFoundException(""));
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_HISTORY,
            TestObjectFactory.DATASETID)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER))
        .andExpect(status().is(404));

    // Test for unauthorized user
    doThrow(new UserUnauthorizedException("")).when(orchestratorService)
        .getDatasetExecutionHistory(metisUser, "" + TestObjectFactory.DATASETID);
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_HISTORY,
            TestObjectFactory.DATASETID)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER))
        .andExpect(status().is(401));
  }

  @Test
  void testGetExecutablePluginsWithDataAvailability() throws Exception {

    // Get the user
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);

    // Create nonempty history
    final PluginWithDataAvailability plugin1 = new PluginWithDataAvailability();
    plugin1.setPluginType(PluginType.OAIPMH_HARVEST);
    plugin1.setHasSuccessfulData(true);
    final PluginWithDataAvailability plugin2 = new PluginWithDataAvailability();
    plugin2.setPluginType(PluginType.ENRICHMENT);
    plugin2.setHasSuccessfulData(false);
    final PluginsWithDataAvailability resultNonEmpty = new PluginsWithDataAvailability();
    resultNonEmpty.setPlugins(Arrays.asList(plugin1, plugin2));

    // Test happy flow with non-empty evolution
    when(orchestratorService
        .getExecutablePluginsWithDataAvailability(metisUser, TestObjectFactory.EXECUTIONID))
        .thenReturn(resultNonEmpty);
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID_PLUGINS_DATA_AVAILABILITY,
            TestObjectFactory.EXECUTIONID)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.plugins", hasSize(2)))
        .andExpect(jsonPath("$.plugins[0].pluginType",
            is(plugin1.getPluginType().name())))
        .andExpect(jsonPath("$.plugins[0].hasSuccessfulData",
            is(plugin1.isHasSuccessfulData())))
        .andExpect(jsonPath("$.plugins[1].pluginType",
            is(plugin2.getPluginType().name())))
        .andExpect(jsonPath("$.plugins[1].hasSuccessfulData",
            is(plugin2.isHasSuccessfulData())));

    // Test happy flow with empty evolution
    final PluginsWithDataAvailability resultEmpty = new PluginsWithDataAvailability();
    resultEmpty.setPlugins(Collections.emptyList());
    when(orchestratorService
        .getExecutablePluginsWithDataAvailability(metisUser, TestObjectFactory.EXECUTIONID))
        .thenReturn(resultEmpty);
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID_PLUGINS_DATA_AVAILABILITY,
            TestObjectFactory.EXECUTIONID)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.plugins", hasSize(0)));

    // Test for bad input
    when(orchestratorService
        .getExecutablePluginsWithDataAvailability(metisUser, TestObjectFactory.EXECUTIONID))
        .thenThrow(new NoWorkflowExecutionFoundException(""));
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID_PLUGINS_DATA_AVAILABILITY,
            TestObjectFactory.EXECUTIONID)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER))
        .andExpect(status().is(404));

    // Test for unauthorized user
    doThrow(new UserUnauthorizedException("")).when(orchestratorService)
        .getExecutablePluginsWithDataAvailability(metisUser, TestObjectFactory.EXECUTIONID);
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID_PLUGINS_DATA_AVAILABILITY,
            TestObjectFactory.EXECUTIONID)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER))
        .andExpect(status().is(401));
  }

  @Test
  void testGetRecordEvolutionForVersion() throws Exception {

    // Get the user
    final MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);

    // Create nonempty evolution step
    final VersionEvolutionStep step1 = new VersionEvolutionStep();
    step1.setFinishedTime(new Date(1));
    step1.setPluginType(ExecutablePluginType.OAIPMH_HARVEST);
    step1.setWorkflowExecutionId("execution 1");
    final VersionEvolutionStep step2 = new VersionEvolutionStep();
    step2.setFinishedTime(new Date(2));
    step2.setPluginType(ExecutablePluginType.TRANSFORMATION);
    step2.setWorkflowExecutionId("execution 2");
    final VersionEvolution resultNonEmpty = new VersionEvolution();
    resultNonEmpty.setEvolutionSteps(Arrays.asList(step1, step2));

    // Test happy flow with non-empty evolution
    final PluginType pluginType = PluginType.MEDIA_PROCESS;
    when(orchestratorService
        .getRecordEvolutionForVersion(metisUser, TestObjectFactory.EXECUTIONID, pluginType))
        .thenReturn(resultNonEmpty);
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EVOLUTION, TestObjectFactory.EXECUTIONID,
            pluginType)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.evolutionSteps", hasSize(2)))
        .andExpect(
            jsonPath("$.evolutionSteps[0].workflowExecutionId", is(step1.getWorkflowExecutionId())))
        .andExpect(jsonPath("$.evolutionSteps[0].pluginType", is(step1.getPluginType().name())))
        .andExpect(jsonPath("$.evolutionSteps[0].finishedTime",
            is((int) step1.getFinishedTime().getTime())))
        .andExpect(
            jsonPath("$.evolutionSteps[1].workflowExecutionId", is(step2.getWorkflowExecutionId())))
        .andExpect(jsonPath("$.evolutionSteps[1].pluginType", is(step2.getPluginType().name())))
        .andExpect(jsonPath("$.evolutionSteps[1].finishedTime",
            is((int) step2.getFinishedTime().getTime())));

    // Test happy flow with empty evolution
    final VersionEvolution resultEmpty = new VersionEvolution();
    resultEmpty.setEvolutionSteps(Collections.emptyList());
    when(orchestratorService
        .getRecordEvolutionForVersion(metisUser, TestObjectFactory.EXECUTIONID, pluginType))
        .thenReturn(resultEmpty);
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EVOLUTION, TestObjectFactory.EXECUTIONID,
            pluginType)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.evolutionSteps", hasSize(0)));

    // Test for bad input
    when(orchestratorService
        .getRecordEvolutionForVersion(metisUser, TestObjectFactory.EXECUTIONID, pluginType))
        .thenThrow(new NoWorkflowExecutionFoundException(""));
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EVOLUTION, TestObjectFactory.EXECUTIONID,
            pluginType)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER))
        .andExpect(status().is(404));

    // Test for unauthorized user
    doThrow(new UserUnauthorizedException("")).when(orchestratorService)
        .getRecordEvolutionForVersion(metisUser, TestObjectFactory.EXECUTIONID, pluginType);
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EVOLUTION, TestObjectFactory.EXECUTIONID,
            pluginType)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER))
        .andExpect(status().is(401));
  }
}
