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
import org.apache.commons.lang3.StringUtils;

/**
 * This is the superclass of all classifiers for specific media types. Classification happens both
 * for the entity as a whole and on individual web resources. This class provides methods for both
 * cases so that subclasses can implement them.
 */
public abstract class AbstractMediaClassifier implements TierClassifier<MediaTier> {

  @Override
  public final MediaTier classify(RdfWrapper entity) {

    // Look at the entity as a whole: we may classify without considering the web resources.
    final MediaTier entityTier = preClassifyEntity(entity);
    if (entityTier != null) {
      return entityTier;
    }

    // Find candidate web resources and determine whether there is a landing page.
    final List<WebResourceWrapper> webResources = entity.getWebResourceWrappers(EnumSet.of(
        WebResourceLinkType.HAS_VIEW, WebResourceLinkType.IS_SHOWN_BY));
    final boolean hasLandingPage = entity
        .getWebResourceWrappers(EnumSet.of(WebResourceLinkType.IS_SHOWN_AT)).stream()
        .map(WebResourceWrapper::getMimeType).anyMatch(StringUtils::isNotBlank);

    // Compute the media tier based on whether it has suitable web resources.
    final MediaTier result;
    if (webResources.isEmpty()) {
      result = classifyEntityWithoutWebResources(entity, hasLandingPage);
    } else {

      // Go by all web resources. Return the maximum tier encountered.
      final boolean hasEmbeddableMedia = EmbeddableMedia.hasEmbeddableMedia(entity);
      final LicenseType entityLicense = entity.getLicenseType();
      result = webResources.stream()
          .map(resource -> classifyWebResource(resource, entityLicense, hasLandingPage,
              hasEmbeddableMedia)).reduce(MediaTier.T0, Tier::max);
    }

    // Done
    return result;
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
   * looking at the web resources. This method is always called.
   *
   * @param entity The entity to classify.
   * @return The classification of this entity. Returns null if we can not do it without looking at
   * the web resources.
   */
  abstract MediaTier preClassifyEntity(RdfWrapper entity);

  /**
   * This method is used to classify an entity without suitable web resources. If this method is
   * called for an entity, the method {@link #classifyWebResource(WebResourceWrapper, boolean,
   * boolean)} is not called for it. Otherwise, it is.
   *
   * @param entity The entity to classify.
   * @param hasLandingPage Whether the entity has a landing page.
   * @return The classification of this entity. Is not null.
   */
  abstract MediaTier classifyEntityWithoutWebResources(RdfWrapper entity, boolean hasLandingPage);

  /**
   * This method is used to classify a web resource. It does not need to concern itself with the
   * license, it should be concerned with the media only. If this method is called for an entity,
   * the method {@link #classifyEntityWithoutWebResources(RdfWrapper, boolean)} is not called for
   * it. Otherwise, it is, once for each suitable web resource.
   *
   * @param webResource The web resource.
   * @param hasEmbeddableMedia Whether the entity has embeddable media.
   * @param hasLandingPage Whether the entity has a landing page.
   * @return The classification.
   */
  abstract MediaTier classifyWebResource(WebResourceWrapper webResource, boolean hasLandingPage,
      boolean hasEmbeddableMedia);
}
