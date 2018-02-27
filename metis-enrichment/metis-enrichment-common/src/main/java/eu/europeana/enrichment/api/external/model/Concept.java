package eu.europeana.enrichment.api.external.model;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://www.europeana.eu/schemas/edm/", name = "Concept")
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

  public Concept() {
    // Required for XML binding.
  }

  public List<Label> getHiddenLabel() {
    return cloneListForGetting(hiddenLabel);
  }

  public void setHiddenLabel(List<Label> hiddenLabel) {
    this.hiddenLabel = cloneListForSetting(hiddenLabel);
  }

  public List<Label> getNotation() {
    return cloneListForGetting(notation);
  }

  public void setNotation(List<Label> notation) {
    this.notation = cloneListForSetting(notation);
  }

  public List<Resource> getBroader() {
    return cloneListForGetting(broader);
  }

  public void setBroader(List<Resource> broader) {
    this.broader = cloneListForSetting(broader);
  }

  public List<Resource> getBroadMatch() {
    return cloneListForGetting(broadMatch);
  }

  public void setBroadMatch(List<Resource> broader) {
    this.broadMatch = cloneListForSetting(broader);
  }

  public List<Resource> getCloseMatch() {
    return cloneListForGetting(closeMatch);
  }

  public void setCloseMatch(List<Resource> closeMatch) {
    this.closeMatch = cloneListForSetting(closeMatch);
  }

  public List<Resource> getExactMatch() {
    return cloneListForGetting(exactMatch);
  }

  public void setExactMatch(List<Resource> exactMatch) {
    this.exactMatch = cloneListForSetting(exactMatch);
  }

  public List<Resource> getInScheme() {
    return cloneListForGetting(inScheme);
  }

  public void setInScheme(List<Resource> inScheme) {
    this.inScheme = cloneListForSetting(inScheme);
  }

  public List<Resource> getNarrower() {
    return cloneListForGetting(narrower);
  }

  public void setNarrower(List<Resource> narrower) {
    this.narrower = cloneListForSetting(narrower);
  }

  public List<Resource> getNarrowMatch() {
    return cloneListForGetting(narrowMatch);
  }

  public void setNarrowMatch(List<Resource> narrowMatch) {
    this.narrowMatch = cloneListForSetting(narrowMatch);
  }

  public List<Resource> getRelated() {
    return cloneListForGetting(related);
  }

  public void setRelated(List<Resource> related) {
    this.related = cloneListForSetting(related);
  }

  public List<Resource> getRelatedMatch() {
    return cloneListForGetting(relatedMatch);
  }

  public void setRelatedMatch(List<Resource> relatedMatch) {
    this.relatedMatch = cloneListForSetting(relatedMatch);
  }
}
