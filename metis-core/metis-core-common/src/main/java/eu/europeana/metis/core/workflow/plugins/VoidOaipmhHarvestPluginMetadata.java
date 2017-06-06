package eu.europeana.metis.core.workflow.plugins;

import eu.europeana.metis.core.dataset.OaipmhHarvestingMetadata;
import java.util.List;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
public class VoidOaipmhHarvestPluginMetadata implements AbstractMetisPluginMetadata {
  private OaipmhHarvestingMetadata oaipmhHarvestingMetadata;
  private final PluginType pluginType = PluginType.OAIPMH_HARVEST;
  private Map<String, List<String>> parameters;

  public VoidOaipmhHarvestPluginMetadata() {
  }

  public VoidOaipmhHarvestPluginMetadata(
      OaipmhHarvestingMetadata oaipmhHarvestingMetadata,
      Map<String, List<String>> parameters) {
    this.oaipmhHarvestingMetadata = oaipmhHarvestingMetadata;
    this.parameters = parameters;
  }

  public OaipmhHarvestingMetadata getOaipmhHarvestingMetadata() {
    return oaipmhHarvestingMetadata;
  }

  public void setOaipmhHarvestingMetadata(
      OaipmhHarvestingMetadata oaipmhHarvestingMetadata) {
    this.oaipmhHarvestingMetadata = oaipmhHarvestingMetadata;
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
