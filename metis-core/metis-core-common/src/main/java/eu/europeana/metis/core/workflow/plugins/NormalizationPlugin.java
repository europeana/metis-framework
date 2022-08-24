package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;

/**
 * Normalization Plugin.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-26
 */
public class NormalizationPlugin extends AbstractExecutablePlugin<NormalizationPluginMetadata> {

  private final String topologyName = Topology.NORMALIZATION.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the
   * plugin.
   */
  NormalizationPlugin() {
    //Required for json serialization
    super(PluginType.NORMALIZATION);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata The plugin metadata.
   */
  NormalizationPlugin(NormalizationPluginMetadata pluginMetadata) {
    super(PluginType.NORMALIZATION, pluginMetadata);
  }

  @Override
  public String getTopologyName() {
    return topologyName;
  }

  @Override
  DpsTask prepareDpsTask(String datasetId, DpsTaskSettings dpsTaskSettings) {
    return createDpsTaskForProcessPlugin(dpsTaskSettings, null);
  }

}
