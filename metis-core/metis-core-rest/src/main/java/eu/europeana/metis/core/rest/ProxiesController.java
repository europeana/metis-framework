package eu.europeana.metis.core.rest;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import eu.europeana.cloud.common.model.dps.StatisticsReport;
import eu.europeana.cloud.common.model.dps.SubTaskInfo;
import eu.europeana.cloud.common.model.dps.TaskErrorsInfo;
import eu.europeana.cloud.service.dps.exception.DpsException;
import eu.europeana.cloud.service.mcs.exception.MCSException;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.core.service.ProxiesService;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.exception.ExternalTaskException;

/**
 * Proxies Controller which encapsulates functionality that has to be proxied to an external resource.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-26
 */
@Controller
public class ProxiesController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProxiesController.class);
  private static final int NUMBER_OF_RECORDS = 5;
  private final ProxiesService proxiesService;

  /**
   * Constructor with required parameters
   *
   * @param proxiesService {@link ProxiesService}
   */
  @Autowired
  public ProxiesController(ProxiesService proxiesService) {
    this.proxiesService = proxiesService;
  }

  /**
   * Get logs from a specific topology task paged.
   *
   * @param topologyName the topology name of the task
   * @param externalTaskId the task identifier
   * @param from integer to start getting logs from
   * @param to integer until where logs should be received
   * @return the list of logs
   * @throws ExternalTaskException can be one of:
   * <ul>
   * <li>{@link DpsException} if an error occurred while retrieving the logs from the external resource</li>
   * </ul>
   */
  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_LOGS, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<SubTaskInfo> getExternalTaskLogs(
      @PathVariable("topologyName") String topologyName,
      @PathVariable("externalTaskId") long externalTaskId,
      @RequestParam(value = "from") int from,
      @RequestParam(value = "to") int to) throws ExternalTaskException {
    LOGGER.info(
        "Requesting proxy call task logs for topologyName: {}, externalTaskId: {}, from: {}, to: {}",
        topologyName, externalTaskId, from, to);
    return proxiesService.getExternalTaskLogs(topologyName, externalTaskId, from, to);
  }

  /**
   * Get the final report that includes all the errors grouped. The number of ids per error can be specified through the parameters.
   * @param topologyName the topology name of the task
   * @param externalTaskId the task identifier
   * @param idsPerError the number of ids that should be displayed per error group
   * @return the list of errors grouped
   * @throws ExternalTaskException can be one of:
   * <ul>
   * <li>{@link DpsException} if an error occurred while retrieving the report from the external resource</li>
   * </ul>
   */
  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_REPORT, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public TaskErrorsInfo getExternalTaskReport(
      @PathVariable("topologyName") String topologyName,
      @PathVariable("externalTaskId") long externalTaskId,
      @RequestParam("idsPerError") int idsPerError) throws ExternalTaskException {
    LOGGER.info(
        "Requesting proxy call task reports for topologyName: {}, externalTaskId: {}",
        topologyName, externalTaskId);
    return proxiesService.getExternalTaskReport(topologyName, externalTaskId, idsPerError);
  }

  /**
   * Get the statistics on the given task.
   * 
   * @param topologyName the topology name of the task
   * @param externalTaskId the task identifier
   * @return the task statistics
   * @throws ExternalTaskException can be one of:
   *         <ul>
   *         <li>{@link DpsException} if an error occurred while retrieving the statistics from the
   *         external resource</li>
   *         </ul>
   */
  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_STATISTICS,
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public StatisticsReport getExternalTaskStatistics(
      @PathVariable("topologyName") String topologyName,
      @PathVariable("externalTaskId") long externalTaskId) throws ExternalTaskException {
    LOGGER.info("Requesting proxy call task statistics for topologyName: {}, externalTaskId: {}",
        topologyName, externalTaskId);
    return proxiesService.getExternalTaskStatistics(topologyName, externalTaskId);
  }

  /**
   * Get a list with record contents from the external resource based on an workflow execution and {@link PluginType}
   * @param workflowExecutionId the execution identifier of the workflow
   * @param pluginType the {@link PluginType} that is to be located inside the workflow
   * @param nextPage the string representation of the next page which is provided from the response and can be used to get the next page of results
   * @return the list of records from the external resource
   * @throws ExternalTaskException can be one of:
   * <ul>
   * <li>{@link MCSException} if an error occurred while retrieving the records from the external resource</li>
   * </ul>
   */
  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_PROXIES_RECORDS, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public RecordsResponse getListOfFileContentsFromPluginExecution(
      @RequestParam("workflowExecutionId") String workflowExecutionId,
      @RequestParam("pluginType") PluginType pluginType,
      @RequestParam(value = "nextPage", required = false) String nextPage
  ) throws ExternalTaskException {
    return proxiesService.getListOfFileContentsFromPluginExecution(workflowExecutionId, pluginType,
        StringUtils.isEmpty(nextPage)?null:nextPage,
        NUMBER_OF_RECORDS);
  }

}
