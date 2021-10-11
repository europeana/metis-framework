package eu.europeana.enrichment.api.external.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlTransient
@XmlSeeAlso({Agent.class, Organization.class})
public abstract class AgentBase extends EnrichmentBase {

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
  private List<Resource> hasMet = new ArrayList<>();
  @XmlElement(name = "biographicalInformation", namespace = "http://rdvocab.info/ElementsGr2/")
  private List<LabelResource> biographicalInformation = new ArrayList<>();
  @XmlElement(name = "placeOfBirth", namespace = "http://rdvocab.info/ElementsGr2/")
  private List<LabelResource> placeOfBirth = new ArrayList<>();
  @XmlElement(name = "placeOfDeath", namespace = "http://rdvocab.info/ElementsGr2/")
  private List<LabelResource> placeOfDeath = new ArrayList<>();
  @XmlElement(name = "dateOfBirth", namespace = "http://rdvocab.info/ElementsGr2/")
  private List<Label> dateOfBirth = new ArrayList<>();
  @XmlElement(name = "dateOfDeath", namespace = "http://rdvocab.info/ElementsGr2/")
  private List<Label> dateOfDeath = new ArrayList<>();
  @XmlElement(name = "dateOfEstablishment", namespace = "http://rdvocab.info/ElementsGr2/")
  private List<Label> dateOfEstablishment = new ArrayList<>();
  @XmlElement(name = "dateOfTermination", namespace = "http://rdvocab.info/ElementsGr2/")
  private List<Label> dateOfTermination = new ArrayList<>();
  @XmlElement(name = "gender", namespace = "http://rdvocab.info/ElementsGr2/")
  private List<Label> gender = new ArrayList<>();
  @XmlElement(name = "professionOrOccupation", namespace = "http://rdvocab.info/ElementsGr2/")
  private List<LabelResource> professionOrOccupation = new ArrayList<>();
  @XmlElement(name = "date", namespace = "http://purl.org/dc/elements/1.1/")
  private List<LabelResource> date = new ArrayList<>();
  @XmlElement(name = "isRelatedTo", namespace = "http://www.europeana.eu/schemas/edm/")
  private List<LabelResource> isRelatedTo = new ArrayList<>();
  // Note: this property is not part of the EDM Agent type according to metis-schema.
  @XmlElement(name = "wasPresentAt", namespace = "http://www.europeana.eu/schemas/edm/")
  private List<Resource> wasPresentAt = new ArrayList<>();
  @XmlElement(name = "sameAs", namespace = "http://www.w3.org/2002/07/owl#")
  private List<Part> sameAs = new ArrayList<>();

  public List<Label> getHiddenLabel() {
    return unmodifiableListAcceptingNull(hiddenLabel);
  }

  public void setHiddenLabel(List<Label> hiddenLabel) {
    this.hiddenLabel = cloneListAcceptingNull(hiddenLabel);
  }

  public List<Label> getFoafName() {
    return unmodifiableListAcceptingNull(name);
  }

  public void setFoafName(List<Label> foafName) {
    this.name = cloneListAcceptingNull(foafName);
  }

  public List<Label> getBeginList() {
    return unmodifiableListAcceptingNull(beginList);
  }

  public void setBeginList(List<Label> beginList) {
    this.beginList = cloneListAcceptingNull(beginList);
  }

  public List<Label> getEndList() {
    return unmodifiableListAcceptingNull(endList);
  }

  public void setEndList(List<Label> endList) {
    this.endList = cloneListAcceptingNull(endList);
  }

  public List<Label> getIdentifier() {
    return unmodifiableListAcceptingNull(identifier);
  }

  public void setIdentifier(List<Label> identifier) {
    this.identifier = cloneListAcceptingNull(identifier);
  }

  public List<Resource> getHasMet() {
    return unmodifiableListAcceptingNull(hasMet);
  }

  public void setHasMet(List<Resource> hasMet) {
    this.hasMet = cloneListAcceptingNull(hasMet);
  }

  public List<LabelResource> getBiographicalInformation() {
    return unmodifiableListAcceptingNull(biographicalInformation);
  }

  public void setBiographicalInformation(List<LabelResource> biographicalInformation) {
    this.biographicalInformation = cloneListAcceptingNull(biographicalInformation);
  }

  public List<LabelResource> getPlaceOfBirth() {
    return unmodifiableListAcceptingNull(placeOfBirth);
  }

  public void setPlaceOfBirth(List<LabelResource> placeOfBirth) {
    this.placeOfBirth = cloneListAcceptingNull(placeOfBirth);
  }

  public List<LabelResource> getPlaceOfDeath() {
    return unmodifiableListAcceptingNull(placeOfDeath);
  }

  public void setPlaceOfDeath(List<LabelResource> placeOfDeath) {
    this.placeOfDeath = cloneListAcceptingNull(placeOfDeath);
  }

  public List<Label> getDateOfBirth() {
    return unmodifiableListAcceptingNull(dateOfBirth);
  }

  public void setDateOfBirth(List<Label> dateOfBirth) {
    this.dateOfBirth = cloneListAcceptingNull(dateOfBirth);
  }

  public List<Label> getDateOfDeath() {
    return unmodifiableListAcceptingNull(dateOfDeath);
  }

  public void setDateOfDeath(List<Label> dateOfDeath) {
    this.dateOfDeath = cloneListAcceptingNull(dateOfDeath);
  }

  public List<Label> getDateOfEstablishment() {
    return unmodifiableListAcceptingNull(dateOfEstablishment);
  }

  public void setDateOfEstablishment(List<Label> dateOfEstablishment) {
    this.dateOfEstablishment = cloneListAcceptingNull(dateOfEstablishment);
  }

  public List<Label> getDateOfTermination() {
    return unmodifiableListAcceptingNull(dateOfTermination);
  }

  public void setDateOfTermination(List<Label> dateOfTermination) {
    this.dateOfTermination = cloneListAcceptingNull(dateOfTermination);
  }

  public List<Label> getGender() {
    return unmodifiableListAcceptingNull(gender);
  }

  public void setGender(List<Label> gender) {
    this.gender = cloneListAcceptingNull(gender);
  }

  public List<LabelResource> getProfessionOrOccupation() {
    return unmodifiableListAcceptingNull(professionOrOccupation);
  }

  public void setProfessionOrOccupation(List<LabelResource> professionOrOccupation) {
    this.professionOrOccupation = cloneListAcceptingNull(professionOrOccupation);
  }

  public List<LabelResource> getDate() {
    return unmodifiableListAcceptingNull(date);
  }

  public void setDate(List<LabelResource> date) {
    this.date = cloneListAcceptingNull(date);
  }

  public List<LabelResource> getIsRelatedTo() {
    return unmodifiableListAcceptingNull(isRelatedTo);
  }

  public void setIsRelatedTo(List<LabelResource> isRelatedTo) {
    this.isRelatedTo = cloneListAcceptingNull(isRelatedTo);
  }

  public List<Resource> getWasPresentAt() {
    return unmodifiableListAcceptingNull(wasPresentAt);
  }

  public void setWasPresentAt(List<Resource> wasPresentAt) {
    this.wasPresentAt = cloneListAcceptingNull(wasPresentAt);
  }

  public List<Part> getSameAs() {
    return unmodifiableListAcceptingNull(sameAs);
  }

  public void setSameAs(List<Part> sameAs) {
    this.sameAs = cloneListAcceptingNull(sameAs);
  }
}
