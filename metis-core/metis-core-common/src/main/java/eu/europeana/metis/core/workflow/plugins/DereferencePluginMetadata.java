package eu.europeana.metis.core.workflow.plugins;

import java.util.List;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
public class DereferencePluginMetadata implements AbstractMetisPluginMetadata {
  private static final PluginType pluginType = PluginType.DEREFERENCE;
  private Map<String, List<String>> parameters;

  public DereferencePluginMetadata() {
  }

  public DereferencePluginMetadata(
      Map<String, List<String>> parameters) {
    this.parameters = parameters;
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
