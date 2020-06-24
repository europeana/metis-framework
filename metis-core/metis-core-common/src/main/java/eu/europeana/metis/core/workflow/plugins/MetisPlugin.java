package eu.europeana.metis.core.workflow.plugins;

import java.util.Date;
import java.util.Optional;

/**
 * This interface represents a plugin. It contains the minimum a plugin should support so that it
 * can be plugged in the Metis workflow registry and can be accessible via the REST API of Metis.
 *
 * @param <M> The type of the plugin metadata that this plugin represents.
 */
public interface MetisPlugin<M extends MetisPluginMetadata> {

  String REPRESENTATION_NAME = "metadataRecord";

  static String getRepresentationName() {
    return REPRESENTATION_NAME;
  }

  String getId();

  void setId(String id);

  /**
   * @return {@link PluginType}
   */
  PluginType getPluginType();

  /**
   * The metadata corresponding to this plugin.
   *
   * @return {@link AbstractMetisPluginMetadata}
   */
  M getPluginMetadata();

  /**
   * @param pluginMetadata {@link AbstractMetisPluginMetadata} to add for the plugin
   */
  void setPluginMetadata(M pluginMetadata);

  /**
   * @return started {@link Date} of the execution of the plugin
   */
  Date getStartedDate();

  /**
   * @param startedDate {@link Date}
   */
  void setStartedDate(Date startedDate);

  /**
   * @return updated {@link Date} of the execution of the plugin
   */
  Date getUpdatedDate();

  /**
   * @param updatedDate {@link Date}
   */
  void setUpdatedDate(Date updatedDate);

  /**
   * @return finished {@link Date} of the execution of the plugin
   */
  Date getFinishedDate();

  /**
   * @param finishedDate {@link Date}
   */
  void setFinishedDate(Date finishedDate);

  /**
   * @return status {@link PluginStatus} of the execution of the plugin
   */
  PluginStatus getPluginStatus();

  /**
   * @param pluginStatus {@link PluginStatus}
   */
  void setPluginStatus(PluginStatus pluginStatus);

  /**
   * This method sets the plugin status and also clears the fail message.
   *
   * @param pluginStatus {@link PluginStatus}
   */
  void setPluginStatusAndResetFailMessage(PluginStatus pluginStatus);

  String getFailMessage();

  void setFailMessage(String failMessage);

  /**
   * @return The data status of this plugin. If null, this should be interpreted as being equal to
   * {@link DataStatus#VALID} (due to backwards-compatibility).
   */
  DataStatus getDataStatus();

  /**
   * Returns the data state for the plugin taking into account the default value.
   *
   * @param plugin The plugin.
   * @return The data status of the given plugin. Is not null.
   */
  static DataStatus getDataStatus(ExecutablePlugin plugin) {
    return Optional.ofNullable(plugin.getDataStatus()).orElse(DataStatus.VALID);
  }

  void setDataStatus(DataStatus dataStatus);

}
