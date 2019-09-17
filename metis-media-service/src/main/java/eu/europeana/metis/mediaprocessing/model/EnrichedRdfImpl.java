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
import java.util.Map.Entry;
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
    final Optional<String> objectThumbnail = getFirstOrOnlyResourceOfType(UrlType.OBJECT)
        .flatMap(this::getWebResource).map(WebResourceType::getAbout)
        .map(this::getThumbnailTargetNames).map(this::getEligiblePreviewThumbnail);
    if (objectThumbnail.isPresent()) {
      return objectThumbnail.get();
    }

    // That failed. Now we need to look at the isShownBy and the first hasView.
    final Optional<WebResourceType> isShownBy = getFirstOrOnlyResourceOfType(UrlType.IS_SHOWN_AT)
        .flatMap(this::getWebResource);
    final Optional<WebResourceType> hasView = getFirstOrOnlyResourceOfType(UrlType.HAS_VIEW)
        .flatMap(this::getWebResource);
    final BigInteger isShownByResolution = isShownBy.map(WebResourceType::getSpatialResolution)
        .map(SpatialResolution::getInteger).orElse(BigInteger.ZERO);
    final BigInteger hasViewResolution = hasView.map(WebResourceType::getSpatialResolution)
        .map(SpatialResolution::getInteger).orElse(BigInteger.ZERO);
    final Optional<String> isShownByThumbnail = isShownBy.map(WebResourceType::getAbout)
        .map(this::getThumbnailTargetNames).map(this::getEligiblePreviewThumbnail);
    final Optional<String> hasViewThumbnail = hasView.map(WebResourceType::getAbout)
        .map(this::getThumbnailTargetNames).map(this::getEligiblePreviewThumbnail);

    // Determine the result based on which one is present.
    final Optional<String> result;
    if (isShownByThumbnail.isPresent()) {
      if (hasViewThumbnail.isPresent()) {
        if (isShownByResolution.compareTo(hasViewResolution) < 0) {
          // Both are present, but the hasView has a strictly larger resolution.
          result = hasViewThumbnail;
        } else {
          // Both are present, but the isShownBy has at least the resolution the hasView does.
          result = isShownByThumbnail;
        }
      } else {
        // Only the isShownBy is present.
        result = isShownByThumbnail;
      }
    } else {
      // The isShownBy is not present.
      result = hasViewThumbnail;
    }

    // Done
    return result.orElse(null);
  }

  private String getEligiblePreviewThumbnail(Entry<String, Set<String>> targetNames) {
    final boolean containsEligibleThumbnail = targetNames.getValue().stream()
        .anyMatch(name -> name.contains("-LARGE"));
    return containsEligibleThumbnail ? targetNames.getKey() : null;
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

  Entry<String, Set<String>> getThumbnailTargetNames(String resourceUrl) {
    return thumbnailTargetNames.entrySet().stream()
        .filter(entry -> entry.getKey().equals(resourceUrl)).findAny().orElse(null);
  }
}
