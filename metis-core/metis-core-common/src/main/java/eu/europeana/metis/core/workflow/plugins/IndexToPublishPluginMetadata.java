package eu.europeana.metis.core.workflow.plugins;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-03
 */
public class IndexToPublishPluginMetadata extends AbstractMetisPluginMetadata {

  private static final PluginType pluginType = PluginType.PUBLISH;

  public IndexToPublishPluginMetadata() {
    //Required for json serialization
  }

  @Override
  public PluginType getPluginType() {
    return pluginType;
  }
}
