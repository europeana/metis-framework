package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.service.dps.DpsTask;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
public class EnrichmentPlugin extends AbstractMetisPlugin {

  private final String topologyName = Topology.ENRICHMENT.getTopologyName();

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the plugin.
   */
  EnrichmentPlugin() {
    //Required for json serialization
    super(PluginType.ENRICHMENT);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata should be {@link EnrichmentPluginMetadata}
   */
  EnrichmentPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    super(PluginType.ENRICHMENT, pluginMetadata);
  }

  @Override
  public String getTopologyName() {
    return topologyName;
  }

  @Override
  DpsTask prepareDpsTask(String ecloudBaseUrl, String ecloudProvider, String ecloudDataset) {
    return createDpsTaskForProcessPlugin(null, ecloudBaseUrl, ecloudProvider, ecloudDataset);
  }
}
