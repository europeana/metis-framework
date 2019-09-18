package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.corelib.definitions.jibx.Aggregation;
import eu.europeana.corelib.definitions.jibx.HasView;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.ResourceType;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

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
    final Function<String, List<UrlType>> listProd = k -> new ArrayList<>();
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

  /**
   * Get the link (URL) for the first link in the document of the given type. This looks in all
   * aggregations and finds the first one with a resource link of the given type, and returns the
   * first of these.
   *
   * @param type The type of the link we are looking for.
   * @return The link, or an empty {@link Optional} in case no such link exists.
   */
  Optional<String> getFirstResourceOfType(UrlType type) {
    return stream(rdf.getAggregationList())
        .flatMap(aggregation -> getLinksOfType(aggregation, type)).filter(Objects::nonNull)
        .map(ResourceType::getResource).filter(StringUtils::isNotBlank).findFirst();
  }

  private static Stream<ResourceType> getLinksOfType(Aggregation aggregation, UrlType type) {
    final Stream<ResourceType> result;
    switch (type) {
      case OBJECT:
        result = Stream.of(aggregation.getObject());
        break;
      case HAS_VIEW:
        result = stream(aggregation.getHasViewList());
        break;
      case IS_SHOWN_AT:
        result = Stream.of(aggregation.getIsShownAt());
        break;
      case IS_SHOWN_BY:
        result = Stream.of(aggregation.getIsShownBy());
        break;
      default:
        throw new IllegalStateException();
    }
    return result;
  }

  /**
   * Gets the web resource for a given URL (i.e. about value).
   * @param resourceUrl The URL for which to obtain the web resource.
   * @return The web resource, or an empty {@link Optional} if it doesn't exist.
   */
  Optional<WebResourceType> getWebResource(String resourceUrl) {
    return stream(rdf.getWebResourceList())
        .filter(webResource -> resourceUrl.equals(webResource.getAbout())).findAny();
  }

  private static <T> Stream<T> stream(List<? extends T> list) {
    return Optional.ofNullable(list).map(List::stream).orElseGet(Stream::empty)
        .filter(Objects::nonNull).map(item -> (T) item);
  }
}
