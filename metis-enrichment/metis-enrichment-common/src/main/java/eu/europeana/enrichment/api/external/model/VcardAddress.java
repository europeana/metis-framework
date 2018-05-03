package eu.europeana.enrichment.api.external.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class supports parsing of the content of Wikidata Address class
 * 
 * @author GrafR
 *
 */
@XmlRootElement(namespace = "http://www.w3.org/2006/vcard/ns#", name = "Address")
@XmlAccessorType(XmlAccessType.FIELD)
public class VcardAddress { 
  
  public VcardAddress() {
    super();
  }
  
  @XmlElement(name = "country-name", namespace = "http://www.w3.org/2006/vcard/ns#")
  private String countryName;

  @XmlElement(name = "locality", namespace = "http://www.w3.org/2006/vcard/ns#")
  private String locality;

  @XmlElement(name = "street-address", namespace = "http://www.w3.org/2006/vcard/ns#")
  private String streetAddress;

  @XmlElement(name = "postal-code", namespace = "http://www.w3.org/2006/vcard/ns#")
  private String postalCode;

  @XmlElement(name = "post-office-box", namespace = "http://www.w3.org/2006/vcard/ns#")
  private String postOfficeBox;
  
  public String getCountryName() {
    return countryName;
  }

  public void setCountryName(String countryName) {
    this.countryName = countryName;
  }
  
  public String getLocality() {
    return locality;
  }

  public void setLocality(String locality) {
    this.locality = locality;
  }
  
  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }
  
  public String getPostOfficeBox() {
    return postOfficeBox;
  }

  public void setPostOfficeBox(String postOfficeBox) {
    this.postOfficeBox = postOfficeBox;
  }
  
  public String getStreetAddress() {
    return streetAddress;
  }

  public void setStreetAddress(String streetAddress) {
    this.streetAddress = streetAddress;
  }
  
}
