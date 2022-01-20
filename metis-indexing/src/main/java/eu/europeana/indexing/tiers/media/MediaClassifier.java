package eu.europeana.indexing.tiers.media;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.TierClassifier;
import eu.europeana.indexing.tiers.view.ContentTierBreakdown;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.jibx.EdmType;
import java.util.Collections;

/**
 * Classifier for the media tier.
 */
public class MediaClassifier implements TierClassifier<MediaTier, ContentTierBreakdown> {

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
  public TierClassification<MediaTier, ContentTierBreakdown> classify(RdfWrapper entity) {
    final TierClassifier<MediaTier, ContentTierBreakdown> deferredClassifier = getDeferredClassifier(entity.getEdmType());
    // TODO: 20/01/2022 Is this initialized correctly?
    if (deferredClassifier == null) {
      return new TierClassification<>(MediaTier.T0, new ContentTierBreakdown(null, null, false,
          false, false, Collections.emptyList()));
    }
    return deferredClassifier.classify(entity);
  }

  private TierClassifier<MediaTier, ContentTierBreakdown> getDeferredClassifier(EdmType edmType) {
    final TierClassifier<MediaTier, ContentTierBreakdown> deferredClassifier;
    if (edmType == null) {
      deferredClassifier = null;
    } else {
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
    }
    return deferredClassifier;
  }
}
