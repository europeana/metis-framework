package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonFormat;
import eu.europeana.cloud.client.dps.rest.DpsClient;
import java.util.Date;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Indexed;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-29
 */
@Embedded
public class TransformationPlugin implements AbstractMetisPlugin {

  @Indexed
  private String id;
  private PluginStatus pluginStatus = PluginStatus.INQUEUE;
  private static final PluginType pluginType = PluginType.VALIDATION_EXTERNAL;

  @Indexed
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date startedDate;
  @Indexed
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date updatedDate;
  @Indexed
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date finishedDate;
  private String externalTaskId;
  private ExecutionProgress executionProgress = new ExecutionProgress();

  private AbstractMetisPluginMetadata pluginMetadata;

  public TransformationPlugin() {
    //Required for json serialization
  }

  public TransformationPlugin(AbstractMetisPluginMetadata pluginMetadata) {
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
  public String getExternalTaskId() {
    return this.externalTaskId;
  }

  @Override
  public void setExternalTaskId(String externalTaskId) {
    this.externalTaskId = externalTaskId;
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
  public ExecutionProgress getExecutionProgress() {
    return executionProgress;
  }

  @Override
  public void setExecutionProgress(
      ExecutionProgress executionProgress) {
    this.executionProgress = executionProgress;
  }

  @Override
  public void execute(DpsClient dpsClient, String ecloudBaseUrl, String ecloudProvider,
      String ecloudDataset) {
    //This is an empty example
  }

  @Override
  public ExecutionProgress monitor(DpsClient dpsClient) {
    return null;
  }

}