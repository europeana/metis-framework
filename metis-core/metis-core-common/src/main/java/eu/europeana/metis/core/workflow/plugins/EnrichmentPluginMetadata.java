package eu.europeana.metis.core.workflow.plugins;

import java.util.List;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
public class EnrichmentPluginMetadata implements AbstractMetisPluginMetadata {
  private static final PluginType pluginType = PluginType.ENRICHMENT;
  private Map<String, List<String>> parameters;
  private boolean mocked = true;

  public EnrichmentPluginMetadata() {
  }

  public EnrichmentPluginMetadata(boolean mocked,
      Map<String, List<String>> parameters) {
    this.mocked = mocked;
    this.parameters = parameters;
  }

  public boolean isMocked() {
    return mocked;
  }

  public void setMocked(boolean mocked) {
    this.mocked = mocked;
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
