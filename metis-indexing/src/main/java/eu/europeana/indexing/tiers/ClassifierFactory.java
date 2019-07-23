package eu.europeana.indexing.tiers;

import eu.europeana.indexing.tiers.media.MediaClassifier;
import eu.europeana.indexing.tiers.metadata.ContextualClassClassifier;
import eu.europeana.indexing.tiers.metadata.EnablingElementsClassifier;
import eu.europeana.indexing.tiers.metadata.LanguageClassifier;
import eu.europeana.indexing.tiers.model.CombinedClassifier;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.TierClassifier;
import java.util.Arrays;

/**
 * A factory class for the classifiers.
 */
public final class ClassifierFactory {

  private ClassifierFactory() {
  }

  /**
   * @return A classifier for the metadata tier.
   */
  public static TierClassifier<MetadataTier> getMetadataClassifier() {
    return new CombinedClassifier<>(Arrays
        .asList(new LanguageClassifier(), new EnablingElementsClassifier(),
            new ContextualClassClassifier()));
  }

  /**
   * @return A classifier for the media tier.
   */
  public static TierClassifier<MediaTier> getMediaClassifier() {
    return new MediaClassifier();
  }
}
