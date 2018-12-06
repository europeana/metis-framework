package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.corelib.definitions.jibx.Aggregation;
import eu.europeana.corelib.definitions.jibx.HasView;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.metis.mediaprocessing.UrlType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class RdfWrapper {

  private final RDF rdf;

  public RdfWrapper(RDF rdf) {
    this.rdf = rdf;
  }

  protected RDF getRdf() {
    return rdf;
  }

  /**
   * Finds links to resources listed in this EDM. Each resource can be listed under some URL type.
   *
   * @param urlTypes types of URLs to find
   * @return resource URLs mapped to their types
   */
  public Map<String, List<UrlType>> getResourceUrls(Set<UrlType> urlTypes) {
    Map<String, List<UrlType>> urls = new HashMap<>();
    Function<String, List<UrlType>> listProd = k -> new ArrayList<>();
    for (Aggregation aggregation : rdf.getAggregationList()) {
      if (urlTypes.contains(UrlType.OBJECT) && aggregation.getObject() != null) {
        urls.computeIfAbsent(aggregation.getObject().getResource(), listProd).add(UrlType.OBJECT);
      }
      if (urlTypes.contains(UrlType.HAS_VIEW) && aggregation.getHasViewList() != null) {
        for (HasView hv : aggregation.getHasViewList()) {
          urls.computeIfAbsent(hv.getResource(), listProd).add(UrlType.HAS_VIEW);
        }
      }
      if (urlTypes.contains(UrlType.IS_SHOWN_BY) && aggregation.getIsShownBy() != null) {
        urls.computeIfAbsent(aggregation.getIsShownBy().getResource(), listProd)
            .add(UrlType.IS_SHOWN_BY);
      }
      if (urlTypes.contains(UrlType.IS_SHOWN_AT) && aggregation.getIsShownAt() != null) {
        urls.computeIfAbsent(aggregation.getIsShownAt().getResource(), listProd)
            .add(UrlType.IS_SHOWN_AT);
      }
    }
    return urls;
  }
}
