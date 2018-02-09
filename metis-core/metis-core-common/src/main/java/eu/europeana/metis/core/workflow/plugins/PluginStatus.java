package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
public enum PluginStatus {
  INQUEUE, RUNNING, FINISHED, CANCELLED, FAILED;

  /**
   * Lookup of a {@link PluginStatus} enum from a provided enum String representation of the enum value.
   * @param enumName the String representation of an enum value
   * @return the {@link PluginStatus} that represents the provided value or null if not found
   */
  @JsonCreator
  public static PluginStatus getPluginStatusFromEnumName(String enumName){
    for (PluginStatus pluginStatus:PluginStatus.values()) {
      if(pluginStatus.name().equalsIgnoreCase(enumName)){
        return pluginStatus;
      }
    }
    return null;
  }
}
