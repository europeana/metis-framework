package eu.europeana.indexing.tiers.media;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceLinkType;
import eu.europeana.indexing.utils.WebResourceWrapper;
import java.util.EnumSet;

/**
 * Classifier for videos.
 */
class VideoClassifier extends AbstractMediaClassifier {

  private static final int MIN_IMAGE_RESOLUTION = 100_000;

  private static final int LARGE_VERTICAL_SIZE = 480;

  @Override
  MediaTier preClassifyEntity(RdfWrapper entity) {

    // Check presence of thumbnail. If not available, tier 0.
    if (!entity.hasThumbnails()) {
      return MediaTier.T0;
    }

    // Check presence of image as edm:Object with sufficient resolution. If not available, tier 0.
    final boolean hasLargeImageAsObject = entity
        .getWebResourceWrappers(EnumSet.of(WebResourceLinkType.OBJECT)).stream()
        .filter(ImageClassifier::hasImageMimeType).mapToLong(WebResourceWrapper::getSize)
        .anyMatch(size -> size >= MIN_IMAGE_RESOLUTION);
    if (!hasLargeImageAsObject) {
      return MediaTier.T0;
    }

    // In any other case, it depends completely on the web resources.
    return null;
  }

  @Override
  MediaTier classifyEntityWithoutWebResources(RdfWrapper entity, boolean hasLandingPage) {

    // If there is a landing page, even an entity without suitable web resources has tier 1.
    return hasLandingPage ? MediaTier.T1 : MediaTier.T0;
  }

  @Override
  MediaTier classifyWebResource(WebResourceWrapper webResource, boolean hasLandingPage,
      boolean hasEmbeddableMedia) {

    // Check mime type.
    final String mimeType = webResource.getMimeType();
    final boolean hasVideoMimeType = mimeType != null && mimeType.startsWith("video");

    // Check the conditions - the conditions for T2-T4 take precedence over those of T1.
    final MediaTier result;
    if ((hasVideoMimeType && webResource.getHeight() >= LARGE_VERTICAL_SIZE) || hasEmbeddableMedia) {
      result = MediaTier.T4;
    } else if (hasVideoMimeType || hasLandingPage) {
      result = MediaTier.T1;
    } else {
      result = MediaTier.T0;
    }

    // Done.
    return result;
  }
}
