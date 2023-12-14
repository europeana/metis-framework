package eu.europeana.indexing.tiers.media;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.view.ContentTierBreakdown;
import eu.europeana.indexing.tiers.view.ResolutionTierMetadata;
import eu.europeana.indexing.tiers.view.ResolutionTierMetadata.ResolutionTierMetadataBuilder;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceWrapper;
import eu.europeana.metis.schema.model.MediaType;
import java.util.Collections;

/**
 * Classifier for images.
 */
class ImageClassifier extends AbstractMediaClassifier {

  private static final int RESOLUTION_SMALL = 100_000;
  private static final int RESOLUTION_MEDIUM = 420_000;
  private static final int RESOLUTION_LARGE = 950_000;

  @Override
  TierClassification<MediaTier, ContentTierBreakdown> preClassifyEntity(RdfWrapper entity) {

    // If the entity has no thumbnails, it can only be tiered 0.
    return entity.hasThumbnails() ? null : new TierClassification<>(MediaTier.T0, new ContentTierBreakdown.Builder()
        .setThumbnailAvailable(false)
        .setMediaTierBeforeLicenseCorrection(MediaTier.T0)
        .setLicenseType(entity.getLicenseType())
        .setMediaResourceTechnicalMetadataList(Collections.emptyList())
        .build());
  }

  @Override
  MediaTier classifyEntityWithoutWebResources(RdfWrapper entity, boolean hasLandingPage) {

    // A record without suitable web resources is tier 0.
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
  ResolutionTierMetadata extractResolutionTierMetadata(WebResourceWrapper webResource, MediaTier mediaTier) {
    return new ResolutionTierMetadataBuilder().setImageResolution(webResource.getSize())
                                              .setImageResolutionTier(mediaTier).build();
  }

  @Override
  MediaType getMediaType() {
    return MediaType.IMAGE;
  }
}

