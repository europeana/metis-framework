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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.authentication.rest.client.AuthenticationClient;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.dataset.DatasetExecutionInformation;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowFoundException;
import eu.europeana.metis.core.exceptions.WorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.WorkflowExecutionAlreadyExistsException;
import eu.europeana.metis.core.rest.exception.RestResponseExceptionHandler;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.test.utils.TestUtils;
import eu.europeana.metis.core.workflow.OrderField;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.exception.UserUnauthorizedException;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-06
 */
public class TestOrchestratorController {

  private static OrchestratorService orchestratorService;
  private static MockMvc orchestratorControllerMock;
  private static AuthenticationClient authenticationClient;

  @BeforeClass
  public static void setUp() {
    orchestratorService = mock(OrchestratorService.class);
    authenticationClient = mock(AuthenticationClient.class);
    OrchestratorController orchestratorController =
        new OrchestratorController(orchestratorService, authenticationClient);
    orchestratorControllerMock = MockMvcBuilders
        .standaloneSetup(orchestratorController)
        .setControllerAdvice(new RestResponseExceptionHandler())
        .build();
  }

  @After
  public void cleanUp() {
    Mockito.reset(orchestratorService, authenticationClient);
  }

  @Test
  public void createWorkflow() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID, Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(201))
        .andExpect(content().string(""));

    verify(orchestratorService, times(1)).createWorkflow(eq(metisUser), anyString(), any(Workflow.class));
  }

  @Test
  public void createWorkflow_Unauthenticated() throws Exception {
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
      .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID, Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));

    verify(orchestratorService, never()).createWorkflow(any(), anyString(), any(Workflow.class));
  }

  @Test
  public void createWorkflow_Unauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    doThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED))
      .when(orchestratorService).createWorkflow(eq(metisUser), any(), any());
    orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID, Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
  }

  @Test
  public void createWorkflow_WorkflowAlreadyExistsException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    doThrow(new WorkflowAlreadyExistsException("Some error")).when(orchestratorService)
        .createWorkflow(any(MetisUser.class), anyString(), any(Workflow.class));
    orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID, Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(409))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));

    verify(orchestratorService, times(1)).createWorkflow(eq(metisUser), anyString(), any(Workflow.class));
  }

  @Test
  public void updateWorkflow() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    orchestratorControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID,Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(204))
        .andExpect(content().string(""));

    verify(orchestratorService, times(1)).updateWorkflow(eq(metisUser), anyString(), any(Workflow.class));
  }

  @Test
  public void updateWorkflow_Unauthenticated() throws Exception {
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    orchestratorControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID,Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));

    verify(orchestratorService, never()).updateWorkflow(any(), anyString(), any(Workflow.class));
  }

  @Test
  public void updateWorkflow_Unauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    doThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED))
        .when(orchestratorService).updateWorkflow(eq(metisUser), anyString(), any(Workflow.class));
    orchestratorControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID,Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
  }

  @Test
  public void updateWorkflow_NoWorkflowFoundException() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    doThrow(new NoWorkflowFoundException("Some error")).when(orchestratorService)
        .updateWorkflow(eq(metisUser), anyString(), any(Workflow.class));
    orchestratorControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID, Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));

    verify(orchestratorService, times(1)).updateWorkflow(eq(metisUser), anyString(), any(Workflow.class));
  }

  @Test
  public void deleteWorkflow() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    orchestratorControllerMock.perform(delete(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID, Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(""))
        .andExpect(status().is(204))
        .andExpect(content().string(""));
    verify(orchestratorService, times(1)).deleteWorkflow(eq(metisUser), anyString());
  }

  @Test
  public void deleteWorkflow_Unauthenticated() throws Exception {
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));
    orchestratorControllerMock.perform(delete(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID, Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(""))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
    verify(orchestratorService, never()).deleteWorkflow(any(), anyString());
  }

  @Test
  public void deleteWorkflow_Unauthorized() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    doThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED))
        .when(orchestratorService).deleteWorkflow(eq(metisUser), any());
    orchestratorControllerMock.perform(delete(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID, Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(""))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
  }

  @Test
  public void getWorkflow() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    when(orchestratorService.getWorkflow(eq(metisUser), anyString())).thenReturn(workflow);
    orchestratorControllerMock.perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID, Integer.toString(TestObjectFactory.DATASETID))
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.datasetId", is(workflow.getDatasetId())));

    verify(orchestratorService, times(1)).getWorkflow(eq(metisUser), anyString());
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutions() throws Exception {
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
  public void addWorkflowInQueueOfWorkflowExecutions_Unauthenticated() throws Exception {
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
  public void addWorkflowInQueueOfWorkflowExecutions_Unauthorized() throws Exception {
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
  public void addWorkflowInQueueOfWorkflowExecutions_WorkflowExecutionAlreadyExistsException()
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
  public void addWorkflowInQueueOfWorkflowExecutions_NoDatasetFoundException()
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
  public void addWorkflowInQueueOfWorkflowExecutions_NoWorkflowFoundException()
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
  public void cancelWorkflowExecution() throws Exception {
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
  public void cancelWorkflowExecution_Unauthenticated() throws Exception {
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
  public void cancelWorkflowExecution_Unauthorized() throws Exception {
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
  public void cancelWorkflowExecution_NoWorkflowExecutionFoundException() throws Exception {
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
  public void getWorkflowExecutionByExecutionId() throws Exception {
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
  public void getLatestFinishedPluginWorkflowExecutionByDatasetIdIfPluginTypeAllowedForExecution()
      throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    AbstractMetisPlugin abstractMetisPlugin = PluginType.VALIDATION_EXTERNAL.getNewPlugin(null);
    abstractMetisPlugin.setId("validation_external_id");
    when(orchestratorService.getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(
        metisUser, Integer.toString(TestObjectFactory.DATASETID), PluginType.VALIDATION_EXTERNAL, null))
        .thenReturn(abstractMetisPlugin);

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
  public void getLatestFinishedPluginWorkflowExecutionByDatasetIdIfPluginTypeAllowedForExecution_HarvestingPlugin()
      throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    when(orchestratorService
        .getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(metisUser, 
            Integer.toString(TestObjectFactory.DATASETID), PluginType.OAIPMH_HARVEST, null))
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
  public void getDatasetExecutionInformation() throws Exception {
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

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

    orchestratorControllerMock.perform(
        get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_INFORMATION,
            Integer.toString(TestObjectFactory.DATASETID))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.lastHarvestedDate", is(simpleDateFormat.format(datasetExecutionInformation.getLastHarvestedDate()))))
        .andExpect(jsonPath("$.lastHarvestedRecords", is(datasetExecutionInformation.getLastHarvestedRecords())))
        .andExpect(jsonPath("$.firstPublishedDate", is(simpleDateFormat.format(datasetExecutionInformation.getFirstPublishedDate()))))
        .andExpect(jsonPath("$.lastPublishedDate", is(simpleDateFormat.format(datasetExecutionInformation.getLastPublishedDate()))))
        .andExpect(jsonPath("$.lastPublishedRecords", is(datasetExecutionInformation.getLastPublishedRecords())));
  }

  @Test
  public void getAllWorkflowExecutionsByDatasetId() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    int listSize = 2;
    List<WorkflowExecution> listOfWorkflowExecutions = TestObjectFactory
        .createListOfWorkflowExecutions(listSize + 1); //To get the effect of next page

    when(orchestratorService.getWorkflowExecutionsPerRequest()).thenReturn(listSize);
    when(orchestratorService.getAllWorkflowExecutions(eq(metisUser), anyString(),
        ArgumentMatchers.anySet(), any(OrderField.class), anyBoolean(), anyInt()))
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
        .andExpect(jsonPath("$.results[0].datasetId", is(Integer.toString(TestObjectFactory.DATASETID))))
        .andExpect(jsonPath("$.results[0].workflowStatus", is(WorkflowStatus.INQUEUE.name())))
        .andExpect(jsonPath("$.results[1].datasetId", is(Integer.toString(TestObjectFactory.DATASETID + 1))))
        .andExpect(jsonPath("$.results[1].workflowStatus", is(WorkflowStatus.INQUEUE.name())))
        .andExpect(jsonPath("$.nextPage").isNotEmpty());
  }

  @Test
  public void getAllWorkflowExecutionsByDatasetIdNegativeNextPage() throws Exception {
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
  public void getAllWorkflowExecutions() throws Exception {
    MetisUser metisUser = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUser);
    int listSize = 2;
    List<WorkflowExecution> listOfWorkflowExecutions = TestObjectFactory
        .createListOfWorkflowExecutions(listSize + 1); //To get the effect of next page

    when(orchestratorService.getWorkflowExecutionsPerRequest()).thenReturn(listSize);
    when(orchestratorService.getAllWorkflowExecutions(eq(metisUser), isNull(),
        ArgumentMatchers.anySet(), any(OrderField.class), anyBoolean(), anyInt()))
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
        .andExpect(jsonPath("$.results[0].datasetId", is(Integer.toString(TestObjectFactory.DATASETID))))
        .andExpect(jsonPath("$.results[0].workflowStatus", is(WorkflowStatus.INQUEUE.name())))
        .andExpect(jsonPath("$.results[1].datasetId", is(Integer.toString(TestObjectFactory.DATASETID + 1))))
        .andExpect(jsonPath("$.results[1].workflowStatus", is(WorkflowStatus.INQUEUE.name())))
        .andExpect(jsonPath("$.nextPage").isNotEmpty());
  }

  @Test
  public void getAllWorkflowExecutionsNegativeNextPage() throws Exception {
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
}
