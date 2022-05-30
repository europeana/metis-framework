package eu.europeana.indexing.tiers.media;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.view.ResolutionTierMetadata;
import eu.europeana.indexing.tiers.view.ResolutionTierMetadata.ResolutionTierMetadataBuilder;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceWrapper;
import eu.europeana.metis.schema.model.MediaType;

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

    // A record without web resources has tier 1 if there is a landing page, otherwise tier 0.
    return hasLandingPage ? MediaTier.T1 : MediaTier.T0;
  }

  @Override
  MediaTier classifyWebResource(WebResourceWrapper webResource, boolean hasLandingPage, boolean hasEmbeddableMedia) {
    final MediaTier result;
    if (webResource != null && mimeTypeIsNotImageOrApplicationPdf(webResource)) {
      result = MediaTier.T4;
    } else {
      result = MediaTier.T0;
    }
    return result;
  }

  @Override
  ResolutionTierMetadata extractResolutionTierMetadata(WebResourceWrapper webResource, MediaTier mediaTier) {
    return new ResolutionTierMetadataBuilder().build();
  }

  @Override
  MediaType getMediaType() {
    return MediaType.THREE_D;
  }

  private boolean mimeTypeIsNotImageOrApplicationPdf(WebResourceWrapper webResource){
    String mimeType = webResource.getMimeType();
    return mimeType != null && webResource.getMediaType() != MediaType.IMAGE && !mimeType.startsWith("application/pdf");
  }
}
