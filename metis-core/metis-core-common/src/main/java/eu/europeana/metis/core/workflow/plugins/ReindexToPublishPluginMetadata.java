package eu.europeana.metis.core.workflow.plugins;

/**
 * This metadata applies to a reindex to publish (which is not a executable plugin type).
 */
public class ReindexToPublishPluginMetadata extends AbstractMetisPluginMetadata {

  @Override
  public PluginType getPluginType() {
    return PluginType.REINDEX_TO_PUBLISH;
  }
}
