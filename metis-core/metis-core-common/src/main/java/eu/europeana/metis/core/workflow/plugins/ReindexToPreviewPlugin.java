package eu.europeana.metis.core.workflow.plugins;

/**
 * This represents a reindex to preview (which is not a executable plugin type).
 */
public class ReindexToPreviewPlugin extends AbstractMetisPlugin<ReindexToPreviewPluginMetadata> {

  ReindexToPreviewPlugin() {
    this(null);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata The plugin metadata.
   */
  public ReindexToPreviewPlugin(ReindexToPreviewPluginMetadata pluginMetadata) {
    super(PluginType.REINDEX_TO_PREVIEW, pluginMetadata);
  }
}
