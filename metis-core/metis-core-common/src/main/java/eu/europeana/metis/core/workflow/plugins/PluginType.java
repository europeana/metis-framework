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
  ENRICHMENT,
  VALIDATION_EXTERNAL,
  TRANSFORMATION,
  VALIDATION_INTERNAL,
  PREVIEW,
  PUBLISH;

  /**
   * Lookup of a {@link PluginType} enum from a provided enum String representation of the enum value.
   * @param enumName the String representation of an enum value
   * @return the {@link PluginType} that represents the provided value or null if not found
   */
  @JsonCreator
  public static PluginType getPluginTypeFromEnumName(@JsonProperty("pluginName")String enumName){
    for (PluginType pluginType:PluginType.values()) {
      if(pluginType.name().equalsIgnoreCase(enumName)){
        return pluginType;
      }
    }
    return null;
  }
}
