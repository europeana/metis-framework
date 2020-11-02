package eu.europeana.enrichment.api.external.model;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

/**
 * Wrapper of {@link EnrichmentBase}, used for easier (de)serialization.
 *
 * @author Joana Sousa
 * @since 2020-11-02
 */
public class EnrichmentResultBaseWrapper {


  @XmlElements(value = {
      @XmlElement(name = "Concept", namespace = "http://www.w3.org/2004/02/skos/core#", type = Concept.class),
      @XmlElement(name = "Agent", namespace = "http://www.europeana.eu/schemas/edm/", type = Agent.class),
      @XmlElement(name = "Place", namespace = "http://www.europeana.eu/schemas/edm/", type = Place.class),
      @XmlElement(name = "Timespan", namespace = "http://www.europeana.eu/schemas/edm/", type = Timespan.class)})
  private EnrichmentBase enrichmentBase;

  public EnrichmentResultBaseWrapper() {
  }

  /**
   * Constructor with all fields
   *
   * @param enrichmentBase the enrichment information class generated
   */
  public EnrichmentResultBaseWrapper(EnrichmentBase enrichmentBase) {
    this.enrichmentBase = enrichmentBase;
  }

  public EnrichmentBase getEnrichmentBase() {
    return enrichmentBase;
  }

  /**
   * Convert a collection of {@link EnrichmentBase} to a list of {@link EnrichmentBaseWrapper}.
   * <p>This is mostly used for dereferencing.</p>
   *
   * @param resultList the collection of {@link EnrichmentBase}
   * @return the converted list
   */
  public static List<EnrichmentResultBaseWrapper> createNullOriginalFieldEnrichmentBaseWrapperList(
      Collection<EnrichmentBase> resultList) {
    return resultList.stream()
        .map(EnrichmentResultBaseWrapper::new).collect(
            Collectors.toList());
  }

}
