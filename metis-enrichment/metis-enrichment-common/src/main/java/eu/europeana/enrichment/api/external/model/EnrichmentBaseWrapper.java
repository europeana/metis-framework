package eu.europeana.enrichment.api.external.model;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

/**
 * Wrapper of {@link EnrichmentBase}, used for easier (de)serialization.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2019-03-07
 * @deprecated
 */
@Deprecated(forRemoval = true)
public class EnrichmentBaseWrapper {

  @XmlElement(name = "rdfFieldName")
  private String rdfFieldName;

  @XmlElements(value = {
      @XmlElement(name = "Concept", namespace = "http://www.w3.org/2004/02/skos/core#", type = Concept.class),
      @XmlElement(name = "Agent", namespace = "http://www.europeana.eu/schemas/edm/", type = Agent.class),
      @XmlElement(name = "Place", namespace = "http://www.europeana.eu/schemas/edm/", type = Place.class),
      @XmlElement(name = "Timespan", namespace = "http://www.europeana.eu/schemas/edm/", type = Timespan.class)})
  private EnrichmentBase enrichmentBase;

  public EnrichmentBaseWrapper() {
  }

  /**
   * Constructor with all fields
   *
   * @param rdfFieldName the rdf field name
   * @param enrichmentBase the enrichment information class generated
   */
  public EnrichmentBaseWrapper(String rdfFieldName, EnrichmentBase enrichmentBase) {
    this.rdfFieldName = rdfFieldName;
    this.enrichmentBase = enrichmentBase;
  }

  public String getRdfFieldName() {
    return rdfFieldName;
  }

  public EnrichmentBase getEnrichmentBase() {
    return enrichmentBase;
  }

  /**
   * Convert a collection of {@link EnrichmentBase} to a list of {@link EnrichmentBaseWrapper} with
   * 'null' {@link #rdfFieldName}.
   * <p>This is mostly used for dereferencing.</p>
   *
   * @param resultList the collection of {@link EnrichmentBase}
   * @return the converted list
   */
  public static List<EnrichmentBaseWrapper> createEnrichmentBaseWrapperList(
      Collection<EnrichmentBase> resultList) {
    return resultList.stream()
        .map(enrichmentBase -> new EnrichmentBaseWrapper(null, enrichmentBase)).collect(
            Collectors.toList());
  }
}
