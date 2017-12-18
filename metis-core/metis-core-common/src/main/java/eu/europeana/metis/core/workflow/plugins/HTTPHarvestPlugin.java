package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonFormat;
import eu.europeana.cloud.client.dps.rest.DpsClient;
import java.util.Date;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Indexed;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
@Embedded
public class HTTPHarvestPlugin implements AbstractMetisPlugin {

  @Indexed
  private String id;
  private PluginStatus pluginStatus = PluginStatus.INQUEUE;
  private static final PluginType pluginType = PluginType.HTTP_HARVEST;

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

  public HTTPHarvestPlugin() {
  }

  public HTTPHarvestPlugin(
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
  public PluginType getPluginType() {
    return pluginType;
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
  public long getExternalTaskId() {
    return 0;
  }

  @Override
  public void setExternalTaskId(long externalTaskId) {

  }

  public Date getStartedDate() {
    return startedDate;
  }

  public void setStartedDate(Date startedDate) {
    this.startedDate = startedDate;
  }

  public Date getFinishedDate() {
    return finishedDate;
  }

  public void setFinishedDate(Date finishedDate) {
    this.finishedDate = finishedDate;
  }

  public Date getUpdatedDate() {
    return updatedDate;
  }

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
  public void execute(DpsClient dpsClient, String ecloudBaseUrl, String ecloudProvider,
      String ecloudDataset) {
    // TODO: 24-11-17 Execution of http harvest topology
  }

  @Override
  public ExecutionRecordsStatistics monitor(String externalTaskId) {
    return null;
  }
}
