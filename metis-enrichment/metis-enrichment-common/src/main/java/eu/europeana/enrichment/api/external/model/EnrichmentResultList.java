package eu.europeana.enrichment.api.external.model;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;


/**
 * Contains a list of {@link EnrichmentBase} results.
 */
@XmlRootElement(namespace = "http://www.europeana.eu/schemas/metis", name = "results")
@XmlAccessorType(XmlAccessType.FIELD)
public class EnrichmentResultList {

  @XmlElement(namespace = "http://www.europeana.eu/schemas/metis", name = "result", type = EnrichmentResultBaseWrapper.class)
  private final List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrapperList = new ArrayList<>();

  public EnrichmentResultList() {
  }

  /**
   * Constructor with initial {@link EnrichmentBase} list.
   *
   * @param enrichmentResultBaseWrappers the list to initialize the class with
   */
  public EnrichmentResultList(List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrappers) {
    this.enrichmentResultBaseWrapperList.addAll(enrichmentResultBaseWrappers);
  }

  public List<EnrichmentResultBaseWrapper> getEnrichmentBaseResultWrapperList() {
    return new ArrayList<>(enrichmentResultBaseWrapperList);
  }
}
