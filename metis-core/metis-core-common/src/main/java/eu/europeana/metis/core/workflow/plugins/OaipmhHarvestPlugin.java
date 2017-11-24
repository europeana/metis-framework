package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonFormat;
import eu.europeana.cloud.client.dps.rest.DpsClient;
import eu.europeana.cloud.common.model.Revision;
import eu.europeana.cloud.service.dps.DpsTask;
import eu.europeana.cloud.service.dps.InputDataType;
import eu.europeana.cloud.service.dps.OAIPMHHarvestingDetails;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Indexed;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
@Embedded
public class OaipmhHarvestPlugin implements AbstractMetisPlugin {
  @Indexed
  private String id;
  private PluginStatus pluginStatus = PluginStatus.INQUEUE;
  private static final PluginType pluginType = PluginType.OAIPMH_HARVEST;

  @Indexed
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date startedDate;
  @Indexed
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date updatedDate;
  @Indexed
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date finishedDate;
  private ExecutionRecordsStatistics executionRecordsStatistics = new ExecutionRecordsStatistics();

  private AbstractMetisPluginMetadata pluginMetadata;

  public OaipmhHarvestPlugin() {
  }

  public OaipmhHarvestPlugin(
      AbstractMetisPluginMetadata pluginMetadata) {
    this.pluginMetadata = pluginMetadata;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public AbstractMetisPluginMetadata getPluginMetadata() {
    return pluginMetadata;
  }

  @Override
  public void setPluginMetadata(
      AbstractMetisPluginMetadata pluginMetadata) {
    this.pluginMetadata = pluginMetadata;
  }

  @Override
  public PluginStatus getPluginStatus() {
    return pluginStatus;
  }

  @Override
  public void setPluginStatus(PluginStatus pluginStatus) {
    this.pluginStatus = pluginStatus;
  }

  @Override
  public PluginType getPluginType() {
    return pluginType;
  }

  @Override
  public Date getStartedDate() {
    return startedDate;
  }

  @Override
  public void setStartedDate(Date startedDate) {
    this.startedDate = startedDate;
  }

  @Override
  public Date getFinishedDate() {
    return finishedDate;
  }

  @Override
  public void setFinishedDate(Date finishedDate) {
    this.finishedDate = finishedDate;
  }

  @Override
  public Date getUpdatedDate() {
    return updatedDate;
  }

  @Override
  public void setUpdatedDate(Date updatedDate) {
    this.updatedDate = updatedDate;
  }

  @Override
  public ExecutionRecordsStatistics getExecutionRecordsStatistics() {
    return executionRecordsStatistics;
  }

  @Override
  public void setExecutionRecordsStatistics(
      ExecutionRecordsStatistics executionRecordsStatistics) {
    this.executionRecordsStatistics = executionRecordsStatistics;
  }

  @Override
  public void execute(DpsClient dpsClient, String ecloudBaseUrl, String ecloudProvider, String ecloudDataset) {
    if (!pluginMetadata.isMocked()){
      String oaipmhUrl = ((OaipmhHarvestPluginMetadata) pluginMetadata).getUrl();
      String setSpec = ((OaipmhHarvestPluginMetadata) pluginMetadata).getSetSpec();
      String metadataFormat = ((OaipmhHarvestPluginMetadata) pluginMetadata).getMetadataFormat();
      DpsTask dpsTask = new DpsTask();

      Map<InputDataType, List<String>> inputDataTypeListHashMap = new EnumMap<>(InputDataType.class);
      inputDataTypeListHashMap.put(InputDataType.REPOSITORY_URLS,
          Collections.singletonList(oaipmhUrl));
      dpsTask.setInputData(inputDataTypeListHashMap);

      Map<String, String> parameters = new HashMap<>();
      parameters.put("PROVIDER_ID", ecloudProvider);
      parameters.put("OUTPUT_DATA_SETS", String.format("%s/data-providers/%s/data-sets/%s",
          ecloudBaseUrl, ecloudProvider, ecloudDataset));
      //assign parameters to the task
      dpsTask.setParameters(parameters);

      OAIPMHHarvestingDetails oaipmhHarvestingDetails = new OAIPMHHarvestingDetails();
      oaipmhHarvestingDetails.setSchemas(new HashSet<>(Collections.singletonList(metadataFormat)));
      oaipmhHarvestingDetails.setSets(new HashSet<>(Collections.singletonList(setSpec)));
      dpsTask.setHarvestingDetails(oaipmhHarvestingDetails);

      Revision revision = new Revision();
      revision.setRevisionName(pluginType.name());
      revision.setRevisionProviderId(ecloudProvider);
      revision.setCreationTimeStamp(startedDate);
      dpsTask.setOutputRevision(revision);

      dpsTask.setTaskName(String.format("%s-%s", pluginType.name(), id));

      dpsClient.submitTask(dpsTask, "oai_topology");
    }
  }

  @Override
  public ExecutionRecordsStatistics monitor(String dataseId) {
    // TODO: 16-11-17 Get execution statistics from ecloud using dps id returned from execute
    return null;
  }
}
