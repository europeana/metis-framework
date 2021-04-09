package eu.europeana.metis.core.dao;

import eu.europeana.metis.core.workflow.plugins.MetisPlugin;
import eu.europeana.metis.core.workflow.plugins.MetisPluginMetadata;
import eu.europeana.metis.core.workflow.plugins.PluginType;
import java.util.Date;
import java.util.Objects;

/**
 * Instances of this class uniquely define a plugin that has been completed. It can be used to test
 * equality of executed plugins.
 */
public class ExecutedMetisPluginId {

  private final Date pluginStartedDate;
  private final PluginType pluginType;

  ExecutedMetisPluginId(Date pluginStartedDate, PluginType pluginType) {
    this.pluginStartedDate = pluginStartedDate != null ? new Date(pluginStartedDate.getTime()) : null;
    this.pluginType = pluginType;
    if (this.pluginStartedDate == null || this.pluginType == null) {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Creates the ID of this plugin.
   *
   * @param plugin The pluign for which to create the ID.
   * @return The ID of this plugin, or null if this plugin has not been started yet.
   */
  public static ExecutedMetisPluginId forPlugin(MetisPlugin plugin) {
    final Date startedDate = plugin.getStartedDate();
    if (startedDate == null) {
      return null;
    }
    return new ExecutedMetisPluginId(startedDate, plugin.getPluginType());
  }

  /**
   * Extracts the ID of the predecessor plugin of this plugin.
   *
   * @param plugin The plugin for which to extract the predecessor ID.
   * @return The ID of the predecessor, or null if no predecessor defined.
   */
  public static ExecutedMetisPluginId forPredecessor(MetisPlugin plugin) {
    return forPredecessor(plugin.getPluginMetadata());
  }

  /**
   * Extracts the ID of the predecessor plugin of this plugin.
   *
   * @param metadata The metadata of the plugin for which to extract the predecessor ID.
   * @return The ID of the predecessor, or null if no predecessor defined.
   */
  public static ExecutedMetisPluginId forPredecessor(MetisPluginMetadata metadata) {
    final Date previousPluginTimestamp = metadata.getRevisionTimestampPreviousPlugin();
    final PluginType previousPluginType = PluginType.getPluginTypeFromEnumName(
            metadata.getRevisionNamePreviousPlugin());
    if (previousPluginTimestamp == null || previousPluginType == null) {
      return null;
    }
    return new ExecutedMetisPluginId(previousPluginTimestamp, previousPluginType);
  }

  public Date getPluginStartedDate() {
    return pluginStartedDate != null ? new Date(pluginStartedDate.getTime()) : null;
  }

  public PluginType getPluginType() {
    return pluginType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ExecutedMetisPluginId that = (ExecutedMetisPluginId) o;
    return Objects.equals(getPluginStartedDate(), that.getPluginStartedDate()) &&
            getPluginType() == that.getPluginType();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getPluginStartedDate(), getPluginType());
  }
}
