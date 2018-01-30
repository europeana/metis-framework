package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.client.dps.rest.DpsClient;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-29
 */
public class ValidationInternalPlugin extends AbstractMetisPlugin {

  public ValidationInternalPlugin() {
    super();
    setPluginType(PluginType.VALIDATION_INTERNAL);
    //Required for json serialization
  }

  public ValidationInternalPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    super(PluginType.VALIDATION_INTERNAL, pluginMetadata);
  }

  @Override
  public void execute(DpsClient dpsClient, String ecloudBaseUrl, String ecloudProvider,
      String ecloudDataset) {
    //This is an empty example
  }

  @Override
  public ExecutionProgress monitor(DpsClient dpsClient) {
    return null;
  }

}
