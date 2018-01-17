package eu.europeana.metis.common.model;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Concept", namespace = "http://www.europeana.eu/schemas/edm/")
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

  public Concept(){ }

  public List<Label> getHiddenLabel() {
    return hiddenLabel;
  }

  public void setHiddenLabel(List<Label> hiddenLabel) {
    this.hiddenLabel = hiddenLabel;
  }

  public List<Label> getNotation() {
    return notation;
  }

  public void setNotation(List<Label> notation) {
    this.notation = notation;
  }


   public List<Resource> getBroader() {
    return broader;
  }

  public void setBroader(List<Resource> broader) {
    this.broader = broader;
  }

  public List<Resource> getBroadMatch() {
    return broadMatch;
  }

  public void setBroadMatch(List<Resource> broader) {
    this.broadMatch = broader;
  }

  public List<Resource> getCloseMatch() {
    return closeMatch;
  }

  public void setCloseMatch(List<Resource> closeMatch) {
    this.closeMatch = closeMatch;
  }

 public List<Resource> getExactMatch() {
    return exactMatch;
  }

  public void setExactMatch(List<Resource> exactMatch) {
    this.exactMatch = exactMatch;
  }

  public List<Resource> getInScheme() {
    return inScheme;
  }

  public void setInScheme(List<Resource> inScheme) {
    this.inScheme = inScheme;
  }

  public List<Resource> getNarrower() {
    return narrower;
  }

  public void setNarrower(List<Resource> narrower) {
    this.narrower = narrower;
  }

  public List<Resource> getNarrowMatch() {
    return narrowMatch;
  }

  public void setNarrowMatch(List<Resource> narrowMatch) {
    this.narrowMatch = narrowMatch;
  }

  public List<Resource> getRelated() {
    return related;
  }

  public void setRelated(List<Resource> related) {
    this.related = related;
  }

  public List<Resource> getRelatedMatch() {
    return relatedMatch;
  }

  public void setRelatedMatch(List<Resource> relatedMatch) {
    this.relatedMatch = relatedMatch;
  }
}

//  ConceptImpl concept = new ObjectMapper().readValue(contextualEntity,
//      ConceptImpl.class);
//  StringBuilder sb = new StringBuilder();
//    sb.append("<skos:Concept rdf:about=\"");
//        sb.append(concept.getAbout());
//        sb.append("\"/>\n");
//        addMap(sb, concept.getPrefLabel(), "skos:prefLabel", "xml:lang", false);
//        addMap(sb, concept.getAltLabel(), "skos:altLabel", "xml:lang", false);
//        addMap(sb, concept.getHiddenLabel(), "skos:hiddenLabel", "xml:lang",
//        false);
//        addMap(sb, concept.getNotation(), "skos:notation", "xml:lang", false);
//        addMap(sb, concept.getNote(), "skos:note", "xml:lang", false);
//        addArray(sb, concept.getBroader(), "skos:broader", "rdf:resource");
//        addArray(sb, concept.getBroadMatch(), "skos:broadMatch", "rdf:resource");
//        addArray(sb, concept.getCloseMatch(), "skos:closeMatch", "rdf:resource");
//        addArray(sb, concept.getExactMatch(), "skos:exactMatch", "rdf:resource");
//        addArray(sb, concept.getInScheme(), "skos:inScheme", "rdf:resource");
//        addArray(sb, concept.getNarrower(), "skos:narrower", "rdf:resource");
//        addArray(sb, concept.getNarrowMatch(), "skos:narrowMatch",
//        "rdf:resource");
//        addArray(sb, concept.getRelated(), "skos:related", "rdf:resource");
//        addArray(sb, concept.getRelatedMatch(), "skos:relatedMatch",
//        "rdf:resource");
//        sb.append("</skos:Concept>\n");