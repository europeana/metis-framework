package eu.europeana.metis.core.rest;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoUserWorkflowExecutionFoundException;
import eu.europeana.metis.core.exceptions.NoUserWorkflowFoundException;
import eu.europeana.metis.core.exceptions.UserWorkflowExecutionAlreadyExistsException;
import eu.europeana.metis.core.service.OrchestratorService;
import eu.europeana.metis.core.workflow.UserWorkflow;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
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
 * Orchestration REST Endpoint
 * Created by ymamakis on 11/15/16.
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
    if (priority == null)
      priority = 0;
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
  @ApiOperation(value = "Get a userWorkflowExecution by datasetName, owner and workflowName", response = UserWorkflowExecution.class)
  public UserWorkflowExecution getUserWorkflowExecution(@PathVariable("datasetName") String datasetName, @QueryParam("owner") String owner,
      @QueryParam("workflowName") String workflowName) {
    UserWorkflowExecution userWorkflowExecution = orchestratorService
        .getUserWorkflowExecution(datasetName, owner, workflowName);
    LOGGER.info(
        "UserWorkflowExecution with datasetName '" + datasetName + "' with owner '" + owner + "' and workflowName '" + workflowName + "' found");
    return userWorkflowExecution;
  }

//    @ResponseBody
//    @RequestMapping(method = RequestMethod.POST, value = RestEndpoints.ORCHESTRATION_SCHEDULE, consumes = "application/json")
//    public String schedule(@PathVariable("datasetId") String datasetId, @PathVariable("operation") String operation,
//                           @RequestBody(required = false) Map<String, List<String>> params, @PathVariable("millis") long millis,
//                           @RequestParam("operatorEmail") String operatorEmail) throws NoDatasetFoundException {
//        return orchestratorService.schedule(datasetId, operation, operatorEmail, params, millis);
//    }
//
//    @ResponseStatus(HttpStatus.OK)
//    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_SCHEDULED)
//    public void executeScheduled() {
//        orchestratorService.executeScheduled();
//    }
//
//    @ResponseBody
//    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_ACTIVE)
//    public List<Execution> getActiveExecutions(@RequestParam("operatorEmail") String operatorEmail) {
//        return orchestratorService.getActiveExecutions(operatorEmail);
//    }
//
//    @ResponseBody
//    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_BYID)
//    public Execution getExecution(@PathVariable("executionId") String executionId) {
//        return orchestratorService.getExecution(executionId);
//    }
//
//    @ResponseBody
//    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_RANGE)
//    public List<Execution> getExecutionsByDate(@RequestParam("start") long start, @RequestParam("end") long end,
//                                               @RequestParam("operatorEmail") String operatorEmail) {
//        return orchestratorService
//            .getExecutionsByDates(new Date(start), new Date(end), operatorEmail);
//    }
//
//    @ResponseBody
//    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_EXECUTIONS)
//    public List<Execution> getExecutions(@RequestParam("offset") int offset, @RequestParam("limit") int limit,
//                                         @RequestParam("operatorEmail") String operatorEmail) {
//        return orchestratorService.getAllExecutions(offset, limit, operatorEmail);
//    }
//
//    @ResponseBody
//    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_DATASET)
//    public List<Execution> getExecutionsForDataset(@PathVariable("datasetId") String datasetId,
//                                                   @RequestParam("offset") int offset, @RequestParam("limit") int limit,
//                                                   @RequestParam("operatorEmail") String operatorEmail) throws NoDatasetFoundException {
//        return orchestratorService
//            .getAllExecutionsForDataset(datasetId, offset, limit, operatorEmail);
//    }
//
//    @ResponseBody
//    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_RANGE_DATASET)
//    public List<Execution> getExecutionsForDatasetByDate(@PathVariable("datasetId") String datasetId,
//                                                         @RequestParam("offset") int offset, @RequestParam("limit") int limit,
//                                                         @RequestParam("start") long start, @RequestParam("end") long end,
//                                                         @RequestParam("operatorEmail") String operatorEmail) throws NoDatasetFoundException {
//        return orchestratorService
//            .getAllExecutionsForDatasetByDates(datasetId, offset, limit, new Date(start), new Date(end), operatorEmail);
//    }
//
//    @ResponseStatus(HttpStatus.OK)
//    @RequestMapping(method = RequestMethod.DELETE, value = RestEndpoints.ORCHESTRATION_BYID)
//    public void cancelExecution(@PathVariable("executionId") String executionId) {
//        orchestratorService.cancel(executionId);
//    }
//
//    @ResponseBody
//    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.ORCHESTRATION_FAILED)
//    public List<String> getFailedRecords(@PathVariable("executionId") String executionId, @RequestParam("offset") int offset, @RequestParam("limit") int limit) {
//        return orchestratorService.getFailedRecords(executionId, offset, limit);
//    }
}
