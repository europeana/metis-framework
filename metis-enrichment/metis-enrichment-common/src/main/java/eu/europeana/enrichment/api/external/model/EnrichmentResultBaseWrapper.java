package eu.europeana.enrichment.api.external.model;

import eu.europeana.enrichment.api.external.DereferenceResultStatus;
import java.util.ArrayList;
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
      @XmlElement(name = "Organization", namespace = "http://xmlns.com/foaf/0.1/", type = Organization.class),
      @XmlElement(name = "Concept", namespace = "http://www.w3.org/2004/02/skos/core#", type = Concept.class),
      @XmlElement(name = "Agent", namespace = "http://www.europeana.eu/schemas/edm/", type = Agent.class),
      @XmlElement(name = "Place", namespace = "http://www.europeana.eu/schemas/edm/", type = Place.class),
      @XmlElement(name = "TimeSpan", namespace = "http://www.europeana.eu/schemas/edm/", type = TimeSpan.class)})
  private List<EnrichmentBase> enrichmentBase = new ArrayList<>();

  private DereferenceResultStatus dereferenceStatus;

  /**
   * Default Constructor
   */
  public EnrichmentResultBaseWrapper() {
    this.dereferenceStatus = DereferenceResultStatus.SUCCESS;
  }

  /**
   * Constructor with all fields
   *
   * @param enrichmentBase the enrichment information class generated
   * @param dereferenceStatus the status of the dereference process
   */
  public EnrichmentResultBaseWrapper(List<EnrichmentBase> enrichmentBase, DereferenceResultStatus dereferenceStatus) {
    this.enrichmentBase = new ArrayList<>(enrichmentBase);
    this.dereferenceStatus = dereferenceStatus;
  }

  public List<EnrichmentBase> getEnrichmentBaseList() {
    return new ArrayList<>(enrichmentBase);
  }

  /**
   * Convert a collection of {@link EnrichmentBase} to a list of {@link
   * EnrichmentResultBaseWrapper}.
   * <p>This is mostly used for dereferencing.</p>
   *
   * @param resultList the collection of {@link EnrichmentBase}
   * @param dereferenceStatus the status of dereferencing process
   * @return the converted list
   */
  public static List<EnrichmentResultBaseWrapper> createEnrichmentResultBaseWrapperList(
      Collection<List<EnrichmentBase>> resultList, DereferenceResultStatus dereferenceStatus) {
    return resultList.stream().map( item -> new EnrichmentResultBaseWrapper(item, dereferenceStatus)).collect(Collectors.toList());
  }

  public void setDereferenceStatus(DereferenceResultStatus dereferenceStatus) {
    this.dereferenceStatus = dereferenceStatus;
  }

  public DereferenceResultStatus getDereferenceStatus() {
    return dereferenceStatus;
  }
}
