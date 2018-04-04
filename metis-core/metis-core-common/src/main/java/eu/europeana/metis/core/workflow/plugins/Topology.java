package eu.europeana.metis.core.workflow.plugins;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-12
 */
public enum Topology {
  OAIPMH_HARVEST("oai_harvest"), VALIDATION("validation"), TRANSFORMATION(
      "xslt_transform"), ENRICHMENT("enrichment"), INDEX("index"), HTTP_HARVEST("http_harvest");

  private String topologyName;

  Topology(String topologyName) {
    this.topologyName = topologyName;
  }

  public String getTopologyName() {
    return topologyName;
  }
}
