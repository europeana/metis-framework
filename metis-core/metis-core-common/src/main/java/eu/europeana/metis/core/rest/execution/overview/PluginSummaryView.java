package eu.europeana.metis.core.rest.execution.overview;

import eu.europeana.metis.core.workflow.plugins.AbstractMetisPlugin;
import eu.europeana.metis.core.workflow.plugins.PluginStatus;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import java.util.Date;
import java.util.Optional;

/**
 * This class represents the vital information on a plugin execution needed for the execution
 * overview.
 */
public class PluginSummaryView {

  private PluginType pluginType;
  private PluginStatus pluginStatus;
  private String failMessage;
  private Date startedDate;
  private Date updatedDate;
  private Date finishedDate;
  private PluginProgressView progress;

  PluginSummaryView() {
  }

  PluginSummaryView(AbstractMetisPlugin plugin) {
    this.pluginType = plugin.getPluginType();
    this.pluginStatus = plugin.getPluginStatus();
    this.failMessage = plugin.getFailMessage();
    this.startedDate = plugin.getStartedDate();
    this.updatedDate = plugin.getUpdatedDate();
    this.finishedDate = plugin.getFinishedDate();
    this.progress = new PluginProgressView(plugin.getExecutionProgress());
  }

  public PluginType getPluginType() {
    return pluginType;
  }

  public PluginStatus getPluginStatus() {
    return pluginStatus;
  }

  public String getFailMessage() {
    return failMessage;
  }

  public Date getStartedDate() {
    return Optional.ofNullable(startedDate).map(Date::getTime).map(Date::new).orElse(null);
  }

  public Date getUpdatedDate() {
    return Optional.ofNullable(updatedDate).map(Date::getTime).map(Date::new).orElse(null);
  }

  public Date getFinishedDate() {
    return Optional.ofNullable(finishedDate).map(Date::getTime).map(Date::new).orElse(null);
  }

  public PluginProgressView getProgress() {
    return progress;
  }
}
