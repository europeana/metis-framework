package eu.europeana.indexing.tiers.metadata;

import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.AgentType;
import eu.europeana.metis.schema.jibx.Concept;
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.TimeSpanType;

/**
 * This enum contains all the groups of enabling elements, including the contextual class with which this group is associated.
 */
public enum ContextualClassGroup {

  TEMPORAL(TimeSpanType.class),
  CONCEPTUAL(Concept.class),
  PERSONAL(AgentType.class),
  GEOGRAPHICAL(PlaceType.class);

  private final Class<? extends AboutType> contextualClass;

  ContextualClassGroup(Class<? extends AboutType> contextualClass) {
    this.contextualClass = contextualClass;
  }

  Class<? extends AboutType> getContextualClass() {
    return contextualClass;
  }
}
