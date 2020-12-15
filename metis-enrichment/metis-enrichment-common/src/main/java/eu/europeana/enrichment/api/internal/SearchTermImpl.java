package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import java.util.Collections;
import java.util.Set;

/**
 * This class is a complete but minimal implementation of {@link SearchTerm}.
 */
public class SearchTermImpl extends AbstractSearchTerm {

  private final Set<EntityType> entityTypes;

  public SearchTermImpl(String textValue, String language, Set<EntityType> entityTypes) {
    super(textValue, language);
    this.entityTypes = Set.copyOf(entityTypes);
  }

  @Override
  public Set<EntityType> getCandidateTypes() {
    return Collections.unmodifiableSet(entityTypes);
  }
}
