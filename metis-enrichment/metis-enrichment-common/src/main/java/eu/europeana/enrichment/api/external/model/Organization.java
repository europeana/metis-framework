package eu.europeana.enrichment.api.external.model;

import static eu.europeana.enrichment.utils.EntityValuesConverter.convertListToLabel;
import static eu.europeana.enrichment.utils.EntityValuesConverter.convertListToLabelResource;
import static eu.europeana.enrichment.utils.EntityValuesConverter.convertListToPart;
import static eu.europeana.enrichment.utils.EntityValuesConverter.convertMapToLabels;
import static eu.europeana.enrichment.utils.EntityValuesConverter.convertMultilingualMapToLabel;
import static eu.europeana.enrichment.utils.EntityValuesConverter.getVcardAddresses;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * This class stores result of the parsing of XSLT/XML organization file with Wikidata content
 *
 */
@XmlRootElement(namespace = "http://xmlns.com/foaf/0.1/", name = "Organization")
@XmlAccessorType(XmlAccessType.FIELD)
public class Organization extends AgentBase {

  @XmlElement(name = "country", namespace = "http://www.europeana.eu/schemas/edm/")
  private String country;

  @XmlElement(name = "homepage", namespace = "http://xmlns.com/foaf/0.1/")
  private Resource homepage;

  // Note: this property is not part of the FOAF Organization type according to metis-schema.
  @XmlElement(name = "description", namespace = "http://purl.org/dc/elements/1.1/")
  private List<Label> descriptions = new ArrayList<>();

  @XmlElement(name = "acronym", namespace = "http://www.europeana.eu/schemas/edm/")
  private List<Label> acronyms = new ArrayList<>();

  @XmlElement(name = "logo", namespace = "http://xmlns.com/foaf/0.1/")
  private Resource logo;

  // Note: this property is not part of the FOAF Organization type according to metis-schema.
  @XmlElement(name = "depiction", namespace = "http://xmlns.com/foaf/0.1/")
  private Resource depiction;

  // Note: this property is not part of the FOAF Organization type according to metis-schema.
  @XmlElement(name = "phone", namespace = "http://xmlns.com/foaf/0.1/")
  private String phone;

  // Note: this property is not part of the FOAF Organization type according to metis-schema.
  @XmlElement(name = "mbox", namespace = "http://xmlns.com/foaf/0.1/")
  private String mbox;

  // Note: this property is not part of the FOAF Organization type according to metis-schema.
  @XmlElement(name = "hasAddress", namespace = "http://www.w3.org/2006/vcard/ns#")
  private VcardAddresses hasAddress;

  public Organization() {
  }

  // Used for creating XML entity from EM model class
  public Organization(eu.europeana.entitymanagement.definitions.model.Organization organization) {
    super(organization);
    init(organization);
  }

  public VcardAddresses getHasAddress() {
    return hasAddress;
  }

  public void setHasAddress(VcardAddresses hasAddress) {
    this.hasAddress = hasAddress;
  }

  public List<Label> getAcronyms() {
    return unmodifiableListAcceptingNull(acronyms);
  }

  public void setAcronyms(List<Label> acronyms) {
    this.acronyms = cloneListAcceptingNull(acronyms);
  }

  public List<Label> getDescriptions() {
    return unmodifiableListAcceptingNull(descriptions);
  }

  public void setDescriptions(List<Label> descriptions) {
    this.descriptions = cloneListAcceptingNull(descriptions);
  }

  public Resource getHomepage() {
    return homepage;
  }

  public void setHomepage(Resource homepage) {
    this.homepage = homepage;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public Resource getLogo() {
    return logo;
  }

  public void setLogo(Resource logo) {
    this.logo = logo;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getMbox() {
    return mbox;
  }

  public void setMbox(String mbox) {
    this.mbox = mbox;
  }

  public Resource getDepiction() {
    return depiction;
  }

  public void setDepiction(Resource depiction) {
    this.depiction = depiction;
  }

  private void init(eu.europeana.entitymanagement.definitions.model.Organization organization) {
    this.country = organization.getCountry().getEntityId();
    this.homepage = new Resource(organization.getHomepage());
    this.descriptions = convertMapToLabels(organization.getDescription());
    this.acronyms = convertMultilingualMapToLabel(organization.getAcronym());
    if (organization.getLogo() != null) {
      this.logo = new Resource(organization.getLogo().getId());
    }
    if (organization.getDepiction() != null) {
      this.depiction = new Resource(organization.getDepiction().getSource());
    }
    if (organization.getPhone() != null) {
      this.phone = String.valueOf(organization.getPhone());
    }
    if (organization.getMbox() != null) {
      this.mbox = String.valueOf(organization.getMbox());
    }
    this.hasAddress = getVcardAddresses(organization.getAddress());
    this.hasAddress = getVcardAddresses(organization.getAddress());
    setHiddenLabel(convertListToLabel(organization.getHiddenLabel()));
    setSameAs(convertListToPart(organization.getSameReferenceLinks()));
    setIdentifier(convertListToLabel(organization.getIdentifier()));
    setIsRelatedTo(convertListToLabelResource(organization.getIsRelatedTo()));
  }
}
