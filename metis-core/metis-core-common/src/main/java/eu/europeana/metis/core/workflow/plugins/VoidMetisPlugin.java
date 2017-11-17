package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Indexed;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
@Embedded
public class VoidMetisPlugin implements AbstractMetisPlugin {

  @Indexed
  private String id;
  private PluginStatus pluginStatus = PluginStatus.INQUEUE;
  private static final PluginType pluginType = PluginType.VOID;
  private boolean mocked = true;

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

  public VoidMetisPlugin() {
  }

  public VoidMetisPlugin(AbstractMetisPluginMetadata pluginMetadata) {
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
  public boolean isMocked() {
    return mocked;
  }

  @Override
  public void setMocked(boolean mocked) {
    this.mocked = mocked;
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
  public void execute() {
  }

  @Override
  public ExecutionRecordsStatistics monitor(String datasetId) {
    return null;
  }

}
