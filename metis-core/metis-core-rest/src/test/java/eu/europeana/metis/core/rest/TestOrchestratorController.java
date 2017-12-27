package eu.europeana.metis.core.rest;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
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
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoScheduledWorkflowFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowFoundException;
import eu.europeana.metis.core.exceptions.ScheduledWorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.WorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.WorkflowExecutionAlreadyExistsException;
import eu.europeana.metis.core.rest.exception.RestResponseExceptionHandler;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.test.utils.TestObjectFactory;
import eu.europeana.metis.core.test.utils.TestUtils;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import java.util.List;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-06
 */
public class TestOrchestratorController {

  private static OrchestratorService orchestratorService;
  private static MockMvc orchestratorControllerMock;

  @BeforeClass
  public static void setUp() throws Exception {
    orchestratorService = mock(OrchestratorService.class);
    OrchestratorController orchestratorController = new OrchestratorController(orchestratorService);
    orchestratorControllerMock = MockMvcBuilders
        .standaloneSetup(orchestratorController)
        .setControllerAdvice(new RestResponseExceptionHandler())
        .build();
  }

  @After
  public void cleanUp() {
    Mockito.reset(orchestratorService);
  }

  @Test
  public void createUserWorkflow() throws Exception {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(201))
        .andExpect(content().string(""));

