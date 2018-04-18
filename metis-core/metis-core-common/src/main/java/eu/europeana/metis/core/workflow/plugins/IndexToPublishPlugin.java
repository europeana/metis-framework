package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-03
 */
public class IndexToPublishPlugin extends AbstractMetisPlugin {

  private final String topologyName = Topology.INDEX.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the plugin.
   */
  public IndexToPublishPlugin() {
    //Required for json serialization
    super(PluginType.PUBLISH);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata should be {@link IndexToPublishPluginMetadata}
   */
  public IndexToPublishPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    super(PluginType.PREVIEW, pluginMetadata);
  }

  /**
   * Required for json serialization.
   *
   * @return the String representation of the topology
   */
  @Override
  public String getTopologyName() {
    return topologyName;
  }

  @Override
  public DpsTask prepareDpsTask(String ecloudBaseUrl, String ecloudProvider, String ecloudDataset) {
    // TODO: 3-4-18 Needs to be implemented
    return null;
  }
}
