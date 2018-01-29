package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.client.dps.rest.DpsClient;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
public class HTTPHarvestPlugin extends AbstractMetisPlugin {

  private AbstractMetisPluginMetadata pluginMetadata;

  public HTTPHarvestPlugin() {
    super();
    setPluginType(PluginType.HTTP_HARVEST);
    //Required for json serialization
  }

  public HTTPHarvestPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    setPluginType(PluginType.HTTP_HARVEST);
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
    // TODO: 24-11-17 Execution of http harvest topology
  }

  @Override
  public ExecutionProgress monitor(DpsClient dpsClient) {
    return null;
  }
}
