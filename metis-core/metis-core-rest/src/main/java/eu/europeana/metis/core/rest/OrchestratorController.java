package eu.europeana.metis.core.rest;

import eu.europeana.metis.utils.CommonStringValues;
import eu.europeana.metis.utils.RestEndpoints;
import eu.europeana.metis.authentication.rest.client.AuthenticationClient;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.common.DaoFieldNames;
import eu.europeana.metis.core.dataset.DatasetExecutionInformation;
import eu.europeana.metis.core.rest.execution.details.WorkflowExecutionView;
import eu.europeana.metis.core.rest.execution.overview.ExecutionAndDatasetView;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.MetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;
import java.util.Date;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Contains all the calls that are related to Orchestration.
 * <p>The {@link OrchestratorService} has control on how to orchestrate different components of the
 * system</p>
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-28
 */
@Controller
public class OrchestratorController {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrchestratorController.class);
  private final OrchestratorService orchestratorService;
  private final AuthenticationClient authenticationClient;

  /**
   * Autowired constructor with all required parameters.
   *
   * @param orchestratorService the orchestratorService object
   * @param authenticationClient the client for the authentication service
   */
  @Autowired
  public OrchestratorController(OrchestratorService orchestratorService,
      AuthenticationClient authenticationClient) {
    this.orchestratorService = orchestratorService;
    this.authenticationClient = authenticationClient;
  }

  /**
   * Create a workflow using a datasetId and the {@link Workflow} that contains the requested
   * plugins. If plugins are disabled, they (their settings) are still saved.
   *
   * @param authorization the authorization header with the access token
   * @param datasetId the dataset identifier to relate the workflow to
   * @param enforcedPredecessorType optional, the plugin type to be used as source data
   * @param workflow the Workflow will all it's requested plugins
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.core.exceptions.WorkflowAlreadyExistsException} if a workflow
   * for the dataset identifier provided already exists</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoDatasetFoundException} if the dataset
   * identifier provided does not exist</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  //WORKFLOWS
  @PostMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID, consumes = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public void createWorkflow(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") String datasetId,
      @RequestParam(value = "enforcedPluginType", required = false, defaultValue = "") ExecutablePluginType enforcedPredecessorType,
      @RequestBody Workflow workflow)
      throws GenericMetisException {
    MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    orchestratorService.createWorkflow(metisUser, datasetId, workflow, enforcedPredecessorType);
  }

  /**
   * Update an already existent workflow using a datasetId and the {@link Workflow} that contains
   * the requested plugins. If plugins are disabled, they (their settings) are still saved. Any
   * settings in plugins that are not sent in the request are removed.
   *
   * @param authorization the authorization header with the access token
   * @param datasetId the identifier of the dataset for which the workflow should be updated
   * @param enforcedPredecessorType optional, the plugin type to be used as source data
   * @param workflow the workflow with the plugins requested
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowFoundException} if a workflow for the
   * dataset identifier provided does not exist</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoDatasetFoundException} if the dataset
   * identifier provided does not exist</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  @PutMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void updateWorkflow(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") String datasetId,
      @RequestParam(value = "enforcedPluginType", required = false, defaultValue = "") ExecutablePluginType enforcedPredecessorType,
      @RequestBody Workflow workflow) throws GenericMetisException {
    MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    orchestratorService.updateWorkflow(metisUser, datasetId, workflow, enforcedPredecessorType);
  }

  /**
   * Deletes a workflow.
   *
   * @param authorization the authorization header with the access token
   * @param datasetId the dataset identifier that corresponds to the workflow to be deleted
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.core.exceptions.NoDatasetFoundException} if the dataset
   * identifier provided does not exist</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  @DeleteMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID,
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteWorkflow(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") String datasetId) throws GenericMetisException {
    MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    orchestratorService.deleteWorkflow(metisUser, datasetId);
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Workflow with datasetId '{}' deleted",
          datasetId.replaceAll(CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""));
    }
  }

  /**
   * Get a workflow for a dataset identifier.
   *
   * @param authorization the authorization header with the access token
   * @param datasetId the dataset identifier
   * @return the Workflow object
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.core.exceptions.NoDatasetFoundException} if the dataset
   * identifier provided does not exist</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Workflow getWorkflow(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") String datasetId) throws GenericMetisException {
    MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    Workflow workflow = orchestratorService.getWorkflow(metisUser, datasetId);
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Workflow with datasetId '{}' found",
          datasetId.replaceAll(CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""));
    }
    return workflow;
  }

  //WORKFLOW EXECUTIONS

  /**
   * Does checking, prepares and adds a WorkflowExecution in the queue. That means it updates the
   * status of the WorkflowExecution to {@link WorkflowStatus#INQUEUE}, adds it to the database and
   * also it's identifier goes into the distributed queue of WorkflowExecutions. The source data for
   * the first plugin in the workflow can be controlled, if required, from the {@code
   * enforcedPredecessorType}, which means that the last valid plugin that is provided with that
   * parameter, will be used as the source data.
   *
   * @param authorization the authorization header with the access token
   * @param datasetId the dataset identifier for which the execution will take place
   * @param enforcedPredecessorType optional, the plugin type to be used as source data
   * @param priority the priority of the execution in case the system gets overloaded, 0 lowest, 10
   * highest
   * @return the WorkflowExecution object that was generated
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowFoundException} if a workflow for the
   * dataset identifier provided does not exist</li>
   * <li>{@link BadContentException} if the workflow is empty or no plugin enabled</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoDatasetFoundException} if the dataset
   * identifier provided does not exist</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * <li>{@link eu.europeana.metis.exception.ExternalTaskException} if there was an exception when
   * contacting the external resource(ECloud)</li>
   * <li>{@link eu.europeana.metis.core.exceptions.PluginExecutionNotAllowed} if the execution of
   * the first plugin was not allowed, because a valid source plugin could not be found</li>
   * <li>{@link eu.europeana.metis.core.exceptions.WorkflowExecutionAlreadyExistsException} if a
   * workflow execution for the generated execution identifier already exists, almost impossible to
   * happen since ids are UUIDs</li>
   * </ul>
   */
  @PostMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_DATASETID_EXECUTE, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public WorkflowExecution addWorkflowInQueueOfWorkflowExecutions(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") String datasetId,
      @RequestParam(value = "enforcedPluginType", required = false, defaultValue = "") ExecutablePluginType enforcedPredecessorType,
      @RequestParam(value = "priority", defaultValue = "0") int priority)
      throws GenericMetisException {
    MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    WorkflowExecution workflowExecution = orchestratorService
        .addWorkflowInQueueOfWorkflowExecutions(metisUser, datasetId, null, enforcedPredecessorType,
            priority);
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("WorkflowExecution for datasetId '{}' added to queue",
          datasetId.replaceAll(CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""));
    }
    return workflowExecution;
  }

  /**
   * Request to cancel a workflow execution. The execution will go into a cancelling state until
   * it's properly {@link WorkflowStatus#CANCELLED} from the system
   *
   * @param authorization the authorization header with the access token
   * @param executionId the execution identifier of the execution to cancel
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no
   * worklfowExecution could be found</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoDatasetFoundException} if the dataset
   * identifier of the workflow does not exist</li>
   * </ul>
   */
  @DeleteMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void cancelWorkflowExecution(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("executionId") String executionId)
      throws GenericMetisException {
    MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    orchestratorService.cancelWorkflowExecution(metisUser, executionId);
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("WorkflowExecution for executionId '{}' is cancelling",
          executionId.replaceAll(CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""));
    }
  }

  /**
   * Get a WorkflowExecution using an execution identifier.
   *
   * @param authorization the authorization header with the access token
   * @param executionId the execution identifier
   * @return the WorkflowExecution object
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.core.exceptions.NoDatasetFoundException} if the dataset
   * identifier provided does not exist</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public WorkflowExecution getWorkflowExecutionByExecutionId(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("executionId") String executionId) throws GenericMetisException {
    MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    WorkflowExecution workflowExecution = orchestratorService
        .getWorkflowExecutionByExecutionId(metisUser, executionId);
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("WorkflowExecution with executionId '{}' {}found.",
          executionId.replaceAll(CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""),
          workflowExecution == null ? "not " : "");
    }
    return workflowExecution;
  }

  /**
   * Check if a specified {@code pluginType} is allowed for execution. This is checked based on, if
   * there was a previous successful finished plugin that follows a specific order (unless the {@code
   * enforcedPredecessorType} is used) and that has the latest successful harvest plugin as an ancestor.
   *
   * @param authorization the authorization header with the access token
   * @param datasetId the dataset identifier of which the executions are based on
   * @param pluginType the pluginType to be checked for allowance of execution
   * @param enforcedPredecessorType optional, the plugin type to be used as source data
   * @return the abstractMetisPlugin that the execution on {@code pluginType} will be based on. Can
   * be null if the {@code pluginType} is the first one in the total order of executions e.g. One of
   * the harvesting plugins.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.core.exceptions.PluginExecutionNotAllowed} if the no plugin was
   * found so the {@code pluginType} will be based upon.</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoDatasetFoundException} if the dataset
   * identifier provided does not exist</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_ALLOWED_PLUGIN, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public MetisPlugin getLatestFinishedPluginWorkflowExecutionByDatasetIdIfPluginTypeAllowedForExecution(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") String datasetId,
      @RequestParam("pluginType") ExecutablePluginType pluginType,
      @RequestParam(value = "enforcedPluginType", required = false, defaultValue = "") ExecutablePluginType enforcedPredecessorType)
      throws GenericMetisException {
    MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    MetisPlugin latestFinishedPluginWorkflowExecutionByDatasetId = orchestratorService
        .getLatestFinishedPluginByDatasetIdIfPluginTypeAllowedForExecution(metisUser, datasetId,
            pluginType, enforcedPredecessorType);
    if (latestFinishedPluginWorkflowExecutionByDatasetId == null) {
      LOGGER.info("PluginType allowed by default");
    } else {
      LOGGER.info("Latest Plugin WorkflowExecution with id '{}' found",
          latestFinishedPluginWorkflowExecutionByDatasetId.getId());
    }
    return latestFinishedPluginWorkflowExecutionByDatasetId;
  }

  /**
   * Retrieve dataset level information of past executions {@link DatasetExecutionInformation}
   *
   * @param authorization the authorization header with the access token
   * @param datasetId the dataset identifier to generate the information for
   * @return the structured class containing all the execution information
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.core.exceptions.NoDatasetFoundException} if the dataset
   * identifier provided does not exist</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_INFORMATION, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public DatasetExecutionInformation getDatasetExecutionInformation(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") String datasetId) throws GenericMetisException {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.debug("Requesting dataset execution information for datasetId: {}",
          datasetId.replaceAll(CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""));
    }
    MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    return orchestratorService.getDatasetExecutionInformation(metisUser, datasetId);
  }

  /**
   * Get all WorkflowExecutions paged.
   *
   * @param authorization the authorization header with the access token
   * @param datasetId the dataset identifier filter
   * @param workflowStatuses a set of workflow statuses to filter, can be empty or null
   * @param orderField the field to be used to sort the results
   * @param ascending a boolean value to request the ordering to ascending or descending
   * @param nextPage the nextPage token, the end of the list is marked with -1 on the response
   * @return a list of all the WorkflowExecutions found
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link BadContentException} if paging is not correctly provided</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoDatasetFoundException} if the dataset
   * identifier provided does not exist</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<WorkflowExecutionView> getAllWorkflowExecutionsByDatasetId(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") String datasetId,
      @RequestParam(value = "workflowStatus", required = false) Set<WorkflowStatus> workflowStatuses,
      @RequestParam(value = "orderField", required = false, defaultValue = "ID") DaoFieldNames orderField,
      @RequestParam(value = "ascending", required = false, defaultValue = "true") boolean ascending,
      @RequestParam(value = "nextPage", required = false, defaultValue = "0") int nextPage)
      throws GenericMetisException {
    if (nextPage < 0) {
      throw new BadContentException(CommonStringValues.NEXT_PAGE_CANNOT_BE_NEGATIVE);
    }
    final MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    final ResponseListWrapper<WorkflowExecutionView> result =
        orchestratorService.getAllWorkflowExecutions(metisUser, datasetId, workflowStatuses,
            orderField, ascending, nextPage);
    logPaging(result, nextPage);
    return result;
  }

  /**
   * Get all WorkflowExecutions paged. Not filtered by datasetId.
   *
   * TODO JV This endpoint is no longer in use. Consider removing it.
   *
   * @param authorization the authorization header with the access token
   * @param workflowStatuses a set of workflow statuses to filter, can be empty or null
   * @param orderField the field to be used to sort the results
   * @param ascending a boolean value to request the ordering to ascending or descending
   * @param nextPage the nextPage token, the end of the list is marked with -1 on the response
   * @return a list of all the WorkflowExecutions found
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link BadContentException} if paging is not correctly provided</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<WorkflowExecutionView> getAllWorkflowExecutions(
      @RequestHeader("Authorization") String authorization,
      @RequestParam(value = "workflowStatus", required = false) Set<WorkflowStatus> workflowStatuses,
      @RequestParam(value = "orderField", required = false, defaultValue = "ID") DaoFieldNames orderField,
      @RequestParam(value = "ascending", required = false, defaultValue = "true") boolean ascending,
      @RequestParam(value = "nextPage", required = false, defaultValue = "0") int nextPage)
      throws GenericMetisException {
    if (nextPage < 0) {
      throw new BadContentException(CommonStringValues.NEXT_PAGE_CANNOT_BE_NEGATIVE);
    }
    final MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    final ResponseListWrapper<WorkflowExecutionView> result =
        orchestratorService.getAllWorkflowExecutions(metisUser, null, workflowStatuses, orderField,
            ascending, nextPage);
    logPaging(result, nextPage);
    return result;
  }

  /**
   * Get the overview of WorkflowExecutions. This returns a list of executions ordered to display an
   * overview. First the ones in queue, then those in progress and then those that are finalized.
   * They will be sorted by creation date. This method does support pagination.
   *
   * @param authorization the authorization header with the access token
   * @param pluginStatuses the plugin statuses to filter. Can be null.
   * @param pluginTypes the plugin types to filter. Can be null.
   * @param fromDate the date from where the results should start. Can be null.
   * @param toDate the date to where the results should end. Can be null.
   * @param nextPage the nextPage token, the end of the list is marked with -1 on the response
   * @param pageCount the number of pages that is requested
   * @return a list of all the WorkflowExecutions together with the datasets that they belong to.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link BadContentException} if paging is not correctly provided</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_OVERVIEW, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<ExecutionAndDatasetView> getWorkflowExecutionsOverview(
      @RequestHeader("Authorization") String authorization,
      @RequestParam(value = "pluginStatus", required = false) Set<PluginStatus> pluginStatuses,
      @RequestParam(value = "pluginType", required = false) Set<PluginType> pluginTypes,
      @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Date fromDate,
      @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) Date toDate,
      @RequestParam(value = "nextPage", required = false, defaultValue = "0") int nextPage,
      @RequestParam(value = "pageCount", required = false, defaultValue = "1") int pageCount)
      throws GenericMetisException {
    if (nextPage < 0) {
      throw new BadContentException(CommonStringValues.NEXT_PAGE_CANNOT_BE_NEGATIVE);
    }
    if (pageCount < 1) {
      throw new BadContentException(CommonStringValues.PAGE_COUNT_CANNOT_BE_ZERO_OR_NEGATIVE);
    }
    final MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    final ResponseListWrapper<ExecutionAndDatasetView> result =
        orchestratorService.getWorkflowExecutionsOverview(metisUser, pluginStatuses, pluginTypes,
            fromDate, toDate, nextPage, pageCount);
    logPaging(result, nextPage);
    return result;
  }

  private static void logPaging(ResponseListWrapper<?> responseListWrapper, int nextPage) {
    LOGGER.debug("Batch of: {} workflowExecutions returned, using batch nextPage: {}",
        responseListWrapper.getListSize(), nextPage);
  }

  /**
   * Retrieve dataset level history of past executions {@link ExecutionHistory}
   *
   * @param authorization the authorization header with the access token
   * @param datasetId the dataset identifier to generate the history for
   * @return the structured class containing all the execution history, ordered by date descending.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.core.exceptions.NoDatasetFoundException} if the dataset
   * identifier provided does not exist</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_DATASET_DATASETID_HISTORY, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ExecutionHistory getDatasetExecutionHistory(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") String datasetId) throws GenericMetisException {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.debug("Requesting dataset execution history for datasetId: {}",
          datasetId.replaceAll(CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""));
    }
    final MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    return orchestratorService.getDatasetExecutionHistory(metisUser, datasetId);
  }

  /**
   * Retrieve a list of executable plugins with data availability {@link
   * PluginsWithDataAvailability} for a given workflow execution.
   *
   * @param authorization the authorization header with the access token
   * @param executionId the identifier of the execution for which to get the plugins
   * @return the structured class containing all the execution history, ordered by date descending.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if an
   * non-existing execution ID or version is provided.</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_EXECUTIONS_EXECUTIONID_PLUGINS_DATA_AVAILABILITY, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public PluginsWithDataAvailability getExecutablePluginsWithDataAvailability(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("executionId") String executionId) throws GenericMetisException {
    if (LOGGER.isInfoEnabled()) {
      final String logSanitizedExecutionId = executionId.replaceAll("[\r\n]", "");
      LOGGER.debug("Requesting plugins with data availability for executionId: {}", logSanitizedExecutionId);
    }
    final MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    return orchestratorService.getExecutablePluginsWithDataAvailability(metisUser, executionId);
  }

  /**
   * Get the evolution of the records from when they were first imported until (and excluding) the
   * specified version.
   *
   * @param authorization The authorization header with the access token
   * @param workflowExecutionId The ID of the workflow exection in which the version is created.
   * @param pluginType The step within the workflow execution that created the version.
   * @return The record evolution.
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if an
   * non-existing execution ID or version is provided.</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_EVOLUTION, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public VersionEvolution getRecordEvolutionForVersion(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("workflowExecutionId") String workflowExecutionId,
      @PathVariable("pluginType") PluginType pluginType
  ) throws GenericMetisException {
    final MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    return orchestratorService
        .getRecordEvolutionForVersion(metisUser, workflowExecutionId, pluginType);
  }
}