    verify(orchestratorService, times(1)).createWorkflow(any(Workflow.class));
  }

  @Test
  public void createUserWorkflow_UserWorkflowAlreadyExistsException() throws Exception {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    doThrow(new WorkflowAlreadyExistsException("Some error")).when(orchestratorService)
        .createWorkflow(any(Workflow.class));
    orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(409))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));

    verify(orchestratorService, times(1)).createWorkflow(any(Workflow.class));
  }

  @Test
  public void updateUserWorkflow() throws Exception {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    orchestratorControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(204))
        .andExpect(content().string(""));

    verify(orchestratorService, times(1)).updateWorkflow(any(Workflow.class));
  }

  @Test
  public void updateUserWorkflow_NoUserWorkflowFoundException() throws Exception {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    doThrow(new NoWorkflowFoundException("Some error")).when(orchestratorService)
        .updateWorkflow(any(Workflow.class));
    orchestratorControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));

    verify(orchestratorService, times(1)).updateWorkflow(any(Workflow.class));
  }

  @Test
  public void deleteUserWorkflow() throws Exception {
    orchestratorControllerMock.perform(delete(RestEndpoints.ORCHESTRATOR_WORKFLOWS)
        .param("workflowOwner", "owner")
        .param("workflowName", "workflow")
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(""))
        .andExpect(status().is(204))
        .andExpect(content().string(""));
    verify(orchestratorService, times(1)).deleteWorkflow(anyString(), anyString());
  }

  @Test
  public void getUserWorkflow() throws Exception {
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    String workflowName = workflow.getWorkflowName();
    when(orchestratorService.getWorkflow(anyString(), anyString())).thenReturn(workflow);
    orchestratorControllerMock.perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS)
        .param("workflowOwner", "owner")
        .param("workflowName", "workflow")
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.workflowName", is(workflowName)));

    verify(orchestratorService, times(1)).getWorkflow(anyString(), anyString());
  }

  @Test
  public void getAllUserWorkflows() throws Exception {
    String workflowOwner = "owner";
    int listSize = 2;
    List<Workflow> listOfWorkflowsSameOwner = TestObjectFactory
        .createListOfUserWorkflowsSameOwner(workflowOwner,
            listSize + 1); //To get the effect of next page
    when(orchestratorService.getWorkflowsPerRequest()).thenReturn(listSize);
    when(orchestratorService.getAllWorkflows(anyString(), anyString()))
        .thenReturn(listOfWorkflowsSameOwner);
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_OWNER, workflowOwner)
            .param("nextPage", "")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.results", hasSize(listSize + 1)))
        .andExpect(jsonPath("$.results[0].workflowOwner", is(workflowOwner)))
        .andExpect(jsonPath("$.results[0].workflowName", is("workflowName0")))
        .andExpect(jsonPath("$.results[1].workflowOwner", is(workflowOwner)))
        .andExpect(jsonPath("$.results[1].workflowName", is("workflowName1")))
        .andExpect(jsonPath("$.nextPage").isNotEmpty());
  }

  @Test
  public void addUserWorkflowInQueueOfUserWorkflowExecutions() throws Exception {
    doNothing().when(orchestratorService)
        .addWorkflowInQueueOfWorkflowExecutions(anyString(), anyString(), anyString(),
            anyInt());
    orchestratorControllerMock.perform(
        post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE,
            TestObjectFactory.DATASETNAME)
            .param("workflowOwner", "owner")
            .param("workflowName", "workflow")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(201))
        .andExpect(content().string(""));
  }

  @Test
  public void addUserWorkflowInQueueOfUserWorkflowExecutions_UserWorkflowExecutionAlreadyExistsException()
      throws Exception {
    doThrow(new WorkflowExecutionAlreadyExistsException("Some error")).when(orchestratorService)
        .addWorkflowInQueueOfWorkflowExecutions(anyString(), anyString(), anyString(),
            anyInt());
    orchestratorControllerMock.perform(
        post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE,
            TestObjectFactory.DATASETNAME)
            .param("workflowOwner", "owner")
            .param("workflowName", "workflow")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(409))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void addUserWorkflowInQueueOfUserWorkflowExecutions_NoDatasetFoundException()
      throws Exception {
    doThrow(new NoDatasetFoundException("Some error")).when(orchestratorService)
        .addWorkflowInQueueOfWorkflowExecutions(anyString(), anyString(), anyString(),
            anyInt());
    orchestratorControllerMock.perform(
        post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE,
            TestObjectFactory.DATASETNAME)
            .param("workflowOwner", "owner")
            .param("workflowName", "workflow")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void addUserWorkflowInQueueOfUserWorkflowExecutions_NoUserWorkflowFoundException()
      throws Exception {
    doThrow(new NoWorkflowFoundException("Some error")).when(orchestratorService)
        .addWorkflowInQueueOfWorkflowExecutions(anyString(), anyString(), anyString(),
            anyInt());
    orchestratorControllerMock.perform(
        post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE,
            TestObjectFactory.DATASETNAME)
            .param("workflowOwner", "owner")
            .param("workflowName", "workflow")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void addUserWorkflowInQueueOfUserWorkflowExecutions_direct() throws Exception {
    doNothing().when(orchestratorService)
        .addWorkflowInQueueOfWorkflowExecutions(anyString(), any(Workflow.class),
            anyInt());
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    orchestratorControllerMock.perform(
        post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE_DIRECT,
            TestObjectFactory.DATASETNAME)
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(201))
        .andExpect(content().string(""));
  }

  @Test
  public void addUserWorkflowInQueueOfUserWorkflowExecutions_direct_UserWorkflowExecutionAlreadyExistsException()
      throws Exception {
    doThrow(new WorkflowExecutionAlreadyExistsException("Some error")).when(orchestratorService)
        .addWorkflowInQueueOfWorkflowExecutions(anyString(), any(Workflow.class),
            anyInt());
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    orchestratorControllerMock.perform(
        post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE_DIRECT,
            TestObjectFactory.DATASETNAME)
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(409))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void addUserWorkflowInQueueOfUserWorkflowExecutions_direct_NoDatasetFoundException()
      throws Exception {
    doThrow(new NoDatasetFoundException("Some error")).when(orchestratorService)
        .addWorkflowInQueueOfWorkflowExecutions(anyString(), any(Workflow.class),
            anyInt());
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    orchestratorControllerMock.perform(
        post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE_DIRECT,
            TestObjectFactory.DATASETNAME)
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void addUserWorkflowInQueueOfUserWorkflowExecutions_direct_UserWorkflowAlreadyExistsException()
      throws Exception {
    doThrow(new WorkflowAlreadyExistsException("Some error")).when(orchestratorService)
        .addWorkflowInQueueOfWorkflowExecutions(anyString(), any(Workflow.class),
            anyInt());
    Workflow workflow = TestObjectFactory.createUserWorkflowObject();
    orchestratorControllerMock.perform(
        post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE_DIRECT,
            TestObjectFactory.DATASETNAME)
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(409))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void cancelUserWorkflowExecution() throws Exception {
    doNothing().when(orchestratorService).cancelWorkflowExecution(anyString());
    orchestratorControllerMock.perform(
        delete(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTION_DATASETID,
            TestObjectFactory.DATASETNAME)
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(204))
        .andExpect(content().string(""));
  }

  @Test
  public void cancelUserWorkflowExecution_NoUserWorkflowExecutionFoundException() throws Exception {
    doThrow(new NoWorkflowExecutionFoundException("Some error")).when(orchestratorService)
        .cancelWorkflowExecution(anyString());
    orchestratorControllerMock.perform(
        delete(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTION_DATASETID,
            TestObjectFactory.DATASETNAME)
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void getRunningUserWorkflowExecution() throws Exception {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createUserWorkflowExecutionObject();
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    when(orchestratorService.getRunningWorkflowExecution(anyString()))
        .thenReturn(workflowExecution);
    orchestratorControllerMock.perform(
        get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTION_DATASETID,
            TestObjectFactory.DATASETNAME)
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.workflowStatus", is(WorkflowStatus.RUNNING.name())));
  }

  @Test
  public void getAllUserWorkflowExecutions() throws Exception {
    int listSize = 2;
    List<WorkflowExecution> listOfWorkflowExecutions = TestObjectFactory
        .createListOfUserWorkflowExecutions(listSize + 1); //To get the effect of next page

    when(orchestratorService.getWorkflowExecutionsPerRequest()).thenReturn(listSize);
    when(orchestratorService.getAllWorkflowExecutions(anyString(), anyString(), anyString(),
        any(WorkflowStatus.class), anyString())).thenReturn(listOfWorkflowExecutions);
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASETID,
            TestObjectFactory.DATASETNAME)
            .param("workflowOwner", "owner")
            .param("workflowName", "workflow")
            .param("workflowStatus", WorkflowStatus.INQUEUE.name())
            .param("nextPage", "")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.results", hasSize(listSize + 1)))
        .andExpect(jsonPath("$.results[0].datasetId", is(String.format("%s%s", TestObjectFactory.DATASETID, 0))))
        .andExpect(jsonPath("$.results[0].workflowName", is("workflowName0")))
        .andExpect(jsonPath("$.results[0].workflowStatus", is(WorkflowStatus.INQUEUE.name())))
        .andExpect(jsonPath("$.results[1].datasetId", is(String.format("%s%s", TestObjectFactory.DATASETID, 1))))
        .andExpect(jsonPath("$.results[1].workflowName", is("workflowName1")))
        .andExpect(jsonPath("$.results[1].workflowStatus", is(WorkflowStatus.INQUEUE.name())))
        .andExpect(jsonPath("$.nextPage").isNotEmpty());
  }

  @Test
  public void getAllUserWorkflowExecutionsByWorkflowStatus() throws Exception {
    int listSize = 2;
    List<WorkflowExecution> listOfWorkflowExecutions = TestObjectFactory
        .createListOfUserWorkflowExecutions(listSize + 1); //To get the effect of next page

    when(orchestratorService.getWorkflowExecutionsPerRequest()).thenReturn(listSize);
    when(orchestratorService.getAllWorkflowExecutions(any(WorkflowStatus.class), anyString()))
        .thenReturn(listOfWorkflowExecutions);
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS)
            .param("workflowStatus", WorkflowStatus.INQUEUE.name())
            .param("nextPage", "")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.results", hasSize(listSize + 1)))
        .andExpect(jsonPath("$.results[0].datasetId", is(String.format("%s%s", TestObjectFactory.DATASETID, 0))))
        .andExpect(jsonPath("$.results[0].workflowName", is("workflowName0")))
        .andExpect(jsonPath("$.results[0].workflowStatus", is(WorkflowStatus.INQUEUE.name())))
        .andExpect(jsonPath("$.results[1].datasetId", is(String.format("%s%s", TestObjectFactory.DATASETID, 1))))
        .andExpect(jsonPath("$.results[1].workflowName", is("workflowName1")))
        .andExpect(jsonPath("$.results[1].workflowStatus", is(WorkflowStatus.INQUEUE.name())))
        .andExpect(jsonPath("$.nextPage").isNotEmpty());
  }

  @Test
  public void scheduleUserWorkflowExecution() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(201))
        .andExpect(content().string(""));

    verify(orchestratorService, times(1)).scheduleWorkflow(any(ScheduledWorkflow.class));
  }

  @Test
  public void scheduleUserWorkflowExecution_BadContentException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    doThrow(new BadContentException("Some error")).when(orchestratorService)
        .scheduleWorkflow(any(ScheduledWorkflow.class));
    orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(406))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void scheduleUserWorkflowExecution_ScheduledUserWorkflowAlreadyExistsException()
      throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    doThrow(new ScheduledWorkflowAlreadyExistsException("Some error")).when(orchestratorService)
        .scheduleWorkflow(any(ScheduledWorkflow.class));
    orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(409))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void scheduleUserWorkflowExecution_NoUserWorkflowFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    doThrow(new NoWorkflowFoundException("Some error")).when(orchestratorService)
        .scheduleWorkflow(any(ScheduledWorkflow.class));
    orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void scheduleUserWorkflowExecution_NoDatasetFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    doThrow(new NoDatasetFoundException("Some error")).when(orchestratorService)
        .scheduleWorkflow(any(ScheduledWorkflow.class));
    orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void getScheduledUserWorkflow() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    when(orchestratorService.getScheduledWorkflowByDatasetId(anyString()))
        .thenReturn(scheduledWorkflow);
    orchestratorControllerMock.perform(
        get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE_DATASETID,
            TestObjectFactory.DATASETNAME)
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.scheduleFrequence", is(ScheduleFrequence.ONCE.name())));

    verify(orchestratorService, times(1)).getScheduledWorkflowByDatasetId(anyString());
  }

  @Test
  public void getAllScheduledUserWorkflows() throws Exception
  {
    int listSize = 2;
    List<ScheduledWorkflow> listOfScheduledWorkflows = TestObjectFactory
        .createListOfScheduledUserWorkflows(listSize + 1);//To get the effect of next page

    when(orchestratorService.getScheduledWorkflowsPerRequest()).thenReturn(listSize);
    when(orchestratorService.getAllScheduledWorkflows(any(ScheduleFrequence.class), anyString()))
        .thenReturn(listOfScheduledWorkflows);
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
            .param("nextPage", "")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.results", hasSize(listSize + 1)))
        .andExpect(jsonPath("$.results[0].datasetId", is(String.format("%s%s", TestObjectFactory.DATASETID, 0))))
        .andExpect(jsonPath("$.results[0].workflowName", is(TestObjectFactory.WORKFLOWNAME)))
        .andExpect(jsonPath("$.results[0].scheduleFrequence", is(ScheduleFrequence.ONCE.name())))
        .andExpect(jsonPath("$.results[1].datasetId", is(String.format("%s%s", TestObjectFactory.DATASETID, 1))))
        .andExpect(jsonPath("$.results[1].workflowName", is(TestObjectFactory.WORKFLOWNAME)))
        .andExpect(jsonPath("$.results[1].scheduleFrequence", is(ScheduleFrequence.ONCE.name())))
        .andExpect(jsonPath("$.nextPage").isNotEmpty());
  }

  @Test
  public void updateScheduledUserWorkflow() throws Exception
  {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    orchestratorControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(204))
        .andExpect(content().string(""));

    verify(orchestratorService, times(1)).updateScheduledWorkflow(any(ScheduledWorkflow.class));
  }

  @Test
  public void updateScheduledUserWorkflow_NoUserWorkflowFoundException() throws Exception
  {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    doThrow(new NoWorkflowFoundException("Some error")).when(orchestratorService).updateScheduledWorkflow(any(ScheduledWorkflow.class));
    orchestratorControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void updateScheduledUserWorkflow_NoScheduledUserWorkflowFoundException() throws Exception
  {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    doThrow(new NoScheduledWorkflowFoundException("Some error")).when(orchestratorService).updateScheduledWorkflow(any(ScheduledWorkflow.class));
    orchestratorControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void updateScheduledUserWorkflow_BadContentException() throws Exception
  {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledUserWorkflowObject();
    doThrow(new BadContentException("Some error")).when(orchestratorService).updateScheduledWorkflow(any(ScheduledWorkflow.class));
    orchestratorControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(406))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void deleteScheduledUserWorkflowExecution() throws Exception
  {
    orchestratorControllerMock.perform(delete(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE_DATASETID, TestObjectFactory.DATASETNAME)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(""))
        .andExpect(status().is(204))
        .andExpect(content().string(""));
    verify(orchestratorService, times(1)).deleteScheduledWorkflow(anyString());
  }

}
