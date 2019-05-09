package eu.europeana.metis.core.workflow.plugins;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.function.Function;

/**
 * This denotes a plugin type that is executable (i.e. can be run by Metis). This is a subset of the
 * list in {@link PluginType}, which contains all plugin types.
 */
public enum ExecutablePluginType {

  HTTP_HARVEST(new PluginCreator<>(HTTPHarvestPluginMetadata.class, HTTPHarvestPlugin::new), false,
      PluginType.HTTP_HARVEST),

  OAIPMH_HARVEST(new PluginCreator<>(OaipmhHarvestPluginMetadata.class, OaipmhHarvestPlugin::new),
      false, PluginType.OAIPMH_HARVEST),

  ENRICHMENT(new PluginCreator<>(EnrichmentPluginMetadata.class, EnrichmentPlugin::new), false,
      PluginType.ENRICHMENT),

  MEDIA_PROCESS(new PluginCreator<>(MediaProcessPluginMetadata.class, MediaProcessPlugin::new),
      false, PluginType.MEDIA_PROCESS),

  LINK_CHECKING(new PluginCreator<>(LinkCheckingPluginMetadata.class, LinkCheckingPlugin::new),
      true, PluginType.LINK_CHECKING),

  VALIDATION_EXTERNAL(
      new PluginCreator<>(ValidationExternalPluginMetadata.class, ValidationExternalPlugin::new),
      false, PluginType.VALIDATION_EXTERNAL),

  TRANSFORMATION(
      new PluginCreator<>(TransformationPluginMetadata.class, TransformationPlugin::new), false,
      PluginType.TRANSFORMATION),

  VALIDATION_INTERNAL(
      new PluginCreator<>(ValidationInternalPluginMetadata.class, ValidationInternalPlugin::new),
      false, PluginType.VALIDATION_INTERNAL),

  NORMALIZATION(new PluginCreator<>(NormalizationPluginMetadata.class, NormalizationPlugin::new),
      false, PluginType.NORMALIZATION),

  PREVIEW(new PluginCreator<>(IndexToPreviewPluginMetadata.class, IndexToPreviewPlugin::new),
      false, PluginType.PREVIEW),

  PUBLISH(new PluginCreator<>(IndexToPublishPluginMetadata.class, IndexToPublishPlugin::new),
      false, PluginType.PUBLISH);

  private final PluginCreator<?> pluginCreator;
  private final boolean revisionLess;
  private final PluginType pluginType;

  ExecutablePluginType(PluginCreator<?> pluginCreator, boolean revisionLess,
      PluginType pluginType) {
    this.pluginCreator = pluginCreator;
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
   * This method creates a new plugin of this type. This method throws a {@link ClassCastException}
   * if the provided metaData is not of the correct type associated with this plugin.
   *
   * @param metaData The metadata for this plugin type.
   * @return A new pluing instance.
   */
  public AbstractExecutablePlugin getNewPlugin(AbstractMetisPluginMetadata metaData) {
    return pluginCreator.createPlugin(metaData);
  }

  /**
   * Describes if a ExecutablePluginType has executions that contain revision information.
   *
   * @return true if there are not revision related with the particular ExecutablePluginType
   */
  public boolean isRevisionLess() {
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

  private static class PluginCreator<M extends AbstractExecutablePluginMetadata> {

    private Class<M> pluginMetadataType;
    private Function<M, AbstractExecutablePlugin<M>> pluginCreatorFunction;

    PluginCreator(Class<M> pluginMetadataType,
        Function<M, AbstractExecutablePlugin<M>> pluginCreatorFunction) {
      this.pluginMetadataType = pluginMetadataType;
      this.pluginCreatorFunction = pluginCreatorFunction;
    }

    AbstractExecutablePlugin<M> createPlugin(AbstractMetisPluginMetadata pluginMetadata) {
      final M castPluginMetadata = pluginMetadataType.cast(pluginMetadata);
      return pluginCreatorFunction.apply(castPluginMetadata);
    }
  }
}
