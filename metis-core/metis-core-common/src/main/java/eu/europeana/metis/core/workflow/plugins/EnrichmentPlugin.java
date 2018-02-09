package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.client.dps.rest.DpsClient;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
public class EnrichmentPlugin extends AbstractMetisPlugin {

  public EnrichmentPlugin() {
    //Required for json serialization
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata required.
   *
   * @param pluginMetadata should be {@link EnrichmentPluginMetadata}
   */
  public EnrichmentPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    super(PluginType.ENRICHMENT, pluginMetadata);
  }

  @Override
  public void setPluginType(PluginType pluginType) {
    super.setPluginType(PluginType.ENRICHMENT);
  }

  @Override
  public void execute(DpsClient dpsClient, String ecloudBaseUrl, String ecloudProvider,
      String ecloudDataset) {
    // TODO: 24-11-17 Execution of enrichment topology
  }

  @Override
  public ExecutionProgress monitor(DpsClient dpsClient) {
    return null;
  }
}
