package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
public enum PluginStatus {
  INQUEUE, RUNNING, FINISHED, CANCELLED, NULL;

  @JsonCreator
  public static PluginStatus getPluginStatusFromEnumName(String name){
    for (PluginStatus pluginStatus:PluginStatus.values()) {
      if(pluginStatus.name().equalsIgnoreCase(name)){
        return pluginStatus;
      }
    }
    return NULL;
  }
}
