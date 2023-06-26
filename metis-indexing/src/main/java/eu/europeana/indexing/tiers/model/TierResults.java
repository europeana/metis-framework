package eu.europeana.indexing.tiers.model;

import eu.europeana.indexing.tiers.model.TierClassifier.TierClassification;
import eu.europeana.indexing.tiers.view.ContentTierBreakdown;
import eu.europeana.indexing.tiers.view.MetadataTierBreakdown;
import eu.europeana.indexing.utils.LicenseType;

/**
 * Object that encapsulates the content and metadata tiers values.
 * It can not change set new values.
 *
 */
public class TierResults {

    private final MediaTier mediaTier;
    private final MediaTier contentTierLicenseCorrection;
    private final LicenseType licenseType;
    private final MetadataTier metadataTier;
    private final MetadataTier metadataTierLanguage;
    private final MetadataTier metadataTierEnablingElements;
    private final MetadataTier metadataTierContextualClasses;

    /**
     * Constructor
     *
     * @param mediaTierClassification The object containing the media classification of the record
     * @param metadataTierClassification The object containing the metadata classification of the record
     */
    public TierResults(TierClassification<MediaTier, ContentTierBreakdown> mediaTierClassification,
        TierClassification<MetadataTier, MetadataTierBreakdown> metadataTierClassification) {
        this.mediaTier = mediaTierClassification.getTier();
        this.metadataTier = metadataTierClassification.getTier();
        contentTierLicenseCorrection = mediaTierClassification.getClassification().getMediaTierBeforeLicenseCorrection();
        licenseType = mediaTierClassification.getClassification().getLicenseType();
        metadataTierLanguage = metadataTierClassification.getClassification().getLanguageBreakdown().getMetadataTier();
        metadataTierEnablingElements = metadataTierClassification.getClassification().getEnablingElements().getMetadataTier();
        metadataTierContextualClasses = metadataTierClassification.getClassification().getContextualClasses().getMetadataTier();
    }

    /**
     * Returns the value of content tier
     * @return the value of content tier
     */
    public MediaTier getMediaTier() {
        return mediaTier;
    }

    /**
     * Returns the value of content tier after license correction
     * @return the value of content tier after license correction
     */
    public MediaTier getContentTierLicenseCorrection(){
        return contentTierLicenseCorrection;
    }

    /**
     * Returns the license type of the record
     * @return the license type of the record
     */
    public LicenseType getLicenseType(){
        return licenseType;
    }

    /**
     * Returns the value for metadata tier
     * @return the value of metadata tier
     */
    public MetadataTier getMetadataTier() {
        return metadataTier;
    }

    /**
     * Returns the value for the metadata tier for language dimension
     * @return the value for the metadata tier for language dimension
     */
    public MetadataTier getMetadataTierLanguage(){
        return metadataTierLanguage;
    }

    /**
     * Returns the value for the metadata tier for enabling elements dimension
     * @return the value for the metadata tier for enabling elements dimension
     */
    public MetadataTier getMetadataTierEnablingElements(){
        return metadataTierEnablingElements;
    }

    /**
     * Returns the value for the metadata tier for contextual classes dimension
     * @return the value for the metadata tier for contextual classes dimension
     */
    public MetadataTier getMetadataTierContextualClasses(){
        return metadataTierContextualClasses;
    }

}
