package eu.europeana.metis.core.rest.controller;

import static eu.europeana.metis.utils.CommonStringValues.CRLF_PATTERN;

import eu.europeana.metis.authentication.rest.client.AuthenticationClient;
import eu.europeana.metis.authentication.user.MetisUserView;
import eu.europeana.metis.core.exceptions.NoDatasetFoundException;
import eu.europeana.metis.core.exceptions.NoWorkflowFoundException;
import eu.europeana.metis.core.exceptions.ScheduledWorkflowAlreadyExistsException;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.service.ScheduleWorkflowService;
import eu.europeana.metis.core.workflow.ScheduleFrequence;
import eu.europeana.metis.core.workflow.ScheduledWorkflow;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;
import eu.europeana.metis.exception.UserUnauthorizedException;
import eu.europeana.metis.utils.CommonStringValues;
import eu.europeana.metis.utils.RestEndpoints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Contains all the calls that are related to scheduling workflows.
 * <p>The {@link ScheduleWorkflowService} has control on how to schedule workflows</p>
 *
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

  /**
   * Schedules a provided workflow.
   *
   * @param authorization the authorization header with the access token
   * @param scheduledWorkflow the scheduled workflow information
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link NoDatasetFoundException} if the dataset does not exist</li>
   * <li>{@link UserUnauthorizedException} if the user is unauthorized</li>
   * <li>{@link BadContentException} if some content send was not acceptable</li>
   * <li>{@link NoWorkflowFoundException} if the workflow for a dataset was not found</li>
   * <li>{@link ScheduledWorkflowAlreadyExistsException} if a scheduled workflow already exists</li>
   * </ul>
   */
  @PostMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE, consumes = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public void scheduleWorkflowExecution(@RequestHeader("Authorization") String authorization,
      @RequestBody ScheduledWorkflow scheduledWorkflow) throws GenericMetisException {
    MetisUserView metisUserView = authenticationClient.getUserByAccessTokenInHeader(authorization);
    scheduleWorkflowService.scheduleWorkflow(metisUserView, scheduledWorkflow);
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(
          "ScheduledWorkflowExecution for datasetId '{}', pointerDate at '{}', scheduled '{}'",
          CRLF_PATTERN.matcher(scheduledWorkflow.getDatasetId()), scheduledWorkflow.getPointerDate(),
          CRLF_PATTERN.matcher(scheduledWorkflow.getScheduleFrequence().name()).replaceAll(""));
    }
  }

  /**
   * Get a scheduled workflow based on datasets identifier.
   *
   * @param authorization the authorization header with the access token
   * @param datasetId the dataset identifier of which a scheduled workflow is to be retrieved
   * @return the scheduled workflow
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link UserUnauthorizedException} if user is unauthorized to access the scheduled
   * workflow</li>
   * <li>{@link NoDatasetFoundException} if dataset identifier does not exist</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE_DATASETID, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ScheduledWorkflow getScheduledWorkflow(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") String datasetId) throws GenericMetisException {
    MetisUserView metisUserView = authenticationClient.getUserByAccessTokenInHeader(authorization);
    ScheduledWorkflow scheduledWorkflow = scheduleWorkflowService
        .getScheduledWorkflowByDatasetId(metisUserView, datasetId);
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("ScheduledWorkflow with with datasetId '{}' found",
          datasetId.replaceAll(CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""));
    }
    return scheduledWorkflow;
  }

  @GetMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ResponseListWrapper<ScheduledWorkflow> getAllScheduledWorkflows(
      @RequestHeader("Authorization") String authorization,
      @RequestParam(value = "nextPage", required = false, defaultValue = "0") int nextPage)
      throws GenericMetisException {

    if (nextPage < 0) {
      throw new BadContentException(CommonStringValues.NEXT_PAGE_CANNOT_BE_NEGATIVE);
    }
    ResponseListWrapper<ScheduledWorkflow> responseListWrapper = new ResponseListWrapper<>();
    MetisUserView metisUserView = authenticationClient.getUserByAccessTokenInHeader(authorization);
    responseListWrapper.setResultsAndLastPage(scheduleWorkflowService
            .getAllScheduledWorkflows(metisUserView, ScheduleFrequence.NULL, nextPage),
        scheduleWorkflowService.getScheduledWorkflowsPerRequest(), nextPage);
    LOGGER.info("Batch of: {} scheduledWorkflows returned, using batch nextPage: {}",
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  @PutMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void updateScheduledWorkflow(@RequestHeader("Authorization") String authorization,
      @RequestBody ScheduledWorkflow scheduledWorkflow) throws GenericMetisException {
    MetisUserView metisUserView = authenticationClient.getUserByAccessTokenInHeader(authorization);
    scheduleWorkflowService.updateScheduledWorkflow(metisUserView, scheduledWorkflow);
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("ScheduledWorkflow with with datasetId '{}' updated",
          CRLF_PATTERN.matcher(scheduledWorkflow.getDatasetId()).replaceAll(""));
    }
  }

  @DeleteMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE_DATASETID, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void deleteScheduledWorkflowExecution(@RequestHeader("Authorization") String authorization,
      @PathVariable("datasetId") String datasetId) throws GenericMetisException {
    MetisUserView metisUserView = authenticationClient.getUserByAccessTokenInHeader(authorization);
    scheduleWorkflowService.deleteScheduledWorkflow(metisUserView, datasetId);
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("ScheduledWorkflowExecution for datasetId '{}' deleted",
          datasetId.replaceAll(CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""));
    }
  }
}
