package eu.europeana.indexing.tiers;

import eu.europeana.indexing.tiers.media.MediaClassifier;
import eu.europeana.indexing.tiers.metadata.ClassifierMode;
import eu.europeana.indexing.tiers.metadata.ContextualClassesClassifier;
import eu.europeana.indexing.tiers.metadata.EnablingElementsClassifier;
import eu.europeana.indexing.tiers.metadata.LanguageClassifier;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataClassifier;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.TierClassifier;
import eu.europeana.indexing.tiers.view.ContentTierBreakdown;
import eu.europeana.indexing.tiers.view.MetadataTierBreakdown;

/**
 * A factory class for the classifiers.
 */
public final class ClassifierFactory {

  private ClassifierFactory() {
  }

  /**
   * get a classifier with in default mode
   *
   * @return A classifier for the metadata tier with Provider Proxies as default mode.
   */
  public static TierClassifier<MetadataTier, MetadataTierBreakdown> getMetadataClassifier() {
    return getMetadataClassifier(ClassifierMode.PROVIDER_PROXIES);
  }

  /**
   * get a metadata classifier with a mode
   *
   * @param classifierMode select the classifier mode for the metadata tier.
   * @return A classifier for the metadata tier.
   */
  public static TierClassifier<MetadataTier, MetadataTierBreakdown> getMetadataClassifier(ClassifierMode classifierMode) {
    return new MetadataClassifier(
        new LanguageClassifier(classifierMode),
        new EnablingElementsClassifier(classifierMode),
        new ContextualClassesClassifier(classifierMode)
    );
  }

  /**
   * get a media classifier
   *
   * @return A classifier for the media tier.
   */
  public static TierClassifier<MediaTier, ContentTierBreakdown> getMediaClassifier() {
    return new MediaClassifier();
  }
}
