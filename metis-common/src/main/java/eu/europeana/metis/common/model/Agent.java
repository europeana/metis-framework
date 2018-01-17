package eu.europeana.metis.common.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Agent", namespace = "http://www.europeana.eu/schemas/edm/")
@XmlAccessorType(XmlAccessType.FIELD)
public class Agent extends EnrichmentBase {

  @XmlElement(name = "hiddenLabel", namespace = "http://www.w3.org/2004/02/skos/core#")
  private List<Label> hiddenLabel = new ArrayList<>();
  @XmlElement(name = "name", namespace = "http://xmlns.com/foaf/0.1/")
  private List<Label> name = new ArrayList<>();
  @XmlElement(name = "begin", namespace = "http://www.europeana.eu/schemas/edm/")
  private List<Label> beginList = new ArrayList<>();
  @XmlElement(name = "end", namespace = "http://www.europeana.eu/schemas/edm/")
  private List<Label> endList = new ArrayList<>();
  @XmlElement(name = "identifier", namespace = "http://purl.org/dc/elements/1.1/")
  private List<Label> identifier = new ArrayList<>();
  @XmlElement(name = "hasMet", namespace = "http://www.europeana.eu/schemas/edm/")
  private List<Label> hasMet = new ArrayList<>();
  @XmlElement(name = "biographicaInformation", namespace = "http://RDVocab.info/ElementsGr2/")
  private List<Label> biographicaInformation = new ArrayList<>();
  @XmlElement(name = "dateOfBirth", namespace = "http://RDVocab.info/ElementsGr2/")
  private List<Label> dateOfBirth = new ArrayList<>();
  @XmlElement(name = "dateOfDeath", namespace = "http://RDVocab.info/ElementsGr2/")
  private List<Label> dateOfDeath = new ArrayList<>();
  @XmlElement(name = "dateOfEstablishment", namespace = "http://RDVocab.info/ElementsGr2/")
  private List<Label> dateOfEstablishment = new ArrayList<>();
  @XmlElement(name = "dateOfTermination", namespace = "http://RDVocab.info/ElementsGr2/")
  private List<Label> dateOfTermination = new ArrayList<>();
  @XmlElement(name = "gender", namespace = "http://RDVocab.info/ElementsGr2/")
  private List<Label> gender = new ArrayList<>();
  @XmlElement(name = "professionOrOccupation", namespace = "http://RDVocab.info/ElementsGr2/")
  private List<LabelResource> professionOrOccupation = new ArrayList<>();
  @XmlElement(name = "date", namespace = "http://purl.org/dc/elements/1.1/")
  private List<LabelResource> date = new ArrayList<>();
  @XmlElement(name = "isRelatedTo", namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
  private List<LabelResource> isRelatedTo = new ArrayList<>();
  @XmlElement(name = "wasPresentAt", namespace = "http://www.europeana.eu/schemas/edm/")
  private List<Resource> wasPresentAt = new ArrayList<>();
  @XmlElement(name = "sameAs", namespace = "http://www.w3.org/2002/07/owl#")
  private List<Part> sameAs = new ArrayList<>();

  public List<Label> getHiddenLabel() {
    return hiddenLabel;
  }

  public void setHiddenLabel(List<Label> hiddenLabel) {
    this.hiddenLabel = hiddenLabel;
  }

  public List<Label> getFoafName() {
    return name;
  }

  public void setFoafName(List<Label> foafName) {
    this.name = foafName;
  }

  public List<Label> getBeginList() {
    return beginList;
  }

  public void setBeginList(List<Label> beginList) {
    this.beginList = beginList;
  }

  public List<Label> getEndList() {
    return endList;
  }

  public void setEndList(List<Label> endList) {
    this.endList = endList;
  }


  public List<Label> getIdentifier() {
    return identifier;
  }

  public void setIdentifier(List<Label> identifier) {
    this.identifier = identifier;
  }


  public List<Label> getHasMet() {
    return hasMet;
  }

  public void setHasMet(List<Label> hasMet) {
    this.hasMet = hasMet;
  }


  public List<Label> getBiographicaInformation() {
    return biographicaInformation;
  }

  public void setBiographicaInformation(List<Label> biographicaInformation) {
    this.biographicaInformation = biographicaInformation;
  }


  public List<Label> getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(List<Label> dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }


  public List<Label> getDateOfDeath() {
    return dateOfDeath;
  }

  public void setDateOfDeath(List<Label> dateOfDeath) {
    this.dateOfDeath = dateOfDeath;
  }


  public List<Label> getDateOfEstablishment() {
    return dateOfEstablishment;
  }

  public void setDateOfEstablishment(List<Label> dateOfEstablishment) {
    this.dateOfEstablishment = dateOfEstablishment;
  }

  public List<Label> getDateOfTermination() {
    return dateOfTermination;
  }

  public void setDateOfTermination(List<Label> dateOfTermination) {
    this.dateOfTermination = dateOfTermination;
  }

 public List<Label> getGender() {
    return gender;
  }

  public void setGender(List<Label> gender) {
    this.gender = gender;
  }

  public List<LabelResource> getProfessionOrOccupation() {
    return professionOrOccupation;
  }

  public void setProfessionOrOccupation(List<LabelResource> professionOrOccupation) {
    this.professionOrOccupation = professionOrOccupation;
  }

  public List<LabelResource> getDate() {
    return date;
  }

  public void setDate(List<LabelResource> date) {
    this.date = date;
  }

  public List<LabelResource> getIsRelatedTo() {
    return isRelatedTo;
  }

  public void setIsRelatedTo(List<LabelResource> isRelatedTo) {
    this.isRelatedTo = isRelatedTo;
  }

  public List<Resource> getWasPresentAt() {
    return wasPresentAt;
  }

  public void setWasPresentAt(List<Resource> wasPresentAt) {
    this.wasPresentAt = wasPresentAt;
  }


  public List<Part> getSameAs() {
    return sameAs;
  }

  public void setSameAs(List<Part> sameAs) {
    this.sameAs = sameAs;
  }
}

//  private String convertAgent(String contextualEntity)
//      throws IOException {
//    AgentImpl agent = new ObjectMapper().readValue(contextualEntity,
//        AgentImpl.class);
//    StringBuilder sb = new StringBuilder();
//    sb.append("<edm:Agent rdf:about=\"");
//    sb.append(agent.getAbout());
//    sb.append("\">");
//    addMap(sb, agent.getPrefLabel(), "skos:prefLabel", "xml:lang", false);
//    addMap(sb, agent.getAltLabel(), "skos:altLabel", "xml:lang", false);
//    addMap(sb, agent.getHiddenLabel(), "skos:hiddenLabel", "xml:lang",
//        false);
//    addMap(sb, agent.getFoafName(), "foaf:name", "xml:lang", false);
//    addMap(sb, agent.getNote(), "skos:note", "xml:lang", false);
//    addMap(sb, agent.getBegin(), "edm:begin", "xml:lang", false);
//    addMap(sb, agent.getEnd(), "edm:end", "xml:lang", false);
//    addMap(sb, agent.getDcIdentifier(), "dc:identifier", "xml:lang", false);
//    addMap(sb, agent.getEdmHasMet(), "edm:hasMet", "xml:lang", false);
//    addMap(sb, agent.getDcIdentifier(), "dc:identifier", "xml:lang", false);

//    addMap(sb, agent.getRdaGr2BiographicalInformation(),
//        "rdaGr2:biographicaInformation", "xml:lang", false);
//    addMap(sb, agent.getRdaGr2DateOfBirth(), "rdaGr2:dateOfBirth",
//        "xml:lang", false);
//    addMap(sb, agent.getRdaGr2DateOfDeath(), "rdaGr2:dateOfDeath",
//        "xml:lang", false);
//    addMap(sb, agent.getRdaGr2DateOfEstablishment(),
//        "rdaGr2:dateOfEstablishment", "xml:lang", false);
//    addMap(sb, agent.getRdaGr2DateOfTermination(),
//        "rdaGr2:dateOfTermination", "xml:lang", false);
//    addMap(sb, agent.getRdaGr2Gender(), "rdaGr2:gender", "xml:lang", false);
//
//    addMapResourceOrLiteral(sb, agent.getRdaGr2ProfessionOrOccupation(),
//        "rdaGr2:professionOrOccupation");

//    addMapResourceOrLiteral(sb, agent.getDcDate(), "dc:date");
//    addMapResourceOrLiteral(sb, agent.getEdmIsRelatedTo(),
//        "edm:isRelatedTo");

//    addArray(sb, agent.getEdmWasPresentAt(), "edm:wasPresentAt",
//        "rdf:resource");

//    addArray(sb, agent.getOwlSameAs(), "owl:sameAs", "rdf:resource");
//    sb.append("</edm:Agent>\n");
//    LOGGER.info(StringEscapeUtils.escapeXml(sb.toString()));
//    return StringEscapeUtils.escapeHtml3(sb.toString());
//  }

//  private void addMapResourceOrLiteral(StringBuilder sb,
//      Map<String, List<String>> values, String element) {
//
//    if (values != null) {
//      for (Entry<String, List<String>> entry : values.entrySet()) {
//        for (String str : entry.getValue()) {
//          sb.append("<");
//          sb.append(element);
//          sb.append(" ");
//          if (isUri(str)) {
//            sb.append("rdf:resource=\"");
//            sb.append(str);
//            sb.append("\"/>\n");
//          } else {
//            sb.append("xml:lang=\"");
//            sb.append(entry.getKey());
//            sb.append("\">");
//            sb.append(str);
//            sb.append("</");
//            sb.append(element);
//            sb.append(">\n");
//          }
//        }
//      }
//    }
//  }
