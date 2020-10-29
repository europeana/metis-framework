package eu.europeana.indexing.tiers.media;

import eu.europeana.metis.schema.jibx.EdmType;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.TierClassifier;
import eu.europeana.indexing.utils.RdfWrapper;
import java.util.Optional;

/**
 * Classifier for the media tier.
 */
public class MediaClassifier implements TierClassifier<MediaTier> {

  private final AudioClassifier audioClassifier;
  private final ImageClassifier imageClassifier;
  private final TextClassifier textClassifier;
  private final VideoClassifier videoClassifier;
  private final ThreeDClassifier threeDClassifier;

  /**
   * Constructor.
   */
  public MediaClassifier() {
    this(new AudioClassifier(), new ImageClassifier(), new TextClassifier(), new VideoClassifier(),
        new ThreeDClassifier());
  }

  /**
   * Constructor for test purposes.
   * 
   * @param audioClassifier The audio classifier to use.
   * @param imageClassifier The image classifier to use.
   * @param textClassifier The text classifier to use.
   * @param videoClassifier The video classifier to use.
   * @param threeDClassifier the 3D classifier to use.
   */
  MediaClassifier(AudioClassifier audioClassifier, ImageClassifier imageClassifier,
      TextClassifier textClassifier, VideoClassifier videoClassifier,
      ThreeDClassifier threeDClassifier) {
    super();
    this.audioClassifier = audioClassifier;
    this.imageClassifier = imageClassifier;
    this.textClassifier = textClassifier;
    this.videoClassifier = videoClassifier;
    this.threeDClassifier = threeDClassifier;
  }

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
