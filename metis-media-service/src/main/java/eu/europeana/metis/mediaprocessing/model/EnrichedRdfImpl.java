package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.corelib.definitions.jibx.Preview;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.SpatialResolution;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of an RDF file that can be enriched with extracted resource metadata.
 */
public class EnrichedRdfImpl extends RdfWrapper implements EnrichedRdf {

  private final Map<String, Set<String>> thumbnailTargetNames = new HashMap<>();

  /**
   * Constructor.
   *
   * @param rdf The RDF to enrich.
   */
  public EnrichedRdfImpl(RDF rdf) {
    super(rdf);
  }

  private WebResource getOrCreateWebResource(String url) {
    if (getRdf().getWebResourceList() == null) {
      getRdf().setWebResourceList(new ArrayList<>());
    }
    for (WebResourceType resource : getRdf().getWebResourceList()) {
      if (resource.getAbout().equals(url)) {
        return new WebResource(resource);
      }
    }
    WebResourceType resource = new WebResourceType();
    resource.setAbout(url);
    getRdf().getWebResourceList().add(resource);
    return new WebResource(resource);
  }

  @Override
  public void enrichResource(ResourceMetadata resource) {
    final WebResource webResource = getOrCreateWebResource(resource.getResourceUrl());
    resource.getMetaData().updateResource(webResource);
    thumbnailTargetNames.put(resource.getResourceUrl(), resource.getThumbnailTargetNames());
  }

  @Override
  public RDF finalizeRdf() {

    // Compute the edm preview URL and set it in the RDF
    final String edmPreviewUrl = getEdmPreviewThumbnailUrl();
    updateEdmPreview(edmPreviewUrl);

    // Done: return the RDF.
    return getRdf();
  }

  /**
   * Determine the value of edm:preview. This is the URL pointing to the thumbnail of the
   * edm:object, if such a thumbnail is available. Otherwise get the thumbnail from either
   * edm:isShownBy or the first edm:hasView. If both of these links exist and have a thumbnail,
   * return the one referring to the image with highest resolution. If there is a tie, take the
   * isShownBy.
   *
   * @return The URL of the thumbnail to be set as edm:preview.
   */
  String getEdmPreviewThumbnailUrl() {

    // First try taking it from the object URL. If it exists, return it.
    final Optional<String> objectThumbnail = getFirstResourceOfType(UrlType.OBJECT)
        .flatMap(this::getWebResource).map(WebResourceType::getAbout)
        .filter(this::hasEligiblePreviewThumbnail);
    if (objectThumbnail.isPresent()) {
      return objectThumbnail.get();
    }

    // That failed. Now we need to look at the isShownBy and the first hasView.
    final Optional<WebResourceType> isShownBy = getFirstResourceOfType(UrlType.IS_SHOWN_BY)
        .flatMap(this::getWebResource);
    final Optional<WebResourceType> hasView = getFirstResourceOfType(UrlType.HAS_VIEW)
        .flatMap(this::getWebResource);
    final BigInteger isShownByResolution = isShownBy.map(WebResourceType::getSpatialResolution)
        .map(SpatialResolution::getInteger).orElse(BigInteger.ZERO);
    final BigInteger hasViewResolution = hasView.map(WebResourceType::getSpatialResolution)
        .map(SpatialResolution::getInteger).orElse(BigInteger.ZERO);
    final Optional<String> isShownByThumbnail = isShownBy.map(WebResourceType::getAbout)
        .filter(this::hasEligiblePreviewThumbnail);
    final Optional<String> hasViewThumbnail = hasView.map(WebResourceType::getAbout)
        .filter(this::hasEligiblePreviewThumbnail);

    // Determine the result based on which one is present.
    final Optional<String> result;
    if (isShownByThumbnail.isPresent()) {
      if (hasViewThumbnail.isPresent() && isShownByResolution.compareTo(hasViewResolution) < 0) {
        // Both are present, and the hasView has a strictly larger resolution: use hasView.
        result = hasViewThumbnail;
      } else {
        // hasView is not present or has a lower (or equal) resolution: use isShownBy.
        result = isShownByThumbnail;
      }
    } else {
      // The isShownBy is not present: use hasView (if that's not present either, return null).
      result = hasViewThumbnail;
    }

    // Done
    return result.orElse(null);
  }

  private boolean hasEligiblePreviewThumbnail(String resourceUrl) {
    return getThumbnailTargetNames(resourceUrl).stream().anyMatch(name -> name.contains("-LARGE"));
  }

  void updateEdmPreview(String url) {
    if (url != null && getRdf().getEuropeanaAggregationList() != null &&
        !getRdf().getEuropeanaAggregationList().isEmpty()) {
      final Preview preview = new Preview();
      preview.setResource(url);
      getRdf().getEuropeanaAggregationList().get(0).setPreview(preview);
    }
  }

  Set<String> getResourceUrls() {
    return Collections.unmodifiableSet(thumbnailTargetNames.keySet());
  }

  Set<String> getThumbnailTargetNames(String resourceUrl) {
    return Optional.ofNullable(thumbnailTargetNames.get(resourceUrl))
        .map(Collections::unmodifiableSet).orElseGet(Collections::emptySet);
  }
}
