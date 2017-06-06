package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
public enum PluginType{
  HTTP_HARVEST,
  OAIPMH_HARVEST,
  DEREFERENCE,
  VOID,
  QA,
  NULL;

  @JsonCreator
  public static PluginType getPluginTypeFromEnumName(@JsonProperty("pluginName")String pluginName){
    for (PluginType pluginType:PluginType.values()) {
      if(pluginType.name().equalsIgnoreCase(pluginName)){
        return pluginType;
      }
    }
    return NULL;
  }
}
