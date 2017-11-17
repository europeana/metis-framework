package eu.europeana.metis.core.rest;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoScheduledUserWorkflowFoundException;
import eu.europeana.metis.core.exceptions.NoUserWorkflowExecutionFoundException;
import eu.europeana.metis.core.exceptions.NoUserWorkflowFoundException;
import eu.europeana.metis.core.exceptions.ScheduledUserWorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.UserWorkflowAlreadyExistsException;
import eu.europeana.metis.core.exceptions.UserWorkflowExecutionAlreadyExistsException;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledUserWorkflow;
import eu.europeana.metis.core.workflow.Workflow;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

  //USER WORKFLOWS
  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS, method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Successful response"),
      @ApiResponse(code = 409, message = "User workflow execution already exists")})
  @ApiOperation(value = "Create a user workflow")
  public void createUserWorkflow(
      @RequestBody Workflow workflow)
      throws UserWorkflowAlreadyExistsException {
    orchestratorService.createUserWorkflow(workflow);
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS, method = RequestMethod.PUT, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Successful response"),
      @ApiResponse(code = 404, message = "Workflow not found")})
  @ApiOperation(value = "Update a user workflow")
  public void updateUserWorkflow(
      @RequestBody Workflow workflow) throws NoUserWorkflowFoundException {
    orchestratorService.updateUserWorkflow(workflow);
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS, method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Successful response")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "workflowOwner", value = "WorkflowOwner", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "workflowName", value = "WorkflowName", dataType = "string", paramType = "query", required = true)
  })
  @ApiOperation(value = "Delete a user workflow by workflowOwner and workflowName")
  public void deleteUserWorkflow(@QueryParam("workflowOwner") String workflowOwner,
      @QueryParam("workflowName") String workflowName) {
    orchestratorService.deleteUserWorkflow(workflowOwner, workflowName);
    LOGGER.info("Workflow with workflowOwner '{}' and workflowName '{}' deleted", workflowOwner,
        workflowName);
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful response")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "workflowOwner", value = "WorkflowOwner", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "workflowName", value = "WorkflowName", dataType = "string", paramType = "query", required = true)
  })
  @ApiOperation(value = "Get a userWorkflow by workflowOwner and workflowName", response = Workflow.class)
  public Workflow getUserWorkflow(@QueryParam("workflowOwner") String workflowOwner,
      @QueryParam("workflowName") String workflowName) {
    Workflow workflow = orchestratorService
        .getUserWorkflow(workflowOwner, workflowName);
    LOGGER.info(
        "Workflow with workflowOwner '{}' and workflowName '{}' found", workflowOwner,
        workflowName);
    return workflow;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_OWNER, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "workflowOwner", value = "workflowOwner", dataType = "string", paramType = "path", required = true),
      @ApiImplicitParam(name = "nextPage", value = "nextPage", dataType = "string", paramType = "query")
  })
  @ApiOperation(value = "Get all userWorkflows by workflowOwner", response = ResponseListWrapper.class)
  public ResponseListWrapper<Workflow> getAllUserWorkflows(
      @PathVariable("workflowOwner") String workflowOwner,
      @QueryParam("nextPage") String nextPage) {
    ResponseListWrapper<Workflow> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper.setResultsAndLastPage(orchestratorService
            .getAllUserWorkflows(workflowOwner, nextPage),
        orchestratorService.getUserWorkflowsPerRequest());
    LOGGER.info("Batch of: {} userWorkflows returned, using batch nextPage: {}",
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }


  //USER WORKFLOW EXECUTIONS
  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_DATASETNAME_EXECUTE, method = RequestMethod.POST, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Successful response"),
      @ApiResponse(code = 404, message = "Dataset or Workflow not found"),
      @ApiResponse(code = 409, message = "User workflow execution already exists")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "datasetName", value = "datasetName", dataType = "string", paramType = "path", required = true),
      @ApiImplicitParam(name = "workflowOwner", value = "WorkflowOwner", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "workflowName", value = "WorkflowName", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "priority", value = "Priority value, default is 0. The higher number the higher priority until 10", dataType = "int", defaultValue = "0", paramType = "query")
  })
  @ApiOperation(value = "Add a user workflow by workflowOwner and workflowName for datasetName to the queue of executions")
  public void addUserWorkflowInQueueOfUserWorkflowExecutions(
      @PathVariable("datasetName") String datasetName,
      @QueryParam("workflowOwner") String workflowOwner,
      @QueryParam("workflowName") String workflowName, @QueryParam("priority") Integer priority)
      throws UserWorkflowExecutionAlreadyExistsException, NoDatasetFoundException, NoUserWorkflowFoundException {
    if (priority == null) {
      priority = 0;
    }
    orchestratorService
        .addUserWorkflowInQueueOfUserWorkflowExecutions(datasetName, workflowOwner,
            workflowName, priority);
    LOGGER.info(
        "UserWorkflowExecution for datasetName '{}' with workflowOwner '{}' and workflowName '{}' added to queue",
        datasetName, workflowOwner, workflowName);
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_DATASETNAME_EXECUTE_DIRECT, method = RequestMethod.POST, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Successful response"),
      @ApiResponse(code = 404, message = "Dataset not found"),
      @ApiResponse(code = 409, message = "UserWorkflowExecution or Workflow already exists")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "datasetName", value = "datasetName", dataType = "string", paramType = "path", required = true),
      @ApiImplicitParam(name = "priority", value = "Priority value, default is 0. The higher number the higher priority until 10", dataType = "int", defaultValue = "0", paramType = "query")
  })
  @ApiOperation(value = "Create a user workflow on the fly and directly send it in the queue of workflow executions")
  public void addUserWorkflowInQueueOfUserWorkflowExecutions(
      @PathVariable("datasetName") String datasetName, @RequestBody Workflow workflow,
      @QueryParam("priority") Integer priority)
      throws UserWorkflowExecutionAlreadyExistsException, NoDatasetFoundException, UserWorkflowAlreadyExistsException {
    if (priority == null) {
      priority = 0;
    }
    orchestratorService
        .addUserWorkflowInQueueOfUserWorkflowExecutions(datasetName, workflow, priority);
    LOGGER.info(
        "UserWorkflowExecution for datasetName '{}' with workflowOwner '{}' started", datasetName,
        workflow.getWorkflowOwner());
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_EXECUTION_DATASETNAME, method = RequestMethod.DELETE, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Successful response"),
      @ApiResponse(code = 404, message = "UserWorkflowExecution not found")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "datasetName", value = "datasetName", dataType = "string", paramType = "path", required = true)
  })
  @ApiOperation(value = "Cancel a user workflow execution by datasetName")
  public void cancelUserWorkflowExecution(
      @PathVariable("datasetName") String datasetName)
      throws NoUserWorkflowExecutionFoundException {
    orchestratorService.cancelUserWorkflowExecution(datasetName);
    LOGGER.info(
        "UserWorkflowExecution for datasetName '{}' is cancelling",
        datasetName);
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_EXECUTION_DATASETNAME, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "datasetName", value = "DatasetName", dataType = "string", paramType = "path", required = true),
  })
  @ApiOperation(value = "Get running userWorkflowExecution by datasetName", response = UserWorkflowExecution.class)
  public UserWorkflowExecution getRunningUserWorkflowExecution(
      @PathVariable("datasetName") String datasetName) {
    UserWorkflowExecution userWorkflowExecution = orchestratorService
        .getRunningUserWorkflowExecution(datasetName);
    LOGGER.info("UserWorkflowExecution with datasetName '{}' found", datasetName);
    return userWorkflowExecution;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_EXECUTIONS_DATASETNAME, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "datasetName", value = "DatasetName", dataType = "string", paramType = "path", required = true),
      @ApiImplicitParam(name = "workflowOwner", value = "WorkflowOwner", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "workflowName", value = "WorkflowName", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "workflowStatus", value = "WorkflowStatus", dataType = "string", paramType = "query"),
      @ApiImplicitParam(name = "nextPage", value = "nextPage", dataType = "string", paramType = "query")
  })
  @ApiOperation(value = "Get all userWorkflowExecutions by datasetName, workflowOwner by workflowName and workflowStatus", response = ResponseListWrapper.class)
  public ResponseListWrapper<UserWorkflowExecution> getAllUserWorkflowExecutions(
      @PathVariable("datasetName") String datasetName,
      @QueryParam("workflowOwner") String workflowOwner,
      @QueryParam("workflowName") String workflowName,
      @QueryParam("workflowStatus") WorkflowStatus workflowStatus,
      @QueryParam("nextPage") String nextPage) {
    ResponseListWrapper<UserWorkflowExecution> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper.setResultsAndLastPage(orchestratorService
            .getAllUserWorkflowExecutions(datasetName, workflowOwner, workflowName, workflowStatus,
                nextPage),
        orchestratorService.getUserWorkflowExecutionsPerRequest());
    LOGGER.info("Batch of: {} userWorkflowExecutions returned, using batch nextPage: {}",
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_EXECUTIONS, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "workflowStatus", value = "workflowStatus", dataType = "string", paramType = "query"),
      @ApiImplicitParam(name = "nextPage", value = "nextPage", dataType = "string", paramType = "query")
  })
  @ApiOperation(value = "Get all userWorkflowExecutions by workflowStatus", response = ResponseListWrapper.class)
  public ResponseListWrapper<UserWorkflowExecution> getAllUserWorkflowExecutions(
      @QueryParam("workflowStatus") WorkflowStatus workflowStatus,
      @QueryParam("nextPage") String nextPage) {
    ResponseListWrapper<UserWorkflowExecution> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper.setResultsAndLastPage(orchestratorService
            .getAllUserWorkflowExecutions(workflowStatus, nextPage),
        orchestratorService.getUserWorkflowExecutionsPerRequest());
    LOGGER.info("Batch of: {} userWorkflowExecutions returned, using batch nextPage: {}",
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  //SCHEDULED USER WORKFLOWS
  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_SCHEDULE, method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Successful response"),
      @ApiResponse(code = 404, message = "Dataset not found"),
      @ApiResponse(code = 404, message = "Workflow not found"),
      @ApiResponse(code = 406, message = "Bad content"),
      @ApiResponse(code = 409, message = "ScheduledUserWorkflow already exist")})
  @ApiOperation(value = "Schedule a user workflow. Only one schedule per datasetName is allowed.")
  public void scheduleUserWorkflowExecution(
      @RequestBody ScheduledUserWorkflow scheduledUserWorkflow)
      throws BadContentException, ScheduledUserWorkflowAlreadyExistsException, NoUserWorkflowFoundException, NoDatasetFoundException {
    orchestratorService.scheduleUserWorkflow(scheduledUserWorkflow);
    LOGGER.info(
        "ScheduledUserWorkflowExecution for datasetName '{}', workflowOwner '{}', workflowName '{}', pointerDate at '{}', scheduled '{}'",
        scheduledUserWorkflow.getDatasetName(),
        scheduledUserWorkflow.getWorkflowOwner(), scheduledUserWorkflow.getWorkflowName(),
        scheduledUserWorkflow.getPointerDate(),
        scheduledUserWorkflow.getScheduleFrequence().name());
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_SCHEDULE_DATASETNAME, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response"),
      @ApiResponse(code = 404, message = "ScheduledUserWorkflow not found")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "datasetName", value = "DatasetName", dataType = "string", paramType = "path", required = true)
  })
  @ApiOperation(value = "Get a scheduledUserWorkflow by datasetName", response = ScheduledUserWorkflow.class)
  public ScheduledUserWorkflow getScheduledUserWorkflow(
      @PathVariable("datasetName") String datasetName) {
    ScheduledUserWorkflow scheduledUserWorkflow = orchestratorService
        .getScheduledUserWorkflowByDatasetName(datasetName);
    LOGGER.info("ScheduledUserWorkflow with with datasetName '{}' found", datasetName);
    return scheduledUserWorkflow;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_SCHEDULE, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "nextPage", value = "nextPage", dataType = "string", paramType = "query")
  })
  @ApiOperation(value = "Get all scheduledUserWorkflows", response = ResponseListWrapper.class)
  public ResponseListWrapper<ScheduledUserWorkflow> getAllScheduledUserWorkflows(
      @QueryParam("nextPage") String nextPage) {
    ResponseListWrapper<ScheduledUserWorkflow> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper.setResultsAndLastPage(orchestratorService
            .getAllScheduledUserWorkflows(ScheduleFrequence.NULL, nextPage),
        orchestratorService.getScheduledUserWorkflowsPerRequest());
    LOGGER.info("Batch of: {} scheduledUserWorkflows returned, using batch nextPage: {}",
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_SCHEDULE, method = RequestMethod.PUT, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Successful response"),
      @ApiResponse(code = 404, message = "Workflow or ScheduledUserWorkflow not found"),
      @ApiResponse(code = 406, message = "Bad content")})
  @ApiOperation(value = "Update a scheduled user workflow for datasetName")
  public void updateScheduledUserWorkflow(
      @RequestBody ScheduledUserWorkflow scheduledUserWorkflow)
      throws BadContentException, NoScheduledUserWorkflowFoundException, NoUserWorkflowFoundException {
    orchestratorService.updateScheduledUserWorkflow(scheduledUserWorkflow);
    LOGGER.info("ScheduledUserWorkflow with with datasetName '{}' updated",
        scheduledUserWorkflow.getDatasetName());
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_SCHEDULE_DATASETNAME, method = RequestMethod.DELETE, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Successful response")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "datasetName", value = "datasetName", dataType = "string", paramType = "path", required = true)
  })
  @ApiOperation(value = "Delete a scheduled user workflow by datasetName")
  public void deleteScheduledUserWorkflowExecution(
      @PathVariable("datasetName") String datasetName) {
    orchestratorService.deleteScheduledUserWorkflow(datasetName);
    LOGGER.info(
        "ScheduledUserWorkflowExecution for datasetName '{}' deleted",
        datasetName);
  }
}
