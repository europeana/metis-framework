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
import eu.europeana.metis.core.workflow.UserWorkflow;
import eu.europeana.metis.core.workflow.UserWorkflowExecution;
import eu.europeana.metis.core.workflow.WorkflowStatus;
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
  public void createUserWorkflow(
      @RequestBody UserWorkflow userWorkflow)
      throws UserWorkflowAlreadyExistsException {
    orchestratorService.createUserWorkflow(userWorkflow);
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS, method = RequestMethod.PUT, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void updateUserWorkflow(
      @RequestBody UserWorkflow userWorkflow) throws NoUserWorkflowFoundException {
    orchestratorService.updateUserWorkflow(userWorkflow);
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS, method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUserWorkflow(@QueryParam("workflowOwner") String workflowOwner,
      @QueryParam("workflowName") String workflowName) {
    orchestratorService.deleteUserWorkflow(workflowOwner, workflowName);
    LOGGER.info("UserWorkflow with workflowOwner '{}' and workflowName '{}' deleted", workflowOwner,
        workflowName);
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserWorkflow getUserWorkflow(@QueryParam("workflowOwner") String workflowOwner,
      @QueryParam("workflowName") String workflowName) {
    UserWorkflow userWorkflow = orchestratorService
        .getUserWorkflow(workflowOwner, workflowName);
    LOGGER.info(
        "UserWorkflow with workflowOwner '{}' and workflowName '{}' found", workflowOwner,
        workflowName);
    return userWorkflow;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_OWNER, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<UserWorkflow> getAllUserWorkflows(
      @PathVariable("workflowOwner") String workflowOwner,
      @QueryParam("nextPage") String nextPage) {
    ResponseListWrapper<UserWorkflow> responseListWrapper = new ResponseListWrapper<>();
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
  public void addUserWorkflowInQueueOfUserWorkflowExecutions(
      @PathVariable("datasetName") String datasetName, @RequestBody UserWorkflow userWorkflow,
      @QueryParam("priority") Integer priority)
      throws UserWorkflowExecutionAlreadyExistsException, NoDatasetFoundException, UserWorkflowAlreadyExistsException {
    if (priority == null) {
      priority = 0;
    }
    orchestratorService
        .addUserWorkflowInQueueOfUserWorkflowExecutions(datasetName, userWorkflow, priority);
    LOGGER.info(
        "UserWorkflowExecution for datasetName '{}' with workflowOwner '{}' started", datasetName,
        userWorkflow.getWorkflowOwner());
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_USERWORKFLOWS_EXECUTION_DATASETNAME, method = RequestMethod.DELETE, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
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
  public void deleteScheduledUserWorkflowExecution(
      @PathVariable("datasetName") String datasetName) {
    orchestratorService.deleteScheduledUserWorkflow(datasetName);
    LOGGER.info(
        "ScheduledUserWorkflowExecution for datasetName '{}' deleted",
        datasetName);
  }
}
