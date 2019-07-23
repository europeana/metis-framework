package eu.europeana.indexing.tiers.media;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceWrapper;

/**
 * Classifier for text.
 */
class TextClassifier extends AbstractMediaClassifier {

  private static final int RESOLUTION_SMALL = 100_000;
  private static final int RESOLUTION_MEDIUM = 420_000;
  private static final int RESOLUTION_LARGE = 950_000;

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
    final String mimeType = webResource.getMimeType();
    final boolean hasPdfMimeType = mimeType != null && mimeType.startsWith("application/pdf");
    final boolean hasImageMimeType = ImageClassifier.hasImageMimeType(webResource);

    // Find resolution. If not 0, means that resource is an image.
    final long resolution = hasImageMimeType ? webResource.getSize() : 0;

    // Check resolution.
    final MediaTier result;
    if (hasPdfMimeType || resolution >= RESOLUTION_LARGE) {
      result = MediaTier.T4;
    } else if (resolution >= RESOLUTION_MEDIUM) {
      result = MediaTier.T2;
    } else if (hasLandingPage || resolution >= RESOLUTION_SMALL) {
      result = MediaTier.T1;
    } else {
      result = MediaTier.T0;
    }
    return result;
  }
}
