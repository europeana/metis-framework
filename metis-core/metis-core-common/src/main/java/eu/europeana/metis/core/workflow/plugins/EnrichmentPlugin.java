package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.client.dps.rest.DpsClient;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-26
 */
public class EnrichmentPlugin extends AbstractMetisPlugin {

  /**
   * Zero argument constructor that initializes the {@link #pluginType} corresponding to the plugin.
   */
  public EnrichmentPlugin() {
    //Required for json serialization
    super(PluginType.ENRICHMENT);
  }

  /**
   * Constructor to initialize the plugin with pluginMetadata.
   * <p>Initializes the {@link #pluginType} as well.</p>
   *
   * @param pluginMetadata should be {@link EnrichmentPluginMetadata}
   */
  public EnrichmentPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    super(PluginType.ENRICHMENT, pluginMetadata);
  }

  @Override
  public String getTopologyName() {
    return null;
  }

  @Override
  public void execute(DpsClient dpsClient, String ecloudBaseUrl, String ecloudProvider,
      String ecloudDataset) {
    // TODO: 24-11-17 Execution of enrichment topology
  }

  @Override
  public ExecutionProgress monitor(DpsClient dpsClient) {
    return null;
  }
}
