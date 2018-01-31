package eu.europeana.metis.core.workflow.plugins;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-12
 */
public enum TopologyName {
  OAIPMH_HARVEST("oai_harvest"), VALIDATION("validation");

  String topologyName;

  TopologyName(String topologyName) {
    this.topologyName = topologyName;
  }

  public String getTopologyName() {
    return topologyName;
  }
}
