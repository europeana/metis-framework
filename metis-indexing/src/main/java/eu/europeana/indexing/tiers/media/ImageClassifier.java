package eu.europeana.indexing.tiers.media;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.view.ResolutionTierMetadataData;
import eu.europeana.indexing.tiers.view.ResolutionTierMetadataData.ResolutionTierPreInitializationBuilder;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceWrapper;
import eu.europeana.metis.schema.model.MediaType;

/**
 * Classifier for images.
 */
class ImageClassifier extends AbstractMediaClassifier {

  private static final int RESOLUTION_SMALL = 100_000;
  private static final int RESOLUTION_MEDIUM = 420_000;
  private static final int RESOLUTION_LARGE = 950_000;

  @Override
  MediaTier preClassifyEntity(RdfWrapper entity) {

    // If the entity has no thumbnails, it can only be tier 0.
    return entity.hasThumbnails() ? null : MediaTier.T0;
  }

  @Override
  MediaTier classifyEntityWithoutWebResources(RdfWrapper entity, boolean hasLandingPage) {

    // A record without suitable web resources has tier 0.
    return MediaTier.T0;
  }

  @Override
  MediaTier classifyWebResource(WebResourceWrapper webResource, boolean hasLandingPage, boolean hasEmbeddableMedia) {

    // Check media type.
    if (webResource.getMediaType() != MediaType.IMAGE) {
      return MediaTier.T0;
    }

    // Check resolution.
    final long resolution = webResource.getSize();
    final MediaTier mediaTier;
    if (resolution >= RESOLUTION_LARGE) {
      mediaTier = MediaTier.T4;
    } else if (resolution >= RESOLUTION_MEDIUM) {
      mediaTier = MediaTier.T2;
    } else if (resolution >= RESOLUTION_SMALL) {
      mediaTier = MediaTier.T1;
    } else {
      mediaTier = MediaTier.T0;
    }

    return mediaTier;
  }

  @Override
  ResolutionTierMetadataData extractResolutionTierMetadata(WebResourceWrapper webResource, MediaTier mediaTier) {
    return new ResolutionTierPreInitializationBuilder().setImageResolution(webResource.getSize())
                                                       .setImageResolutionTier(mediaTier).createResolutionTierData();
  }

  @Override
  MediaType getMediaType() {
    return MediaType.IMAGE;
  }
}

