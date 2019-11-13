package eu.europeana.metis.core.workflow.plugins;

/**
 * This represents a reindex to publish (which is not a executable plugin type).
 */
public class ReindexToPublishPlugin extends AbstractMetisPlugin<ReindexToPublishPluginMetadata> {

  ReindexToPublishPlugin() {
    this(null);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata The plugin metadata.
   */
  public ReindexToPublishPlugin(ReindexToPublishPluginMetadata pluginMetadata) {
    super(PluginType.REINDEX_TO_PUBLISH, pluginMetadata);
  }
}
