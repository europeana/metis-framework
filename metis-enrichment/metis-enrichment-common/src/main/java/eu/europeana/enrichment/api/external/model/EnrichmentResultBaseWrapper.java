package eu.europeana.enrichment.api.external.model;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Wrapper of {@link EnrichmentBase}, used for easier (de)serialization.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2019-03-07
 */
@XmlRootElement(namespace = "http://www.europeana.eu/schemas/metis", name = "result")
@XmlAccessorType(XmlAccessType.FIELD)
public class EnrichmentResultBaseWrapper {

  @XmlElements(value = {
      @XmlElement(name = "Concept", namespace = "http://www.w3.org/2004/02/skos/core#", type = Concept.class),
      @XmlElement(name = "Agent", namespace = "http://www.europeana.eu/schemas/edm/", type = Agent.class),
      @XmlElement(name = "Place", namespace = "http://www.europeana.eu/schemas/edm/", type = Place.class),
      @XmlElement(name = "Timespan", namespace = "http://www.europeana.eu/schemas/edm/", type = Timespan.class)})
  private List<EnrichmentBase> enrichmentBase;

  public EnrichmentResultBaseWrapper() {
  }

  /**
   * Constructor with all fields
   *
   * @param enrichmentBase the enrichment information class generated
   */
  public EnrichmentResultBaseWrapper(List<EnrichmentBase> enrichmentBase) {
    this.enrichmentBase = enrichmentBase;
  }

  public List<EnrichmentBase> getEnrichmentBaseList() {
    return enrichmentBase;
  }

  /**
   * Convert a collection of {@link EnrichmentBase} to a list of {@link
   * EnrichmentResultBaseWrapper}.
   * <p>This is mostly used for dereferencing.</p>
   *
   * @param resultList the collection of {@link EnrichmentBase}
   * @return the converted list
   */
  public static List<EnrichmentResultBaseWrapper> createNullOriginalFieldEnrichmentBaseWrapperList(
      Collection<List<EnrichmentBase>> resultList) {
    return resultList.stream().map(EnrichmentResultBaseWrapper::new).collect(Collectors.toList());
  }

}
