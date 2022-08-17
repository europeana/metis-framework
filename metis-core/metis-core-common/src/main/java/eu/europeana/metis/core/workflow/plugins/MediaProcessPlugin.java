package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;
import eu.europeana.cloud.service.dps.PluginParameterKeys;

import java.util.HashMap;
import java.util.Map;

/**
 * Media Process Plugin.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-20
 */
public class MediaProcessPlugin extends AbstractExecutablePlugin<MediaProcessPluginMetadata> {

  private final String topologyName = Topology.MEDIA_PROCESS.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the
   * plugin.
   */
  MediaProcessPlugin() {
    //Required for json serialization
    super(PluginType.MEDIA_PROCESS);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata The plugin metadata.
   */
  MediaProcessPlugin(MediaProcessPluginMetadata pluginMetadata) {
    super(PluginType.MEDIA_PROCESS, pluginMetadata);
  }

  @Override
  public String getTopologyName() {
    return topologyName;
  }

  @Override
  DpsTask prepareDpsTask(String datasetId,
      DpsTaskSettings dpsTaskSettings) {
    final Map<String, String> extraParameters = new HashMap<>();
    ThrottlingLevelValuePair.ThrottlingLevel throttlingLevel = getPluginMetadata().getThrottlingLevel() == null ?
    ThrottlingLevelValuePair.ThrottlingLevel.WEAK : getPluginMetadata().getThrottlingLevel();
    extraParameters.put(PluginParameterKeys.MAXIMUM_PARALLELIZATION,
            String.valueOf(dpsTaskSettings.getThrottlingValues().getThreadNumberFromLevel(throttlingLevel)));

    return createDpsTaskForProcessPlugin(dpsTaskSettings, extraParameters);
  }

}
