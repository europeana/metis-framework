package eu.europeana.indexing.utils;

import eu.europeana.corelib.definitions.jibx.Aggregation;
import eu.europeana.corelib.definitions.jibx.ResourceType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * This enum lists the various link types that web resources can have. A web resource can have none
 * or multiple of these, depending on how it is referenced in the entity's aggregation.
 */
public enum WebResourceLinkType {

  HAS_VIEW(aggregation -> new ArrayList<>(aggregation.getHasViewList())),

  IS_SHOWN_AT(aggregation -> Collections.singletonList(aggregation.getIsShownAt())),

  IS_SHOWN_BY(aggregation -> Collections.singletonList(aggregation.getIsShownBy())),

  OBJECT(aggregation -> Collections.singletonList(aggregation.getObject()));

  private final Function<Aggregation, List<ResourceType>> resourceExtractor;

  WebResourceLinkType(Function<Aggregation, List<ResourceType>> resourceExtractor) {
    this.resourceExtractor = resourceExtractor;
  }

  final List<ResourceType> getResourcesOfType(Aggregation aggregation) {
    return resourceExtractor.apply(aggregation);
  }
}
