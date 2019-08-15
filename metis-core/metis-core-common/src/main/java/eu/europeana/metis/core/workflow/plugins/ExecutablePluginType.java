package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This denotes a plugin type that is executable (i.e. can be run by Metis). This is a subset of the
 * list in {@link PluginType}, which contains all plugin types.
 */
public enum ExecutablePluginType {

  HTTP_HARVEST(false, PluginType.HTTP_HARVEST),

  OAIPMH_HARVEST(false, PluginType.OAIPMH_HARVEST),

  ENRICHMENT(false, PluginType.ENRICHMENT),

  MEDIA_PROCESS(false, PluginType.MEDIA_PROCESS),

  LINK_CHECKING(true, PluginType.LINK_CHECKING),

  VALIDATION_EXTERNAL(false, PluginType.VALIDATION_EXTERNAL),

  TRANSFORMATION(false, PluginType.TRANSFORMATION),

  VALIDATION_INTERNAL(false, PluginType.VALIDATION_INTERNAL),

  NORMALIZATION(false, PluginType.NORMALIZATION),

  PREVIEW(false, PluginType.PREVIEW),

  PUBLISH(false, PluginType.PUBLISH);

  private final boolean revisionLess;
  private final PluginType pluginType;

  ExecutablePluginType(boolean revisionLess, PluginType pluginType) {
    this.revisionLess = revisionLess;
    this.pluginType = pluginType;
  }

  /**
   * @return the corresponding instance of {@link PluginType}.
   */
  public PluginType toPluginType() {
    return pluginType;
  }

  /**
   * Describes if a ExecutablePluginType has executions that contain revision information.
   *
   * @return true if there are not revision related with the particular ExecutablePluginType
   */
  public boolean isRevisionLess() {
    // TODO JOCHEN no longer needed?
    return revisionLess;
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
