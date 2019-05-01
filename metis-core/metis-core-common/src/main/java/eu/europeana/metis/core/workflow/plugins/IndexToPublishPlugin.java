package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;

/**
 * Index to Publish Plugin.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-03
 */
public class IndexToPublishPlugin extends AbstractExecutablePlugin<IndexToPublishPluginMetadata> {

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
   * @param pluginMetadata The plugin metadata.
   */
  IndexToPublishPlugin(IndexToPublishPluginMetadata pluginMetadata) {
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
  public DpsTask prepareDpsTask(
      EcloudBasePluginParameters ecloudBasePluginParameters) {
    String datasetId = getPluginMetadata().getDatasetId();
    boolean useAlternativeIndexingEnvironment = getPluginMetadata()
        .isUseAlternativeIndexingEnvironment();
    boolean preserveTimestamps = getPluginMetadata().isPreserveTimestamps();
    return createDpsTaskForIndexPlugin(ecloudBasePluginParameters, datasetId,
        useAlternativeIndexingEnvironment, preserveTimestamps, "PUBLISH");
  }
}
