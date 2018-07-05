package eu.europeana.metis.core.rest;

import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.authentication.rest.client.AuthenticationClient;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.service.ScheduleWorkflowService;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-05
 */
@Controller
public class ScheduleWorkflowController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleWorkflowController.class);
  private final ScheduleWorkflowService scheduleWorkflowService;
  private final AuthenticationClient authenticationClient;

  public ScheduleWorkflowController(ScheduleWorkflowService scheduleWorkflowService,
      AuthenticationClient authenticationClient) {
    this.scheduleWorkflowService = scheduleWorkflowService;
    this.authenticationClient = authenticationClient;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE, method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public void scheduleWorkflowExecution(
      @RequestHeader("Authorization") String authorization,
      @RequestBody ScheduledWorkflow scheduledWorkflow)
      throws GenericMetisException {
    MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    scheduleWorkflowService.scheduleWorkflow(metisUser, scheduledWorkflow);
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(
          "ScheduledWorkflowExecution for datasetId '{}', pointerDate at '{}', scheduled '{}'",
          scheduledWorkflow.getDatasetId(), scheduledWorkflow.getPointerDate(),
          scheduledWorkflow.getScheduleFrequence().name());
    }
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE_DATASETID, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ScheduledWorkflow getScheduledWorkflow(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") String datasetId)
      throws GenericMetisException {
    MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    ScheduledWorkflow scheduledWorkflow = scheduleWorkflowService
        .getScheduledWorkflowByDatasetId(metisUser, datasetId);
    LOGGER.info("ScheduledWorkflow with with datasetId '{}' found",
        datasetId.replaceAll(CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""));
    return scheduledWorkflow;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<ScheduledWorkflow> getAllScheduledWorkflows(
      @RequestHeader("Authorization") String authorization,
      @RequestParam(value = "nextPage", required = false, defaultValue = "0") int nextPage)
      throws GenericMetisException {
    MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    if (nextPage < 0) {
      throw new BadContentException(CommonStringValues.NEXT_PAGE_CANNOT_BE_NEGATIVE);
    }
    ResponseListWrapper<ScheduledWorkflow> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper.setResultsAndLastPage(scheduleWorkflowService
            .getAllScheduledWorkflows(metisUser, ScheduleFrequence.NULL, nextPage),
        scheduleWorkflowService.getScheduledWorkflowsPerRequest(), nextPage);
    LOGGER.info("Batch of: {} scheduledWorkflows returned, using batch nextPage: {}",
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE, method = RequestMethod.PUT, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void updateScheduledWorkflow(
      @RequestHeader("Authorization") String authorization,
      @RequestBody ScheduledWorkflow scheduledWorkflow)
      throws GenericMetisException {
    MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    scheduleWorkflowService.updateScheduledWorkflow(metisUser, scheduledWorkflow);
    LOGGER.info("ScheduledWorkflow with with datasetId '{}' updated",
        scheduledWorkflow.getDatasetId());
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE_DATASETID, method = RequestMethod.DELETE, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void deleteScheduledWorkflowExecution(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") String datasetId)
      throws GenericMetisException {
    MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    scheduleWorkflowService.deleteScheduledWorkflow(metisUser, datasetId);
    LOGGER.info("ScheduledWorkflowExecution for datasetId '{}' deleted",
        datasetId.replaceAll(CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""));
  }
}
