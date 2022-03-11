package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;
import eu.europeana.cloud.service.dps.metis.indexing.TargetIndexingDatabase;

/**
 * Index to Preview Plugin.
 * <b>Note: Adding another layer of hierarchy e.g. AbstractIndexPlugin seems to not work with morphia at this point in time 18/11/2021</b>
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-03
 */
public class IndexToPreviewPlugin extends AbstractExecutablePlugin<IndexToPreviewPluginMetadata> {
  protected final String topologyName = Topology.INDEX.getTopologyName();

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

  @Override
  public DpsTask prepareDpsTask(String datasetId, EcloudBasePluginParameters ecloudBasePluginParameters) {
    return createDpsTaskForIndexPlugin(ecloudBasePluginParameters, datasetId,
        getPluginMetadata().isIncrementalIndexing(),
        getPluginMetadata().getHarvestDate(),
        getPluginMetadata().isPreserveTimestamps(),
        getPluginMetadata().getDatasetIdsToRedirectFrom(),
        getPluginMetadata().isPerformRedirects(), getTargetIndexingDatabase().name());
  }

  @Override
  public String getTopologyName() {
    return topologyName;
  }

  /**
   * Get the target indexing database.
   *
   * @return the target indexing database
   */
  public TargetIndexingDatabase getTargetIndexingDatabase() {
    return TargetIndexingDatabase.PREVIEW;
  }
}
