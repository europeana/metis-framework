package eu.europeana.metis.core.rest;

import eu.europeana.cloud.common.model.dps.SubTaskInfo;
import eu.europeana.cloud.common.model.dps.TaskErrorsInfo;
import eu.europeana.cloud.service.mcs.exception.MCSException;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.core.service.ProxiesService;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.exception.ExternalTaskException;
import java.util.List;
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

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-26
 */
@Controller
public class ProxiesController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProxiesController.class);
  private final ProxiesService proxiesService;

  @Autowired
  public ProxiesController(ProxiesService proxiesService) {
    this.proxiesService = proxiesService;
  }

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

  @RequestMapping(value = RestEndpoints.ORCHESTRATOR_PROXIES_REVISION, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<RecordResponse> getListOfFileContentsFromPluginExecution(
      @RequestParam("workflowExecutionId") String workflowExecutionId,
      @RequestParam("pluginType") PluginType pluginType
  ) throws MCSException {
    return proxiesService.getListOfFileContentsFromPluginExecution(workflowExecutionId, pluginType);
  }

}
