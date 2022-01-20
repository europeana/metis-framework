package eu.europeana.indexing.tiers.view;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.utils.LicenseType;
import eu.europeana.indexing.utils.WebResourceLinkType;
import eu.europeana.metis.schema.model.MediaType;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@JsonInclude(Include.NON_NULL)
public class MediaResourceTechnicalMetadata {

  private final String resourceUrl;
  private final MediaType mediaType;
  private final String mimeType;
  private final Set<WebResourceLinkType> elementLinkTypes;
  private final Long imageResolution;
  private final MediaTier imageResolutionTier;
  private final Long verticalResolution;
  private final MediaTier verticalResolutionTier;
  private final LicenseType licenseType;
  private final MediaTier mediaTier;

  private MediaResourceTechnicalMetadata(String resourceUrl, MediaType mediaType, String mimeType,
      Set<WebResourceLinkType> elementLinkTypes, Long imageResolution,
      MediaTier imageResolutionTier, Long verticalResolution, MediaTier verticalResolutionTier, LicenseType licenseType,
      MediaTier mediaTier) {
    this.resourceUrl = resourceUrl;
    this.mediaType = mediaType;
    this.mimeType = mimeType;
    this.elementLinkTypes = elementLinkTypes;
    this.imageResolution = imageResolution;
    this.imageResolutionTier = imageResolutionTier;
    this.verticalResolution = verticalResolution;
    this.verticalResolutionTier = verticalResolutionTier;
    this.licenseType = licenseType;
    this.mediaTier = mediaTier;
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
    return elementLinkTypes;
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

  public LicenseType getLicenseType() {
    return licenseType;
  }

  public MediaTier getMediaTier() {
    return mediaTier;
  }

  public static class MediaResourceTechnicalMetadataBuilder {

    private String resourceUrl;
    private MediaType mediaType;
    private String mimeType;
    private Set<WebResourceLinkType> elementLinkTypes;
    private LicenseType licenseType;
    private MediaTier mediaTier;
    private final Long imageResolution;
    private final MediaTier imageResolutionTier;
    private final Long verticalResolution;
    private final MediaTier verticalResolutionTier;

    private MediaResourceTechnicalMetadataBuilder(Long imageResolution, MediaTier imageResolutionTier, Long verticalResolution,
        MediaTier verticalResolutionTier) {
      this.imageResolution = (Objects.isNull(imageResolution) || imageResolution == 0) ? null : imageResolution;
      this.imageResolutionTier = imageResolutionTier;
      this.verticalResolution = (Objects.isNull(verticalResolution) || verticalResolution == 0) ? null : verticalResolution;
      this.verticalResolutionTier = verticalResolutionTier;
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

      return new MediaResourceTechnicalMetadata(resourceUrl, mediaType, mimeType, elementLinkTypes, imageResolution,
          imageResolutionTier, verticalResolution, verticalResolutionTier, licenseType, mediaTier);

    }
  }

  public static class ResolutionTierPreInitializationBuilder {

    private Long imageResolution;
    private MediaTier imageResolutionTier;
    private Long verticalResolution;
    private MediaTier verticalResolutionTier;

    public ResolutionTierPreInitializationBuilder setImageResolution(Long imageResolution) {
      this.imageResolution = imageResolution;
      return this;
    }

    public ResolutionTierPreInitializationBuilder setImageResolutionTier(MediaTier imageResolutionTier) {
      this.imageResolutionTier = imageResolutionTier;
      return this;
    }

    public ResolutionTierPreInitializationBuilder setVerticalResolution(Long verticalResolution) {
      this.verticalResolution = verticalResolution;
      return this;
    }

    public ResolutionTierPreInitializationBuilder setVerticalResolutionTier(MediaTier verticalResolutionTier) {
      this.verticalResolutionTier = verticalResolutionTier;
      return this;
    }

    public MediaResourceTechnicalMetadataBuilder createMediaResourceTechnicalMetadataBuilder() {
      //Both null or only one of the two null
      final boolean areBothTiersNull = Objects.isNull(imageResolutionTier) && Objects.isNull(verticalResolutionTier);
      final boolean isOneOfTiersNull = Objects.isNull(imageResolutionTier) || Objects.isNull(verticalResolutionTier);
      isTrue(areBothTiersNull || isOneOfTiersNull);

      return new MediaResourceTechnicalMetadataBuilder(imageResolution, imageResolutionTier, verticalResolution,
          verticalResolutionTier);
    }
  }
}
