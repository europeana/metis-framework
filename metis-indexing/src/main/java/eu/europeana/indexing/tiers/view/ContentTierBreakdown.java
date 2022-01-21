package eu.europeana.indexing.tiers.view;

import eu.europeana.indexing.utils.LicenseType;
import eu.europeana.metis.schema.model.MediaType;
import java.util.ArrayList;
import java.util.List;

public class ContentTierBreakdown {

  private final MediaType recordType;
  private final LicenseType licenseType;
  private final boolean thumbnailAvailable;
  private final boolean landingPageAvailable;
  private final boolean embeddableMediaAvailable;
  private final List<MediaResourceTechnicalMetadata> mediaResourceTechnicalMetadataList;
  private final List<ProcessingError> processingErrorsList;

  public ContentTierBreakdown(MediaType recordType, LicenseType licenseType, boolean thumbnailAvailable,
      boolean landingPageAvailable, boolean embeddableMediaAvailable,
      List<MediaResourceTechnicalMetadata> mediaResourceTechnicalMetadataList) {
    this(recordType, licenseType, thumbnailAvailable, landingPageAvailable, embeddableMediaAvailable,
        mediaResourceTechnicalMetadataList, null);
  }

  public ContentTierBreakdown(ContentTierBreakdown contentTierBreakdown, List<ProcessingError> processingErrorsList) {
    this(contentTierBreakdown.getRecordType(), contentTierBreakdown.getLicenseType(), contentTierBreakdown.isThumbnailAvailable(),
        contentTierBreakdown.isLandingPageAvailable(), contentTierBreakdown.isEmbeddableMediaAvailable(),
        contentTierBreakdown.mediaResourceTechnicalMetadataList, processingErrorsList);
  }

  private ContentTierBreakdown(MediaType recordType, LicenseType licenseType, boolean thumbnailAvailable,
      boolean landingPageAvailable, boolean embeddableMediaAvailable,
      List<MediaResourceTechnicalMetadata> mediaResourceTechnicalMetadataList, List<ProcessingError> processingErrorsList) {
    this.recordType = recordType;
    this.licenseType = licenseType;
    this.thumbnailAvailable = thumbnailAvailable;
    this.landingPageAvailable = landingPageAvailable;
    this.embeddableMediaAvailable = embeddableMediaAvailable;
    this.mediaResourceTechnicalMetadataList =
        mediaResourceTechnicalMetadataList == null ? new ArrayList<>() : new ArrayList<>(mediaResourceTechnicalMetadataList);
    this.processingErrorsList = processingErrorsList == null ? new ArrayList<>() : new ArrayList<>(processingErrorsList);
  }

  public MediaType getRecordType() {
    return recordType;
  }

  public LicenseType getLicenseType() {
    return licenseType;
  }

  public boolean isThumbnailAvailable() {
    return thumbnailAvailable;
  }

  public boolean isLandingPageAvailable() {
    return landingPageAvailable;
  }

  public boolean isEmbeddableMediaAvailable() {
    return embeddableMediaAvailable;
  }

  public List<MediaResourceTechnicalMetadata> getMediaResourceTechnicalMetadataList() {
    return mediaResourceTechnicalMetadataList;
  }

  public List<ProcessingError> getProcessingErrorsList() {
    return processingErrorsList;
  }
}

