package eu.europeana.metis.core.workflow.plugins;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-05-16
 */
public class LinkCheckingPluginMetadata extends AbstractMetisPluginMetadata {

  private static final PluginType pluginType = PluginType.LINK_CHECKING;
  private Map<String, Integer> connectionLimitToDomains = new HashMap<>();

  public LinkCheckingPluginMetadata() {
    //Required for json serialization
  }

  @Override
  public PluginType getPluginType() {
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
