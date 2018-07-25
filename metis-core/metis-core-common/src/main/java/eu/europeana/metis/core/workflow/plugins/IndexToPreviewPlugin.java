package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-03
 */
public class IndexToPreviewPlugin extends AbstractMetisPlugin {

  private final String topologyName = Topology.INDEX.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the
   * plugin.
   */
  IndexToPreviewPlugin() {
    //Required for json serialization
    this(null);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata should be {@link IndexToPreviewPluginMetadata}
   */
  IndexToPreviewPlugin(AbstractMetisPluginMetadata pluginMetadata) {
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
    boolean useAlternativeIndexingEnvironment = ((IndexToPreviewPluginMetadata) getPluginMetadata())
        .getUseAlternativeIndexingEnvironment();
    boolean preserveTimestamps = ((IndexToPreviewPluginMetadata) getPluginMetadata())
        .isPreserveTimestamps();
    return createDpsTaskForIndexPlugin(useAlternativeIndexingEnvironment, preserveTimestamps,
        "PREVIEW", ecloudBaseUrl, ecloudProvider,
        ecloudDataset);
  }
}
