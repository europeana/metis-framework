package eu.europeana.indexing.tiers.media;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceWrapper;
import eu.europeana.metis.utils.MediaType;

/**
 * Classifier for audio.
 */
class AudioClassifier extends AbstractMediaClassifier {

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

    // Check the conditions - the conditions for T2-T4 take precedence over those of T1.
    final MediaTier result;
    if (hasEmbeddableMedia || webResource.getMediaType() == MediaType.AUDIO) {
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
