package eu.europeana.metis.core.service;

import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.common.model.File;
import eu.europeana.cloud.common.model.Representation;
import eu.europeana.cloud.common.model.dps.NodeReport;
import eu.europeana.cloud.common.model.dps.StatisticsReport;
import eu.europeana.cloud.common.model.dps.SubTaskInfo;
import eu.europeana.cloud.common.model.dps.TaskErrorsInfo;
import eu.europeana.cloud.common.response.CloudTagsResponse;
import eu.europeana.cloud.common.response.ResultSlice;
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.cloud.mcs.driver.FileServiceClient;
import eu.europeana.cloud.mcs.driver.RecordServiceClient;
import eu.europeana.cloud.service.dps.exception.DpsException;
import eu.europeana.cloud.service.mcs.exception.MCSException;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException;
import eu.europeana.metis.core.rest.ListOfIds;
import eu.europeana.metis.core.rest.PaginatedRecordsResponse;
import eu.europeana.metis.core.rest.Record;
import eu.europeana.metis.core.rest.RecordsResponse;
import eu.europeana.metis.core.rest.stats.NodePathStatistics;
import eu.europeana.metis.core.rest.stats.RecordStatistics;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.MetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.exception.ExternalTaskException;
import eu.europeana.metis.exception.GenericMetisException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Proxies Service which encapsulates functionality that has to be proxied to an external resource.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-26
 */
public class ProxiesService {

