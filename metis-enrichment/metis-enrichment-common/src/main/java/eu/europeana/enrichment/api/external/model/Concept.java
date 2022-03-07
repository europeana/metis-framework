package eu.europeana.enrichment.api.external.model;

import eu.europeana.enrichment.utils.EntityXmlUtils;

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
  @XmlElement(name = "relatedMatch", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Resource> relatedMatch;

  /**
   * Default constructor.
   */
  public Concept() {
    super();
    // Required for XML binding.
  }

  // Used for creating XML entity from EM model class
  public Concept(eu.europeana.entitymanagement.definitions.model.Concept concept) {
    super(concept);
    init(concept);
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

  private void init(eu.europeana.entitymanagement.definitions.model.Concept concept){
    if (concept.getHiddenLabel() != null) {
      this.hiddenLabel = EntityXmlUtils.convertListToXmlLabel(concept.getHiddenLabel());
    }
    if (concept.getNotation() != null) {
      this.notation = EntityXmlUtils.convertMultilingualMapToXmlLabel(concept.getNotation());
    }
    if (concept.getBroader() != null) {
      this.broader = EntityXmlUtils.convertListToXmlResource(concept.getBroader());
    }
    if (concept.getBroadMatch() != null) {
      this.broadMatch = EntityXmlUtils.convertListToXmlResource(concept.getBroadMatch());
    }
    if (concept.getCloseMatch() != null) {
      this.closeMatch = EntityXmlUtils.convertListToXmlResource(concept.getCloseMatch());
    }
    if (concept.getSameReferenceLinks() != null) {
      this.exactMatch = EntityXmlUtils.convertListToXmlResource(concept.getSameReferenceLinks());
    }
    if (concept.getInScheme() != null) {
      this.inScheme = EntityXmlUtils.convertListToXmlResource(concept.getInScheme());
    }
    if (concept.getNarrower() != null) {
      this.narrower = EntityXmlUtils.convertListToXmlResource(concept.getNarrower());
    }
    if (concept.getNarrowMatch() != null) {
      this.narrowMatch = EntityXmlUtils.convertListToXmlResource(concept.getNarrowMatch());
    }
    if (concept.getRelated() != null) {
      this.related = EntityXmlUtils.convertListToXmlResource(concept.getRelated());
    }
    if (concept.getRelatedMatch() != null) {
      this.relatedMatch = EntityXmlUtils.convertListToXmlResource(concept.getRelatedMatch());
    }
  }
}
