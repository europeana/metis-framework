package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.metis.core.dataset.HttpHarvestingMetadata;
import java.util.List;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
public class HTTPHarvestPluginMetadata implements AbstractMetisPluginMetadata {
  private HttpHarvestingMetadata httpHarvestingMetadata;
  private final PluginType pluginType = PluginType.HTTP_HARVEST;
  private Map<String, List<String>> parameters;

  public HTTPHarvestPluginMetadata() {
  }

  public HTTPHarvestPluginMetadata(
      HttpHarvestingMetadata httpHarvestingMetadata,
      Map<String, List<String>> parameters) {
    this.httpHarvestingMetadata = httpHarvestingMetadata;
    this.parameters = parameters;
  }

  public HttpHarvestingMetadata getHttpHarvestingMetadata() {
    return httpHarvestingMetadata;
  }

  public void setHttpHarvestingMetadata(
      HttpHarvestingMetadata httpHarvestingMetadata) {
    this.httpHarvestingMetadata = httpHarvestingMetadata;
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
