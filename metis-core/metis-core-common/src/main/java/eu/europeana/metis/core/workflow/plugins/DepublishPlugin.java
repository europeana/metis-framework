package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Depublish Plugin.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2020-06-16
 */
public class DepublishPlugin extends AbstractExecutablePlugin<DepublishPluginMetadata> {

  private final String topologyName = Topology.DEPUBLISH.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the
   * plugin.
   */
  DepublishPlugin() {
    //Required for json serialization
    this(null);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata The plugin metadata.
   */
  DepublishPlugin(DepublishPluginMetadata pluginMetadata) {
    super(PluginType.DEPUBLISH, pluginMetadata);
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
    Map<String, String> extraParameters = new HashMap<>();
    extraParameters.put("METIS_DATASET_ID", datasetId);
    extraParameters.put("DATASET_DEPUBLISH",
        Boolean.toString(getPluginMetadata().isDatasetDepublish()));
    if (!getPluginMetadata().isDatasetDepublish()) {
      extraParameters
          .put("RECORD_IDS_TO_DEPUBLISH", getPluginMetadata().getRecordIdsToDepublish().stream()
              .collect(Collectors.joining(",")));
    }
    DpsTask dpsTask = new DpsTask();
    dpsTask.setParameters(extraParameters);
    return dpsTask;
  }
}
