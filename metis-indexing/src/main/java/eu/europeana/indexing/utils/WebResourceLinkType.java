package eu.europeana.indexing.utils;

import eu.europeana.metis.schema.jibx.Aggregation;
import eu.europeana.metis.schema.jibx.ResourceType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * This enum lists the various link types that web resources can have. A web resource can have none
 * or multiple of these, depending on how it is referenced in the entity's aggregation.
 */
public enum WebResourceLinkType {

  HAS_VIEW(createResourceListExtractor(Aggregation::getHasViewList)),

  IS_SHOWN_AT(createSingletonResourceExtractor(Aggregation::getIsShownAt)),

  IS_SHOWN_BY(createSingletonResourceExtractor(Aggregation::getIsShownBy)),

  OBJECT(createSingletonResourceExtractor(Aggregation::getObject));

  private final Function<Aggregation, List<ResourceType>> resourceExtractor;

  WebResourceLinkType(Function<Aggregation, List<ResourceType>> resourceExtractor) {
    this.resourceExtractor = resourceExtractor;
  }

  private static Function<Aggregation, List<ResourceType>> createSingletonResourceExtractor(
      Function<Aggregation, ? extends ResourceType> propertyGetter) {
    return propertyGetter
        .andThen(property -> property == null ? null : Collections.singletonList(property));
  }

  private static Function<Aggregation, List<ResourceType>> createResourceListExtractor(
      Function<Aggregation, List<? extends ResourceType>> propertyGetter) {
    return propertyGetter
        .andThen(properties -> properties == null ? null : new ArrayList<>(properties));
  }

  final List<ResourceType> getResourcesOfType(Aggregation aggregation) {
    return resourceExtractor.apply(aggregation);
  }
}
