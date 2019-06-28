package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.corelib.definitions.jibx.Preview;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
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

  private WebResource getWebResource(String url) {
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
    final WebResource webResource = getWebResource(resource.getResourceUrl());
    resource.getMetaData().updateResource(webResource);
    thumbnailTargetNames.put(resource.getResourceUrl(), resource.getThumbnailTargetNames());
  }

  @Override
  public RDF finalizeRdf() {

    // Compute the edm preview URL and set it in the RDF
    final String edmPreviewUrl = getEdmPreviewUrl();
    updateEdmPreview(edmPreviewUrl);

    // Done: return the RDF.
    return getRdf();
  }

  String getEdmPreviewUrl() {

    // First try taking it from the object URL.
    final Set<String> objectUrls = getResourceUrls(Collections.singleton(UrlType.OBJECT)).keySet();
    if (!objectUrls.isEmpty()) {
      return objectUrls.iterator().next();
    }

    // That failed. Now find the first large thumbnail in a is shown by or has view resource.
    final Set<UrlType> otherTypes = EnumSet.of(UrlType.IS_SHOWN_BY, UrlType.HAS_VIEW);
    final Set<String> otherUrls = getResourceUrls(otherTypes).keySet();
    return getResourceUrls().stream().filter(otherUrls::contains).map(this::getThumbnailTargetNames)
        .map(this::getEligiblePreviewThumbnail).filter(Objects::nonNull).findFirst().orElse(null);
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
