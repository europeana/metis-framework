package eu.europeana.metis.core.workflow.plugins;

/**
 * Media Process Plugin Metadata.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-20
 */
public class MediaProcessPluginMetadata extends AbstractExecutablePluginMetadata {

  private static final ExecutablePluginType pluginType = ExecutablePluginType.MEDIA_PROCESS;
  private ThrottlingLevelValuePair.ThrottlingLevel throttlingLevel;


  public MediaProcessPluginMetadata() {
    //Required for json serialization
  }

  @Override
  public ExecutablePluginType getExecutablePluginType() {
    return pluginType;
  }

  public ThrottlingLevelValuePair.ThrottlingLevel getThrottlingLevel() {
    return throttlingLevel;
  }

  public void setThrottlingLevel(ThrottlingLevelValuePair.ThrottlingLevel throttlingLevel) {
    this.throttlingLevel = throttlingLevel;
  }
}
