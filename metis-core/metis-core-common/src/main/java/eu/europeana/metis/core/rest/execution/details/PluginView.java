package eu.europeana.metis.core.rest.execution.details;

import com.fasterxml.jackson.annotation.JsonFormat;
import eu.europeana.metis.core.workflow.plugins.AbstractExecutablePlugin;
import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.DataStatus;
import eu.europeana.metis.core.workflow.plugins.MetisPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import eu.europeana.metis.utils.CommonStringValues;
import java.util.Date;

/**
 * This class represents the complete information on a plugin execution needed for the execution history.
 */
public class PluginView {

  private final PluginType pluginType;
  private final String id;
  private final PluginStatus pluginStatus;
  private final DataStatus dataStatus;
  private final String failMessage;
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private final Date startedDate;
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private final Date updatedDate;
  @JsonFormat(pattern = CommonStringValues.DATE_FORMAT)
  private final Date finishedDate;
  private final String externalTaskId;
  private final PluginProgressView executionProgress;
  private final String topologyName;
  private final boolean canDisplayRawXml;
  private final MetisPluginMetadata pluginMetadata;

  PluginView(AbstractMetisPlugin plugin, boolean canDisplayRawXml) {
    this.pluginType = plugin.getPluginType();
    this.id = plugin.getId();
    this.pluginStatus = plugin.getPluginStatus();
    this.dataStatus = plugin.getDataStatus();
    this.failMessage = plugin.getFailMessage();
    this.startedDate = plugin.getStartedDate();
    this.finishedDate = plugin.getFinishedDate();
    this.canDisplayRawXml = canDisplayRawXml;
    if (plugin instanceof AbstractExecutablePlugin) {
      this.updatedDate = ((AbstractExecutablePlugin<?>) plugin).getUpdatedDate();
      this.externalTaskId = ((AbstractExecutablePlugin<?>) plugin).getExternalTaskId();
      this.executionProgress = new PluginProgressView(((AbstractExecutablePlugin<?>) plugin).getExecutionProgress());
      this.topologyName = ((AbstractExecutablePlugin<?>) plugin).getTopologyName();
      this.pluginMetadata = ((AbstractExecutablePlugin<?>) plugin).getPluginMetadata();
    } else {
      this.updatedDate = null;
      this.externalTaskId = null;
      this.executionProgress = null;
      this.topologyName = null;
      this.pluginMetadata = null;
    }
  }

  public PluginType getPluginType() {
    return pluginType;
  }

  public String getId() {
    return id;
  }

  public PluginStatus getPluginStatus() {
    return pluginStatus;
  }

  public DataStatus getDataStatus() {
    return dataStatus;
  }

  public String getFailMessage() {
    return failMessage;
  }

  public Date getStartedDate() {
    return startedDate != null ? new Date(startedDate.getTime()) : null;
  }

  public Date getUpdatedDate() {
    return updatedDate != null ? new Date(updatedDate.getTime()) : null;
  }

  public Date getFinishedDate() {
    return finishedDate != null ? new Date(finishedDate.getTime()) : null;
  }

  public String getExternalTaskId() {
    return externalTaskId;
  }

  public PluginProgressView getExecutionProgress() {
    return executionProgress;
  }

  public String getTopologyName() {
    return topologyName;
  }

  public boolean isCanDisplayRawXml() {
    return canDisplayRawXml;
  }

  public MetisPluginMetadata getPluginMetadata() {
    return pluginMetadata;
  }
}
