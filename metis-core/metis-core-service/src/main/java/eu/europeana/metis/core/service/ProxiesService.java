package eu.europeana.metis.core.service;

import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.client.uis.rest.CloudException;
import eu.europeana.cloud.client.uis.rest.UISClient;
import eu.europeana.cloud.common.model.File;
import eu.europeana.cloud.common.model.Representation;
import eu.europeana.cloud.common.model.Revision;
import eu.europeana.cloud.common.model.dps.NodeReport;
import eu.europeana.cloud.common.model.dps.StatisticsReport;
import eu.europeana.cloud.common.model.dps.SubTaskInfo;
import eu.europeana.cloud.common.model.dps.TaskErrorsInfo;
import eu.europeana.cloud.common.response.CloudTagsResponse;
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.cloud.mcs.driver.FileServiceClient;
import eu.europeana.cloud.mcs.driver.RecordServiceClient;
import eu.europeana.cloud.service.dps.exception.DpsException;
import eu.europeana.cloud.service.mcs.exception.MCSException;
import eu.europeana.cloud.service.uis.exception.RecordDoesNotExistException;
import eu.europeana.metis.authentication.user.MetisUserView;
import eu.europeana.metis.core.common.RecordIdUtils;
import eu.europeana.metis.core.dao.DataEvolutionUtils;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException;
import eu.europeana.metis.core.rest.ListOfIds;
import eu.europeana.metis.core.rest.PaginatedRecordsResponse;
import eu.europeana.metis.core.rest.Record;
import eu.europeana.metis.core.rest.RecordsResponse;
import eu.europeana.metis.core.rest.stats.NodePathStatistics;
import eu.europeana.metis.core.rest.stats.RecordStatistics;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.ExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.ExecutablePluginType;
import eu.europeana.metis.core.workflow.plugins.MetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.core.workflow.plugins.*;
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
import java.util.Optional;
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
  private final UISClient uisClient;
  private final String ecloudProvider;
  private final Authorizer authorizer;
  private final ProxiesHelper proxiesHelper;
  private final DataEvolutionUtils dataEvolutionUtils;

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
      FileServiceClient fileServiceClient, DpsClient dpsClient, UISClient uisClient, String ecloudProvider,
      Authorizer authorizer) {
    this(workflowExecutionDao, ecloudDataSetServiceClient, recordServiceClient, fileServiceClient,
        dpsClient, uisClient, ecloudProvider, authorizer, new ProxiesHelper());
  }

  ProxiesService(WorkflowExecutionDao workflowExecutionDao,
      DataSetServiceClient ecloudDataSetServiceClient, RecordServiceClient recordServiceClient,
      FileServiceClient fileServiceClient, DpsClient dpsClient, UISClient uisClient, String ecloudProvider,
      Authorizer authorizer, ProxiesHelper proxiesHelper) {
    this.workflowExecutionDao = workflowExecutionDao;
    this.ecloudDataSetServiceClient = ecloudDataSetServiceClient;
    this.recordServiceClient = recordServiceClient;
    this.fileServiceClient = fileServiceClient;
    this.dpsClient = dpsClient;
    this.uisClient = uisClient;
    this.ecloudProvider = ecloudProvider;
    this.authorizer = authorizer;
    this.proxiesHelper = proxiesHelper;
    this.dataEvolutionUtils = new DataEvolutionUtils(this.workflowExecutionDao);
  }

  /**
   * Get logs from a specific topology task paged.
   *
   * @param metisUserView the user wishing to perform this operation
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
  public List<SubTaskInfo> getExternalTaskLogs(MetisUserView metisUserView, String topologyName,
      long externalTaskId, int from, int to) throws GenericMetisException {
    authorizer.authorizeReadExistingDatasetById(metisUserView,
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
   * @param metisUserView the user wishing to perform this operation
   * @param topologyName the topology name of the task
   * @param externalTaskId the task identifier
   * @return true if final report available, false if not or ecloud response {@link javax.ws.rs.core.Response.Status)} is not OK,
   * based on {@link DpsClient#checkIfErrorReportExists}
   * @throws GenericMetisException can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authorized to perform this task</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no
   * workflow execution exists for the provided external task identifier</li>
   * <li>{@link ExternalTaskException} containing {@link DpsException} if an error occurred while checking if the error report exists</li>
   * </ul>
   */
  public boolean existsExternalTaskReport(MetisUserView metisUserView, String topologyName,
      long externalTaskId)
      throws GenericMetisException {
    authorizer.authorizeReadExistingDatasetById(metisUserView,
        getDatasetIdFromExternalTaskId(externalTaskId));
    try {
      return dpsClient.checkIfErrorReportExists(topologyName, externalTaskId);
    } catch (DpsException e) {
      throw new ExternalTaskException(String.format(
          "Checking if the error report exists failed. topologyName: %s, externalTaskId: %s",
          topologyName, externalTaskId), e);
    }
  }

  /**
   * Get the final report that includes all the errors grouped. The number of ids per error can be specified through the
   * parameters.
   *
   * @param metisUserView the user wishing to perform this operation
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
  public TaskErrorsInfo getExternalTaskReport(MetisUserView metisUserView, String topologyName,
      long externalTaskId, int idsPerError) throws GenericMetisException {
    authorizer.authorizeReadExistingDatasetById(metisUserView,
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
   * @param metisUserView the user wishing to perform this operation
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
  public RecordStatistics getExternalTaskStatistics(MetisUserView metisUserView, String topologyName,
      long externalTaskId) throws GenericMetisException {

    // Authorize
    authorizer.authorizeReadExistingDatasetById(metisUserView,
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
   * Get additional statistics on a node. This method can be used to elaborate on one of the items returned by {@link
   * #getExternalTaskStatistics(MetisUserView, String, long)}.
   *
   * @param metisUserView the user wishing to perform this operation
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
  public NodePathStatistics getAdditionalNodeStatistics(MetisUserView metisUserView, String topologyName,
      long externalTaskId, String nodePath) throws GenericMetisException {

    // Authorize
    authorizer.authorizeReadExistingDatasetById(metisUserView,
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
   * Get a list with record contents from the external resource based on a workflow execution and {@link PluginType}.
   *
   * @param metisUserView the user wishing to perform this operation
   * @param workflowExecutionId the execution identifier of the workflow
   * @param pluginType the {@link ExecutablePluginType} that is to be located inside the workflow
   * @param nextPage the string representation of the next page which is provided from the response and can be used to get the
   * next page of results.
   * TODO: The nextPage parameter is currently ignored and we should decide if we would support it again in the future.
   * @param numberOfRecords the number of records per response
   * @return the list of records from the external resource
   * @throws GenericMetisException can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.exception.ExternalTaskException} if an error occurred while
   * retrieving the records from the external resource</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authorized to perform this task</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no
   * workflow execution exists for the provided identifier</li>
   * </ul>
   */
  public PaginatedRecordsResponse getListOfFileContentsFromPluginExecution(
      MetisUserView metisUserView,
      String workflowExecutionId, ExecutablePluginType pluginType, String nextPage,
      int numberOfRecords) throws GenericMetisException {

    // Get the right workflow execution and plugin type.
    final Pair<WorkflowExecution, ExecutablePlugin> executionAndPlugin = getExecutionAndPlugin(
        metisUserView, workflowExecutionId, pluginType);
    if (executionAndPlugin == null) {
      return new PaginatedRecordsResponse(Collections.emptyList(), null);
    }

    // Get the list of records.
    final String datasetId = executionAndPlugin.getLeft().getEcloudDatasetId();
    final String representationName = MetisPlugin.getRepresentationName();
    final String revisionName = executionAndPlugin.getRight().getPluginType().name();
    final String revisionTimestamp = pluginDateFormatForEcloud
        .format(executionAndPlugin.getRight().getStartedDate());
    final List<CloudTagsResponse> revisionsWithDeletedFlagSetToFalse;
    try {
      revisionsWithDeletedFlagSetToFalse = ecloudDataSetServiceClient.getRevisionsWithDeletedFlagSetToFalse(
          ecloudProvider, datasetId, representationName, revisionName, ecloudProvider, revisionTimestamp, numberOfRecords);
    } catch (MCSException e) {
      throw new ExternalTaskException(String.format(
          "Getting record list with file content failed. workflowExecutionId: %s, pluginType: %s",
          workflowExecutionId, pluginType), e);
    }

    // Get the records themselves.
    final List<Record> records = new ArrayList<>(revisionsWithDeletedFlagSetToFalse.size());
    for (CloudTagsResponse cloudTagsResponse : revisionsWithDeletedFlagSetToFalse) {
      final Record record = getRecord(executionAndPlugin.getRight(), cloudTagsResponse.getCloudId());
      if (record == null) {
        throw new IllegalStateException("This can't happen: eCloud just told us the record exists");
      }
      records.add(getRecord(executionAndPlugin.getRight(), cloudTagsResponse.getCloudId()));
    }

    // Compile the result.
    return new PaginatedRecordsResponse(records, null);
  }

  /**
   * Get a list with record contents from the external resource based on an workflow execution and {@link PluginType}.
   *
   * @param metisUserView the user wishing to perform this operation
   * @param workflowExecutionId the execution identifier of the workflow
   * @param pluginType the {@link ExecutablePluginType} that is to be located inside the workflow
   * @param ecloudIds the list of ecloud IDs of the records we wish to obtain
   * @return the list of records from the external resource
   * @throws GenericMetisException can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.exception.ExternalTaskException} if an error occurred while
   * retrieving the records from the external resource</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authorized to perform this task</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no workflow
   * execution exists for the provided identifier</li>
   * </ul>
   */
  public RecordsResponse getListOfFileContentsFromPluginExecution(MetisUserView metisUserView,
      String workflowExecutionId, ExecutablePluginType pluginType, ListOfIds ecloudIds)
      throws GenericMetisException {

    // Get the right workflow execution and plugin type.
    final Pair<WorkflowExecution, ExecutablePlugin> executionAndPlugin = getExecutionAndPlugin(
        metisUserView, workflowExecutionId, pluginType);
    if (executionAndPlugin == null) {
      throw new NoWorkflowExecutionFoundException(String
          .format("No executable plugin of type %s found for workflowExecution with id: %s",
              pluginType.name(), workflowExecutionId));
    }

    // Get the records.
    final List<Record> records = new ArrayList<>(ecloudIds.getIds().size());
    for (String cloudId : ecloudIds.getIds()) {
      Optional.ofNullable(getRecord(executionAndPlugin.getRight(), cloudId)).ifPresent(records::add);
    }

    // Done.
    return new RecordsResponse(records);
  }

  /**
   * Get a list with record contents from the external resource based on a workflow execution and the predecessor
   * of the given {@link PluginType}.
   *
   * @param metisUserView the user wishing to perform this operation
   * @param workflowExecutionId the execution identifier of the workflow
   * @param pluginType the {@link ExecutablePluginType} that is to be located inside the workflow
   * @param ecloudIds the list of ecloud IDs of the records we wish to obtain
   * @return the list of records from the external resource
   * @throws GenericMetisException can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.exception.ExternalTaskException} if an error occurred while retrieving the records from the external
   * resource</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authorized to perform this task</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no workflow
   * execution exists for the provided identifier</li>
   * </ul>
   */
  public RecordsResponse getListOfFileContentsFromPredecessorPluginExecution(MetisUserView metisUserView,
          String workflowExecutionId, ExecutablePluginType pluginType, ListOfIds ecloudIds)
          throws GenericMetisException {

    // Get the right workflow execution and plugin type.
    final Pair<WorkflowExecution, ExecutablePlugin> executionAndPlugin = getExecutionAndPlugin(
            metisUserView, workflowExecutionId, pluginType);
    if (executionAndPlugin == null) {
      throw new NoWorkflowExecutionFoundException(String
              .format("No executable plugin of type %s found for workflowExecution with id: %s",
                      pluginType.name(), workflowExecutionId));
    }

    Pair<MetisPlugin, WorkflowExecution> predecessorPlugin =
            dataEvolutionUtils.getPreviousExecutionAndPlugin(executionAndPlugin.getRight(), executionAndPlugin.getLeft().getDatasetId());
    if(predecessorPlugin == null){
      throw new NoWorkflowExecutionFoundException(String
              .format("No predecessor for executable plugin of type %s found for workflowExecution with id: %s",
                      pluginType.name(), workflowExecutionId));
    }

    ExecutablePlugin predecessorExecutablePlugin = (ExecutablePlugin) predecessorPlugin.getLeft();

    // Get the records.
    final List<Record> records = new ArrayList<>(ecloudIds.getIds().size());
    for (String cloudId : ecloudIds.getIds()) {
      Optional.ofNullable(getRecord(predecessorExecutablePlugin, cloudId)).ifPresent(records::add);
    }

    // Done.
    return new RecordsResponse(records);
  }

  /**
   * Get a record from the external resource based on o searchId, workflow execution and {@link PluginType}.
   *
   * @param metisUserView the user wishing to perform this operation
   * @param workflowExecutionId the execution identifier of the workflow
   * @param pluginType the {@link ExecutablePluginType} that is to be located inside the workflow
   * @param idToSearch the ID we are searching for and for which we want to find a record
   * @return the record from the external resource
   * @throws GenericMetisException can be one of:
   * <ul>
   * <li>{@link eu.europeana.metis.exception.ExternalTaskException} if an error occurred while
   * retrieving the records from the external resource</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not
   * authorized to perform this task</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no workflow
   * execution exists for the provided identifier</li>
   * </ul>
   */
  public Record searchRecordByIdFromPluginExecution(MetisUserView metisUserView,
      String workflowExecutionId, ExecutablePluginType pluginType, String idToSearch)
      throws GenericMetisException {

    // Get the right workflow execution and plugin type.
    final Pair<WorkflowExecution, ExecutablePlugin> executionAndPlugin = getExecutionAndPlugin(
        metisUserView, workflowExecutionId, pluginType);
    if (executionAndPlugin == null) {
      throw new NoWorkflowExecutionFoundException(String
          .format("No executable plugin of type %s found for workflowExecution with id: %s",
              pluginType.name(), workflowExecutionId));
    }

    // Check whether the searched ID is known as a Europeana ID. Otherwise, assume it is an ecloudId.
    final String datasetId = executionAndPlugin.getLeft().getDatasetId();
    String ecloudId = null;
    try {
      final String normalizedRecordId = RecordIdUtils.checkAndNormalizeRecordId(datasetId, idToSearch)
          .map(id -> RecordIdUtils.composeFullRecordId(datasetId, id)).orElse(null);
      if (normalizedRecordId != null) {
        ecloudId = uisClient.getCloudId(ecloudProvider, normalizedRecordId).getId();
      }
    } catch (BadContentException e) {
      // Normalization failed.
      ecloudId = idToSearch;
    } catch (CloudException e) {
      if (e.getCause() instanceof RecordDoesNotExistException) {
        // The record ID does not exist.
        ecloudId = idToSearch;
      } else {
        // Some other connectivity issue.
        throw new ExternalTaskException(
            String.format("Failed to lookup cloudId for idToSearch: %s", idToSearch), e);
      }
    }

    // Try to retrieve the record.
    return ecloudId == null ? null : getRecord(executionAndPlugin.getRight(), ecloudId);
  }

  Pair<WorkflowExecution, ExecutablePlugin> getExecutionAndPlugin(MetisUserView metisUserView,
      String workflowExecutionId, ExecutablePluginType pluginType) throws GenericMetisException {

    // Get the workflow execution - check that the user has rights to access this.
    final WorkflowExecution workflowExecution = workflowExecutionDao.getById(workflowExecutionId);
    if (workflowExecution == null) {
      throw new NoWorkflowExecutionFoundException(
          String.format("No workflow execution found for workflowExecutionId: %s, in METIS",
              workflowExecutionId));
    }
    authorizer.authorizeReadExistingDatasetById(metisUserView, workflowExecution.getDatasetId());

    // Get the plugin for which to get the records and return.
    final MetisPlugin plugin = workflowExecution
        .getMetisPluginWithType(pluginType.toPluginType()).orElse(null);
    if (plugin instanceof ExecutablePlugin) {
      return new ImmutablePair<>(workflowExecution, (ExecutablePlugin) plugin);
    }
    return null;
  }

  Record getRecord(ExecutablePlugin plugin, String ecloudId) throws ExternalTaskException {

    // Get the representation(s) for the given combination of plugin and record ID.
    final List<Representation> representations;
    try {
      final Revision revision = new Revision(plugin.getPluginType().name(), ecloudProvider, plugin.getStartedDate());
      representations = recordServiceClient.getRepresentationsByRevision(ecloudId, MetisPlugin.getRepresentationName(), revision);
    } catch (MCSException e) {
      throw new ExternalTaskException(String.format(
          "Getting record list with file content failed. externalTaskId: %s, pluginType: %s, ecloudId: %s",
          plugin.getExternalTaskId(), plugin.getPluginType(), ecloudId), e);
    }

    // If no representation is found, return null.
    if (representations == null || representations.isEmpty()) {
      return null;
    }
    final Representation representation = representations.get(0);

    // Perform checks on the file lists.
    if (representation.getFiles() == null || representation.getFiles().isEmpty()) {
      throw new ExternalTaskException(String.format(
          "Expecting one file in the representation, but received none. externalTaskId: %s, pluginType: %s, ecloudId: %s",
          plugin.getExternalTaskId(), plugin.getPluginType(), ecloudId));
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
