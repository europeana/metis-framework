package eu.europeana.metis.core.workflow.plugins;

/**
 * Contains all topology names.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-12
 */
public enum Topology {

  HTTP_HARVEST("http_harvest"),
  OAIPMH_HARVEST("oai_harvest"),

  VALIDATION("validation"),

  TRANSFORMATION("xslt_transform"),

  NORMALIZATION("normalization"),

  ENRICHMENT("enrichment"),

  MEDIA_PROCESS("media_process"),

  LINK_CHECKING("link_checker"),

  INDEX("indexer"),

  DEPUBLISH("depublisher");

  private String topologyName;

  Topology(String topologyName) {
    this.topologyName = topologyName;
  }

  public String getTopologyName() {
    return topologyName;
  }
}
