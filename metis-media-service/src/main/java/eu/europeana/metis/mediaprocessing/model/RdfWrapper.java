package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.metis.schema.jibx.Aggregation;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.schema.jibx.ResourceType;
import eu.europeana.metis.schema.jibx.WebResourceType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    return switch (type) {
      case OBJECT -> Stream.of(aggregation.getObject());
      case HAS_VIEW -> stream(aggregation.getHasViewList());
      case IS_SHOWN_AT -> Stream.of(aggregation.getIsShownAt());
      case IS_SHOWN_BY -> Stream.of(aggregation.getIsShownBy());
    };
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
