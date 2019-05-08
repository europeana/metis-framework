package eu.europeana.indexing.utils;

import eu.europeana.corelib.definitions.jibx.Aggregation;
import eu.europeana.corelib.definitions.jibx.ResourceType;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang.StringUtils;

/**
 * This enum lists the various link types that web resources can have. A web resource can have none
 * or multiple of these, depending on how it is referenced in the entity's aggregation.
 */
public enum WebResourceLinkType {

  HAS_VIEW {
    @Override
    List<? extends ResourceType> getResourcesOfType(Aggregation aggregation) {
      return aggregation.getHasViewList();
    }
  },

  IS_SHOWN_AT {
    @Override
    List<? extends ResourceType> getResourcesOfType(Aggregation aggregation) {
      return Collections.singletonList(aggregation.getIsShownAt());
    }
  },

  IS_SHOWN_BY {
    @Override
    List<? extends ResourceType> getResourcesOfType(Aggregation aggregation) {
      return Collections.singletonList(aggregation.getIsShownBy());
    }
  },

  OBJECT {
    @Override
    List<? extends ResourceType> getResourcesOfType(Aggregation aggregation) {
      return Collections.singletonList(aggregation.getObject());
    }
  };


  abstract List<? extends ResourceType> getResourcesOfType(Aggregation aggregation);

  /**
   * This method retrieves all URLs (as {@link String} objects) that the entity contains of this
   * link type.
   *
   * @param entity The entity for which to get the URLs.
   * @return The URLs. They are not blank or null. The list is not null, but could be empty.
   */
  public Set<String> getUrlsOfType(RdfWrapper entity) {
    return entity.getAggregations().stream().map(this::getResourcesOfType).filter(Objects::nonNull)
        .flatMap(List::stream).filter(Objects::nonNull).map(ResourceType::getResource)
        .filter(StringUtils::isNotBlank).collect(Collectors.toSet());
  }

  /**
   * This method creates a map of all web resource URLs in this entity with the given link types.
   *
   * @param entity The entity to analyze.
   * @param types The types to which we limit our search. We only return web resources that have at
   * least one of the given types.
   * @return The map of URLs to link types. The link types will include all types with which a given
   * URL occurs, not just those those that we searched for.
   */
  public static Map<String, Set<WebResourceLinkType>> getAllLinksForTypes(RdfWrapper entity,
      Set<WebResourceLinkType> types) {

    // All types with the urls that have that type. This is the complete overview.
    final Map<WebResourceLinkType, Set<String>> urlsByType = Stream.of(values())
        .collect(Collectors.toMap(Function.identity(), type -> type.getUrlsOfType(entity)));

    // The result map with empty type lists. Only contains the urls with one of the required types.
    final Map<String, Set<WebResourceLinkType>> result = types.stream().map(urlsByType::get)
        .flatMap(Set::stream)
        .collect(Collectors.toMap(Function.identity(), url -> new HashSet<>()));

    // Add the right types to the urls.
    for (Entry<WebResourceLinkType, Set<String>> typeWithUrls : urlsByType.entrySet()) {
      for (String url : typeWithUrls.getValue()) {
        if (result.containsKey(url)) {
          result.get(url).add(typeWithUrls.getKey());
        }
      }
    }

    // Done.
    return result;
  }
}
