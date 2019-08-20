package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This denotes a plugin type that is executable (i.e. can be run by Metis). This is a subset of the
 * list in {@link PluginType}, which contains all plugin types.
 */
public enum ExecutablePluginType {

  HTTP_HARVEST(PluginType.HTTP_HARVEST),

  OAIPMH_HARVEST(PluginType.OAIPMH_HARVEST),

  ENRICHMENT(PluginType.ENRICHMENT),

  MEDIA_PROCESS(PluginType.MEDIA_PROCESS),

  LINK_CHECKING(PluginType.LINK_CHECKING),

  VALIDATION_EXTERNAL(PluginType.VALIDATION_EXTERNAL),

  TRANSFORMATION(PluginType.TRANSFORMATION),

  VALIDATION_INTERNAL(PluginType.VALIDATION_INTERNAL),

  NORMALIZATION(PluginType.NORMALIZATION),

  PREVIEW(PluginType.PREVIEW),

  PUBLISH(PluginType.PUBLISH);

  private final PluginType pluginType;

  ExecutablePluginType(PluginType pluginType) {
    this.pluginType = pluginType;
  }

  /**
   * @return the corresponding instance of {@link PluginType}.
   */
  public PluginType toPluginType() {
    return pluginType;
  }

  /**
   * Lookup of a {@link ExecutablePluginType} enum from a provided enum String representation of the
   * enum value.
   *
   * @param enumName the String representation of an enum value
   * @return the {@link ExecutablePluginType} that represents the provided value or null if not
   * found
   */
  @JsonCreator
  public static ExecutablePluginType getPluginTypeFromEnumName(
      @JsonProperty("pluginName") String enumName) {
    for (ExecutablePluginType pluginType : values()) {
      if (pluginType.name().equalsIgnoreCase(enumName)) {
        return pluginType;
      }
    }
    return null;
  }
}
