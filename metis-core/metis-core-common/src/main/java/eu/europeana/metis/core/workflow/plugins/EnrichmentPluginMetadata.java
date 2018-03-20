package eu.europeana.metis.core.workflow.plugins;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
public class EnrichmentPluginMetadata extends AbstractMetisPluginMetadata {

  private static final PluginType pluginType = PluginType.ENRICHMENT;

  public EnrichmentPluginMetadata() {
    //Required for json serialization
  }

  @Override
  public PluginType getPluginType() {
    return pluginType;
  }
}
