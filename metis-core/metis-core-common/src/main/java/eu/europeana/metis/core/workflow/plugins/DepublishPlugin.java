package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;
import eu.europeana.cloud.service.dps.PluginParameterKeys;
import eu.europeana.metis.core.common.RecordIdUtils;
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
  public DepublishPlugin(DepublishPluginMetadata pluginMetadata) {
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
      DpsTaskSettings dpsTaskSettings) {

    Map<String, String> extraParameters = new HashMap<>();
    extraParameters.put(PluginParameterKeys.METIS_DATASET_ID, datasetId);
    //Do set the records ids parameter only if record ids depublication enabled and there are record ids
    if (!getPluginMetadata().isDatasetDepublish()) {
      if (CollectionUtils.isEmpty(getPluginMetadata().getRecordIdsToDepublish())) {
        throw new IllegalStateException(
            "Requested record depublication but there are no records ids for depublication in the db");
      } else {
        final String recordIdList = String.join(",", RecordIdUtils
                .composeFullRecordIds(datasetId, getPluginMetadata().getRecordIdsToDepublish()));
        extraParameters.put(PluginParameterKeys.RECORD_IDS_TO_DEPUBLISH, recordIdList);
      }
    }
    //TODO: 2024-09-24 - Update below key with the PluginParameterKeys equivalent when it's available
    extraParameters.put("DEPUBLICATION_REASON", getPluginMetadata().getDepublicationReason().name());
    DpsTask dpsTask = new DpsTask();
    dpsTask.setParameters(extraParameters);
    return dpsTask;
  }
}
