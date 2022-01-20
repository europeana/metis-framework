package eu.europeana.indexing.tiers.media;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceWrapper;
import eu.europeana.metis.schema.model.MediaType;

/**
 * Classifier for videos.
 */
class VideoClassifier extends AbstractMediaClassifier {

  private static final int LARGE_VERTICAL_SIZE = 480;

  @Override
  MediaTier preClassifyEntity(RdfWrapper entity) {

    // We always have to look at the web resources.
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
    final boolean hasVideoMimeType = webResource.getMediaType() == MediaType.VIDEO;

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

  @Override
  MediaType getMediaType() {
    return MediaType.VIDEO;
  }
}
