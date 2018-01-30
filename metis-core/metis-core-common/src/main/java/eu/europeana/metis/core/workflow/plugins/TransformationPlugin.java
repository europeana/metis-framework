package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.cloud.client.dps.rest.DpsClient;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-29
 */
public class TransformationPlugin extends AbstractMetisPlugin {

  public TransformationPlugin() {
    super();
    setPluginType(PluginType.TRANSFORMATION);
    //Required for json serialization
  }

  public TransformationPlugin(AbstractMetisPluginMetadata pluginMetadata) {
    super(PluginType.TRANSFORMATION, pluginMetadata);
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
