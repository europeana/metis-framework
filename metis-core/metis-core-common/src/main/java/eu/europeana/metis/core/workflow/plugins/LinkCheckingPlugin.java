package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-05-16
 */
public class LinkCheckingPlugin extends AbstractMetisPlugin {

  private final String topologyName = Topology.LINK_CHECKING.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the plugin.
   */
  LinkCheckingPlugin() {
    //Required for json serialization
    super(PluginType.LINK_CHECKING);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata should be {@link LinkCheckingPluginMetadata}
   */
  LinkCheckingPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    super(PluginType.LINK_CHECKING, pluginMetadata);
  }

  @Override
  public String getTopologyName() {
    return topologyName;
  }

  @Override
  DpsTask prepareDpsTask(String ecloudBaseUrl, String ecloudProvider, String ecloudDataset) {
    Map<String, Integer> connectionLimitToDomains = ((LinkCheckingPluginMetadata) getPluginMetadata())
        .getConnectionLimitToDomains();
    return createDpsTaskForProcessPlugin(
        createParametersForHostConnectionLimits(connectionLimitToDomains), ecloudBaseUrl,
        ecloudProvider, ecloudDataset);
  }
}
