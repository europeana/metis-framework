package eu.europeana.metis.core.rest;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoScheduledWorkflowFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowFoundException;
import eu.europeana.metis.core.exceptions.ScheduledWorkflowAlreadyExistsException;
import eu.europeana.metis.core.rest.exception.RestResponseExceptionHandler;
import eu.europeana.metis.core.service.ScheduleWorkflowService;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.test.utils.TestUtils;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import eu.europeana.metis.exception.BadContentException;
import java.util.List;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-05
 */
public class TestScheduleWorkflowController {

  private static ScheduleWorkflowService scheduleWorkflowService;
  private static MockMvc scheduleWorkflowControllerMock;

  @BeforeClass
  public static void setUp() {
    scheduleWorkflowService = mock(ScheduleWorkflowService.class);
    ScheduleWorkflowController scheduleWorkflowController = new ScheduleWorkflowController(
        scheduleWorkflowService);
    scheduleWorkflowControllerMock = MockMvcBuilders
        .standaloneSetup(scheduleWorkflowController)
        .setControllerAdvice(new RestResponseExceptionHandler())
        .build();
  }

  @After
  public void cleanUp() {
    Mockito.reset(scheduleWorkflowService);
  }

  @Test
  public void scheduleWorkflowExecution() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduleWorkflowControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(201))
        .andExpect(content().string(""));

    verify(scheduleWorkflowService, times(1)).scheduleWorkflow(any(ScheduledWorkflow.class));
  }

  @Test
  public void scheduleWorkflowExecution_BadContentException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    doThrow(new BadContentException("Some error")).when(scheduleWorkflowService)
        .scheduleWorkflow(any(ScheduledWorkflow.class));
    scheduleWorkflowControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(406))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void scheduleWorkflowExecution_ScheduledWorkflowAlreadyExistsException()
      throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    doThrow(new ScheduledWorkflowAlreadyExistsException("Some error")).when(scheduleWorkflowService)
        .scheduleWorkflow(any(ScheduledWorkflow.class));
    scheduleWorkflowControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(409))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void scheduleWorkflowExecution_NoWorkflowFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    doThrow(new NoWorkflowFoundException("Some error")).when(scheduleWorkflowService)
        .scheduleWorkflow(any(ScheduledWorkflow.class));
    scheduleWorkflowControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void scheduleWorkflowExecution_NoDatasetFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    doThrow(new NoDatasetFoundException("Some error")).when(scheduleWorkflowService)
        .scheduleWorkflow(any(ScheduledWorkflow.class));
    scheduleWorkflowControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void getScheduledWorkflow() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    when(scheduleWorkflowService.getScheduledWorkflowByDatasetId(anyInt()))
        .thenReturn(scheduledWorkflow);
    scheduleWorkflowControllerMock.perform(
        get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE_DATASETID,
            TestObjectFactory.DATASETID)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.scheduleFrequence", is(ScheduleFrequence.ONCE.name())));

    verify(scheduleWorkflowService, times(1)).getScheduledWorkflowByDatasetId(anyInt());
  }

  @Test
  public void getAllScheduledWorkflows() throws Exception {
    int listSize = 2;
    List<ScheduledWorkflow> listOfScheduledWorkflows = TestObjectFactory
        .createListOfScheduledWorkflows(listSize + 1);//To get the effect of next page

    when(scheduleWorkflowService.getScheduledWorkflowsPerRequest()).thenReturn(listSize);
    when(scheduleWorkflowService.getAllScheduledWorkflows(any(ScheduleFrequence.class), anyInt()))
        .thenReturn(listOfScheduledWorkflows);
    scheduleWorkflowControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
            .param("nextPage", "")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.results", hasSize(listSize + 1)))
        .andExpect(jsonPath("$.results[0].datasetId", is(TestObjectFactory.DATASETID)))
        .andExpect(jsonPath("$.results[0].scheduleFrequence", is(ScheduleFrequence.ONCE.name())))
        .andExpect(jsonPath("$.results[1].datasetId", is(TestObjectFactory.DATASETID + 1)))
        .andExpect(jsonPath("$.results[1].scheduleFrequence", is(ScheduleFrequence.ONCE.name())))
        .andExpect(jsonPath("$.nextPage").isNotEmpty());
  }

  @Test
  public void getAllScheduledWorkflowsNegativeNextPage() throws Exception {
    scheduleWorkflowControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
            .param("nextPage", "-1")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(406));
  }

  @Test
  public void updateScheduledWorkflow() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    scheduleWorkflowControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(204))
        .andExpect(content().string(""));

    verify(scheduleWorkflowService, times(1)).updateScheduledWorkflow(any(ScheduledWorkflow.class));
  }

  @Test
  public void updateScheduledWorkflow_NoWorkflowFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    doThrow(new NoWorkflowFoundException("Some error")).when(scheduleWorkflowService)
        .updateScheduledWorkflow(any(ScheduledWorkflow.class));
    scheduleWorkflowControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void updateScheduledWorkflow_NoScheduledWorkflowFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    doThrow(new NoScheduledWorkflowFoundException("Some error")).when(scheduleWorkflowService)
        .updateScheduledWorkflow(any(ScheduledWorkflow.class));
    scheduleWorkflowControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void updateScheduledWorkflow_BadContentException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    doThrow(new BadContentException("Some error")).when(scheduleWorkflowService)
        .updateScheduledWorkflow(any(ScheduledWorkflow.class));
    scheduleWorkflowControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(406))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void deleteScheduledWorkflowExecution() throws Exception {
    scheduleWorkflowControllerMock.perform(
        delete(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE_DATASETID, TestObjectFactory.DATASETID)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(204))
        .andExpect(content().string(""));
    verify(scheduleWorkflowService, times(1)).deleteScheduledWorkflow(anyInt());
  }

}
