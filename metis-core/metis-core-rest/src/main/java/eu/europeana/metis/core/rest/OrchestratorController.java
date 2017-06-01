package eu.europeana.metis.core.rest;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.core.dataset.DatasetListWrapper;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoUserWorkflowExecutionFoundException;
import eu.europeana.metis.core.exceptions.NoUserWorkflowFoundException;
import eu.europeana.metis.core.exceptions.UserWorkflowExecutionAlreadyExistsException;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.workflow.UserWorkflow;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import eu.europeana.metis.core.workflow.UserWorkflowExecutionWrapper;
import eu.europeana.metis.core.workflow.WorkflowStatus;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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

  private final Logger LOGGER = LoggerFactory.getLogger(OrchestratorController.class);
  private final OrchestratorService orchestratorService;

  @Autowired
  public OrchestratorController(OrchestratorService orchestratorService) {
    this.orchestratorService = orchestratorService;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS, method = RequestMethod.POST, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Successful response"),
      @ApiResponse(code = 406, message = "Bad content")})
  @ApiOperation(value = "Create a user workflow")
  public void createUserWorkflow(
      @RequestBody UserWorkflow userWorkflow)
      throws BadContentException {
    orchestratorService.createUserWorkflow(userWorkflow);
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
    LOGGER.info(
        "UserWorkflow with owner '" + owner + "' and workflowName '" + workflowName + "' deleted");
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
        "UserWorkflow with owner '" + owner + "' and workflowName '" + workflowName + "' found");
    return userWorkflow;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_DATASETNAME, method = RequestMethod.POST, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Successful response")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "datasetName", value = "datasetName", dataType = "string", paramType = "path", required = true),
      @ApiImplicitParam(name = "owner", value = "Owner", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "workflowName", value = "WorkflowName", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "priority", value = "Priority value, 0 is normal the higher number the higher priority", dataType = "int", paramType = "query")
  })
  @ApiOperation(value = "Add a user workflow by owner and workflowName for datasetName to the queue of executions")
  public void addUserWorkflowInQueueOfUserWorkflowExecutions(
      @PathVariable("datasetName") String datasetName, @QueryParam("owner") String owner,
      @QueryParam("workflowName") String workflowName, @QueryParam("priority") Integer priority)
      throws NoUserWorkflowFoundException, NoDatasetFoundException, UserWorkflowExecutionAlreadyExistsException {
    if (priority == null) {
      priority = 0;
    }
    orchestratorService
        .addUserWorkflowInQueueOfUserWorkflowExecutions(datasetName, owner,
            workflowName, priority);
    LOGGER.info("UserWorkflowExecution for datasetName '" + datasetName + "' with owner '" + owner
        + "' and workflowName '" + workflowName + "' started");
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_EXECUTION_DATASETNAME, method = RequestMethod.DELETE, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Successful response")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "datasetName", value = "datasetName", dataType = "string", paramType = "path", required = true),
      @ApiImplicitParam(name = "owner", value = "Owner", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "workflowName", value = "WorkflowName", dataType = "string", paramType = "query", required = true)
  })
  @ApiOperation(value = "Cancel a user workflow by owner and workflowName for datasetName")
  public void cancelUserWorkflowExecution(
      @PathVariable("datasetName") String datasetName, @QueryParam("owner") String owner,
      @QueryParam("workflowName") String workflowName)
      throws InterruptedException, ExecutionException, NoUserWorkflowExecutionFoundException {
    orchestratorService.cancelUserWorkflowExecution(datasetName, owner, workflowName);
    LOGGER.info("UserWorkflowExecution for datasetName '" + datasetName + "' with owner '" + owner
        + "' and workflowName '" + workflowName + "' cancelled");
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_EXECUTION_DATASETNAME, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response"),
      @ApiResponse(code = 404, message = "UserWorkflowExecution not found")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "datasetName", value = "Owner", dataType = "string", paramType = "query", required = true),
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
        "UserWorkflowExecution with datasetName '" + datasetName + "' with owner '" + owner
            + "' and workflowName '" + workflowName + "' found");
    return userWorkflowExecution;
  }


  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_EXECUTIONS_DATASETNAME, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "datasetName", value = "Owner", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "owner", value = "Owner", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "workflowName", value = "WorkflowName", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "workflowStatus", value = "workflowStatus", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "nextPage", value = "nextPage", dataType = "string", paramType = "query")
  })
  @ApiOperation(value = "Get all userWorkflowExecutions by datasetName, owner and workflowName", response = DatasetListWrapper.class)
  public UserWorkflowExecutionWrapper getAllUserWorkflowExecutions(
      @PathVariable("datasetName") String datasetName, @QueryParam("owner") String owner,
      @QueryParam("workflowName") String workflowName, @QueryParam("workflowStatus") WorkflowStatus workflowStatus, @QueryParam("nextPage") String nextPage) {
    UserWorkflowExecutionWrapper userWorkflowExecutionWrapper = new UserWorkflowExecutionWrapper();
    userWorkflowExecutionWrapper.setUserWorkflowExecutionsAndLastPage(orchestratorService
            .getAllUserWorkflowExecutions(datasetName, owner, workflowName, workflowStatus, nextPage),
        orchestratorService.getUserWorkflowExecutionsPerRequest());
    LOGGER.info("Batch of: " + userWorkflowExecutionWrapper.getListSize()
        + " userWorkflowExecutions returned, using batch nextPage: " + nextPage);
    return userWorkflowExecutionWrapper;
  }
}
