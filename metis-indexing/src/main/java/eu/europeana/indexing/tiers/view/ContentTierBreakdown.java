package eu.europeana.indexing.tiers.view;

import eu.europeana.indexing.utils.LicenseType;
import eu.europeana.metis.schema.model.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Class containing the content tier breakdown.
 */
public class ContentTierBreakdown {

  private final MediaType recordType;
  private final LicenseType licenseType;
  private final boolean thumbnailAvailable;
  private final boolean landingPageAvailable;
  private final boolean embeddableMediaAvailable;
  private final List<MediaResourceTechnicalMetadata> mediaResourceTechnicalMetadataList;
  private final List<ProcessingError> processingErrorsList;

  /**
   * Constructor with required parameters.
   *
   * @param recordType the record media type
   * @param licenseType the license type
   * @param thumbnailAvailable the flag indicating if a thumbnails is available
   * @param landingPageAvailable the flag indicating if a page is available
   * @param embeddableMediaAvailable the flag indicating if embeddable media are available
   * @param mediaResourceTechnicalMetadataList the list of media resource technical metadata
   */
  public ContentTierBreakdown(MediaType recordType, LicenseType licenseType, boolean thumbnailAvailable,
      boolean landingPageAvailable, boolean embeddableMediaAvailable,
      List<MediaResourceTechnicalMetadata> mediaResourceTechnicalMetadataList) {
    this(recordType, licenseType, thumbnailAvailable, landingPageAvailable, embeddableMediaAvailable,
        mediaResourceTechnicalMetadataList, null);
  }

  /**
   * Constructor with required parameters.
   * <p>It create a copy of the content tier breakdown extended with the processing errors list.</p>
   *
   * @param contentTierBreakdown the content tier breakdown
   * @param processingErrorsList the processing errors list
   */
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
    return new ArrayList<>(mediaResourceTechnicalMetadataList);
  }

  public List<ProcessingError> getProcessingErrorsList() {
    return new ArrayList<>(processingErrorsList);
  }
}

