package eu.europeana.metis.core.workflow.plugins;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import org.bson.types.ObjectId;

/**
 * This class implements the functionality of creating executable plugins, given a plugin metadata
 * object.
 */
public final class ExecutablePluginFactory {

  private static final Map<ExecutablePluginType, PluginCreator<?>> pluginCreators;

  static {
    final Map<ExecutablePluginType, PluginCreator<?>> creators = new EnumMap<>(
        ExecutablePluginType.class);
    creators.put(ExecutablePluginType.HTTP_HARVEST,
        new PluginCreator<>(HTTPHarvestPluginMetadata.class, HTTPHarvestPlugin::new));
    creators.put(ExecutablePluginType.OAIPMH_HARVEST,
        new PluginCreator<>(OaipmhHarvestPluginMetadata.class, OaipmhHarvestPlugin::new));
    creators.put(ExecutablePluginType.ENRICHMENT,
        new PluginCreator<>(EnrichmentPluginMetadata.class, EnrichmentPlugin::new));
    creators.put(ExecutablePluginType.MEDIA_PROCESS,
        new PluginCreator<>(MediaProcessPluginMetadata.class, MediaProcessPlugin::new));
    creators.put(ExecutablePluginType.LINK_CHECKING,
        new PluginCreator<>(LinkCheckingPluginMetadata.class, LinkCheckingPlugin::new));
    creators.put(ExecutablePluginType.VALIDATION_EXTERNAL,
        new PluginCreator<>(ValidationExternalPluginMetadata.class, ValidationExternalPlugin::new));
    creators.put(ExecutablePluginType.TRANSFORMATION,
        new PluginCreator<>(TransformationPluginMetadata.class, TransformationPlugin::new));
    creators.put(ExecutablePluginType.VALIDATION_INTERNAL,
        new PluginCreator<>(ValidationInternalPluginMetadata.class, ValidationInternalPlugin::new));
    creators.put(ExecutablePluginType.NORMALIZATION,
        new PluginCreator<>(NormalizationPluginMetadata.class, NormalizationPlugin::new));
    creators.put(ExecutablePluginType.PREVIEW,
        new PluginCreator<>(IndexToPreviewPluginMetadata.class, IndexToPreviewPlugin::new));
    creators.put(ExecutablePluginType.PUBLISH,
        new PluginCreator<>(IndexToPublishPluginMetadata.class, IndexToPublishPlugin::new));
    pluginCreators = Collections.unmodifiableMap(creators);
  }

  private ExecutablePluginFactory() {
  }

  /**
   * This method creates a new plugin for the provided plugin metadata. This method also sets the
   * new plugins's id and data status.
   *
   * @param metadata The metadata for which to create a plugin. Cannot be null.
   * @return A new pluing instance.
   */
  public static AbstractExecutablePlugin createPlugin(ExecutablePluginMetadata metadata) {

    // Find the right creator.
    final PluginCreator<?> creator = pluginCreators.get(metadata.getExecutablePluginType());
    if (creator == null) {
      throw new IllegalStateException(
          "Found unknown executable plugin type: " + metadata.getExecutablePluginType());
    }

    // Perform the creation.
    final AbstractExecutablePlugin plugin = creator.createPlugin(metadata);
    plugin.setId(new ObjectId().toString() + "-" + plugin.getPluginType().name());
    plugin.setDataStatus(DataStatus.NOT_YET_GENERATED);
    return plugin;
  }

  private static class PluginCreator<M extends AbstractExecutablePluginMetadata> {

    private Class<M> pluginMetadataType;
    private Function<M, AbstractExecutablePlugin<M>> pluginCreatorFunction;

    PluginCreator(Class<M> pluginMetadataType,
        Function<M, AbstractExecutablePlugin<M>> pluginCreatorFunction) {
      this.pluginMetadataType = pluginMetadataType;
      this.pluginCreatorFunction = pluginCreatorFunction;
    }

    AbstractExecutablePlugin<M> createPlugin(MetisPluginMetadata pluginMetadata) {
      final M castPluginMetadata = pluginMetadataType.cast(pluginMetadata);
      return pluginCreatorFunction.apply(castPluginMetadata);
    }
  }
}
