package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-26
 */
public class NormalizationPlugin extends AbstractMetisPlugin {

  private final String topologyName = Topology.NORMALIZATION.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the plugin.
   */
  NormalizationPlugin() {
    //Required for json serialization
    super(PluginType.NORMALIZATION);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata should be {@link NormalizationPluginMetadata}
   */
  NormalizationPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    super(PluginType.NORMALIZATION, pluginMetadata);
  }

  @Override
  public String getTopologyName() {
    return topologyName;
  }

  @Override
  DpsTask prepareDpsTask(String ecloudBaseUrl, String ecloudProvider, String ecloudDataset) {
    return createDpsTaskForProcessPlugin(null, ecloudBaseUrl, ecloudProvider, ecloudDataset);
  }

}
