package eu.europeana.metis.core.rest;

import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.core.dataset.DatasetExecutionInformation;
import eu.europeana.metis.core.execution.ExecutionRules;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.workflow.OrderField;
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
 * Contains all the calls that are related to Orchestration.
 * <p>The {@link OrchestratorService} has control on how to orchestrate different components of the system</p>
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-28
 */
@Controller
public class OrchestratorController {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrchestratorController.class);
  private final OrchestratorService orchestratorService;

  /**
   * Autowired constructor with all required parameters.
   *
   * @param orchestratorService the orchestratorService object
   */
  @Autowired
  public OrchestratorController(OrchestratorService orchestratorService) {
    this.orchestratorService = orchestratorService;
  }

  /**
   * Create a workflow using a datasetId and the {@link Workflow} that contains the requested plugins.
   * When creating a new workflow all the plugins specified will be automatically enabled.
   *
   * @param datasetId the dataset identifier to relate the workflow to
   * @param workflow the Workflow will all it's requested plugins
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.core.exceptions.WorkflowAlreadyExistsException} if a workflow for the dataset identifier provided already exists</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoDatasetFoundException} if the dataset identifier provided does not exist</li>
   * </ul>
   */
  //WORKFLOWS
  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID, method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public void createWorkflow(@PathVariable("datasetId") int datasetId,
      @RequestBody Workflow workflow)
      throws GenericMetisException {
    orchestratorService.createWorkflow(datasetId, workflow);
  }

  /**
   * Update an already existent workflow using a datasetId and the {@link Workflow} that contains the requested plugins.
   * When updating an existent workflow all specified plugins will be enabled and all plugins that were existent in the system
   * beforehand will be kept with their configuration but will be disabled.
   *
   * @param datasetId the identifier of the dataset for which the workflow should be updated
   * @param workflow the workflow with the plugins requested
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowFoundException} if a workflow for the dataset identifier provided does not exist</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoDatasetFoundException} if the dataset identifier provided does not exist</li>
   * </ul>
   */
  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID, method = RequestMethod.PUT, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void updateWorkflow(@PathVariable("datasetId") int datasetId,
      @RequestBody Workflow workflow) throws GenericMetisException {
    orchestratorService.updateWorkflow(datasetId, workflow);
  }

  /**
   * Deletes a workflow.
   *
   * @param datasetId the dataset identifier that corresponds to the workflow to be deleted
   */
  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID, method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteWorkflow(@PathVariable("datasetId") int datasetId) {
    orchestratorService.deleteWorkflow(datasetId);
    LOGGER.info("Workflow with datasetId '{}' deleted",
        datasetId);
  }

  /**
   * Get a workflow for a dataset identifier.
   *
   * @param datasetId the dataset identifier
   * @return the Workflow object
   */
  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Workflow getWorkflow(@PathVariable("datasetId") int datasetId) {
    Workflow workflow = orchestratorService.getWorkflow(datasetId);
    LOGGER.info("Workflow with datasetId '{}' found", datasetId);
    return workflow;
  }

  /**
   * Get all workflows for a workflow owner paged.
   *
   * @param workflowOwner the workflow owner used as a fielter
   * @param nextPage the nextPage token, the end of the list is marked with -1 on the response
   * @return a list of the Workflow objects
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link BadContentException} if paging is not correctly provided</li>
   * </ul>
   */
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

  /**
   * Does checking, prepares and adds a WorkflowExecution in the queue.
   * That means it updates the status of the WorkflowExecution to {@link WorkflowStatus#INQUEUE}, adds it to the database
   * and also it's identifier goes into the distributed queue of WorkflowExecutions.
   * The source data for the first plugin in the workflow can be controlled, if required, from the {@code enforcedPluginType},
   * which means that the last valid plugin that is provided with that parameter, will be used as the source data.
   *
   * @param datasetId the dataset identifier for which the execution will take place
   * @param enforcedPluginType optional, the plugin type to be used as source data
   * @param priority the priority of the execution in case the system gets overloaded, 0 lowest, 10 highest
   * @return the WorkflowExecution object that was generated
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowFoundException} if a workflow for the dataset identifier provided does not exist</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoDatasetFoundException} if the dataset identifier provided does not exist</li>
   * <li>{@link eu.europeana.metis.exception.ExternalTaskException} if there was an exception when contacting the external resource(ECloud)</li>
   * <li>{@link eu.europeana.metis.core.exceptions.PluginExecutionNotAllowed} if the execution of the first plugin was not allowed, because a valid source plugin could not be found</li>
   * <li>{@link eu.europeana.metis.core.exceptions.WorkflowExecutionAlreadyExistsException} if a workflow execution for the generated execution identifier already exists, almost impossible to happen since ids are UUIDs</li>
   * </ul>
   */
  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE, method = RequestMethod.POST, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public WorkflowExecution addWorkflowInQueueOfWorkflowExecutions(
      @PathVariable("datasetId") int datasetId,
      @RequestParam(value = "enforcedPluginType", required = false, defaultValue = "") PluginType enforcedPluginType,
      @RequestParam(value = "priority", defaultValue = "0") int priority)
      throws GenericMetisException {
    WorkflowExecution workflowExecution = orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(datasetId, enforcedPluginType, priority);
    LOGGER.info("WorkflowExecution for datasetId '{}' added to queue", datasetId);
    return workflowExecution;
  }

  /**
   * Request to cancel a workflow execution.
   * The execution will go into a cancelling state until it's properly {@link WorkflowStatus#CANCELLED} from the system
   *
   * @param executionId the execution identifier of the execution to cancel
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no worklfowExecution could be found</li>
   * </ul>
   */
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

  /**
   * Get a WorkflowExecution using an execution identifier.
   *
   * @param executionId the execution identifier
   * @return the WorkflowExecution object
   */
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

  /**
   * Check if a specified {@code pluginType} is allowed for execution.
   * This is checked based on, if there was a previous successful finished plugin that follows a specific order unless the {@code enforcedPluginType} is used.
   *
   * @param datasetId the dataset identifier of which the executions are based on
   * @param pluginType the pluginType to be checked for allowance of execution
   * @param enforcedPluginType optional, the plugin type to be used as source data
   * @return the abstractMetisPlugin that the execution on {@code pluginType} will be based on. Can be null if the
   * {@code pluginType} is the first one in the total order of executions e.g. One of the harvesting plugins.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.core.exceptions.PluginExecutionNotAllowed} if the no plugin was found so the {@code pluginType} will be based upon.</li>
   * </ul>
   */
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

  /**
   * Retrieve dataset level information of past executions {@link DatasetExecutionInformation}
   *
   * @param datasetId the dataset identifier to generate the information for
   * @return the structured class containing all the execution information
   */
  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_INFORMATION, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public DatasetExecutionInformation getDatasetExecutionInformation(
      @PathVariable("datasetId") int datasetId) {
    LOGGER.info("Requesting dataset execution information for datasetId: {}", datasetId);
    return orchestratorService.getDatasetExecutionInformation(datasetId);
  }

  /**
   * Get all WorkflowExecutions paged.
   *
   * @param datasetId the dataset identifier filter
   * @param workflowOwner the workflow owner, can be null
   * @param workflowStatuses a set of workflow statuses to filter, can be empty or null
   * @param orderField the field to be used to sort the results
   * @param ascending a boolean value to request the ordering to ascending or descending
   * @param nextPage the nextPage token, the end of the list is marked with -1 on the response
   * @return a list of all the WorkflowExecutions found
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link BadContentException} if paging is not correctly provided</li>
   * </ul>
   */
  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<WorkflowExecution> getAllWorkflowExecutionsByDatasetId(
      @PathVariable("datasetId") int datasetId,
      @RequestParam(value = "workflowOwner", required = false) String workflowOwner,
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
            .getAllWorkflowExecutions(datasetId, workflowOwner, workflowStatuses,
                orderField, ascending, nextPage),
        orchestratorService.getWorkflowExecutionsPerRequest(), nextPage);
    LOGGER.info("Batch of: {} workflowExecutions returned, using batch nextPage: {}",
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  /**
   * Get all WorkflowExecutions paged.
   * Not filtered by datasetId.
   *
   * @param workflowOwner the workflow owner, can be null
   * @param workflowStatuses a set of workflow statuses to filter, can be empty or null
   * @param orderField the field to be used to sort the results
   * @param ascending a boolean value to request the ordering to ascending or descending
   * @param nextPage the nextPage token, the end of the list is marked with -1 on the response
   * @return a list of all the WorkflowExecutions found
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link BadContentException} if paging is not correctly provided</li>
   * </ul>
   */
  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<WorkflowExecution> getAllWorkflowExecutions(
      @RequestParam(value = "workflowOwner", required = false) String workflowOwner,
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
            .getAllWorkflowExecutions(-1, workflowOwner, workflowStatuses,
                orderField, ascending, nextPage),
        orchestratorService.getWorkflowExecutionsPerRequest(), nextPage);
    LOGGER.info("Batch of: {} workflowExecutions returned, using batch nextPage: {}",
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }
}
