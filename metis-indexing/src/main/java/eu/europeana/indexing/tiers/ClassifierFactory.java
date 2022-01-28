package eu.europeana.indexing.tiers;

import eu.europeana.indexing.tiers.media.MediaClassifier;
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
   * @return A classifier for the metadata tier.
   */
  public static TierClassifier<MetadataTier, MetadataTierBreakdown> getMetadataClassifier() {
    return new MetadataClassifier(new LanguageClassifier(), new EnablingElementsClassifier(), new ContextualClassesClassifier());
  }

  /**
   * @return A classifier for the media tier.
   */
  public static TierClassifier<MediaTier, ContentTierBreakdown> getMediaClassifier() {
    return new MediaClassifier();
  }
}
