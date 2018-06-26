package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-24
 */
public class HTTPHarvestPlugin extends AbstractMetisPlugin {

  private final String topologyName = Topology.HTTP_HARVEST.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the plugin.
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
   * @param pluginMetadata should be {@link HTTPHarvestPluginMetadata}
   */
  HTTPHarvestPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    super(PluginType.HTTP_HARVEST, pluginMetadata);
  }

  @Override
  public String getTopologyName() {
    return topologyName;
  }

  @Override
  DpsTask prepareDpsTask(String ecloudBaseUrl, String ecloudProvider, String ecloudDataset) {
    String targetUrl = ((HTTPHarvestPluginMetadata) getPluginMetadata()).getUrl();
    String datasetId = ((HTTPHarvestPluginMetadata) getPluginMetadata()).getDatasetId();
    boolean useDefaultIdentifiers = ((HTTPHarvestPluginMetadata) getPluginMetadata())
        .isUseDefaultIdentifiers();
    Map<String, String> parameters = new HashMap<>();
    parameters.put("METIS_DATASET_ID", datasetId);
    parameters.put("USE_DEFAULT_IDENTIFIERS", String.valueOf(useDefaultIdentifiers));
    return createDpsTaskForHarvestPlugin(parameters, targetUrl, ecloudBaseUrl, ecloudProvider, ecloudDataset);
  }
}
