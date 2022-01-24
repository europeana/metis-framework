package eu.europeana.indexing.tiers.media;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.view.ResolutionTierMetadata;
import eu.europeana.indexing.tiers.view.ResolutionTierMetadata.ResolutionTierMetadataBuilder;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceWrapper;
import eu.europeana.metis.schema.model.MediaType;
import org.apache.commons.lang3.StringUtils;

/**
 * Classifier for 3D objects.
 */
class ThreeDClassifier extends AbstractMediaClassifier {

  @Override
  MediaTier preClassifyEntity(RdfWrapper entity) {

    // We always have to look at the web resources.
    return null;
  }

  @Override
  MediaTier classifyEntityWithoutWebResources(RdfWrapper entity, boolean hasLandingPage) {

    // A record without suitable web resources has tier 0.
    return MediaTier.T0;
  }

  @Override
  MediaTier classifyWebResource(WebResourceWrapper webResource, boolean hasLandingPage, boolean hasEmbeddableMedia) {

    // T2-T4 if there is a mime type (any whatsoever), T0 otherwise.
    return StringUtils.isNotBlank(webResource.getMimeType()) ? MediaTier.T4 : MediaTier.T0;
  }

  @Override
  ResolutionTierMetadata extractResolutionTierMetadata(WebResourceWrapper webResource, MediaTier mediaTier) {
    return new ResolutionTierMetadataBuilder().build();
  }

  @Override
  MediaType getMediaType() {
    return MediaType.THREE_D;
  }
}
