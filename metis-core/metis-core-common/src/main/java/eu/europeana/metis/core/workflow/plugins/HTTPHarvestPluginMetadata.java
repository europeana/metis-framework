package eu.europeana.metis.core.workflow.plugins;

/**
 * HTTP Harvest Plugin Metadata.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
public class HTTPHarvestPluginMetadata extends AbstractHarvestPluginMetadata {

  private static final ExecutablePluginType pluginType = ExecutablePluginType.HTTP_HARVEST;
  private String url;
  private String user;
  private String password;

  public HTTPHarvestPluginMetadata() {
    //Required for json serialization
  }

  @Override
  public boolean isIncrementalHarvest() {
    return false;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public ExecutablePluginType getExecutablePluginType() {
    return pluginType;
  }

}
