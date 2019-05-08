package eu.europeana.indexing.solr.tiers.media;

import eu.europeana.indexing.solr.tiers.model.MediaTier;
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
  MediaTier classifyEntity(RdfWrapper entity) {

    // We always have to look at the web resources.
    return null;
  }

  @Override
  MediaTier classifyWebResource(WebResourceWrapper webResource, boolean hasLandingPage,
      boolean hasEmbeddableMedia) {

    // Check mime type.
    final String mimeType = webResource.getMimeType();
    final boolean hasPdfMimeType = mimeType != null && mimeType.startsWith("application/pdf");
    final boolean hasImageMimeType = ImageClassifier.hasImageMimeType(webResource);

    // Find resolution. If not 0, means that resource is either image or PDF.
    final long resolution;
    if (hasPdfMimeType) {
      resolution = webResource.getSpatialResolution();
    } else if (hasImageMimeType) {
      resolution = webResource.getSize();
    } else {
      resolution = 0;
    }

    // Check resolution.
    final MediaTier result;
    if (resolution >= RESOLUTION_LARGE) {
      result = MediaTier.T4;
    } else if (resolution >= RESOLUTION_MEDIUM) {
      result = MediaTier.T2;
    } else if (hasLandingPage || (hasImageMimeType && resolution >= RESOLUTION_SMALL)) {
      result = MediaTier.T1;
    } else {
      result = MediaTier.T0;
    }
    return result;
  }
}
