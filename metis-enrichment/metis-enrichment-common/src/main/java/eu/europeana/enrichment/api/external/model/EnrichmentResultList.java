package eu.europeana.enrichment.api.external.model;

import java.util.ArrayList;
import java.util.Collection;
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

  @XmlElement(name = "enrichmentBaseWrapperList", type = EnrichmentBaseWrapper.class)
  private final List<EnrichmentBaseWrapper> enrichmentBaseWrapperList = new ArrayList<>();

  public EnrichmentResultList() {
  }

  /**
   * Constructor with initial {@link EnrichmentBase} list.
   *
   * @param enrichmentBaseWrapperList the list to initialize the class with
   */
  public EnrichmentResultList(Collection<EnrichmentBaseWrapper> enrichmentBaseWrapperList) {
    this.enrichmentBaseWrapperList.addAll(enrichmentBaseWrapperList);
  }

  public List<EnrichmentBaseWrapper> getEnrichmentBaseWrapperList() {
    return enrichmentBaseWrapperList;
  }
}
