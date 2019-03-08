package eu.europeana.enrichment.api.external.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2019-03-07
 */
public class EnrichmentBaseWrapper {
  @XmlElement(name = "originalField")
  private String originalField;

  @XmlElements(value = {
      @XmlElement(name = "Concept", namespace = "http://www.w3.org/2004/02/skos/core#", type = Concept.class),
      @XmlElement(name = "Agent", namespace = "http://www.europeana.eu/schemas/edm/", type = Agent.class),
      @XmlElement(name = "Place", namespace = "http://www.europeana.eu/schemas/edm/", type = Place.class),
      @XmlElement(name = "Timespan", namespace = "http://www.europeana.eu/schemas/edm/", type = Timespan.class)})
  private EnrichmentBase enrichmentBase;

  public EnrichmentBaseWrapper() {
  }

  public EnrichmentBaseWrapper(String originalField, EnrichmentBase enrichmentBase) {
    this.originalField = originalField;
    this.enrichmentBase = enrichmentBase;
  }

  public String getOriginalField() {
    return originalField;
  }

  public EnrichmentBase getEnrichmentBase() {
    return enrichmentBase;
  }
}
