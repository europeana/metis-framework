package eu.europeana.enrichment.api.external.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


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
  //TODO: Eventually, when the previous constructor is gone, make this constructor have as input parameter a Collection
  public EnrichmentResultList(List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrappers) {
    this.enrichmentResultBaseWrapperList.addAll(enrichmentResultBaseWrappers);
  }

  public List<EnrichmentResultBaseWrapper> getEnrichmentBaseResultWrapperList() {
    return enrichmentResultBaseWrapperList;
  }
}
