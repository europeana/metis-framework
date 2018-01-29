package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.client.dps.rest.DpsClient;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
public class EnrichmentPlugin extends AbstractMetisPlugin {

  private AbstractMetisPluginMetadata pluginMetadata;

  public EnrichmentPlugin() {
    super();
    setPluginType(PluginType.ENRICHMENT);
    //Required for json serialization
  }

  public EnrichmentPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    setPluginType(PluginType.ENRICHMENT);
    this.pluginMetadata = pluginMetadata;
  }

  @Override
  public AbstractMetisPluginMetadata getPluginMetadata() {
    return pluginMetadata;
  }

  @Override
  public void setPluginMetadata(
      AbstractMetisPluginMetadata pluginMetadata) {
    this.pluginMetadata = pluginMetadata;
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
