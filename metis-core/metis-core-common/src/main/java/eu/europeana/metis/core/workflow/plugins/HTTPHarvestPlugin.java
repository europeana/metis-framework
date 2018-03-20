package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
public class HTTPHarvestPlugin extends AbstractMetisPlugin {

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the plugin.
   */
  public HTTPHarvestPlugin() {
    //Required for json serialization
    super(PluginType.HTTP_HARVEST);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata should be {@link HTTPHarvestPluginMetadata}
   */
  public HTTPHarvestPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    super(PluginType.HTTP_HARVEST, pluginMetadata);
  }

  @Override
  public String getTopologyName() {
    return null;
  }

  @Override
  DpsTask prepareDpsTask(String ecloudBaseUrl, String ecloudProvider, String ecloudDataset) {
    // TODO: 6-3-18 When HTTP topology ready
    return null;
  }

}
