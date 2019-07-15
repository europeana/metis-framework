package eu.europeana.indexing.tiers.media;

import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.indexing.utils.WebResourceWrapper;
import org.apache.commons.lang3.StringUtils;

/**
 * Classifier for 3D objects.
 */
class ThreeDClassifier extends AbstractMediaClassifier {

  @Override
  MediaTier classifyEntity(RdfWrapper entity) {

    // We always have to look at the web resources.
    return null;
  }

  @Override
  MediaTier classifyWebResource(WebResourceWrapper webResource, boolean hasLandingPage,
      boolean hasEmbeddableMedia) {

    // T2-T4 if there is a mime type (any whatsoever), T0 otherwise.
    return StringUtils.isNotBlank(webResource.getMimeType()) ? MediaTier.T4 : MediaTier.T0;
  }
}
