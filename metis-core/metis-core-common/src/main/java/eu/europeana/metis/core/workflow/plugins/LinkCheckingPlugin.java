package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;
import java.util.Map;

/**
 * Link Checking Plugin.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-05-16
 */
public class LinkCheckingPlugin extends AbstractExecutablePlugin<LinkCheckingPluginMetadata> {

  private final String topologyName = Topology.LINK_CHECKING.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the
   * plugin.
   */
  LinkCheckingPlugin() {
    //Required for json serialization
    super(PluginType.LINK_CHECKING);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata The plugin metadata.
   */
  LinkCheckingPlugin(LinkCheckingPluginMetadata pluginMetadata) {
    super(PluginType.LINK_CHECKING, pluginMetadata);
  }

  @Override
  public String getTopologyName() {
    return topologyName;
  }

  @Override
  DpsTask prepareDpsTask(EcloudBasePluginParameters ecloudBasePluginParameters) {
    final Map<String, String> extraParameters = createParametersForHostConnectionLimits(
        getPluginMetadata().getConnectionLimitToDomains());
    if (Boolean.TRUE.equals(getPluginMetadata().getPerformSampling())
        && getPluginMetadata().getSampleSize() != null) {
      extraParameters.put("SAMPLE_SIZE", getPluginMetadata().getSampleSize().toString());
    }
    return createDpsTaskForProcessPlugin(ecloudBasePluginParameters, extraParameters);
  }
}
