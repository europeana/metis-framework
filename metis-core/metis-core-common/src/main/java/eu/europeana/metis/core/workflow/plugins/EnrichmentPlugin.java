package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;

/**
 * Enrichment Plugin.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
public class EnrichmentPlugin extends AbstractExecutablePlugin<EnrichmentPluginMetadata> {

  private final String topologyName = Topology.ENRICHMENT.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the
   * plugin.
   */
  EnrichmentPlugin() {
    //Required for json serialization
    super(PluginType.ENRICHMENT);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata The plugin metadata.
   */
  EnrichmentPlugin(EnrichmentPluginMetadata pluginMetadata) {
    super(PluginType.ENRICHMENT, pluginMetadata);
  }

  @Override
  public String getTopologyName() {
    return topologyName;
  }

  @Override
  DpsTask prepareDpsTask(String datasetId, EcloudBasePluginParameters ecloudBasePluginParameters) {
    return createDpsTaskForProcessPlugin(ecloudBasePluginParameters, null);
  }
}
