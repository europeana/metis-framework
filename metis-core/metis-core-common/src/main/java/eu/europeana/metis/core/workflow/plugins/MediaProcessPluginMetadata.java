package eu.europeana.metis.core.workflow.plugins;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-20
 */
public class MediaProcessPluginMetadata extends AbstractMetisPluginMetadata {

  private static final PluginType pluginType = PluginType.MEDIA_PROCESS;

  public MediaProcessPluginMetadata() {
    //Required for json serialization
  }

  @Override
  public PluginType getPluginType() {
    return pluginType;
  }
}
