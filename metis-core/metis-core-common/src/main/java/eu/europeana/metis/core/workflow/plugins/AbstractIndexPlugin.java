package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;

public abstract class AbstractIndexPlugin<M extends AbstractIndexPluginMetadata> extends AbstractExecutablePlugin<M> {

  protected final String topologyName = Topology.INDEX.getTopologyName();
  // TODO: 01/11/2021 If we want to fix that we should do an update of the database
  //Initialize with negative for past executions
  private int totalDatabaseRecords = -1;

  /**
   * Required by (de)serialization in db.
   * <p>It is not to be used manually</p>
   */
  AbstractIndexPlugin() {
    //Required by (de)serialization in db
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata required and the pluginType.
   *
   * @param pluginType a {@link PluginType} related to the implemented plugin
   * @param pluginMetadata the plugin metadata
   */
  public AbstractIndexPlugin(PluginType pluginType, M pluginMetadata) {
    super(pluginType, pluginMetadata);
  }

  @Override
  public String getTopologyName() {
    return topologyName;
  }

  @Override
  public DpsTask prepareDpsTask(String datasetId, EcloudBasePluginParameters ecloudBasePluginParameters) {
    return createDpsTaskForIndexPlugin(ecloudBasePluginParameters, datasetId,
        getPluginMetadata().isIncrementalIndexing(),
        getPluginMetadata().getHarvestDate(),
        getPluginMetadata().isUseAlternativeIndexingEnvironment(),
        getPluginMetadata().isPreserveTimestamps(),
        getPluginMetadata().getDatasetIdsToRedirectFrom(),
        getPluginMetadata().isPerformRedirects(), getPluginType().name());
  }

  public int getTotalDatabaseRecords() {
    return totalDatabaseRecords;
  }

  public void setTotalDatabaseRecords(int totalDatabaseRecords) {
    this.totalDatabaseRecords = totalDatabaseRecords;
  }
}
