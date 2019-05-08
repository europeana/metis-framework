package eu.europeana.indexing.solr.tiers.media;

import eu.europeana.indexing.solr.tiers.model.MediaTier;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceWrapper;

/**
 * Classifier for audio.
 */
class AudioClassifier extends AbstractMediaClassifier {

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
    final boolean hasSoundMimeType = mimeType != null && mimeType.startsWith("audio");

    // Check the conditions - the conditions for T2-T4 take precedence over those of T1.
    final MediaTier result;
    if (hasSoundMimeType || hasEmbeddableMedia) {
      result = MediaTier.T4;
    } else if (hasLandingPage) {
      result = MediaTier.T1;
    } else {
      result = MediaTier.T0;
    }

    // Done.
    return result;
  }
}
