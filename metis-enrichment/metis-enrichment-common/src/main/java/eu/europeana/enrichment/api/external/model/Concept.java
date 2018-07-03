package eu.europeana.enrichment.api.external.model;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Model class of a Concept(for example "painting")
 */
@XmlRootElement(namespace = "http://www.w3.org/2004/02/skos/core#", name = "Concept")
@XmlAccessorType(XmlAccessType.FIELD)
public class Concept extends EnrichmentBase {

  @XmlElement(name = "hiddenLabel", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Label> hiddenLabel;
  @XmlElement(name = "notation", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Label> notation;
  @XmlElement(name = "broader", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Resource> broader;
  @XmlElement(name = "broadMatch", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Resource> broadMatch;
  @XmlElement(name = "closeMatch", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Resource> closeMatch;
  @XmlElement(name = "exactMatch", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Resource> exactMatch;
  @XmlElement(name = "inScheme", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Resource> inScheme;
  @XmlElement(name = "narrower", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Resource> narrower;
  @XmlElement(name = "narrowMatch", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Resource> narrowMatch;
  @XmlElement(name = "related", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Resource> related;
  @XmlElement(name = "related", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Resource> relatedMatch;

  /**
   * Default constructor.
   */
  public Concept() {
    super();
    // Required for XML binding.
  }

  public List<Label> getHiddenLabel() {
    return unmodifiableListAcceptingNull(hiddenLabel);
  }

  public void setHiddenLabel(List<Label> hiddenLabel) {
    this.hiddenLabel = cloneListAcceptingNull(hiddenLabel);
  }

  public List<Label> getNotation() {
    return unmodifiableListAcceptingNull(notation);
  }

  public void setNotation(List<Label> notation) {
    this.notation = cloneListAcceptingNull(notation);
  }

  public List<Resource> getBroader() {
    return unmodifiableListAcceptingNull(broader);
  }

  public void setBroader(List<Resource> broader) {
    this.broader = cloneListAcceptingNull(broader);
  }

  public List<Resource> getBroadMatch() {
    return unmodifiableListAcceptingNull(broadMatch);
  }

  public void setBroadMatch(List<Resource> broader) {
    this.broadMatch = cloneListAcceptingNull(broader);
  }

  public List<Resource> getCloseMatch() {
    return unmodifiableListAcceptingNull(closeMatch);
  }

  public void setCloseMatch(List<Resource> closeMatch) {
    this.closeMatch = cloneListAcceptingNull(closeMatch);
  }

  public List<Resource> getExactMatch() {
    return unmodifiableListAcceptingNull(exactMatch);
  }

  public void setExactMatch(List<Resource> exactMatch) {
    this.exactMatch = cloneListAcceptingNull(exactMatch);
  }

  public List<Resource> getInScheme() {
    return unmodifiableListAcceptingNull(inScheme);
  }

  public void setInScheme(List<Resource> inScheme) {
    this.inScheme = cloneListAcceptingNull(inScheme);
  }

  public List<Resource> getNarrower() {
    return unmodifiableListAcceptingNull(narrower);
  }

  public void setNarrower(List<Resource> narrower) {
    this.narrower = cloneListAcceptingNull(narrower);
  }

  public List<Resource> getNarrowMatch() {
    return unmodifiableListAcceptingNull(narrowMatch);
  }

  public void setNarrowMatch(List<Resource> narrowMatch) {
    this.narrowMatch = cloneListAcceptingNull(narrowMatch);
  }

  public List<Resource> getRelated() {
    return unmodifiableListAcceptingNull(related);
  }

  public void setRelated(List<Resource> related) {
    this.related = cloneListAcceptingNull(related);
  }

  public List<Resource> getRelatedMatch() {
    return unmodifiableListAcceptingNull(relatedMatch);
  }

  public void setRelatedMatch(List<Resource> relatedMatch) {
    this.relatedMatch = cloneListAcceptingNull(relatedMatch);
  }
}
