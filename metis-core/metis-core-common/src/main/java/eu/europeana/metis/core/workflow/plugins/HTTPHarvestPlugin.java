package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;
import eu.europeana.cloud.service.dps.PluginParameterKeys;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP Harvest Plugin.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
public class HTTPHarvestPlugin extends AbstractExecutablePlugin<HTTPHarvestPluginMetadata> {

  private final String topologyName = Topology.HTTP_HARVEST.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the
   * plugin.
   */
  HTTPHarvestPlugin() {
    // Required for json serialization
    super(PluginType.HTTP_HARVEST);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>
   * Initializes the {@link #pluginType} as well.
   * </p>
   *
   * @param pluginMetadata The plugin metadata.
   */
  HTTPHarvestPlugin(HTTPHarvestPluginMetadata pluginMetadata) {
    super(PluginType.HTTP_HARVEST, pluginMetadata);
  }

  @Override
  public String getTopologyName() {
    return topologyName;
  }

  @Override
  DpsTask prepareDpsTask(String datasetId, DpsTaskSettings dpsTaskSettings) {
    String targetUrl = getPluginMetadata().getUrl();
    Map<String, String> parameters = new HashMap<>();
    parameters.put(PluginParameterKeys.METIS_DATASET_ID, datasetId);
    return createDpsTaskForHarvestPlugin(dpsTaskSettings, parameters, targetUrl,
            getPluginMetadata().isIncrementalHarvest());
  }
}
