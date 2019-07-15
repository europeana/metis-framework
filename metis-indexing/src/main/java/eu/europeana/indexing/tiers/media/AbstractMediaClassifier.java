package eu.europeana.indexing.tiers.media;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.Tier;
import eu.europeana.indexing.tiers.model.TierClassifier;
import eu.europeana.indexing.utils.LicenseType;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceLinkType;
import eu.europeana.indexing.utils.WebResourceWrapper;
import java.util.EnumSet;
import java.util.List;

/**
 * This is the superclass of all classifiers for specific media types. Classification happens both
 * for the entity as a whole and on individual web resources. This class provides methods for both
 * cases so that subclasses can implement them.
 */
public abstract class AbstractMediaClassifier implements TierClassifier<MediaTier> {

  @Override
  public final MediaTier classify(RdfWrapper entity) {

    // Look at the entity as a whole: we may classify without considering the web resources.
    final MediaTier entityTier = classifyEntity(entity);
    if (entityTier != null) {
      return entityTier;
    }

    // So that did not give an answer. Further analyze the entity.
    final boolean hasLandingPage = !entity
        .getUrlsOfTypes(EnumSet.of(WebResourceLinkType.IS_SHOWN_AT)).isEmpty();
    final boolean hasEmbeddableMedia = EmbeddableMedia.hasEmbeddableMedia(entity);
    final LicenseType entityLicense = entity.getLicenseType();

    // Go by all web resources. Return the maximum tier encountered.
    final List<WebResourceWrapper> webResources = entity.getWebResourceWrappers(EnumSet.of(
        WebResourceLinkType.HAS_VIEW, WebResourceLinkType.IS_SHOWN_BY));
    return webResources.stream()
        .map(resource -> classifyWebResource(resource, entityLicense, hasLandingPage,
            hasEmbeddableMedia))
        .max(Tier.getComparator()).orElse(MediaTier.T0);
  }

  private MediaTier classifyWebResource(WebResourceWrapper webResource, LicenseType entityLicense,
      boolean hasLandingPage, boolean hasEmbeddableMedia) {

    // Perform the classification on the basis of the media.
    final MediaTier tierForResource = classifyWebResource(webResource, hasLandingPage,
        hasEmbeddableMedia);

    // Compute the maximum tier for the license
    final LicenseType resourceLicense = webResource.getLicenseType();
    final MediaTier maxTier;
    if (entityLicense == LicenseType.OPEN || resourceLicense == LicenseType.OPEN) {
      maxTier = MediaTier.T4;
    } else if (entityLicense == LicenseType.RESTRICTED
        || resourceLicense == LicenseType.RESTRICTED) {
      maxTier = MediaTier.T3;
    } else {
      maxTier = MediaTier.T2;
    }

    // Lower the media tier if forced by the license - find the minimum of the two.
    return Tier.min(tierForResource, maxTier);
  }

  /**
   * This method is used to 'preprocess' the entity: we may be able to classify the record without
   * looking at the web resources.
   *
   * @param entity The entity to classify.
   * @return The classification of this entity. Returns null if we can not do it without looking at
   * the web resources.
   */
  abstract MediaTier classifyEntity(RdfWrapper entity);

  /**
   * This method is used to classify a web resource. It does not need to concern itself with the
   * license, it should be concerned with the media only.
   *
   * @param webResource The web resource.
   * @param hasEmbeddableMedia Whether the entity has embeddable media.
   * @param hasLandingPage Whether the entity has a landing page.
   * @return The classification.
   */
  abstract MediaTier classifyWebResource(WebResourceWrapper webResource, boolean hasLandingPage,
      boolean hasEmbeddableMedia);
}
