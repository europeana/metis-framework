package eu.europeana.metis.core.workflow.plugins;

import java.util.List;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
public class VoidMetisPluginMetadata implements AbstractMetisPluginMetadata {

  private static final PluginType pluginType = PluginType.VOID;
  private boolean mocked = true;
  private Map<String, List<String>> parameters;

  public VoidMetisPluginMetadata() {
  }

  public VoidMetisPluginMetadata(
      Map<String, List<String>> parameters) {
    this.parameters = parameters;
  }

  @Override
  public PluginType getPluginType() {
    return pluginType;
  }

  @Override
  public boolean isMocked() {
    return this.mocked;
  }

  @Override
  public void setMocked(boolean mocked) {
    this.mocked = mocked;
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
