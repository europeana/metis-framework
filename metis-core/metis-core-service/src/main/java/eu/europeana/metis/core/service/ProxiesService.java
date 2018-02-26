package eu.europeana.metis.core.service;

import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.common.model.dps.SubTaskInfo;
import eu.europeana.cloud.common.model.dps.TaskErrorsInfo;
import eu.europeana.cloud.common.response.CloudTagsResponse;
import eu.europeana.cloud.common.response.RepresentationRevisionResponse;
import eu.europeana.cloud.common.response.ResultSlice;
import eu.europeana.cloud.mcs.driver.DataSetServiceClient;
import eu.europeana.cloud.mcs.driver.FileServiceClient;
import eu.europeana.cloud.mcs.driver.RecordServiceClient;
import eu.europeana.cloud.service.dps.exception.DpsException;
import eu.europeana.cloud.service.mcs.exception.MCSException;
import eu.europeana.metis.core.dao.WorkflowExecutionDao;
import eu.europeana.metis.core.rest.RecordResponse;
import eu.europeana.metis.core.workflow.WorkflowExecution;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.exception.ExternalTaskException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-26
 */
public class ProxiesService {

  private final WorkflowExecutionDao workflowExecutionDao;
  private final DataSetServiceClient ecloudDataSetServiceClient;
  private final RecordServiceClient recordServiceClient;
  private final FileServiceClient fileServiceClient;
  private final DpsClient dpsClient;
  private String ecloudProvider; //Initialize with setter

  @Autowired
  public ProxiesService(WorkflowExecutionDao workflowExecutionDao,
      DataSetServiceClient ecloudDataSetServiceClient, RecordServiceClient recordServiceClient,
      FileServiceClient fileServiceClient, DpsClient dpsClient) {
    this.workflowExecutionDao = workflowExecutionDao;
    this.ecloudDataSetServiceClient = ecloudDataSetServiceClient;
    this.recordServiceClient = recordServiceClient;
    this.fileServiceClient = fileServiceClient;
    this.dpsClient = dpsClient;
  }

  public List<SubTaskInfo> getExternalTaskLogs(String topologyName, long externalTaskId, int from,
      int to) throws ExternalTaskException {
    List<SubTaskInfo> detailedTaskReportBetweenChunks;
    try {
      detailedTaskReportBetweenChunks = dpsClient
          .getDetailedTaskReportBetweenChunks(topologyName, externalTaskId, from, to);
    } catch (DpsException e) {
      throw new ExternalTaskException(String.format(
          "Getting the task detailed logs failed. topologyName: %s, externalTaskId: %s, from: %s, to: %s",
          topologyName, externalTaskId, from, to), e);
    }
    for (SubTaskInfo subTaskInfo : detailedTaskReportBetweenChunks) { //Hide sensitive information
      subTaskInfo.setAdditionalInformations(null);
    }
    return detailedTaskReportBetweenChunks;
  }

  public TaskErrorsInfo getExternalTaskReport(String topologyName, long externalTaskId,
      int idsPerError) throws ExternalTaskException {
    TaskErrorsInfo taskErrorsInfo;
    try {
      taskErrorsInfo = dpsClient
          .getTaskErrorsReport(topologyName, externalTaskId, null, idsPerError);
    } catch (DpsException e) {
      throw new ExternalTaskException(String.format(
          "Getting the task error report failed. topologyName: %s, externalTaskId: %s, idsPerError: %s",
          topologyName, externalTaskId, idsPerError), e);
    }
    return taskErrorsInfo;
  }

  public List<RecordResponse> getListOfFileContentsFromPluginExecution(String workflowExecutionId,
      PluginType pluginType)
      throws ExternalTaskException {
    WorkflowExecution workflowExecution = workflowExecutionDao.getById(workflowExecutionId);
    AbstractMetisPlugin abstractMetisPluginToGetRecords = null;
    for (AbstractMetisPlugin abstractMetisPlugin : workflowExecution.getMetisPlugins()) {
      if (abstractMetisPlugin.getPluginType() == pluginType) {
        abstractMetisPluginToGetRecords = abstractMetisPlugin;
      }
    }

    List<RecordResponse> recordResponse = new ArrayList<>();
    if (abstractMetisPluginToGetRecords != null) {
      String providerId = ecloudProvider;
      String datasetId = workflowExecution.getEcloudDatasetId();
      String representationName = AbstractMetisPlugin.getRepresentationName();
      String revisionName = abstractMetisPluginToGetRecords.getPluginType().name();
      String revisionProviderId = ecloudProvider;
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
      String revisionTimestamp = dateFormat
          .format(abstractMetisPluginToGetRecords.getStartedDate());
      try {
        ResultSlice<CloudTagsResponse> resultSlice = ecloudDataSetServiceClient
            .getDataSetRevisionsChunk(providerId, datasetId, representationName, revisionName,
                revisionProviderId, revisionTimestamp, null, 5);

        for (CloudTagsResponse cloudTagsResponse : resultSlice.getResults()) {
          RepresentationRevisionResponse representationRevision = recordServiceClient
              .getRepresentationRevision(cloudTagsResponse.getCloudId(), representationName,
                  revisionName, revisionProviderId, revisionTimestamp);
          try {
            InputStream inputStream = fileServiceClient
                .getFile(representationRevision.getFiles().get(0).getContentUri().toString());
            recordResponse.add(new RecordResponse(cloudTagsResponse.getCloudId(),
                IOUtils.toString(inputStream, StandardCharsets.UTF_8)));
          } catch (IOException e) {
            throw new ExternalTaskException("Getting while reading the contents of the file.", e);
          }
        }
      } catch (MCSException e) {
        throw new ExternalTaskException(String.format(
            "Getting record list with file content failed. workflowExecutionId: %s, pluginType: %s",
            workflowExecutionId, pluginType), e);
      }
    }
    return recordResponse;
  }

  public void setEcloudProvider(String ecloudProvider) {
    this.ecloudProvider = ecloudProvider;
  }
}
