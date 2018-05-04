package eu.europeana.metis.core.workflow.plugins;

import java.util.function.Function;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
public enum PluginType {

  HTTP_HARVEST(HTTPHarvestPlugin::new),

  OAIPMH_HARVEST(OaipmhHarvestPlugin::new),

  ENRICHMENT(EnrichmentPlugin::new),

  MEDIA_PROCESS(MediaProcessPlugin::new),

  VALIDATION_EXTERNAL(ValidationExternalPlugin::new),

  TRANSFORMATION(TransformationPlugin::new),

  VALIDATION_INTERNAL(ValidationInternalPlugin::new),

  NORMALIZATION(NormalizationPlugin::new),

  PREVIEW(IndexToPreviewPlugin::new),

  PUBLISH(IndexToPublishPlugin::new);

  private final Function<AbstractMetisPluginMetadata, AbstractMetisPlugin> pluginCreator;

  private PluginType(Function<AbstractMetisPluginMetadata, AbstractMetisPlugin> pluginCreator) {
    this.pluginCreator = pluginCreator;
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
