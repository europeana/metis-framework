package eu.europeana.enrichment.internal.model;

import dev.morphia.annotations.Entity;
import java.util.Objects;

@Entity
public class Address {

  private String about;
  private String vcardStreetAddress;
  private String vcardLocality;
  private String vcardPostalCode;
  private String vcardCountryName;
  private String vcardPostOfficeBox;
  private String vcardHasGeo;

  public String getAbout() {
    return about;
  }

  public void setAbout(String about) {
    this.about = about;
  }

  public String getVcardStreetAddress() {
    return vcardStreetAddress;
  }

  public void setVcardStreetAddress(String vcardStreetAddress) {
    this.vcardStreetAddress = vcardStreetAddress;
  }

  public String getVcardLocality() {
    return vcardLocality;
  }

  public void setVcardLocality(String vcardLocality) {
    this.vcardLocality = vcardLocality;
  }

  public String getVcardPostalCode() {
    return vcardPostalCode;
  }

  public void setVcardPostalCode(String vcardPostalCode) {
    this.vcardPostalCode = vcardPostalCode;
  }

  public String getVcardCountryName() {
    return vcardCountryName;
  }

  public void setVcardCountryName(String vcardCountryName) {
    this.vcardCountryName = vcardCountryName;
  }

  public String getVcardPostOfficeBox() {
    return vcardPostOfficeBox;
  }

  public void setVcardPostOfficeBox(String vcardPostOfficeBox) {
    this.vcardPostOfficeBox = vcardPostOfficeBox;
  }

  public String getVcardHasGeo() {
    return vcardHasGeo;
  }

  public void setVcardHasGeo(String vcardHasGeo) {
    this.vcardHasGeo = vcardHasGeo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Address address = (Address) o;
    return Objects.equals(about, address.about) && Objects
        .equals(vcardStreetAddress, address.vcardStreetAddress) && Objects
        .equals(vcardLocality, address.vcardLocality) && Objects
        .equals(vcardPostalCode, address.vcardPostalCode) && Objects
        .equals(vcardCountryName, address.vcardCountryName) && Objects
        .equals(vcardPostOfficeBox, address.vcardPostOfficeBox) && Objects
        .equals(vcardHasGeo, address.vcardHasGeo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(about, vcardStreetAddress, vcardLocality, vcardPostalCode, vcardCountryName,
        vcardPostOfficeBox, vcardHasGeo);
  }
}
