package eu.europeana.metis.core.rest;

import eu.europeana.cloud.common.model.dps.SubTaskInfo;
import eu.europeana.cloud.common.model.dps.TaskErrorsInfo;
import eu.europeana.metis.CommonStringValues;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.authentication.rest.client.AuthenticationClient;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.rest.stats.NodePathStatistics;
import eu.europeana.metis.core.rest.stats.RecordStatistics;
import eu.europeana.metis.core.service.ProxiesService;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.exception.GenericMetisException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Proxies Controller which encapsulates functionality that has to be proxied to an external
 * resource.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-26
 */
@Controller
public class ProxiesController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProxiesController.class);
  private static final int NUMBER_OF_RECORDS = 5;
  private final ProxiesService proxiesService;
  private final AuthenticationClient authenticationClient;

  /**
   * Constructor with required parameters
   *
   * @param proxiesService {@link ProxiesService}
   * @param authenticationClient the client for the authentication service
   */
  @Autowired
  public ProxiesController(ProxiesService proxiesService,
      AuthenticationClient authenticationClient) {
    this.proxiesService = proxiesService;
    this.authenticationClient = authenticationClient;
  }

  /**
   * Get logs from a specific topology task paged.
   *
   * @param authorization the authorization header with the access token
   * @param topologyName the topology name of the task
   * @param externalTaskId the task identifier
   * @param from integer to start getting logs from
   * @param to integer until where logs should be received
   * @return the list of logs
   * @throws GenericMetisException can be one of:
   * <ul>
   * <li>{@link eu.europeana.cloud.service.dps.exception.DpsException} if an error occurred while
   * retrieving the logs from the external resource</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no
   * workflow execution exists for the provided external task identifier</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_LOGS, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<SubTaskInfo> getExternalTaskLogs(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("topologyName") String topologyName,
      @PathVariable("externalTaskId") long externalTaskId,
      @RequestParam(value = "from") int from,
      @RequestParam(value = "to") int to) throws GenericMetisException {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(
          "Requesting proxy call task logs for topologyName: {}, externalTaskId: {}, from: {}, to: {}",
          topologyName.replaceAll(CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""),
          externalTaskId, from, to);
    }
    final MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    return proxiesService.getExternalTaskLogs(metisUser, topologyName, externalTaskId, from, to);
  }

  /**
   * Check if final report is available.
   *
   * @param authorization the authorization header with the access token
   * @param topologyName the topology name of the task
   * @param externalTaskId the task identifier
   * @return true if final report available, false if not or ecloud response {@link
   * javax.ws.rs.core.Response.Status)} is not OK, based on {@link eu.europeana.cloud.client.dps.rest.DpsClient#checkIfErrorReportExists}
   * @throws GenericMetisException can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no
   * workflow execution exists for the provided external task identifier</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_REPORT_EXISTS, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Map<String, Boolean> existsExternalTaskReport(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("topologyName") String topologyName,
      @PathVariable("externalTaskId") long externalTaskId) throws GenericMetisException {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(
          "Requesting proxy call to check if task report exists for topologyName: {}, externalTaskId: {}",
          topologyName.replaceAll(CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""),
          externalTaskId);
    }
    final MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    return Collections.singletonMap("existsExternalTaskReport",
        proxiesService.existsExternalTaskReport(metisUser, topologyName, externalTaskId));
  }

  /**
   * Get the final report that includes all the errors grouped. The number of ids per error can be
   * specified through the parameters.
   *
   * @param authorization the authorization header with the access token
   * @param topologyName the topology name of the task
   * @param externalTaskId the task identifier
   * @param idsPerError the number of ids that should be displayed per error group
   * @return the list of errors grouped
   * @throws GenericMetisException can be one of:
   * <ul>
   * <li>{@link eu.europeana.cloud.service.dps.exception.DpsException} if an error occurred while
   * retrieving the report from the external resource</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no
   * workflow execution exists for the provided external task identifier</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_REPORT, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public TaskErrorsInfo getExternalTaskReport(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("topologyName") String topologyName,
      @PathVariable("externalTaskId") long externalTaskId,
      @RequestParam("idsPerError") int idsPerError) throws GenericMetisException {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Requesting proxy call task reports for topologyName: {}, externalTaskId: {}",
          topologyName.replaceAll(CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""),
          externalTaskId);
    }
    final MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    return proxiesService
        .getExternalTaskReport(metisUser, topologyName, externalTaskId, idsPerError);
  }

  /**
   * Get the statistics on the given task.
   *
   * @param authorization the authorization header with the access token
   * @param topologyName the topology name of the task
   * @param externalTaskId the task identifier
   * @return the task statistics
   * @throws GenericMetisException can be one of:
   * <ul>
   * <li>{@link eu.europeana.cloud.service.dps.exception.DpsException} if an error occurred while
   * retrieving the statistics from the external resource</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no
   * workflow execution exists for the provided external task identifier</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_STATISTICS,
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public RecordStatistics getExternalTaskStatistics(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("topologyName") String topologyName,
      @PathVariable("externalTaskId") long externalTaskId) throws GenericMetisException {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Requesting proxy call task statistics for topologyName: {}, externalTaskId: {}",
          topologyName.replaceAll(CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""),
          externalTaskId);
    }
    final MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    return proxiesService.getExternalTaskStatistics(metisUser, topologyName, externalTaskId);
  }

  /**
   * Get additional statistics on a node. This method can be used to elaborate on one of the items
   * returned by {@link #getExternalTaskStatistics(String, String, long)}.
   *
   * @param authorization the authorization header with the access token
   * @param topologyName the topology name of the task
   * @param externalTaskId the task identifier
   * @param nodePath the path of the node for which this request is made
   * @return the list of errors grouped
   * @throws GenericMetisException can be one of:
   * <ul>
   * <li>{@link eu.europeana.cloud.service.dps.exception.DpsException} if an error occurred while
   * retrieving the statistics from the external resource</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no
   * workflow execution exists for the provided external task identifier</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.ORCHESTRATOR_PROXIES_TOPOLOGY_TASK_NODE_STATISTICS,
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public NodePathStatistics getAdditionalNodeStatistics(
      @RequestHeader("Authorization") String authorization,
      @PathVariable("topologyName") String topologyName,
      @PathVariable("externalTaskId") long externalTaskId,
      @RequestParam("nodePath") String nodePath) throws GenericMetisException {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(
          "Requesting proxy call additional node statistics for topologyName: {}, externalTaskId: {}",
          topologyName.replaceAll(CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""),
          externalTaskId);
    }
    final MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    return proxiesService
        .getAdditionalNodeStatistics(metisUser, topologyName, externalTaskId, nodePath);
  }

  /**
   * Get a list with record contents from the external resource based on an workflow execution and
   * {@link PluginType}.
   *
   * @param authorization the authorization header with the access token
   * @param workflowExecutionId the execution identifier of the workflow
   * @param pluginType the {@link PluginType} that is to be located inside the workflow
   * @param nextPage the string representation of the next page which is provided from the response
   * and can be used to get the next page of results
   * @return the list of records from the external resource
   * @throws GenericMetisException can be one of:
   * <ul>
   * <li>{@link eu.europeana.cloud.service.mcs.exception.MCSException} if an error occurred while
   * retrieving the records from the external resource</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no
   * workflow execution exists for the provided identifier</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authenticated or authorized to perform this operation</li>
   * </ul>
   */
  @GetMapping(value = RestEndpoints.ORCHESTRATOR_PROXIES_RECORDS,
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public RecordsResponse getListOfFileContentsFromPluginExecution(
      @RequestHeader("Authorization") String authorization,
      @RequestParam("workflowExecutionId") String workflowExecutionId,
      @RequestParam("pluginType") ExecutablePluginType pluginType,
      @RequestParam(value = "nextPage", required = false) String nextPage
  ) throws GenericMetisException {
    final MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    return proxiesService
        .getListOfFileContentsFromPluginExecution(metisUser, workflowExecutionId, pluginType,
            StringUtils.isEmpty(nextPage) ? null : nextPage, NUMBER_OF_RECORDS);
  }

  /**
   * Get a list with record contents from the external resource based on an workflow execution and
   * {@link PluginType}.
   *
   * @param authorization the authorization header with the access token
   * @param workflowExecutionId the execution identifier of the workflow
   * @param pluginType the {@link ExecutablePluginType} that is to be located inside the workflow
   * @param ecloudIds the list of ecloud IDs of the records we wish to obtain
   * @return the list of records from the external resource
   * @throws GenericMetisException can be one of:
   * <ul>
   * <li>{@link eu.europeana.cloud.service.mcs.exception.MCSException} if an error occurred while
   * retrieving the records from the external resource</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authorized to perform this task</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no workflow
   * execution exists for the provided identifier</li>
   * </ul>
   */
  @PostMapping(value = RestEndpoints.ORCHESTRATOR_PROXIES_RECORDS_BY_IDS,
      consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public RecordsResponse getListOfFileContentsFromPluginExecution(
      @RequestHeader("Authorization") String authorization,
      @RequestParam("workflowExecutionId") String workflowExecutionId,
      @RequestParam("pluginType") ExecutablePluginType pluginType,
      @RequestBody ListOfIds ecloudIds
  ) throws GenericMetisException {
    final MetisUser metisUser = authenticationClient.getUserByAccessTokenInHeader(authorization);
    return proxiesService.getListOfFileContentsFromPluginExecution(metisUser, workflowExecutionId,
        pluginType, ecloudIds);
  }
}
