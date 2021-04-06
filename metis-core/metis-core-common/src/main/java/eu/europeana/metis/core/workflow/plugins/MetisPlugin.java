package eu.europeana.metis.core.workflow.plugins;

import java.util.Date;
import java.util.Optional;

/**
 * This interface represents a plugin. It contains the minimum a plugin should support so that it
 * can be plugged in the Metis workflow registry and can be accessible via the REST API of Metis.
 */
public interface MetisPlugin {

  String REPRESENTATION_NAME = "metadataRecord";

  static String getRepresentationName() {
    return REPRESENTATION_NAME;
  }

  String getId();

  /**
   * @return {@link PluginType}
   */
  PluginType getPluginType();

  /**
   * The metadata corresponding to this plugin.
   *
   * @return {@link MetisPluginMetadata}
   */
  MetisPluginMetadata getPluginMetadata();

  /**
   * @return started {@link Date} of the execution of the plugin
   */
  Date getStartedDate();

  /**
   * @return updated {@link Date} of the execution of the plugin
   */
  Date getUpdatedDate();

  /**
   * @return finished {@link Date} of the execution of the plugin
   */
  Date getFinishedDate();

  /**
   * @return status {@link PluginStatus} of the execution of the plugin
   */
  PluginStatus getPluginStatus();

  String getFailMessage();

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
}
