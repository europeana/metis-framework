package eu.europeana.metis.core.rest.controller;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
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

import eu.europeana.metis.authentication.rest.client.AuthenticationClient;
import eu.europeana.metis.authentication.user.MetisUserView;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoScheduledWorkflowFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowFoundException;
import eu.europeana.metis.core.exceptions.ScheduledWorkflowAlreadyExistsException;
import eu.europeana.metis.core.rest.exception.RestResponseExceptionHandler;
import eu.europeana.metis.core.rest.utils.TestObjectFactory;
import eu.europeana.metis.core.rest.utils.TestUtils;
import eu.europeana.metis.core.service.ScheduleWorkflowService;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import eu.europeana.metis.utils.CommonStringValues;
import eu.europeana.metis.utils.RestEndpoints;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-05
 */
class TestScheduleWorkflowController {

  private static ScheduleWorkflowService scheduleWorkflowService;
  private static MockMvc scheduleWorkflowControllerMock;
  private static AuthenticationClient authenticationClient;

  @BeforeAll
  static void setUp() {
    scheduleWorkflowService = mock(ScheduleWorkflowService.class);
    authenticationClient = mock(AuthenticationClient.class);
    ScheduleWorkflowController scheduleWorkflowController = new ScheduleWorkflowController(
        scheduleWorkflowService, authenticationClient);
    scheduleWorkflowControllerMock = MockMvcBuilders
        .standaloneSetup(scheduleWorkflowController)
        .setControllerAdvice(new RestResponseExceptionHandler())
        .setMessageConverters(new MappingJackson2HttpMessageConverter(),
            new MappingJackson2XmlHttpMessageConverter(),
            new StringHttpMessageConverter(StandardCharsets.UTF_8))
        .build();
  }

  @AfterEach
  void cleanUp() {
    reset(scheduleWorkflowService);
    reset(authenticationClient);
  }

