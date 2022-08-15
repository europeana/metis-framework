package eu.europeana.metis.core.workflow.plugins;

/**
 * Media Process Plugin Metadata.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-20
 */
public class MediaProcessPluginMetadata extends AbstractExecutablePluginMetadata {

  private static final ExecutablePluginType pluginType = ExecutablePluginType.MEDIA_PROCESS;
  private String throttlingLevel;


  public MediaProcessPluginMetadata() {
    //Required for json serialization
  }

  @Override
  public ExecutablePluginType getExecutablePluginType() {
    return pluginType;
  }

  public String getThrottlingLevel() {
    return throttlingLevel;
  }
}
