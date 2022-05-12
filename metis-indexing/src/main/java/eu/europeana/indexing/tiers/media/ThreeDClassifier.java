package eu.europeana.indexing.tiers.media;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.view.ResolutionTierMetadata;
import eu.europeana.indexing.tiers.view.ResolutionTierMetadata.ResolutionTierMetadataBuilder;
import eu.europeana.indexing.utils.LicenseType;
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

    if(webResource == null){
      return MediaTier.T0;
    }

    MediaTier result = MediaTier.T0;

    if(hasMediaResourceThatIsNotImageOrPdf(webResource) && hasLicenseType(webResource,LicenseType.OPEN)){
      result = MediaTier.T4;
    } else if(hasMediaResourceThatIsNotImageOrPdf(webResource) && hasLicenseType(webResource, LicenseType.RESTRICTED)){
      result = MediaTier.T3;
    } else if(hasMediaResourceThatIsNotImageOrPdf(webResource) && webResource.getLicenseType().isPresent()){
      result = MediaTier.T2;
    } else if(hasLandingPage && onlyContainsShownAtWebResource(webResource) &&
            webResource.getLicenseType().isPresent()){
      result = MediaTier.T1;
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

  private boolean hasMediaResourceThatIsNotImageOrPdf(WebResourceWrapper webResource){
    Set<WebResourceLinkType> extractedLinkTypes = webResource.getLinkTypes();
    String mimeType = webResource.getMimeType();
    return extractedLinkTypes != null && mimeType != null && (extractedLinkTypes.contains(WebResourceLinkType.IS_SHOWN_BY) ||
            extractedLinkTypes.contains(WebResourceLinkType.HAS_VIEW)) && !mimeType.contains("image") &&
            !mimeType.contains("application/pdf");
  }

  private boolean hasLicenseType(WebResourceWrapper webResource, LicenseType licenseType){
    return webResource.getLicenseType().map(otherLicenseType -> otherLicenseType == licenseType).orElse(false);
  }

  private boolean onlyContainsShownAtWebResource(WebResourceWrapper webResource){
    Set<WebResourceLinkType> linkTypes = webResource.getLinkTypes();
    return linkTypes.contains(WebResourceLinkType.IS_SHOWN_AT) &&
            !linkTypes.contains(WebResourceLinkType.IS_SHOWN_BY) &&
            !linkTypes.contains(WebResourceLinkType.HAS_VIEW);
  }
}
