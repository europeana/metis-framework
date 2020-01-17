package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;
import java.util.List;

/**
 * Index to Preview Plugin.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-03
 */
public class IndexToPreviewPlugin extends AbstractExecutablePlugin<IndexToPreviewPluginMetadata> {

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
   * @param pluginMetadata The plugin metadata.
   */
  IndexToPreviewPlugin(IndexToPreviewPluginMetadata pluginMetadata) {
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
  public DpsTask prepareDpsTask(String datasetId,
      EcloudBasePluginParameters ecloudBasePluginParameters) {
    boolean useAlternativeIndexingEnvironment = getPluginMetadata()
        .isUseAlternativeIndexingEnvironment();
    boolean preserveTimestamps = getPluginMetadata().isPreserveTimestamps();
    final List<String> datasetIdsToRedirectFrom = getPluginMetadata().getDatasetIdsToRedirectFrom();
    return createDpsTaskForIndexPlugin(ecloudBasePluginParameters, datasetId,
        useAlternativeIndexingEnvironment, preserveTimestamps, datasetIdsToRedirectFrom, "PREVIEW");
  }
}
