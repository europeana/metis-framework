package eu.europeana.metis.common.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({Agent.class, Concept.class, Place.class, Timespan.class})
public class EnrichmentBase {
  @XmlElement(name = "altLabel", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Label> altLabelList = new ArrayList<>();
  @XmlAttribute(name = "about", namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
  private String about;
  @XmlElement(name = "prefLabel", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Label> prefLabelList = new ArrayList<>();
  @XmlElement(name = "note", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Label> notes = new ArrayList<>();

  public String getAbout() {
    return about;
  }

  public void setAbout(String about) {
    this.about = about;
  }

  public List<Label> getPrefLabelList() {
    return prefLabelList;
  }

  public void setPrefLabelList(List<Label> prefLabelList) {
    this.prefLabelList = prefLabelList;
  }

  public List<Label> getAltLabelList() {
    return altLabelList;
  }
  public void setAltLabelList(List<Label> altLabelList) {
    this.altLabelList = altLabelList;
  }

  public List<Label> getNotes() {
    return notes;
  }

  public void setNotes(List<Label> notes) {
    this.notes = notes;
  }
}
