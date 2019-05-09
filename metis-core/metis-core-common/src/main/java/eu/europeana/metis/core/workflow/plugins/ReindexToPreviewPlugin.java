package eu.europeana.metis.core.workflow.plugins;

/**
 * This represents a reindex to preview (which is not a executable plugin type).
 */
public class ReindexToPreviewPlugin extends AbstractMetisPlugin<ReindexToPreviewPluginMetadata> {

  ReindexToPreviewPlugin() {
    this(null);
  }

  ReindexToPreviewPlugin(ReindexToPreviewPluginMetadata pluginMetadata) {
    super(PluginType.REINDEX_TO_PREVIEW, pluginMetadata);
  }
}
