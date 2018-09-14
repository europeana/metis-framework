package eu.europeana.metis.core.service;

import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.common.model.Representation;
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
import eu.europeana.metis.core.rest.Record;
import eu.europeana.metis.core.rest.RecordsResponse;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.exception.ExternalTaskException;
import eu.europeana.metis.exception.GenericMetisException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 * Proxies Service which encapsulates functionality that has to be proxied to an external resource.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-26
 */
public class ProxiesService {

  private final WorkflowExecutionDao workflowExecutionDao;
  private final DataSetServiceClient ecloudDataSetServiceClient;
  private final RecordServiceClient recordServiceClient;
  private final FileServiceClient fileServiceClient;
  private final DpsClient dpsClient;
  private final String ecloudProvider;
  private final Authorizer authorizer;

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
    this.workflowExecutionDao = workflowExecutionDao;
    this.ecloudDataSetServiceClient = ecloudDataSetServiceClient;
    this.recordServiceClient = recordServiceClient;
    this.fileServiceClient = fileServiceClient;
    this.dpsClient = dpsClient;
    this.ecloudProvider = ecloudProvider;
    this.authorizer = authorizer;
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
   * <li>{@link DpsException} if an error occurred while retrieving the logs from the external resource</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not authorized to perform this task</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no workflow execution exists for the provided external task identifier</li>
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
   * <li>{@link DpsException} if an error occurred while retrieving the report from the external resource</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not authorized to perform this task</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no workflow execution exists for the provided external task identifier</li>
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
   * @return the list of errors grouped
   * @throws GenericMetisException can be one of:
   * <ul>
   * <li>{@link DpsException} if an error occurred while retrieving the statistics from the external resource</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not authorized to perform this task</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no workflow execution exists for the provided external task identifier</li>
   * </ul>
   */
  public StatisticsReport getExternalTaskStatistics(MetisUser metisUser, String topologyName,
      long externalTaskId) throws GenericMetisException {
    authorizer.authorizeReadExistingDatasetById(metisUser,
        getDatasetIdFromExternalTaskId(externalTaskId));
    try {
      return dpsClient.getTaskStatisticsReport(topologyName, externalTaskId);
    } catch (DpsException e) {
      throw new ExternalTaskException(String.format(
          "Getting the task statistics failed. topologyName: %s, externalTaskId: %s",
          topologyName, externalTaskId), e);
    }
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
   * {@link PluginType}
   *
   * @param metisUser the user wishing to perform this operation
   * @param workflowExecutionId the execution identifier of the workflow
   * @param pluginType the {@link PluginType} that is to be located inside the workflow
   * @param nextPage the string representation of the next page which is provided from the response
   * and can be used to get the next page of results
   * @param numberOfRecords the number of records per response
   * @return the list of records from the external resource
   * @throws GenericMetisException can be one of:
   * <ul>
   * <li>{@link MCSException} if an error occurred while retrieving the records from the external resource</li>
   * <li>{@link eu.europeana.metis.exception.UserUnauthorizedException} if the user is not authorized to perform this task</li>
   * <li>{@link eu.europeana.metis.core.exceptions.NoWorkflowExecutionFoundException} if no workflow execution exists for the provided identifier</li>
   * </ul>
   */
  public RecordsResponse getListOfFileContentsFromPluginExecution(MetisUser metisUser,
      String workflowExecutionId, PluginType pluginType, String nextPage, int numberOfRecords)
      throws GenericMetisException {
    WorkflowExecution workflowExecution = workflowExecutionDao.getById(workflowExecutionId);
    if (workflowExecution == null) {
      throw new NoWorkflowExecutionFoundException(
          String.format("No workflow execution found for workflowExecutionId: %s, in METIS",
              workflowExecutionId));
    }
    authorizer.authorizeReadExistingDatasetById(metisUser, workflowExecution.getDatasetId());

    AbstractMetisPlugin abstractMetisPluginToGetRecords = null;
    for (AbstractMetisPlugin abstractMetisPlugin : workflowExecution.getMetisPlugins()) {
      if (abstractMetisPlugin.getPluginType() == pluginType) {
        abstractMetisPluginToGetRecords = abstractMetisPlugin;
      }
    }

    String nextPageAfterResponse = null;
    List<Record> records = new ArrayList<>();
    if (abstractMetisPluginToGetRecords != null) {
      String providerId = ecloudProvider;
      String datasetId = workflowExecution.getEcloudDatasetId();
      String representationName = AbstractMetisPlugin.getRepresentationName();
      String revisionName = abstractMetisPluginToGetRecords.getPluginType().name();
      String revisionProviderId = ecloudProvider;
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
      String revisionTimestamp =
          dateFormat.format(abstractMetisPluginToGetRecords.getStartedDate());
      try {
        ResultSlice<CloudTagsResponse> resultSlice = ecloudDataSetServiceClient
            .getDataSetRevisionsChunk(providerId, datasetId, representationName, revisionName,
                revisionProviderId, revisionTimestamp, nextPage, numberOfRecords);
        nextPageAfterResponse = resultSlice.getNextSlice();

        for (CloudTagsResponse cloudTagsResponse : resultSlice.getResults()) {
          Representation representation =
              recordServiceClient.getRepresentationByRevision(cloudTagsResponse.getCloudId(),
                  representationName, revisionName, revisionProviderId, revisionTimestamp);
          InputStream inputStream = fileServiceClient
              .getFile(representation.getFiles().get(0).getContentUri().toString());
          records.add(new Record(cloudTagsResponse.getCloudId(),
              IOUtils.toString(inputStream, StandardCharsets.UTF_8.name())));
        }
      } catch (MCSException e) {
        throw new ExternalTaskException(String.format(
            "Getting record list with file content failed. workflowExecutionId: %s, pluginType: %s",
            workflowExecutionId, pluginType), e);
      } catch (IOException e) {
        throw new ExternalTaskException("Getting while reading the contents of the file.", e);
      }
    }
    return new RecordsResponse(records, nextPageAfterResponse);
  }
}
