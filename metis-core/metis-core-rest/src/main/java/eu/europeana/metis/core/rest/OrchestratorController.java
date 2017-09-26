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
import eu.europeana.metis.core.workflow.ScheduledUserWorkflow;
import eu.europeana.metis.core.workflow.UserWorkflow;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
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
  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS, method = RequestMethod.POST, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Successful response"),
      @ApiResponse(code = 409, message = "User workflow execution already exists")})
  @ApiOperation(value = "Create a user workflow")
  public void createUserWorkflow(
      @RequestBody UserWorkflow userWorkflow)
      throws UserWorkflowAlreadyExistsException {
    orchestratorService.createUserWorkflow(userWorkflow);
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS, method = RequestMethod.PUT, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Successful response"),
      @ApiResponse(code = 406, message = "Bad content")})
  @ApiOperation(value = "Update a user workflow")
  public void updateUserWorkflow(
      @RequestBody UserWorkflow userWorkflow) throws NoUserWorkflowFoundException {
    orchestratorService.updateUserWorkflow(userWorkflow);
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS, method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Successful response")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "owner", value = "Owner", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "workflowName", value = "WorkflowName", dataType = "string", paramType = "query", required = true)
  })
  @ApiOperation(value = "Delete a user workflow by owner and workflowName")
  public void deleteUserWorkflow(@QueryParam("owner") String owner,
      @QueryParam("workflowName") String workflowName) {
    orchestratorService.deleteUserWorkflow(owner, workflowName);
    LOGGER.info("UserWorkflow with owner '{}' and workflowName '{}' deleted", owner, workflowName);
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response"),
      @ApiResponse(code = 404, message = "UserWorkflow not found")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "owner", value = "Owner", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "workflowName", value = "WorkflowName", dataType = "string", paramType = "query", required = true)
  })
  @ApiOperation(value = "Get a userWorkflow by owner and workflowName", response = UserWorkflow.class)
  public UserWorkflow getUserWorkflow(@QueryParam("owner") String owner,
      @QueryParam("workflowName") String workflowName) {
    UserWorkflow userWorkflow = orchestratorService
        .getUserWorkflow(owner, workflowName);
    LOGGER.info(
        "UserWorkflow with owner '{}' and workflowName '{}' found", owner, workflowName);
    return userWorkflow;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_OWNER, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "owner", value = "Owner", dataType = "string", paramType = "path", required = true),
      @ApiImplicitParam(name = "nextPage", value = "nextPage", dataType = "string", paramType = "query")
  })
  @ApiOperation(value = "Get all userWorkflows by owner", response = ResponseListWrapper.class)
  public ResponseListWrapper<UserWorkflow> getAllUserWorkflows(
      @PathVariable("owner") String owner,
      @QueryParam("nextPage") String nextPage) {
    ResponseListWrapper<UserWorkflow> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper.setResultsAndLastPage(orchestratorService
            .getAllUserWorkflows(owner, nextPage),
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
      @ApiResponse(code = 201, message = "Successful response")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "datasetName", value = "datasetName", dataType = "string", paramType = "path", required = true),
      @ApiImplicitParam(name = "owner", value = "Owner", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "workflowName", value = "WorkflowName", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "priority", value = "Priority value, default is 0. The higher number the higher priority until 10", dataType = "int", defaultValue = "0", paramType = "query")
  })
  @ApiOperation(value = "Add a user workflow by owner and workflowName for datasetName to the queue of executions")
  public void addUserWorkflowInQueueOfUserWorkflowExecutions(
      @PathVariable("datasetName") String datasetName, @QueryParam("owner") String owner,
      @QueryParam("workflowName") String workflowName, @QueryParam("priority") Integer priority)
      throws NoUserWorkflowFoundException, NoDatasetFoundException, UserWorkflowExecutionAlreadyExistsException, IOException {
    if (priority == null) {
      priority = 0;
    }
    orchestratorService
        .addUserWorkflowInQueueOfUserWorkflowExecutions(datasetName, owner,
            workflowName, priority);
    LOGGER.info(
        "UserWorkflowExecution for datasetName '{}' with owner '{}' and workflowName '{}' added to queue",
        datasetName, owner, workflowName);
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_EXECUTION_DATASETNAME, method = RequestMethod.DELETE, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Successful response")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "datasetName", value = "datasetName", dataType = "string", paramType = "path", required = true)
  })
  @ApiOperation(value = "Cancel a user workflow by owner and workflowName for datasetName")
  public void cancelUserWorkflowExecution(
      @PathVariable("datasetName") String datasetName)
      throws ExecutionException, NoUserWorkflowExecutionFoundException {
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
      @ApiResponse(code = 200, message = "Successful response"),
      @ApiResponse(code = 404, message = "UserWorkflowExecution not found")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "datasetName", value = "DatasetName", dataType = "string", paramType = "path", required = true),
      @ApiImplicitParam(name = "owner", value = "Owner", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "workflowName", value = "WorkflowName", dataType = "string", paramType = "query", required = true)
  })
  @ApiOperation(value = "Get running userWorkflowExecution by datasetName, owner and workflowName", response = UserWorkflowExecution.class)
  public UserWorkflowExecution getRunningUserWorkflowExecution(
      @PathVariable("datasetName") String datasetName, @QueryParam("owner") String owner,
      @QueryParam("workflowName") String workflowName) {
    UserWorkflowExecution userWorkflowExecution = orchestratorService
        .getRunningUserWorkflowExecution(datasetName, owner, workflowName);
    LOGGER.info(
        "UserWorkflowExecution with datasetName '{}' with owner '{}' and workflowName '{}' found",
        datasetName, owner, workflowName);
    return userWorkflowExecution;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_DATASETNAME_EXECUTE_DIRECT, method = RequestMethod.POST, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Successful response"),
      @ApiResponse(code = 406, message = "Bad content")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "datasetName", value = "datasetName", dataType = "string", paramType = "path", required = true),
      @ApiImplicitParam(name = "priority", value = "Priority value, default is 0. The higher number the higher priority until 10", dataType = "int", defaultValue = "0", paramType = "query")
  })
  @ApiOperation(value = "Create a user workflow on the fly and directly send it in the queue of workflow executions")
  public void addUserWorkflowInQueueOfUserWorkflowExecutions(
      @PathVariable("datasetName") String datasetName, @RequestBody UserWorkflow userWorkflow,
      @QueryParam("priority") Integer priority)
      throws UserWorkflowExecutionAlreadyExistsException, NoDatasetFoundException, UserWorkflowAlreadyExistsException, IOException {
    if (priority == null) {
      priority = 0;
    }
    orchestratorService
        .addUserWorkflowInQueueOfUserWorkflowExecutions(datasetName, userWorkflow, priority);
    LOGGER.info(
        "UserWorkflowExecution for datasetName '{}' with owner '{}' started", datasetName,
        userWorkflow.getOwner());
  }


  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_EXECUTIONS_DATASETNAME, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "datasetName", value = "DatasetName", dataType = "string", paramType = "path", required = true),
      @ApiImplicitParam(name = "owner", value = "Owner", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "workflowName", value = "WorkflowName", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "workflowStatus", value = "WorkflowStatus", dataType = "string", paramType = "query"),
      @ApiImplicitParam(name = "nextPage", value = "nextPage", dataType = "string", paramType = "query")
  })
  @ApiOperation(value = "Get all userWorkflowExecutions by datasetName, owner by workflowName and workflowStatus", response = ResponseListWrapper.class)
  public ResponseListWrapper<UserWorkflowExecution> getAllUserWorkflowExecutions(
      @PathVariable("datasetName") String datasetName, @QueryParam("owner") String owner,
      @QueryParam("workflowName") String workflowName,
      @QueryParam("workflowStatus") WorkflowStatus workflowStatus,
      @QueryParam("nextPage") String nextPage) {
    ResponseListWrapper<UserWorkflowExecution> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper.setResultsAndLastPage(orchestratorService
            .getAllUserWorkflowExecutions(datasetName, owner, workflowName, workflowStatus, nextPage),
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
  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_SCHEDULE, method = RequestMethod.POST, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Successful response"),
      @ApiResponse(code = 404, message = "Dataset not found"),
      @ApiResponse(code = 404, message = "UserWorkflow not found"),
      @ApiResponse(code = 406, message = "Bad content"),
      @ApiResponse(code = 409, message = "ScheduledUserWorkflow already exist")})
  @ApiOperation(value = "Schedule a user workflow")
  public void scheduleUserWorkflowExecution(
      @RequestBody ScheduledUserWorkflow scheduledUserWorkflow)
      throws NoDatasetFoundException, BadContentException, NoUserWorkflowFoundException, ScheduledUserWorkflowAlreadyExistsException {
    orchestratorService.scheduleUserWorkflow(scheduledUserWorkflow);
    LOGGER.info(
        "ScheduledUserWorkflowExecution for datasetName '{}', workflowOwner '{}', workflowName, pointerDate at '{}', scheduled '{}'",
        scheduledUserWorkflow.getDatasetName(),
        scheduledUserWorkflow.getWorkflowOwner(), scheduledUserWorkflow.getWorkflowName(),
        scheduledUserWorkflow.getPointerDate(),
        scheduledUserWorkflow.getScheduleFrequence().name());
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_SCHEDULE, method = RequestMethod.PUT, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Successful response"),
      @ApiResponse(code = 406, message = "Bad content")})
  @ApiOperation(value = "Update a scheduled user workflow")
  public void updateScheduledUserWorkflow(
      @RequestBody ScheduledUserWorkflow scheduledUserWorkflow)
      throws NoScheduledUserWorkflowFoundException, BadContentException {
    orchestratorService.updateScheduledUserWorkflow(scheduledUserWorkflow);
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_SCHEDULE, method = RequestMethod.DELETE, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Successful response")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "datasetName", value = "datasetName", dataType = "string", paramType = "path", required = true),
      @ApiImplicitParam(name = "workflowOwner", value = "WorkflowOwner", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "workflowName", value = "WorkflowName", dataType = "string", paramType = "query", required = true),
  })
  @ApiOperation(value = "Delete a scheduled user workflow by datasetName workflowOwner and workflowName")
  public void deleteScheduledUserWorkflowExecution(
      @PathVariable("datasetName") String datasetName,
      @QueryParam("workflowOwner") String workflowOwner,
      @QueryParam("workflowName") String workflowName) {
    orchestratorService.deleteScheduledUserWorkflow(datasetName, workflowOwner, workflowName);
    LOGGER.info(
        "ScheduledUserWorkflowExecution for datasetName '{}', workflowOwner '{}', workflowName '{}' is cancelling",
        datasetName, workflowOwner, workflowName);
  }
}
