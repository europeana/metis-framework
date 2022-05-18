package eu.europeana.indexing.tiers.media;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.view.ResolutionTierMetadata;
import eu.europeana.indexing.tiers.view.ResolutionTierMetadata.ResolutionTierMetadataBuilder;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceLinkType;
import eu.europeana.indexing.utils.WebResourceWrapper;
import eu.europeana.metis.schema.model.MediaType;

import java.util.Set;

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

    final MediaTier result;

    if(webResource == null){
      result = MediaTier.T0;
    } else if(mimeTypeIsNotImageOrApplicationPdf(webResource) && containsIsShownByOrHasViewWebResource(webResource)){
      result = MediaTier.T4;
    } else if(hasLandingPage && onlyContainsShownAtWebResource(webResource)){
      result = MediaTier.T1;
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

  private boolean containsIsShownByOrHasViewWebResource(WebResourceWrapper webResource){
    Set<WebResourceLinkType> extractedLinkTypes = webResource.getLinkTypes();
    return extractedLinkTypes != null && (extractedLinkTypes.contains(WebResourceLinkType.IS_SHOWN_BY) ||
            extractedLinkTypes.contains(WebResourceLinkType.HAS_VIEW));
  }

  private boolean onlyContainsShownAtWebResource(WebResourceWrapper webResource){
    Set<WebResourceLinkType> linkTypes = webResource.getLinkTypes();
    return linkTypes.contains(WebResourceLinkType.IS_SHOWN_AT) &&
            !linkTypes.contains(WebResourceLinkType.IS_SHOWN_BY) &&
            !linkTypes.contains(WebResourceLinkType.HAS_VIEW);
  }
}
