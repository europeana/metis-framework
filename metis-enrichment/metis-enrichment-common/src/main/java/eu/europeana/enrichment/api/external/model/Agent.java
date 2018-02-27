package eu.europeana.enrichment.api.external.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "http://www.europeana.eu/schemas/edm/", name = "Agent")
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
    return cloneListForGetting(hiddenLabel);
  }

  public void setHiddenLabel(List<Label> hiddenLabel) {
    this.hiddenLabel = cloneListForSetting(hiddenLabel);
  }

  public List<Label> getFoafName() {
    return cloneListForGetting(name);
  }

  public void setFoafName(List<Label> foafName) {
    this.name = cloneListForSetting(foafName);
  }

  public List<Label> getBeginList() {
    return cloneListForGetting(beginList);
  }

  public void setBeginList(List<Label> beginList) {
    this.beginList = cloneListForSetting(beginList);
  }

  public List<Label> getEndList() {
    return cloneListForGetting(endList);
  }

  public void setEndList(List<Label> endList) {
    this.endList = cloneListForSetting(endList);
  }

  public List<Label> getIdentifier() {
    return cloneListForGetting(identifier);
  }

  public void setIdentifier(List<Label> identifier) {
    this.identifier = cloneListForSetting(identifier);
  }

  public List<Label> getHasMet() {
    return cloneListForGetting(hasMet);
  }

  public void setHasMet(List<Label> hasMet) {
    this.hasMet = cloneListForSetting(hasMet);
  }

  public List<Label> getBiographicaInformation() {
    return cloneListForGetting(biographicaInformation);
  }

  public void setBiographicaInformation(List<Label> biographicaInformation) {
    this.biographicaInformation = cloneListForSetting(biographicaInformation);
  }

  public List<Label> getDateOfBirth() {
    return cloneListForGetting(dateOfBirth);
  }

  public void setDateOfBirth(List<Label> dateOfBirth) {
    this.dateOfBirth = cloneListForSetting(dateOfBirth);
  }

  public List<Label> getDateOfDeath() {
    return cloneListForGetting(dateOfDeath);
  }

  public void setDateOfDeath(List<Label> dateOfDeath) {
    this.dateOfDeath = cloneListForSetting(dateOfDeath);
  }

  public List<Label> getDateOfEstablishment() {
    return cloneListForGetting(dateOfEstablishment);
  }

  public void setDateOfEstablishment(List<Label> dateOfEstablishment) {
    this.dateOfEstablishment = cloneListForSetting(dateOfEstablishment);
  }

  public List<Label> getDateOfTermination() {
    return cloneListForGetting(dateOfTermination);
  }

  public void setDateOfTermination(List<Label> dateOfTermination) {
    this.dateOfTermination = cloneListForSetting(dateOfTermination);
  }

  public List<Label> getGender() {
    return cloneListForGetting(gender);
  }

  public void setGender(List<Label> gender) {
    this.gender = cloneListForSetting(gender);
  }

  public List<LabelResource> getProfessionOrOccupation() {
    return cloneListForGetting(professionOrOccupation);
  }

  public void setProfessionOrOccupation(List<LabelResource> professionOrOccupation) {
    this.professionOrOccupation = cloneListForSetting(professionOrOccupation);
  }

  public List<LabelResource> getDate() {
    return cloneListForGetting(date);
  }

  public void setDate(List<LabelResource> date) {
    this.date = cloneListForSetting(date);
  }

  public List<LabelResource> getIsRelatedTo() {
    return cloneListForGetting(isRelatedTo);
  }

  public void setIsRelatedTo(List<LabelResource> isRelatedTo) {
    this.isRelatedTo = cloneListForSetting(isRelatedTo);
  }

  public List<Resource> getWasPresentAt() {
    return cloneListForGetting(wasPresentAt);
  }

  public void setWasPresentAt(List<Resource> wasPresentAt) {
    this.wasPresentAt = cloneListForSetting(wasPresentAt);
  }

  public List<Part> getSameAs() {
    return cloneListForGetting(sameAs);
  }

  public void setSameAs(List<Part> sameAs) {
    this.sameAs = cloneListForSetting(sameAs);
  }
}
