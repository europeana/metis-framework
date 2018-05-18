package eu.europeana.metis.core.workflow.plugins;

import java.util.function.Function;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
public enum PluginType {

  HTTP_HARVEST(HTTPHarvestPlugin::new, false),

  OAIPMH_HARVEST(OaipmhHarvestPlugin::new, false),

  ENRICHMENT(EnrichmentPlugin::new, false),

  MEDIA_PROCESS(MediaProcessPlugin::new, false),

  LINK_CHECKING(LinkCheckingPlugin::new, true),

  VALIDATION_EXTERNAL(ValidationExternalPlugin::new, false),

  TRANSFORMATION(TransformationPlugin::new, false),

  VALIDATION_INTERNAL(ValidationInternalPlugin::new, false),

  NORMALIZATION(NormalizationPlugin::new, false),

  PREVIEW(IndexToPreviewPlugin::new, false),

  PUBLISH(IndexToPublishPlugin::new, false);

  private final Function<AbstractMetisPluginMetadata, AbstractMetisPlugin> pluginCreator;
  private final boolean revisionLess;

  private PluginType(Function<AbstractMetisPluginMetadata, AbstractMetisPlugin> pluginCreator,
      boolean revisionLess) {
    this.pluginCreator = pluginCreator;
    this.revisionLess = revisionLess;
  }

  /**
   * This method creates a new plugin of this type.
   *
   * @param metaData The metadata for this plugin type.
   * @return A new pluing instance.
   */
  public AbstractMetisPlugin getNewPlugin(AbstractMetisPluginMetadata metaData) {
    return pluginCreator.apply(metaData);
  }

  /**
   * Describes if a PluginType has executions that contain revision information.
   * @return true if there are not revision related with the particular PluginType
   */
  public boolean isRevisionLess() {
    return revisionLess;
  }

  /**
   * Lookup of a {@link PluginType} enum from a provided enum String representation of the enum
   * value.
   *
   * @param enumName the String representation of an enum value
   * @return the {@link PluginType} that represents the provided value or null if not found
   */
  @JsonCreator
  public static PluginType getPluginTypeFromEnumName(@JsonProperty("pluginName") String enumName) {
    for (PluginType pluginType : PluginType.values()) {
      if (pluginType.name().equalsIgnoreCase(enumName)) {
        return pluginType;
      }
    }
    return null;
  }
}
