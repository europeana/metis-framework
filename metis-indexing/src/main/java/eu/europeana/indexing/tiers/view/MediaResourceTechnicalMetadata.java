package eu.europeana.indexing.tiers.view;

import eu.europeana.indexing.utils.LicenseType;

public class MediaResourceTechnicalMetadata {

  private String resourceUrl;
  private String mediaType;
  private String elementLinkType;
  private String imageResolution;
  private String verticalResolution;
  private LicenseType licenseType;

  public MediaResourceTechnicalMetadata() {
  }

  public String getResourceUrl() {
    return resourceUrl;
  }

  public void setResourceUrl(String resourceUrl) {
    this.resourceUrl = resourceUrl;
  }

  public String getMediaType() {
    return mediaType;
  }

  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  public String getElementLinkType() {
    return elementLinkType;
  }

  public void setElementLinkType(String elementLinkType) {
    this.elementLinkType = elementLinkType;
  }

  public String getImageResolution() {
    return imageResolution;
  }

  public void setImageResolution(String imageResolution) {
    this.imageResolution = imageResolution;
  }

  public String getVerticalResolution() {
    return verticalResolution;
  }

  public void setVerticalResolution(String verticalResolution) {
    this.verticalResolution = verticalResolution;
  }

  public LicenseType getLicenseType() {
    return licenseType;
  }

  public void setLicenseType(LicenseType licenseType) {
    this.licenseType = licenseType;
  }
}
