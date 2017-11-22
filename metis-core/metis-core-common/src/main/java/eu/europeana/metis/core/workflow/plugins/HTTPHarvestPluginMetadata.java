package eu.europeana.metis.core.workflow.plugins;

import java.util.List;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
public class HTTPHarvestPluginMetadata implements AbstractMetisPluginMetadata {
  private static final PluginType pluginType = PluginType.HTTP_HARVEST;
  private boolean mocked = true;
  private String url;
  private String user;
  private String password;
  private Map<String, List<String>> parameters;

  public HTTPHarvestPluginMetadata() {
  }

  public HTTPHarvestPluginMetadata(boolean mocked, String url, String user, String password,
      Map<String, List<String>> parameters) {
    this.mocked = mocked;
    this.url = url;
    this.user = user;
    this.password = password;
    this.parameters = parameters;
  }

  public boolean isMocked() {
    return mocked;
  }

  public void setMocked(boolean mocked) {
    this.mocked = mocked;
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
  public PluginType getPluginType() {
    return pluginType;
  }

  @Override
  public Map<String, List<String>> getParameters() {
    return parameters;
  }

  @Override
  public void setParameters(Map<String, List<String>> parameters) {
    this.parameters = parameters;
  }

}