  @Test
  void scheduleWorkflowExecution() throws Exception {
    MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUserView);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory.createScheduledWorkflowObject();
    scheduleWorkflowControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(201))
        .andExpect(content().string(""));
    verify(authenticationClient, times(1))
        .getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER);
    verify(scheduleWorkflowService, times(1))
        .scheduleWorkflow(any(MetisUserView.class), any(ScheduledWorkflow.class));
  }

  @Test
  void scheduleWorkflowExecution_Unauthenticated() throws Exception {
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory.createScheduledWorkflowObject();
    scheduleWorkflowControllerMock
        .perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
  }

  @Test
  void scheduleWorkflowExecution_Unauthorized() throws Exception {
    MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUserView);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory.createScheduledWorkflowObject();
    doThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED))
        .when(scheduleWorkflowService).scheduleWorkflow(any(), any());
    scheduleWorkflowControllerMock
        .perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
  }

  @Test
  void scheduleWorkflowExecution_BadContentException() throws Exception {
    MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUserView);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory.createScheduledWorkflowObject();
    doThrow(new BadContentException("Some error")).when(scheduleWorkflowService)
        .scheduleWorkflow(any(MetisUserView.class), any(ScheduledWorkflow.class));
    scheduleWorkflowControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(406))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  void scheduleWorkflowExecution_ScheduledWorkflowAlreadyExistsException()
      throws Exception {
    MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUserView);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory.createScheduledWorkflowObject();
    doThrow(new ScheduledWorkflowAlreadyExistsException("Some error")).when(scheduleWorkflowService)
        .scheduleWorkflow(any(MetisUserView.class), any(ScheduledWorkflow.class));
    scheduleWorkflowControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(409))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  void scheduleWorkflowExecution_NoWorkflowFoundException() throws Exception {
    MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUserView);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory.createScheduledWorkflowObject();
    doThrow(new NoWorkflowFoundException("Some error")).when(scheduleWorkflowService)
        .scheduleWorkflow(any(MetisUserView.class), any(ScheduledWorkflow.class));
    scheduleWorkflowControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  void scheduleWorkflowExecution_NoDatasetFoundException() throws Exception {
    MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUserView);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory.createScheduledWorkflowObject();
    doThrow(new NoDatasetFoundException("Some error")).when(scheduleWorkflowService)
        .scheduleWorkflow(any(MetisUserView.class), any(ScheduledWorkflow.class));
    scheduleWorkflowControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  void getScheduledWorkflow() throws Exception {
    MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUserView);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory.createScheduledWorkflowObject();
    when(scheduleWorkflowService.getScheduledWorkflowByDatasetId(any(MetisUserView.class), anyString()))
        .thenReturn(scheduledWorkflow);
    scheduleWorkflowControllerMock.perform(
        get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE_DATASETID,
            Integer.toString(TestObjectFactory.DATASETID))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.scheduleFrequence", is(ScheduleFrequence.ONCE.name())));
    verify(authenticationClient, times(1))
        .getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER);
    verify(scheduleWorkflowService, times(1))
        .getScheduledWorkflowByDatasetId(any(MetisUserView.class), anyString());
  }

  @Test
  void getAllScheduledWorkflows() throws Exception {
    MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUserView);
    int listSize = 2;
    List<ScheduledWorkflow> listOfScheduledWorkflows = TestObjectFactory
        .createListOfScheduledWorkflows(listSize + 1);//To get the effect of next page

    when(scheduleWorkflowService.getScheduledWorkflowsPerRequest()).thenReturn(listSize);
    when(scheduleWorkflowService
        .getAllScheduledWorkflows(any(MetisUserView.class), any(ScheduleFrequence.class), anyInt()))
        .thenReturn(listOfScheduledWorkflows);
    scheduleWorkflowControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .param("nextPage", "")
            .contentType(MediaType.APPLICATION_JSON)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.results", hasSize(listSize + 1)))
        .andExpect(
            jsonPath("$.results[0].datasetId", is(Integer.toString(TestObjectFactory.DATASETID))))
        .andExpect(jsonPath("$.results[0].scheduleFrequence", is(ScheduleFrequence.ONCE.name())))
        .andExpect(jsonPath("$.results[1].datasetId",
            is(Integer.toString(TestObjectFactory.DATASETID + 1))))
        .andExpect(jsonPath("$.results[1].scheduleFrequence", is(ScheduleFrequence.ONCE.name())))
        .andExpect(jsonPath("$.nextPage").isNotEmpty());
    verify(authenticationClient, times(1))
        .getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER);
  }

  @Test
  void getAllScheduledWorkflowsNegativeNextPage() throws Exception {
    MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUserView);
    scheduleWorkflowControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .param("nextPage", "-1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(""))
        .andExpect(status().is(406));
  }

  @Test
  void updateScheduledWorkflow() throws Exception {
    MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUserView);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduleWorkflowControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(204))
        .andExpect(content().string(""));
    verify(authenticationClient, times(1))
        .getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER);
    verify(scheduleWorkflowService, times(1))
        .updateScheduledWorkflow(any(MetisUserView.class), any(ScheduledWorkflow.class));
  }

  @Test
  void updateScheduledWorkflow_Unauthenticated() throws Exception {
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory.createScheduledWorkflowObject();
    scheduleWorkflowControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
  }

  @Test
  void updateScheduledWorkflow_Unauthorized() throws Exception {
    MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUserView);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory.createScheduledWorkflowObject();
    doThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED))
        .when(scheduleWorkflowService).updateScheduledWorkflow(any(), any());
    scheduleWorkflowControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
  }

  @Test
  void updateScheduledWorkflow_NoWorkflowFoundException() throws Exception {
    MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUserView);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    doThrow(new NoWorkflowFoundException("Some error")).when(scheduleWorkflowService)
        .updateScheduledWorkflow(any(MetisUserView.class), any(ScheduledWorkflow.class));
    scheduleWorkflowControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  void updateScheduledWorkflow_NoScheduledWorkflowFoundException() throws Exception {
    MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUserView);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    doThrow(new NoScheduledWorkflowFoundException("Some error")).when(scheduleWorkflowService)
        .updateScheduledWorkflow(any(MetisUserView.class), any(ScheduledWorkflow.class));
    scheduleWorkflowControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  void updateScheduledWorkflow_BadContentException() throws Exception {
    MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUserView);
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    doThrow(new BadContentException("Some error")).when(scheduleWorkflowService)
        .updateScheduledWorkflow(any(MetisUserView.class), any(ScheduledWorkflow.class));
    scheduleWorkflowControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
        .contentType(MediaType.APPLICATION_JSON)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(406))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  void deleteScheduledWorkflowExecution() throws Exception {
    MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUserView);
    scheduleWorkflowControllerMock.perform(
        delete(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE_DATASETID,
            Integer.toString(TestObjectFactory.DATASETID))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(""))
        .andExpect(status().is(204))
        .andExpect(content().string(""));
    verify(authenticationClient, times(1))
        .getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER);
    verify(scheduleWorkflowService, times(1))
        .deleteScheduledWorkflow(any(MetisUserView.class), anyString());
  }


  @Test
  void deleteScheduledWorkflowExecution_Unauthenticated() throws Exception {
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED));
    scheduleWorkflowControllerMock.perform(
        delete(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE_DATASETID,
            Integer.toString(TestObjectFactory.DATASETID))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(""))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
  }

  @Test
  void deleteScheduledWorkflowExecution_Unauthorized() throws Exception {
    MetisUserView metisUserView = TestObjectFactory.createMetisUser(TestObjectFactory.EMAIL);
    when(authenticationClient.getUserByAccessTokenInHeader(TestObjectFactory.AUTHORIZATION_HEADER))
        .thenReturn(metisUserView);
    doThrow(new UserUnauthorizedException(CommonStringValues.UNAUTHORIZED))
        .when(scheduleWorkflowService).deleteScheduledWorkflow(any(), any());
    scheduleWorkflowControllerMock.perform(
        delete(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE_DATASETID,
            Integer.toString(TestObjectFactory.DATASETID))
            .header("Authorization", TestObjectFactory.AUTHORIZATION_HEADER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(""))
        .andExpect(status().is(401))
        .andExpect(jsonPath("$.errorMessage", is(CommonStringValues.UNAUTHORIZED)));
  }
}
