package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.corelib.definitions.jibx.Aggregation;
import eu.europeana.corelib.definitions.jibx.HasView;
import eu.europeana.corelib.definitions.jibx.RDF;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * This class provides extra functionality in relation to an RDF file.
 */
public class RdfWrapper {

  private final RDF rdf;

  /**
   * Constructor.
   *
   * @param rdf The RDF file.
   */
  public RdfWrapper(RDF rdf) {
    this.rdf = rdf;
  }

  /**
   * @return The RDF file.
   */
  protected RDF getRdf() {
    return rdf;
  }

  /**
   * Retrieves resources from the RDF that are referenced with one of the given reference URL
   * types.
   *
   * @param urlTypes The URL types with which a resource is to be referenced.
   * @return A map containing the resource URLs as keys and their respective reference URL types as
   * values.
   */
  public Map<String, List<UrlType>> getResourceUrls(Set<UrlType> urlTypes) {

    // Sanity check.
    final Map<String, List<UrlType>> urls = new HashMap<>();
    if (rdf.getAggregationList() == null) {
      return urls;
    }

    // Go by all aggregations.
    Function<String, List<UrlType>> listProd = k -> new ArrayList<>();
    for (Aggregation aggregation : rdf.getAggregationList()) {

      // Add object link.
      if (urlTypes.contains(UrlType.OBJECT) && aggregation.getObject() != null) {
        urls.computeIfAbsent(aggregation.getObject().getResource(), listProd).add(UrlType.OBJECT);
      }

      // Add has view links.
      if (urlTypes.contains(UrlType.HAS_VIEW) && aggregation.getHasViewList() != null) {
        for (HasView hv : aggregation.getHasViewList()) {
          urls.computeIfAbsent(hv.getResource(), listProd).add(UrlType.HAS_VIEW);
        }
      }

      // Add is shown by link
      if (urlTypes.contains(UrlType.IS_SHOWN_BY) && aggregation.getIsShownBy() != null) {
        urls.computeIfAbsent(aggregation.getIsShownBy().getResource(), listProd)
            .add(UrlType.IS_SHOWN_BY);
      }

      // Add is shown at link
      if (urlTypes.contains(UrlType.IS_SHOWN_AT) && aggregation.getIsShownAt() != null) {
        urls.computeIfAbsent(aggregation.getIsShownAt().getResource(), listProd)
            .add(UrlType.IS_SHOWN_AT);
      }
    }

    // Done.
    return urls;
  }
}
