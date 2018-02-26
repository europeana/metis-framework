package eu.europeana.metis.core.rest;

import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.core.exceptions.NoWorkflowFoundException;
import eu.europeana.metis.core.execution.ExecutionRules;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.workflow.OrderField;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;
import java.util.Set;
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
      throws GenericMetisException {
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
      @RequestParam(value = "nextPage", required = false, defaultValue = "0") int nextPage)
      throws GenericMetisException {
    if (nextPage < 0) {
      throw new BadContentException(CommonStringValues.NEXT_PAGE_CANNOT_BE_NEGATIVE);
    }
    ResponseListWrapper<Workflow> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper
        .setResultsAndLastPage(orchestratorService.getAllWorkflows(workflowOwner, nextPage),
            orchestratorService.getWorkflowsPerRequest(), nextPage);
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
      @RequestParam(value = "enforcedPluginType", required = false, defaultValue = "") PluginType enforcedPluginType,
      @RequestParam(value = "priority", defaultValue = "0") int priority)
      throws GenericMetisException {
    WorkflowExecution workflowExecution = orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(datasetId, workflowOwner,
            workflowName, enforcedPluginType, priority);
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
      @RequestParam(value = "enforcedPluginType", required = false, defaultValue = "") PluginType enforcedPluginType,
      @RequestParam(value = "priority", defaultValue = "0") int priority)
      throws GenericMetisException {
    WorkflowExecution workflowExecution = orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(datasetId, workflow, enforcedPluginType, priority);
    LOGGER.info(
        "WorkflowExecution for datasetId '{}' with workflowOwner '{}' started", datasetId,
        workflow.getWorkflowOwner());
    return workflowExecution;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID, method = RequestMethod.DELETE, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void cancelWorkflowExecution(
      @PathVariable("executionId") String executionId)
      throws GenericMetisException {
    orchestratorService.cancelWorkflowExecution(executionId);
    LOGGER.info(
        "WorkflowExecution for executionId '{}' is cancelling",
        executionId);
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID, method = RequestMethod.GET, produces = {
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

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_ALLOWED_PLUGIN, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public AbstractMetisPlugin getLatestFinishedPluginWorkflowExecutionByDatasetIdIfPluginTypeAllowedForExecution(
      @PathVariable("datasetId") int datasetId,
      @RequestParam("pluginType") PluginType pluginType,
      @RequestParam(value = "enforcedPluginType", required = false, defaultValue = "") PluginType enforcedPluginType)
      throws GenericMetisException {
    AbstractMetisPlugin latestFinishedPluginWorkflowExecutionByDatasetId = orchestratorService
        .getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(datasetId, pluginType,
            enforcedPluginType);
    if (latestFinishedPluginWorkflowExecutionByDatasetId != null) {
      LOGGER.info("Latest Plugin WorkflowExecution with id '{}' found",
          latestFinishedPluginWorkflowExecutionByDatasetId.getId());
    } else if (ExecutionRules.getHarvestPluginGroup().contains(pluginType)) {
      LOGGER.info("PluginType allowed by default");
    }
    return latestFinishedPluginWorkflowExecutionByDatasetId;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<WorkflowExecution> getAllWorkflowExecutionsByDatasetId(
      @PathVariable("datasetId") int datasetId,
      @RequestParam(value = "workflowOwner", required = false) String workflowOwner,
      @RequestParam(value = "workflowName", required = false) String workflowName,
      @RequestParam(value = "workflowStatus", required = false) Set<WorkflowStatus> workflowStatuses,
      @RequestParam(value = "orderField", required = false, defaultValue = "ID") OrderField orderField,
      @RequestParam(value = "ascending", required = false, defaultValue = "true") boolean ascending,
      @RequestParam(value = "nextPage", required = false, defaultValue = "0") int nextPage)
      throws GenericMetisException {
    if (nextPage < 0) {
      throw new BadContentException(CommonStringValues.NEXT_PAGE_CANNOT_BE_NEGATIVE);
    }
    ResponseListWrapper<WorkflowExecution> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper.setResultsAndLastPage(orchestratorService
            .getAllWorkflowExecutions(datasetId, workflowOwner, workflowName, workflowStatuses,
                orderField, ascending, nextPage),
        orchestratorService.getWorkflowExecutionsPerRequest(), nextPage);
    LOGGER.info("Batch of: {} workflowExecutions returned, using batch nextPage: {}",
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<WorkflowExecution> getAllWorkflowExecutions(
      @RequestParam(value = "workflowOwner", required = false) String workflowOwner,
      @RequestParam(value = "workflowName", required = false) String workflowName,
      @RequestParam(value = "workflowStatus", required = false) Set<WorkflowStatus> workflowStatuses,
      @RequestParam(value = "orderField", required = false, defaultValue = "ID") OrderField orderField,
      @RequestParam(value = "ascending", required = false, defaultValue = "true") boolean ascending,
      @RequestParam(value = "nextPage", required = false, defaultValue = "0") int nextPage)
      throws GenericMetisException {
    if (nextPage < 0) {
      throw new BadContentException(CommonStringValues.NEXT_PAGE_CANNOT_BE_NEGATIVE);
    }
    ResponseListWrapper<WorkflowExecution> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper.setResultsAndLastPage(orchestratorService
            .getAllWorkflowExecutions(-1, workflowOwner, workflowName, workflowStatuses,
                orderField, ascending, nextPage),
        orchestratorService.getWorkflowExecutionsPerRequest(), nextPage);
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
      throws GenericMetisException {
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
      @RequestParam(value = "nextPage", required = false, defaultValue = "0") int nextPage)
      throws GenericMetisException {
    if (nextPage < 0) {
      throw new BadContentException(CommonStringValues.NEXT_PAGE_CANNOT_BE_NEGATIVE);
    }
    ResponseListWrapper<ScheduledWorkflow> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper.setResultsAndLastPage(orchestratorService
            .getAllScheduledWorkflows(ScheduleFrequence.NULL, nextPage),
        orchestratorService.getScheduledWorkflowsPerRequest(), nextPage);
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
      throws GenericMetisException {
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
}
