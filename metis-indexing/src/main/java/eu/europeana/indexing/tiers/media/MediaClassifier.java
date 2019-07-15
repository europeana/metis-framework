package eu.europeana.indexing.tiers.media;

import eu.europeana.corelib.definitions.jibx.EdmType;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.TierClassifier;
import eu.europeana.indexing.utils.RdfWrapper;
import java.util.Optional;

/**
 * Classifier for the media tier.
 */
public class MediaClassifier implements TierClassifier<MediaTier> {

  private final AudioClassifier audioClassifier = new AudioClassifier();
  private final ImageClassifier imageClassifier = new ImageClassifier();
  private final TextClassifier textClassifier = new TextClassifier();
  private final VideoClassifier videoClassifier = new VideoClassifier();
  private final ThreeDClassifier threeDClassifier = new ThreeDClassifier();

  @Override
  public MediaTier classify(RdfWrapper entity) {
    return Optional.ofNullable(entity.getEdmType()).map(this::getDeferredClassifier)
        .map(classifier -> classifier.classify(entity)).orElse(MediaTier.T0);
  }

  private TierClassifier<MediaTier> getDeferredClassifier(EdmType edmType) {
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
      case _3_D:
        deferredClassifier = threeDClassifier;
        break;
      default:
        deferredClassifier = null;
    }
    return deferredClassifier;
  }
}
