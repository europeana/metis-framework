package eu.europeana.indexing.tiers.view;

import eu.europeana.indexing.utils.LicenseType;
import eu.europeana.metis.schema.model.MediaType;
import java.util.List;

public class ContentTierBreakdown {

  // TODO: 22/12/2021 3D is not supported EdmType contains is but we should avoid using jibx. Check if we can add it in metis
  private MediaType recordType;
  // TODO: 22/12/2021 CLOSED not supported. We should add it in metis
  private LicenseType licenseType;
  private boolean thumbnailAvailable;
  private boolean landingPageAvailable;
  private boolean embeddableMediaAvailable;
  private List<MediaResourceTechnicalMetadata> mediaResourceTechnicalMetadataList;
  private List<ProcessingError> processingErrorsList;

  public ContentTierBreakdown() {
  }

  public MediaType getRecordType() {
    return recordType;
  }

  public void setRecordType(MediaType recordType) {
    this.recordType = recordType;
  }

  public LicenseType getLicenseType() {
    return licenseType;
  }

  public void setLicenseType(LicenseType licenseType) {
    this.licenseType = licenseType;
  }

  public boolean isThumbnailAvailable() {
    return thumbnailAvailable;
  }

  public void setThumbnailAvailable(boolean thumbnailAvailable) {
    this.thumbnailAvailable = thumbnailAvailable;
  }

  public boolean isLandingPageAvailable() {
    return landingPageAvailable;
  }

  public void setLandingPageAvailable(boolean landingPageAvailable) {
    this.landingPageAvailable = landingPageAvailable;
  }

  public boolean isEmbeddableMediaAvailable() {
    return embeddableMediaAvailable;
  }

  public void setEmbeddableMediaAvailable(boolean embeddableMediaAvailable) {
    this.embeddableMediaAvailable = embeddableMediaAvailable;
  }

  public List<MediaResourceTechnicalMetadata> getMediaResourceTechnicalMetadataList() {
    return mediaResourceTechnicalMetadataList;
  }

  public void setMediaResourceTechnicalMetadataList(
      List<MediaResourceTechnicalMetadata> mediaResourceTechnicalMetadataList) {
    this.mediaResourceTechnicalMetadataList = mediaResourceTechnicalMetadataList;
  }

  public List<ProcessingError> getProcessingErrorsList() {
    return processingErrorsList;
  }

  public void setProcessingErrorsList(List<ProcessingError> processingErrorsList) {
    this.processingErrorsList = processingErrorsList;
  }
}

