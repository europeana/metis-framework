package eu.europeana.indexing.tiers.media;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.view.ResolutionTierMetadataData;
import eu.europeana.indexing.tiers.view.ResolutionTierMetadataData.ResolutionTierPreInitializationBuilder;
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
  MediaTier classifyWebResource(WebResourceWrapper webResource, boolean hasLandingPage, boolean hasEmbeddableMedia) {

    // Check mime type.
    final boolean hasVideoMimeType = webResource.getMediaType() == MediaType.VIDEO;

    // Check the conditions - the conditions for T2-T4 take precedence over those of T1.
    final MediaTier mediaTier;
    if ((hasVideoMimeType && webResource.getHeight() >= LARGE_VERTICAL_SIZE) || hasEmbeddableMedia) {
      mediaTier = MediaTier.T4;
    } else if (hasVideoMimeType || hasLandingPage) {
      mediaTier = MediaTier.T1;
    } else {
      mediaTier = MediaTier.T0;
    }

    return mediaTier;
  }

  @Override
  ResolutionTierMetadataData extractResolutionTierMetadata(WebResourceWrapper webResource, MediaTier mediaTier) {
    return new ResolutionTierPreInitializationBuilder().setVerticalResolution(webResource.getHeight())
                                                       .setVerticalResolutionTier(mediaTier).createResolutionTierData();
  }


  @Override
  MediaType getMediaType() {
    return MediaType.VIDEO;
  }
}