  protected final DateFormat pluginDateFormatForEcloud = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);

  private final WorkflowExecutionDao workflowExecutionDao;
  private final DataSetServiceClient ecloudDataSetServiceClient;
  private final RecordServiceClient recordServiceClient;
  private final FileServiceClient fileServiceClient;
  private final DpsClient dpsClient;
  private final String ecloudProvider;
  private final Authorizer authorizer;
  private final ProxiesHelper proxiesHelper;

  /**
   * Constructor with required parameters.
   *
   * @param workflowExecutionDao {@link WorkflowExecutionDao}
   * @param ecloudDataSetServiceClient {@link DataSetServiceClient}
   * @param recordServiceClient {@link RecordServiceClient}
   * @param fileServiceClient {@link FileServiceClient}
   * @param dpsClient {@link DpsClient}
   * @param ecloudProvider the ecloud provider string
   * @param authorizer the authorizer
   */
  public ProxiesService(WorkflowExecutionDao workflowExecutionDao,
      DataSetServiceClient ecloudDataSetServiceClient, RecordServiceClient recordServiceClient,
      FileServiceClient fileServiceClient, DpsClient dpsClient, String ecloudProvider,
      Authorizer authorizer) {
    this(workflowExecutionDao, ecloudDataSetServiceClient, recordServiceClient, fileServiceClient,
        dpsClient, ecloudProvider, authorizer, new ProxiesHelper());
  }

  ProxiesService(WorkflowExecutionDao workflowExecutionDao,
      DataSetServiceClient ecloudDataSetServiceClient, RecordServiceClient recordServiceClient,
      FileServiceClient fileServiceClient, DpsClient dpsClient, String ecloudProvider,
      Authorizer authorizer, ProxiesHelper proxiesHelper) {
    this.workflowExecutionDao = workflowExecutionDao;
    this.ecloudDataSetServiceClient = ecloudDataSetServiceClient;
    this.recordServiceClient = recordServiceClient;
    this.fileServiceClient = fileServiceClient;
    this.dpsClient = dpsClient;
    this.ecloudProvider = ecloudProvider;
    this.authorizer = authorizer;
    this.proxiesHelper = proxiesHelper;
  }

  /**
   * Get logs from a specific topology task paged.
   *
   * @param metisUser the user wishing to perform this operation
   * @param topologyName the topology name of the task
   * @param externalTaskId the task identifier
   * @param from integer to start getting logs from
   * @param to integer until where logs should be received
   * @return the list of logs
   * @throws GenericMetisException can be one of:
   * <ul>
   * <li>{@link DpsException} if an error occurred while retrieving the logs from the external
   * resource</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authorized to perform this task</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no
   * workflow execution exists for the provided external task identifier</li>
   * </ul>
   */
  public List<SubTaskInfo> getExternalTaskLogs(MetisUser metisUser, String topologyName,
      long externalTaskId, int from, int to) throws GenericMetisException {
    authorizer.authorizeReadExistingDatasetById(metisUser,
        getDatasetIdFromExternalTaskId(externalTaskId));
    List<SubTaskInfo> detailedTaskReportBetweenChunks;
    try {
      detailedTaskReportBetweenChunks =
          dpsClient.getDetailedTaskReportBetweenChunks(topologyName, externalTaskId, from, to);
    } catch (DpsException e) {
      throw new ExternalTaskException(String.format(
          "Getting the task detailed logs failed. topologyName: %s, externalTaskId: %s, from: %s, to: %s",
          topologyName, externalTaskId, from, to), e);
    }
    for (SubTaskInfo subTaskInfo : detailedTaskReportBetweenChunks) { // Hide sensitive information
      subTaskInfo.setAdditionalInformations(null);
    }
    return detailedTaskReportBetweenChunks;
  }

  /**
   * Check if final report is available.
   *
   * @param metisUser the user wishing to perform this operation
   * @param topologyName the topology name of the task
   * @param externalTaskId the task identifier
   * @return true if final report available, false if not or ecloud response {@link
   * javax.ws.rs.core.Response.Status)} is not OK, based on {@link DpsClient#checkIfErrorReportExists}
   * @throws GenericMetisException can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authorized to perform this task</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no
   * workflow execution exists for the provided external task identifier</li>
   * </ul>
   */
  public boolean existsExternalTaskReport(MetisUser metisUser, String topologyName,
      long externalTaskId)
      throws GenericMetisException {
    authorizer.authorizeReadExistingDatasetById(metisUser,
        getDatasetIdFromExternalTaskId(externalTaskId));
    return dpsClient.checkIfErrorReportExists(topologyName, externalTaskId);
  }

  /**
   * Get the final report that includes all the errors grouped. The number of ids per error can be
   * specified through the parameters.
   *
   * @param metisUser the user wishing to perform this operation
   * @param topologyName the topology name of the task
   * @param externalTaskId the task identifier
   * @param idsPerError the number of ids that should be displayed per error group
   * @return the list of errors grouped
   * @throws GenericMetisException can be one of:
   * <ul>
   * <li>{@link DpsException} if an error occurred while retrieving the report from the external
   * resource</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authorized to perform this task</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no
   * workflow execution exists for the provided external task identifier</li>
   * </ul>
   */
  public TaskErrorsInfo getExternalTaskReport(MetisUser metisUser, String topologyName,
      long externalTaskId, int idsPerError) throws GenericMetisException {
    authorizer.authorizeReadExistingDatasetById(metisUser,
        getDatasetIdFromExternalTaskId(externalTaskId));
    TaskErrorsInfo taskErrorsInfo;
    try {
      taskErrorsInfo =
          dpsClient.getTaskErrorsReport(topologyName, externalTaskId, null, idsPerError);
    } catch (DpsException e) {
      throw new ExternalTaskException(String.format(
          "Getting the task error report failed. topologyName: %s, externalTaskId: %s, idsPerError: %s",
          topologyName, externalTaskId, idsPerError), e);
    }
    return taskErrorsInfo;
  }

  /**
   * Get the statistics of an external task.
   *
   * @param metisUser the user wishing to perform this operation
   * @param topologyName the topology name of the task
   * @param externalTaskId the task identifier
   * @return the record statistics for the given task.
   * @throws GenericMetisException can be one of:
   * <ul>
   * <li>{@link DpsException} if an error occurred while retrieving the statistics from the
   * external resource</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authorized to perform this task</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no
   * workflow execution exists for the provided external task identifier</li>
   * </ul>
   */
  public RecordStatistics getExternalTaskStatistics(MetisUser metisUser, String topologyName,
      long externalTaskId) throws GenericMetisException {

    // Authorize
    authorizer.authorizeReadExistingDatasetById(metisUser,
        getDatasetIdFromExternalTaskId(externalTaskId));

    // Obtain the report from eCloud.
    final StatisticsReport report;
    try {
      report = dpsClient.getTaskStatisticsReport(topologyName, externalTaskId);
    } catch (DpsException e) {
      throw new ExternalTaskException(String.format(
          "Getting the task statistics failed. topologyName: %s, externalTaskId: %s",
          topologyName, externalTaskId), e);
    }

    // Convert them and done.
    return proxiesHelper.compileRecordStatistics(report);
  }

  /**
   * Get additional statistics on a node. This method can be used to elaborate on one of the items
   * returned by {@link #getExternalTaskStatistics(MetisUser, String, long)}.
   *
   * @param metisUser the user wishing to perform this operation
   * @param topologyName the topology name of the task
   * @param externalTaskId the task identifier
   * @param nodePath the path of the node for which this request is made
   * @return the node statistics for the given path in the given task.
   * @throws GenericMetisException can be one of:
   * <ul>
   * <li>{@link DpsException} if an error occurred while retrieving the statistics from the
   * external resource</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authorized to perform this task</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no
   * workflow execution exists for the provided external task identifier</li>
   * </ul>
   */
  public NodePathStatistics getAdditionalNodeStatistics(MetisUser metisUser, String topologyName,
      long externalTaskId, String nodePath) throws GenericMetisException {

    // Authorize
    authorizer.authorizeReadExistingDatasetById(metisUser,
        getDatasetIdFromExternalTaskId(externalTaskId));

    // Obtain the reports from eCloud.
    final List<NodeReport> nodeReports;
    try {
      nodeReports = dpsClient.getElementReport(topologyName, externalTaskId, nodePath);
    } catch (DpsException e) {
      throw new ExternalTaskException(String.format(
          "Getting the additional node statistics failed. topologyName: %s, externalTaskId: %s",
          topologyName, externalTaskId), e);
    }

    // Convert them and done.
    return proxiesHelper.compileNodePathStatistics(nodePath, nodeReports);
  }

  private String getDatasetIdFromExternalTaskId(long externalTaskId)
      throws NoWorkflowExecutionFoundException {
    final WorkflowExecution workflowExecution =
        this.workflowExecutionDao.getByExternalTaskId(externalTaskId);
    if (workflowExecution == null) {
      throw new NoWorkflowExecutionFoundException(String
          .format("No workflow execution found for externalTaskId: %d, in METIS", externalTaskId));
    }
    return workflowExecution.getDatasetId();
  }

  /**
   * Get a list with record contents from the external resource based on an workflow execution and
   * {@link PluginType}.
   *
   * @param metisUser the user wishing to perform this operation
   * @param workflowExecutionId the execution identifier of the workflow
   * @param pluginType the {@link ExecutablePluginType} that is to be located inside the workflow
   * @param nextPage the string representation of the next page which is provided from the response
   * and can be used to get the next page of results
   * @param numberOfRecords the number of records per response
   * @return the list of records from the external resource
   * @throws GenericMetisException can be one of:
   * <ul>
   * <li>{@link MCSException} if an error occurred while retrieving the records from the external
   * resource</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authorized to perform this task</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no
   * workflow execution exists for the provided identifier</li>
   * </ul>
   */
  public PaginatedRecordsResponse getListOfFileContentsFromPluginExecution(MetisUser metisUser,
      String workflowExecutionId, ExecutablePluginType pluginType, String nextPage,
      int numberOfRecords) throws GenericMetisException {

    // Get the right workflow execution and plugin type.
    final Pair<WorkflowExecution, AbstractExecutablePlugin> executionAndPlugin = getExecutionAndPlugin(
        metisUser, workflowExecutionId, pluginType);
    if (executionAndPlugin == null) {
      return new PaginatedRecordsResponse(Collections.emptyList(), null);
    }

    // Get the list of records.
    final String datasetId = executionAndPlugin.getLeft().getEcloudDatasetId();
    final String representationName = MetisPlugin.getRepresentationName();
    final String revisionName = executionAndPlugin.getRight().getPluginType().name();
    final String revisionTimestamp = pluginDateFormatForEcloud
        .format(executionAndPlugin.getRight().getStartedDate());
    final ResultSlice<CloudTagsResponse> resultSlice;
    final String nextPageAfterResponse;
    try {
      resultSlice = ecloudDataSetServiceClient
          .getDataSetRevisionsChunk(ecloudProvider, datasetId, representationName, revisionName,
              ecloudProvider, revisionTimestamp, nextPage, numberOfRecords);
      nextPageAfterResponse = resultSlice.getNextSlice();
    } catch (MCSException e) {
      throw new ExternalTaskException(String.format(
          "Getting record list with file content failed. workflowExecutionId: %s, pluginType: %s",
          workflowExecutionId, pluginType), e);
    }

    // Get the records themselves.
    final List<Record> records = new ArrayList<>(resultSlice.getResults().size());
    for (CloudTagsResponse cloudTagsResponse : resultSlice.getResults()) {
      records.add(getRecord(executionAndPlugin.getRight(), cloudTagsResponse.getCloudId()));
    }

    // Compile the result.
    return new PaginatedRecordsResponse(records, nextPageAfterResponse);
  }

  /**
   * Get a list with record contents from the external resource based on an workflow execution and
   * {@link PluginType}.
   *
   * @param metisUser the user wishing to perform this operation
   * @param workflowExecutionId the execution identifier of the workflow
   * @param pluginType the {@link ExecutablePluginType} that is to be located inside the workflow
   * @param ecloudIds the list of ecloud IDs of the records we wish to obtain
   * @return the list of records from the external resource
   * @throws GenericMetisException can be one of:
   * <ul>
   * <li>{@link MCSException} if an error occurred while retrieving the records from the external
   * resource</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authorized to perform this task</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no workflow
   * execution exists for the provided identifier</li>
   * </ul>
   */
  public RecordsResponse getListOfFileContentsFromPluginExecution(MetisUser metisUser,
      String workflowExecutionId, ExecutablePluginType pluginType, ListOfIds ecloudIds)
      throws GenericMetisException {

    // Get the right workflow execution and plugin type.
    final Pair<WorkflowExecution, AbstractExecutablePlugin> executionAndPlugin = getExecutionAndPlugin(
        metisUser, workflowExecutionId, pluginType);
    if (executionAndPlugin == null) {
      throw new NoWorkflowExecutionFoundException(String
          .format("No executable plugin of type %s found for workflowExecution with id: %s",
              pluginType.name(), workflowExecutionId));
    }

    // Get the records.
    final List<Record> records = new ArrayList<>(ecloudIds.getIds().size());
    for (String cloudId : ecloudIds.getIds()) {
      records.add(getRecord(executionAndPlugin.getRight(), cloudId));
    }

    // Done.
    return new RecordsResponse(records);
  }

  Pair<WorkflowExecution, AbstractExecutablePlugin> getExecutionAndPlugin(MetisUser metisUser,
      String workflowExecutionId, ExecutablePluginType pluginType) throws GenericMetisException {

    // Get the workflow execution - check that the user has rights to access this.
    final WorkflowExecution workflowExecution = workflowExecutionDao.getById(workflowExecutionId);
    if (workflowExecution == null) {
      throw new NoWorkflowExecutionFoundException(
          String.format("No workflow execution found for workflowExecutionId: %s, in METIS",
              workflowExecutionId));
    }
    authorizer.authorizeReadExistingDatasetById(metisUser, workflowExecution.getDatasetId());

    // Get the plugin for which to get the records and return.
    final AbstractMetisPlugin plugin = workflowExecution
        .getMetisPluginWithType(pluginType.toPluginType()).orElse(null);
    if (plugin instanceof AbstractExecutablePlugin) {
      return new ImmutablePair<>(workflowExecution, (AbstractExecutablePlugin) plugin);
    }
    return null;
  }

  Record getRecord(AbstractExecutablePlugin plugin, String ecloudId) throws ExternalTaskException {

    // Get the representation(s) for the given combination of plugin and record ID.
    final List<Representation> representations;
    try {
      representations = recordServiceClient
          .getRepresentationsByRevision(ecloudId, MetisPlugin.getRepresentationName(),
              plugin.getPluginType().name(), ecloudProvider,
              pluginDateFormatForEcloud.format(plugin.getStartedDate()));
    } catch (MCSException e) {
      throw new ExternalTaskException(String.format(
          "Getting record list with file content failed. externalTaskId: %s, pluginType: %s, ecloudId: %s",
          plugin.getExternalTaskId(), plugin.getPluginType(), ecloudId), e);
    }

    // Perform checks on the representation version and file lists.
    if (representations == null || representations.size() != 1) {
      final int size = representations == null ? 0 : representations.size();
      throw new ExternalTaskException(String.format(
          "Expecting one representation, but received %s. externalTaskId: %s, pluginType: %s, ecloudId: %s",
          size, plugin.getExternalTaskId(), plugin.getPluginType(), ecloudId));
    }
    final Representation representation = representations.get(0);
    if (representation.getFiles()==null ||representation.getFiles().size()!=1){
      final int size = representation.getFiles() == null ? 0 : representation.getFiles().size();
      throw new ExternalTaskException(String.format(
          "Expecting one file in the representation, but received %s. externalTaskId: %s, pluginType: %s, ecloudId: %s",
          size, plugin.getExternalTaskId(), plugin.getPluginType(), ecloudId));
    }
    final File file = representation.getFiles().get(0);

    // Obtain the file contents belonging to this representation version.
    try {
      final InputStream inputStream = fileServiceClient.getFile(file.getContentUri().toString());
      return new Record(ecloudId, IOUtils.toString(inputStream, StandardCharsets.UTF_8.name()));
    } catch (MCSException e) {
      throw new ExternalTaskException(String.format(
          "Getting record list with file content failed. externalTaskId: %s, pluginType: %s",
          plugin.getExternalTaskId(), plugin.getPluginType()), e);
    } catch (IOException e) {
      throw new ExternalTaskException("Problem while reading the contents of the file.", e);
    }
  }

  String getEcloudProvider() {
    return ecloudProvider;
  }
}
