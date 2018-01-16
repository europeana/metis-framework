package eu.europeana.metis.core.rest;

import eu.europeana.cloud.common.model.dps.SubTaskInfo;
import eu.europeana.cloud.common.model.dps.TaskErrorsInfo;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoScheduledWorkflowFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowFoundException;
import eu.europeana.metis.core.exceptions.ScheduledWorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.WorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.WorkflowExecutionAlreadyExistsException;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-28
 */
@Controller
public class OrchestratorController {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrchestratorController.class);
  private final OrchestratorService orchestratorService;

  @Autowired
  public OrchestratorController(OrchestratorService orchestratorService) {
    this.orchestratorService = orchestratorService;
  }

  //WORKFLOWS
  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS, method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public void createWorkflow(
      @RequestBody Workflow workflow)
      throws WorkflowAlreadyExistsException {
    orchestratorService.createWorkflow(workflow);
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS, method = RequestMethod.PUT, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void updateWorkflow(
      @RequestBody Workflow workflow) throws NoWorkflowFoundException {
    orchestratorService.updateWorkflow(workflow);
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS, method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteWorkflow(@RequestParam("workflowOwner") String workflowOwner,
      @RequestParam("workflowName") String workflowName) {
    orchestratorService.deleteWorkflow(workflowOwner, workflowName);
    LOGGER.info("Workflow with workflowOwner '{}' and workflowName '{}' deleted", workflowOwner,
        workflowName);
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Workflow getWorkflow(@RequestParam("workflowOwner") String workflowOwner,
      @RequestParam("workflowName") String workflowName) {
    Workflow workflow = orchestratorService
        .getWorkflow(workflowOwner, workflowName);
    LOGGER.info(
        "Workflow with workflowOwner '{}' and workflowName '{}' found", workflowOwner,
        workflowName);
    return workflow;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_OWNER, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<Workflow> getAllWorkflows(
      @PathVariable("workflowOwner") String workflowOwner,
      @RequestParam(value = "nextPage", required = false) String nextPage) {
    ResponseListWrapper<Workflow> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper.setResultsAndLastPage(orchestratorService
            .getAllWorkflows(workflowOwner, nextPage),
        orchestratorService.getWorkflowsPerRequest());
    LOGGER.info("Batch of: {} workflows returned, using batch nextPage: {}",
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }


  //WORKFLOW EXECUTIONS
  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE, method = RequestMethod.POST, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public WorkflowExecution addWorkflowInQueueOfWorkflowExecutions(
      @PathVariable("datasetId") int datasetId,
      @RequestParam("workflowOwner") String workflowOwner,
      @RequestParam("workflowName") String workflowName,
      @RequestParam(value = "priority", defaultValue = "0") int priority)
      throws WorkflowExecutionAlreadyExistsException, NoDatasetFoundException, NoWorkflowFoundException {
    WorkflowExecution workflowExecution = orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(datasetId, workflowOwner,
            workflowName, priority);
    LOGGER.info(
        "WorkflowExecution for datasetId '{}' with workflowOwner '{}' and workflowName '{}' added to queue",
        datasetId, workflowOwner, workflowName);
    return workflowExecution;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE_DIRECT, method = RequestMethod.POST, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public WorkflowExecution addWorkflowInQueueOfWorkflowExecutions(
      @PathVariable("datasetId") int datasetId, @RequestBody Workflow workflow,
      @RequestParam(value = "priority", defaultValue = "0") int priority)
      throws WorkflowExecutionAlreadyExistsException, NoDatasetFoundException, WorkflowAlreadyExistsException {
    WorkflowExecution workflowExecution = orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(datasetId, workflow, priority);
    LOGGER.info(
        "WorkflowExecution for datasetId '{}' with workflowOwner '{}' started", datasetId,
        workflow.getWorkflowOwner());
    return workflowExecution;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTION_EXECUTIONID, method = RequestMethod.DELETE, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void cancelWorkflowExecution(
      @PathVariable("executionId") String executionId)
      throws NoWorkflowExecutionFoundException {
    orchestratorService.cancelWorkflowExecution(executionId);
    LOGGER.info(
        "WorkflowExecution for executionId '{}' is cancelling",
        executionId);
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTION_EXECUTIONID, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public WorkflowExecution getWorkflowExecutionByExecutionId(
      @PathVariable("executionId") String executionId) {
    WorkflowExecution workflowExecution = orchestratorService
        .getWorkflowExecutionByExecutionId(executionId);
    LOGGER.info("WorkflowExecution with executionId '{}' found", executionId);
    return workflowExecution;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASETID, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<WorkflowExecution> getAllWorkflowExecutions(
      @PathVariable("datasetId") int datasetId,
      @RequestParam("workflowOwner") String workflowOwner,
      @RequestParam("workflowName") String workflowName,
      @RequestParam("workflowStatus") WorkflowStatus workflowStatus,
      @RequestParam(value = "nextPage", required = false) String nextPage) {
    ResponseListWrapper<WorkflowExecution> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper.setResultsAndLastPage(orchestratorService
            .getAllWorkflowExecutions(datasetId, workflowOwner, workflowName, workflowStatus,
                nextPage),
        orchestratorService.getWorkflowExecutionsPerRequest());
    LOGGER.info("Batch of: {} workflowExecutions returned, using batch nextPage: {}",
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<WorkflowExecution> getAllWorkflowExecutions(
      @RequestParam("workflowStatus") WorkflowStatus workflowStatus,
      @RequestParam(value = "nextPage", required = false) String nextPage) {
    ResponseListWrapper<WorkflowExecution> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper.setResultsAndLastPage(orchestratorService
            .getAllWorkflowExecutions(workflowStatus, nextPage),
        orchestratorService.getWorkflowExecutionsPerRequest());
    LOGGER.info("Batch of: {} workflowExecutions returned, using batch nextPage: {}",
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  //SCHEDULED WORKFLOWS
  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE, method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public void scheduleWorkflowExecution(
      @RequestBody ScheduledWorkflow scheduledWorkflow)
      throws BadContentException, ScheduledWorkflowAlreadyExistsException, NoWorkflowFoundException, NoDatasetFoundException {
    orchestratorService.scheduleWorkflow(scheduledWorkflow);
    LOGGER.info(
        "ScheduledWorkflowExecution for datasetId '{}', workflowOwner '{}', workflowName '{}', pointerDate at '{}', scheduled '{}'",
        scheduledWorkflow.getDatasetId(),
        scheduledWorkflow.getWorkflowOwner(), scheduledWorkflow.getWorkflowName(),
        scheduledWorkflow.getPointerDate(),
        scheduledWorkflow.getScheduleFrequence().name());
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE_DATASETID, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ScheduledWorkflow getScheduledWorkflow(
      @PathVariable("datasetId") int datasetId) {
    ScheduledWorkflow scheduledWorkflow = orchestratorService
        .getScheduledWorkflowByDatasetId(datasetId);
    LOGGER.info("ScheduledWorkflow with with datasetId '{}' found", datasetId);
    return scheduledWorkflow;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<ScheduledWorkflow> getAllScheduledWorkflows(
      @RequestParam(value = "nextPage", required = false) String nextPage) {
    ResponseListWrapper<ScheduledWorkflow> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper.setResultsAndLastPage(orchestratorService
            .getAllScheduledWorkflows(ScheduleFrequence.NULL, nextPage),
        orchestratorService.getScheduledWorkflowsPerRequest());
    LOGGER.info("Batch of: {} scheduledWorkflows returned, using batch nextPage: {}",
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE, method = RequestMethod.PUT, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void updateScheduledWorkflow(
      @RequestBody ScheduledWorkflow scheduledWorkflow)
      throws BadContentException, NoScheduledWorkflowFoundException, NoWorkflowFoundException {
    orchestratorService.updateScheduledWorkflow(scheduledWorkflow);
    LOGGER.info("ScheduledWorkflow with with datasetId '{}' updated",
        scheduledWorkflow.getDatasetId());
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE_DATASETID, method = RequestMethod.DELETE, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void deleteScheduledWorkflowExecution(
      @PathVariable("datasetId") int datasetId) {
    orchestratorService.deleteScheduledWorkflow(datasetId);
    LOGGER.info(
        "ScheduledWorkflowExecution for datasetId '{}' deleted",
        datasetId);
  }

  //PROXIES
  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_LOGS, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<SubTaskInfo> getExternalTaskLogs(
      @PathVariable("topologyName") String topologyName,
      @PathVariable("externalTaskId") long externalTaskId,
      @RequestParam(value = "from") int from,
      @RequestParam(value = "to") int to) {
    LOGGER.info(
        "Requesting proxy call task logs for topologyName: {}, externalTaskId: {}, from: {}, to: {}",
        topologyName, externalTaskId, from, to);
    return orchestratorService.getExternalTaskLogs(topologyName, externalTaskId, from, to);
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_REPORT, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public TaskErrorsInfo getExternalTaskReport(
      @PathVariable("topologyName") String topologyName,
      @PathVariable("externalTaskId") long externalTaskId) {
    LOGGER.info(
        "Requesting proxy call task reports for topologyName: {}, externalTaskId: {}",
        topologyName, externalTaskId);
    return orchestratorService.getExternalTaskReport(topologyName, externalTaskId);
  }
}
