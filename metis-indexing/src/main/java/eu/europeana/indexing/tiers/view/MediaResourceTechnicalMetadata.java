package eu.europeana.indexing.tiers.view;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.utils.LicenseType;
import eu.europeana.indexing.utils.WebResourceLinkType;
import eu.europeana.metis.schema.model.MediaType;
import java.util.HashSet;
import java.util.Set;

@JsonInclude(Include.NON_NULL)
public final class MediaResourceTechnicalMetadata {

  private final String resourceUrl;
  private final MediaType mediaType;
  private final String mimeType;
  private final Set<WebResourceLinkType> elementLinkTypes;
  private final LicenseType licenseType;
  private final MediaTier mediaTier;
  private final Long imageResolution;
  private final MediaTier imageResolutionTier;
  private final Long verticalResolution;
  private final MediaTier verticalResolutionTier;

  private MediaResourceTechnicalMetadata(String resourceUrl, MediaType mediaType, String mimeType,
      Set<WebResourceLinkType> elementLinkTypes, LicenseType licenseType,
      MediaTier mediaTier, ResolutionTierMetadataData resolutionTierMetadataData) {
    this.resourceUrl = resourceUrl;
    this.mediaType = mediaType;
    this.mimeType = mimeType;
    this.elementLinkTypes = elementLinkTypes == null ? new HashSet<>() : new HashSet<>(elementLinkTypes);
    this.licenseType = licenseType;
    this.mediaTier = mediaTier;
    this.imageResolution = resolutionTierMetadataData.getImageResolution();
    this.imageResolutionTier = resolutionTierMetadataData.getImageResolutionTier();
    this.verticalResolution = resolutionTierMetadataData.getVerticalResolution();
    this.verticalResolutionTier = resolutionTierMetadataData.getVerticalResolutionTier();
  }

  public String getResourceUrl() {
    return resourceUrl;
  }

  public MediaType getMediaType() {
    return mediaType;
  }

  public String getMimeType() {
    return mimeType;
  }

  public Set<WebResourceLinkType> getElementLinkTypes() {
    return new HashSet<>(elementLinkTypes);
  }

  public LicenseType getLicenseType() {
    return licenseType;
  }

  public MediaTier getMediaTier() {
    return mediaTier;
  }

  public Long getImageResolution() {
    return imageResolution;
  }

  public MediaTier getImageResolutionTier() {
    return imageResolutionTier;
  }

  public Long getVerticalResolution() {
    return verticalResolution;
  }

  public MediaTier getVerticalResolutionTier() {
    return verticalResolutionTier;
  }

  public static class MediaResourceTechnicalMetadataBuilder {

    private String resourceUrl;
    private MediaType mediaType;
    private String mimeType;
    private Set<WebResourceLinkType> elementLinkTypes;
    private LicenseType licenseType;
    private MediaTier mediaTier;
    private final ResolutionTierMetadataData resolutionTierMetadataData;

    public MediaResourceTechnicalMetadataBuilder(ResolutionTierMetadataData resolutionTierMetadataData) {
      this.resolutionTierMetadataData = new ResolutionTierMetadataData(resolutionTierMetadataData);
    }

    public MediaResourceTechnicalMetadataBuilder setResourceUrl(String resourceUrl) {
      this.resourceUrl = resourceUrl;
      return this;
    }

    public MediaResourceTechnicalMetadataBuilder setMediaType(MediaType mediaType) {
      this.mediaType = mediaType;
      return this;
    }

    public MediaResourceTechnicalMetadataBuilder setMimeType(String mimeType) {
      this.mimeType = mimeType;
      return this;
    }

    public MediaResourceTechnicalMetadataBuilder setElementLinkTypes(Set<WebResourceLinkType> elementLinkTypes) {
      this.elementLinkTypes = elementLinkTypes == null ? new HashSet<>() : new HashSet<>(elementLinkTypes);
      return this;
    }

    public MediaResourceTechnicalMetadataBuilder setLicenseType(LicenseType licenseType) {
      this.licenseType = licenseType;
      return this;
    }

    public MediaResourceTechnicalMetadataBuilder setMediaTier(MediaTier mediaTier) {
      this.mediaTier = mediaTier;
      return this;
    }

    public MediaResourceTechnicalMetadata createMediaResourceTechnicalMetadata() {
      //Verify fields that should not be blank/empty
      notBlank(resourceUrl);
      notNull(mediaType);
      notNull(elementLinkTypes); //Can be empty but shouldn't be null
      notNull(licenseType);
      notNull(mediaTier);

      return new MediaResourceTechnicalMetadata(resourceUrl, mediaType, mimeType, elementLinkTypes, licenseType, mediaTier,
          resolutionTierMetadataData);

    }
  }
}
