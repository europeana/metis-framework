package eu.europeana.indexing.tiers.model;

/**
 * Object that encapsulates the content and metadata tiers values.
 * It can not change set new values.
 *
 */
public class TierResults {

    private final MediaTier mediaTier;
    private final MetadataTier metadataTier;

    /**
     * Constructor
     *
     * @param mediaTier The value for content tier
     * @param metadataTier The value for metadata tier
     */
    public TierResults(MediaTier mediaTier, MetadataTier metadataTier) {
        this.mediaTier = mediaTier;
        this.metadataTier = metadataTier;
    }

    /**
     * Returns the value of content tier
     * @return the value of content tier
     */
    public MediaTier getMediaTier() {
        return mediaTier;
    }

    /**
     * Returns the value for metadata tier
     * @return the value of metadata tier
     */
    public MetadataTier getMetadataTier() {
        return metadataTier;
    }

}
