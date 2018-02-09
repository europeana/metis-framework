package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.client.dps.rest.DpsClient;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
public class HTTPHarvestPlugin extends AbstractMetisPlugin {

  public HTTPHarvestPlugin() {
    //Required for json serialization
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata required.
   *
   * @param pluginMetadata should be {@link HTTPHarvestPluginMetadata}
   */
  public HTTPHarvestPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    super(PluginType.HTTP_HARVEST, pluginMetadata);
  }

  @Override
  public void setPluginType(PluginType pluginType) {
    super.setPluginType(PluginType.HTTP_HARVEST);
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
