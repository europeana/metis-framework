package eu.europeana.metis.core.workflow.plugins;

/**
 * This metadata applies to a reindex to preview (which is not a executable plugin type).
 */
public class ReindexToPreviewPluginMetadata extends AbstractMetisPluginMetadata {

  @Override
  public PluginType getPluginType() {
    return PluginType.REINDEX_TO_PREVIEW;
  }
}
