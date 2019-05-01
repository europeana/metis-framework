package eu.europeana.metis.core.workflow.plugins;

import java.util.HashMap;
import java.util.Map;

/**
 * Media Process Plugin Metadata.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-04-20
 */
public class MediaProcessPluginMetadata extends AbstractExecutablePluginMetadata {

  private static final ExecutablePluginType pluginType = ExecutablePluginType.MEDIA_PROCESS;
  private Map<String, Integer> connectionLimitToDomains = new HashMap<>();

  public MediaProcessPluginMetadata() {
    //Required for json serialization
  }

  @Override
  public ExecutablePluginType getExecutablePluginType() {
    return pluginType;
  }

  public Map<String, Integer> getConnectionLimitToDomains() {
    return connectionLimitToDomains;
  }

  public void setConnectionLimitToDomains(
      Map<String, Integer> connectionLimitToDomains) {
    this.connectionLimitToDomains = connectionLimitToDomains;
  }
}
