package eu.europeana.metis.core.rest;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
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
import eu.europeana.metis.core.workflow.OrderField;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.core.workflow.plugins.ValidationExternalPlugin;
import eu.europeana.metis.exception.BadContentException;
import java.util.List;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
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
  public static void setUp() {
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
  public void createWorkflow() throws Exception {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(201))
        .andExpect(content().string(""));

    verify(orchestratorService, times(1)).createWorkflow(any(Workflow.class));
  }

  @Test
  public void createWorkflow_WorkflowAlreadyExistsException() throws Exception {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
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
  public void updateWorkflow() throws Exception {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    orchestratorControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(204))
        .andExpect(content().string(""));

    verify(orchestratorService, times(1)).updateWorkflow(any(Workflow.class));
  }

  @Test
  public void updateWorkflow_NoWorkflowFoundException() throws Exception {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
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
  public void deleteWorkflow() throws Exception {
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
  public void getWorkflow() throws Exception {
    Workflow workflow = TestObjectFactory.createWorkflowObject();
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
  public void getAllWorkflows() throws Exception {
    String workflowOwner = "owner";
    int listSize = 2;
    List<Workflow> listOfWorkflowsSameOwner = TestObjectFactory
        .createListOfUserWorkflowsSameOwner(workflowOwner,
            listSize + 1); //To get the effect of next page
    when(orchestratorService.getWorkflowsPerRequest()).thenReturn(listSize);
    when(orchestratorService.getAllWorkflows(anyString(), anyInt()))
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
  public void getAllWorkflowsNegativeNextPage() throws Exception {
    String workflowOwner = "owner";

    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_OWNER, workflowOwner)
            .param("nextPage", "-1")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(406));
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutions() throws Exception {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    when(orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(anyInt(), anyString(), anyString(),
            (PluginType) isNull(), anyInt())).thenReturn(workflowExecution);
    orchestratorControllerMock.perform(
        post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE,
            TestObjectFactory.DATASETID)
            .param("workflowOwner", "owner")
            .param("workflowName", "workflow")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(201))
        .andExpect(jsonPath("$.workflowStatus", is(WorkflowStatus.INQUEUE.name())));
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutions_WorkflowExecutionAlreadyExistsException()
      throws Exception {
    doThrow(new WorkflowExecutionAlreadyExistsException("Some error")).when(orchestratorService)
        .addWorkflowInQueueOfWorkflowExecutions(anyInt(), anyString(), anyString(),
            (PluginType) isNull(), anyInt());
    orchestratorControllerMock.perform(
        post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE,
            TestObjectFactory.DATASETID)
            .param("workflowOwner", "owner")
            .param("workflowName", "workflow")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(409))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutions_NoDatasetFoundException()
      throws Exception {
    doThrow(new NoDatasetFoundException("Some error")).when(orchestratorService)
        .addWorkflowInQueueOfWorkflowExecutions(anyInt(), anyString(), anyString(),
            (PluginType) isNull(), anyInt());
    orchestratorControllerMock.perform(
        post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE,
            TestObjectFactory.DATASETID)
            .param("workflowOwner", "owner")
            .param("workflowName", "workflow")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutions_NoWorkflowFoundException()
      throws Exception {
    doThrow(new NoWorkflowFoundException("Some error")).when(orchestratorService)
        .addWorkflowInQueueOfWorkflowExecutions(anyInt(), anyString(), anyString(),
            (PluginType) isNull(), anyInt());
    orchestratorControllerMock.perform(
        post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE,
            TestObjectFactory.DATASETID)
            .param("workflowOwner", "owner")
            .param("workflowName", "workflow")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutions_direct() throws Exception {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    when(orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(anyInt(), any(Workflow.class),
            (PluginType) isNull(), anyInt())).thenReturn(workflowExecution);
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    orchestratorControllerMock.perform(
        post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE_DIRECT,
            TestObjectFactory.DATASETID)
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(201))
        .andExpect(jsonPath("$.workflowStatus", is(WorkflowStatus.INQUEUE.name())));
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutions_direct_WorkflowExecutionAlreadyExistsException()
      throws Exception {
    doThrow(new WorkflowExecutionAlreadyExistsException("Some error")).when(orchestratorService)
        .addWorkflowInQueueOfWorkflowExecutions(anyInt(), any(Workflow.class),
            (PluginType) isNull(), anyInt());
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    orchestratorControllerMock.perform(
        post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE_DIRECT,
            TestObjectFactory.DATASETID)
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(409))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutions_direct_NoDatasetFoundException()
      throws Exception {
    doThrow(new NoDatasetFoundException("Some error")).when(orchestratorService)
        .addWorkflowInQueueOfWorkflowExecutions(anyInt(), any(Workflow.class),
            (PluginType) isNull(), anyInt());
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    orchestratorControllerMock.perform(
        post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE_DIRECT,
            TestObjectFactory.DATASETID)
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void addWorkflowInQueueOfWorkflowExecutions_direct_WorkflowAlreadyExistsException()
      throws Exception {
    doThrow(new WorkflowAlreadyExistsException("Some error")).when(orchestratorService)
        .addWorkflowInQueueOfWorkflowExecutions(anyInt(), any(Workflow.class),
            (PluginType) isNull(), anyInt());
    Workflow workflow = TestObjectFactory.createWorkflowObject();
    orchestratorControllerMock.perform(
        post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE_DIRECT,
            TestObjectFactory.DATASETID)
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(TestUtils.convertObjectToJsonBytes(workflow)))
        .andExpect(status().is(409))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void cancelWorkflowExecution() throws Exception {
    doNothing().when(orchestratorService).cancelWorkflowExecution(anyString());
    orchestratorControllerMock.perform(
        delete(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID,
            TestObjectFactory.EXECUTIONID)
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(204))
        .andExpect(content().string(""));
  }

  @Test
  public void cancelWorkflowExecution_NoWorkflowExecutionFoundException() throws Exception {
    doThrow(new NoWorkflowExecutionFoundException("Some error")).when(orchestratorService)
        .cancelWorkflowExecution(anyString());
    orchestratorControllerMock.perform(
        delete(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID,
            TestObjectFactory.EXECUTIONID)
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void getWorkflowExecutionByExecutionId() throws Exception {
    WorkflowExecution workflowExecution = TestObjectFactory
        .createWorkflowExecutionObject();
    workflowExecution.setWorkflowStatus(WorkflowStatus.RUNNING);
    when(orchestratorService.getWorkflowExecutionByExecutionId(anyString()))
        .thenReturn(workflowExecution);
    orchestratorControllerMock.perform(
        get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID,
            TestObjectFactory.EXECUTIONID)
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.workflowStatus", is(WorkflowStatus.RUNNING.name())));
  }


  @Test
  public void getLatestFinishedPluginWorkflowExecutionByDatasetIdIfPluginTypeAllowedForExecution()
      throws Exception {
    AbstractMetisPlugin abstractMetisPlugin = new ValidationExternalPlugin();
    abstractMetisPlugin.setId("validation_external_id");
    when(orchestratorService.getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(
        TestObjectFactory.DATASETID, PluginType.VALIDATION_EXTERNAL, null))
        .thenReturn(abstractMetisPlugin);

    orchestratorControllerMock.perform(
        get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_ALLOWED_PLUGIN,
            TestObjectFactory.DATASETID)
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .param("pluginType", "VALIDATION_EXTERNAL")
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.pluginType", is(PluginType.VALIDATION_EXTERNAL.name())));
  }

  @Test
  public void getLatestFinishedPluginWorkflowExecutionByDatasetIdIfPluginTypeAllowedForExecution_HarvestingPlugin()
      throws Exception {
    when(orchestratorService
        .getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(
            TestObjectFactory.DATASETID, PluginType.OAIPMH_HARVEST, null))
        .thenReturn(null);

    orchestratorControllerMock.perform(
        get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_ALLOWED_PLUGIN,
            TestObjectFactory.DATASETID)
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .param("pluginType", "OAIPMH_HARVEST")
            .content(""))
        .andExpect(status().is(200));
  }

  @Test
  public void getAllWorkflowExecutionsByDatasetId() throws Exception {
    int listSize = 2;
    List<WorkflowExecution> listOfWorkflowExecutions = TestObjectFactory
        .createListOfWorkflowExecutions(listSize + 1); //To get the effect of next page

    when(orchestratorService.getWorkflowExecutionsPerRequest()).thenReturn(listSize);
    when(orchestratorService.getAllWorkflowExecutions(anyInt(), anyString(), anyString(),
        ArgumentMatchers.<WorkflowStatus>anySet(), any(OrderField.class), anyBoolean(), anyInt()))
        .thenReturn(listOfWorkflowExecutions);
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID,
            TestObjectFactory.DATASETID)
            .param("workflowOwner", "owner")
            .param("workflowName", "workflow")
            .param("workflowStatus", WorkflowStatus.INQUEUE.name())
            .param("nextPage", "")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.results", hasSize(listSize + 1)))
        .andExpect(jsonPath("$.results[0].datasetId", is(TestObjectFactory.DATASETID)))
        .andExpect(jsonPath("$.results[0].workflowName", is("workflowName0")))
        .andExpect(jsonPath("$.results[0].workflowStatus", is(WorkflowStatus.INQUEUE.name())))
        .andExpect(jsonPath("$.results[1].datasetId", is(TestObjectFactory.DATASETID + 1)))
        .andExpect(jsonPath("$.results[1].workflowName", is("workflowName1")))
        .andExpect(jsonPath("$.results[1].workflowStatus", is(WorkflowStatus.INQUEUE.name())))
        .andExpect(jsonPath("$.nextPage").isNotEmpty());
  }

  @Test
  public void getAllWorkflowExecutionsByDatasetIdNegativeNextPage() throws Exception {
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID,
            TestObjectFactory.DATASETID)
            .param("workflowOwner", "owner")
            .param("workflowName", "workflow")
            .param("workflowStatus", WorkflowStatus.INQUEUE.name())
            .param("nextPage", "-1")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(406));
  }

  @Test
  public void getAllWorkflowExecutions() throws Exception {
    int listSize = 2;
    List<WorkflowExecution> listOfWorkflowExecutions = TestObjectFactory
        .createListOfWorkflowExecutions(listSize + 1); //To get the effect of next page

    when(orchestratorService.getWorkflowExecutionsPerRequest()).thenReturn(listSize);
    when(orchestratorService.getAllWorkflowExecutions(anyInt(), anyString(), anyString(), ArgumentMatchers.<WorkflowStatus>anySet(), any(OrderField.class), anyBoolean(), anyInt()))
        .thenReturn(listOfWorkflowExecutions);
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS)
            .param("workflowOwner", "owner")
            .param("workflowName", "workflow")
            .param("workflowStatus", WorkflowStatus.INQUEUE.name())
            .param("nextPage", "")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.results", hasSize(listSize + 1)))
        .andExpect(jsonPath("$.results[0].datasetId", is(TestObjectFactory.DATASETID)))
        .andExpect(jsonPath("$.results[0].workflowOwner", is("workflowOwner")))
        .andExpect(jsonPath("$.results[0].workflowName", is("workflowName0")))
        .andExpect(jsonPath("$.results[0].workflowStatus", is(WorkflowStatus.INQUEUE.name())))
        .andExpect(jsonPath("$.results[1].datasetId", is(TestObjectFactory.DATASETID + 1)))
        .andExpect(jsonPath("$.results[1].workflowOwner", is("workflowOwner")))
        .andExpect(jsonPath("$.results[1].workflowName", is("workflowName1")))
        .andExpect(jsonPath("$.results[1].workflowStatus", is(WorkflowStatus.INQUEUE.name())))
        .andExpect(jsonPath("$.nextPage").isNotEmpty());
  }

  @Test
  public void getAllWorkflowExecutionsNegativeNextPage() throws Exception {

    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS)
            .param("workflowStatus", WorkflowStatus.INQUEUE.name())
            .param("nextPage", "-1")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(406));
  }

  @Test
  public void scheduleWorkflowExecution() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(201))
        .andExpect(content().string(""));

    verify(orchestratorService, times(1)).scheduleWorkflow(any(ScheduledWorkflow.class));
  }

  @Test
  public void scheduleWorkflowExecution_BadContentException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    doThrow(new BadContentException("Some error")).when(orchestratorService)
        .scheduleWorkflow(any(ScheduledWorkflow.class));
    orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(406))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void scheduleWorkflowExecution_ScheduledWorkflowAlreadyExistsException()
      throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    doThrow(new ScheduledWorkflowAlreadyExistsException("Some error")).when(orchestratorService)
        .scheduleWorkflow(any(ScheduledWorkflow.class));
    orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(409))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void scheduleWorkflowExecution_NoWorkflowFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    doThrow(new NoWorkflowFoundException("Some error")).when(orchestratorService)
        .scheduleWorkflow(any(ScheduledWorkflow.class));
    orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void scheduleWorkflowExecution_NoDatasetFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    doThrow(new NoDatasetFoundException("Some error")).when(orchestratorService)
        .scheduleWorkflow(any(ScheduledWorkflow.class));
    orchestratorControllerMock.perform(post(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void getScheduledWorkflow() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    when(orchestratorService.getScheduledWorkflowByDatasetId(anyInt()))
        .thenReturn(scheduledWorkflow);
    orchestratorControllerMock.perform(
        get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE_DATASETID,
            TestObjectFactory.DATASETID)
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.scheduleFrequence", is(ScheduleFrequence.ONCE.name())));

    verify(orchestratorService, times(1)).getScheduledWorkflowByDatasetId(anyInt());
  }

  @Test
  public void getAllScheduledWorkflows() throws Exception {
    int listSize = 2;
    List<ScheduledWorkflow> listOfScheduledWorkflows = TestObjectFactory
        .createListOfScheduledWorkflows(listSize + 1);//To get the effect of next page

    when(orchestratorService.getScheduledWorkflowsPerRequest()).thenReturn(listSize);
    when(orchestratorService.getAllScheduledWorkflows(any(ScheduleFrequence.class), anyInt()))
        .thenReturn(listOfScheduledWorkflows);
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
            .param("nextPage", "")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(200))
        .andExpect(jsonPath("$.results", hasSize(listSize + 1)))
        .andExpect(jsonPath("$.results[0].datasetId", is(TestObjectFactory.DATASETID)))
        .andExpect(jsonPath("$.results[0].workflowName", is(TestObjectFactory.WORKFLOWNAME)))
        .andExpect(jsonPath("$.results[0].scheduleFrequence", is(ScheduleFrequence.ONCE.name())))
        .andExpect(jsonPath("$.results[1].datasetId", is(TestObjectFactory.DATASETID + 1)))
        .andExpect(jsonPath("$.results[1].workflowName", is(TestObjectFactory.WORKFLOWNAME)))
        .andExpect(jsonPath("$.results[1].scheduleFrequence", is(ScheduleFrequence.ONCE.name())))
        .andExpect(jsonPath("$.nextPage").isNotEmpty());
  }

  @Test
  public void getAllScheduledWorkflowsNegativeNextPage() throws Exception {
    orchestratorControllerMock
        .perform(get(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
            .param("nextPage", "-1")
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(406));
  }

  @Test
  public void updateScheduledWorkflow() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    orchestratorControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(204))
        .andExpect(content().string(""));

    verify(orchestratorService, times(1)).updateScheduledWorkflow(any(ScheduledWorkflow.class));
  }

  @Test
  public void updateScheduledWorkflow_NoWorkflowFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    doThrow(new NoWorkflowFoundException("Some error")).when(orchestratorService)
        .updateScheduledWorkflow(any(ScheduledWorkflow.class));
    orchestratorControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void updateScheduledWorkflow_NoScheduledWorkflowFoundException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    doThrow(new NoScheduledWorkflowFoundException("Some error")).when(orchestratorService)
        .updateScheduledWorkflow(any(ScheduledWorkflow.class));
    orchestratorControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(404))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void updateScheduledWorkflow_BadContentException() throws Exception {
    ScheduledWorkflow scheduledWorkflow = TestObjectFactory
        .createScheduledWorkflowObject();
    doThrow(new BadContentException("Some error")).when(orchestratorService)
        .updateScheduledWorkflow(any(ScheduledWorkflow.class));
    orchestratorControllerMock.perform(put(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE)
        .contentType(TestUtils.APPLICATION_JSON_UTF8)
        .content(TestUtils.convertObjectToJsonBytes(scheduledWorkflow)))
        .andExpect(status().is(406))
        .andExpect(content().string("{\"errorMessage\":\"Some error\"}"));
  }

  @Test
  public void deleteScheduledWorkflowExecution() throws Exception {
    orchestratorControllerMock.perform(
        delete(RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE_DATASETID, TestObjectFactory.DATASETID)
            .contentType(TestUtils.APPLICATION_JSON_UTF8)
            .content(""))
        .andExpect(status().is(204))
        .andExpect(content().string(""));
    verify(orchestratorService, times(1)).deleteScheduledWorkflow(anyInt());
  }
}
