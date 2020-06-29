package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.CollectionUtils;

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
    //Do set the records ids parameter only if record ids depublication enabled and there are record ids
    if (!getPluginMetadata().isDatasetDepublish()) {
      if (CollectionUtils.isEmpty(getPluginMetadata().getRecordIdsToDepublish())) {
        throw new IllegalStateException(
            "Requested record depublication but there are no records ids for depublication in the db");
      } else {
        extraParameters.put("RECORD_IDS_TO_DEPUBLISH",
            String.join(",", getPluginMetadata().getRecordIdsToDepublish()));
      }
    }
    DpsTask dpsTask = new DpsTask();
    dpsTask.setParameters(extraParameters);
    return dpsTask;
  }
}
