package eu.europeana.metis.core.rest;

import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.RestEndpoints;
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

  public ScheduleWorkflowController(
      ScheduleWorkflowService scheduleWorkflowService) {
    this.scheduleWorkflowService = scheduleWorkflowService;
  }
  
  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE, method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public void scheduleWorkflowExecution(@RequestBody ScheduledWorkflow scheduledWorkflow)
      throws GenericMetisException {
    scheduleWorkflowService.scheduleWorkflow(scheduledWorkflow);
    LOGGER.info(
        "ScheduledWorkflowExecution for datasetId '{}', workflowOwner '{}', pointerDate at '{}', scheduled '{}'",
        scheduledWorkflow.getDatasetId(),
        scheduledWorkflow.getWorkflowOwner(),
        scheduledWorkflow.getPointerDate(),
        scheduledWorkflow.getScheduleFrequence().name());
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE_DATASETID, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public ScheduledWorkflow getScheduledWorkflow(
      @PathVariable("datasetId") int datasetId) {
    ScheduledWorkflow scheduledWorkflow = scheduleWorkflowService
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
    responseListWrapper.setResultsAndLastPage(scheduleWorkflowService
            .getAllScheduledWorkflows(ScheduleFrequence.NULL, nextPage),
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
      @RequestBody ScheduledWorkflow scheduledWorkflow)
      throws GenericMetisException {
    scheduleWorkflowService.updateScheduledWorkflow(scheduledWorkflow);
    LOGGER.info("ScheduledWorkflow with with datasetId '{}' updated",
        scheduledWorkflow.getDatasetId());
  }

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_WORKFLOWS_SCHEDULE_DATASETID, method = RequestMethod.DELETE, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void deleteScheduledWorkflowExecution(
      @PathVariable("datasetId") int datasetId) {
    scheduleWorkflowService.deleteScheduledWorkflow(datasetId);
    LOGGER.info("ScheduledWorkflowExecution for datasetId '{}' deleted", datasetId);
  }
}
