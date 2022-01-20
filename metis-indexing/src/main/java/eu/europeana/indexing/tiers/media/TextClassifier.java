package eu.europeana.indexing.tiers.media;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.view.MediaResourceTechnicalMetadata.ResolutionTierPreInitializationBuilder;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceWrapper;
import eu.europeana.metis.schema.model.MediaType;

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
      boolean hasEmbeddableMedia, ResolutionTierPreInitializationBuilder resolutionTierPreInitializationBuilder) {

    // Check mime type.
    final String mimeType = webResource.getMimeType();
    final boolean hasPdfMimeType = mimeType != null && mimeType.startsWith("application/pdf");
    final boolean hasImageMimeType = webResource.getMediaType() == MediaType.IMAGE;

    // Find resolution. If not 0, means that resource is an image.
    final long resolution = hasImageMimeType ? webResource.getSize() : 0;

    // Check resolution.
    final MediaTier mediaTier;
    if (hasPdfMimeType || resolution >= RESOLUTION_LARGE) {
      mediaTier = MediaTier.T4;
    } else if (resolution >= RESOLUTION_MEDIUM) {
      mediaTier = MediaTier.T2;
    } else if (hasLandingPage || resolution >= RESOLUTION_SMALL) {
      mediaTier = MediaTier.T1;
    } else {
      mediaTier = MediaTier.T0;
    }

    //Extend builder
    resolutionTierPreInitializationBuilder.setImageResolution(webResource.getSize())
                                          .setImageResolutionTier(mediaTier);

    return mediaTier;
  }

  @Override
  MediaType getMediaType() {
    return MediaType.TEXT;
  }
}
