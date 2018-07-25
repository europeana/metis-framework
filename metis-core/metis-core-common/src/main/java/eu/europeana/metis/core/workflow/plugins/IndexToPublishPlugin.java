package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-03
 */
public class IndexToPublishPlugin extends AbstractMetisPlugin {

  private final String topologyName = Topology.INDEX.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the
   * plugin.
   */
  IndexToPublishPlugin() {
    //Required for json serialization
    this(null);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata should be {@link IndexToPublishPluginMetadata}
   */
  IndexToPublishPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    super(PluginType.PUBLISH, pluginMetadata);
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
    boolean useAlternativeIndexingEnvironment = ((IndexToPublishPluginMetadata) getPluginMetadata())
        .getUseAlternativeIndexingEnvironment();
    boolean preserveTimestamps = ((IndexToPublishPluginMetadata) getPluginMetadata())
        .isPreserveTimestamps();
    return createDpsTaskForIndexPlugin(useAlternativeIndexingEnvironment, preserveTimestamps,
        "PUBLISH", ecloudBaseUrl, ecloudProvider,
        ecloudDataset);
  }
}
