package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-20
 */
public class MediaProcessPlugin extends AbstractMetisPlugin {

  private final String topologyName = Topology.MEDIA_PROCESS.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the plugin.
   */
  MediaProcessPlugin() {
    //Required for json serialization
    super(PluginType.MEDIA_PROCESS);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata should be {@link MediaProcessPluginMetadata}
   */
  MediaProcessPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    super(PluginType.MEDIA_PROCESS, pluginMetadata);
  }

  @Override
  public String getTopologyName() {
    return topologyName;
  }

  @Override
  DpsTask prepareDpsTask(String ecloudBaseUrl, String ecloudProvider, String ecloudDataset) {
    Map<String, Integer> connectionLimitToDomains = ((MediaProcessPluginMetadata) getPluginMetadata())
        .getConnectionLimitToDomains();
    return createDpsTaskForProcessPlugin(
        createParametersForHostConnectionLimits(connectionLimitToDomains), ecloudBaseUrl,
        ecloudProvider, ecloudDataset);
  }

}
