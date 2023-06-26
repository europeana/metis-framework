package eu.europeana.indexing.tiers.view;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.utils.LicenseType;
import eu.europeana.metis.schema.model.MediaType;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing the content tier breakdown.
 */
public class ContentTierBreakdown {

    private final MediaType recordType;
    private final MediaTier mediaTierBeforeLicenseCorrection;
    private final LicenseType licenseType;
    private final boolean thumbnailAvailable;
    private final boolean landingPageAvailable;
    private final boolean mediaResource3DAvailable;
    private final boolean embeddableMediaAvailable;
    private final List<MediaResourceTechnicalMetadata> mediaResourceTechnicalMetadataList;
    private final List<ProcessingError> processingErrorsList;

    /**
     * Constructor with required parameters.
     * <p>It creates a copy of the content tier breakdown extended with the processing errors list.</p>
     *
     * @param contentTierBreakdown the content tier breakdown
     * @param processingErrorsList the processing errors list
     */
    public ContentTierBreakdown(ContentTierBreakdown contentTierBreakdown, List<ProcessingError> processingErrorsList) {
        this(new Builder()
                .setRecordType(contentTierBreakdown.getRecordType())
                .setMediaTierBeforeLicenseCorrection(contentTierBreakdown.getMediaTierBeforeLicenseCorrection())
                .setLicenseType(contentTierBreakdown.getLicenseType())
                .setThumbnailAvailable(contentTierBreakdown.isThumbnailAvailable())
                .setLandingPageAvailable(contentTierBreakdown.isLandingPageAvailable())
                .setMediaResource3DAvailable(contentTierBreakdown.isMediaResource3DAvailable())
                .setEmbeddableMediaAvailable(contentTierBreakdown.isEmbeddableMediaAvailable())
                .setMediaResourceTechnicalMetadataList(contentTierBreakdown.mediaResourceTechnicalMetadataList)
                .setProcessingErrorsList(processingErrorsList));
    }

    private ContentTierBreakdown(Builder builder) {
        this.recordType = builder.recordType;
        this.mediaTierBeforeLicenseCorrection = builder.mediaTierBeforeLicenseCorrection;
        this.licenseType = builder.licenseType;
        this.thumbnailAvailable = builder.thumbnailAvailable;
        this.landingPageAvailable = builder.landingPageAvailable;
        this.mediaResource3DAvailable = builder.mediaResource3DAvailable;
        this.embeddableMediaAvailable = builder.embeddableMediaAvailable;
        this.mediaResourceTechnicalMetadataList =
                builder.mediaResourceTechnicalMetadataList == null ? new ArrayList<>() : new ArrayList<>(builder.mediaResourceTechnicalMetadataList);
        this.processingErrorsList = builder.processingErrorsList == null ? new ArrayList<>() : new ArrayList<>(builder.processingErrorsList);
    }

    public MediaType getRecordType() {
        return recordType;
    }

    public MediaTier getMediaTierBeforeLicenseCorrection(){
      return mediaTierBeforeLicenseCorrection;
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

    public boolean isMediaResource3DAvailable() {
        return mediaResource3DAvailable;
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

  /**
   * Builder class of ContentTierBreakdown. Used to make the build of ContentTierBreakdown easier
   */
  public static class Builder {
        private MediaType recordType;
        private MediaTier mediaTierBeforeLicenseCorrection;
        private LicenseType licenseType;
        private boolean thumbnailAvailable;
        private boolean landingPageAvailable;
        private boolean mediaResource3DAvailable;
        private boolean embeddableMediaAvailable;
        private List<MediaResourceTechnicalMetadata> mediaResourceTechnicalMetadataList;
        private List<ProcessingError> processingErrorsList;

        /**
         * Constructor of the object's builder
         */
        public Builder() {
            //There are no mandatory values
        }

        public Builder setRecordType(MediaType recordType) {
            this.recordType = recordType;
            return this;
        }

        public Builder setMediaTierBeforeLicenseCorrection(MediaTier mediaTierBeforeLicenseCorrection){
          this.mediaTierBeforeLicenseCorrection = mediaTierBeforeLicenseCorrection;
          return this;
        }

        public Builder setLicenseType(LicenseType licenseType) {
            this.licenseType = licenseType;
            return this;
        }

        public Builder setThumbnailAvailable(boolean thumbnailAvailable) {
            this.thumbnailAvailable = thumbnailAvailable;
            return this;
        }

        public Builder setLandingPageAvailable(boolean landingPageAvailable) {
            this.landingPageAvailable = landingPageAvailable;
            return this;
        }

        public Builder setMediaResource3DAvailable(boolean mediaResource3DAvailable) {
            this.mediaResource3DAvailable = mediaResource3DAvailable;
            return this;
        }

        public Builder setEmbeddableMediaAvailable(boolean embeddableMediaAvailable) {
            this.embeddableMediaAvailable = embeddableMediaAvailable;
            return this;
        }

        public Builder setMediaResourceTechnicalMetadataList(List<MediaResourceTechnicalMetadata> mediaResourceTechnicalMetadataList) {
            this.mediaResourceTechnicalMetadataList = mediaResourceTechnicalMetadataList == null ? new ArrayList<>() :
                    new ArrayList<>(mediaResourceTechnicalMetadataList);
            return this;
        }

        public Builder setProcessingErrorsList(List<ProcessingError> processingErrorsList) {
            this.processingErrorsList = processingErrorsList == null ? new ArrayList<>() : new ArrayList<>(processingErrorsList);
            return this;
        }

        /**
         * Creates a new ContentTierBreakdown object based on the values that were set from the builder
         *
         * @return A new instance of ContentTierBreakdown object
         */
        public ContentTierBreakdown build() {
            return new ContentTierBreakdown(this);
        }
    }
}

