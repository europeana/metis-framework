package eu.europeana.indexing.solr.tiers.media;

import eu.europeana.corelib.definitions.jibx.EdmType;
import eu.europeana.indexing.solr.tiers.model.MediaTier;
import eu.europeana.indexing.solr.tiers.model.TierClassifier;
import eu.europeana.indexing.utils.RdfWrapper;

/**
 * Classifier for the media tier.
 */
public class MediaClassifier implements TierClassifier<MediaTier> {

  private final AudioClassifier audioClassifier = new AudioClassifier();
  private final ImageClassifier imageClassifier = new ImageClassifier();
  private final TextClassifier textClassifier = new TextClassifier();
  private final VideoClassifier videoClassifier = new VideoClassifier();

  @Override
  public MediaTier classify(RdfWrapper entity) {

    // Get the type, and do sanity check.
    final EdmType edmType = entity.getEdmType();
    if (edmType == null) {
      return MediaTier.MIN;
    }

    // Get the right classifier.
    final TierClassifier<MediaTier> deferredClassifier;
    switch (edmType) {
      case SOUND:
        deferredClassifier = audioClassifier;
        break;
      case IMAGE:
        deferredClassifier = imageClassifier;
        break;
      case TEXT:
        deferredClassifier = textClassifier;
        break;
      case VIDEO:
        deferredClassifier = videoClassifier;
        break;
      default:
        deferredClassifier = null;
    }

    // Perform the classification.
    return deferredClassifier == null ? MediaTier.T0 : deferredClassifier.classify(entity);
  }
}
