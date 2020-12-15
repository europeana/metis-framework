package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

/**
 * This class is a complete but minimal implementation of {@link ReferenceTerm}.
 */
public class ReferenceTermImpl extends AbstractReferenceTerm{

  private final Set<EntityType> entityTypes;

  public ReferenceTermImpl(URL reference, Set<EntityType> entityTypes) {
    super(reference);
    this.entityTypes = Set.copyOf(entityTypes);
  }

  @Override
  public Set<EntityType> getCandidateTypes() {
    return Collections.unmodifiableSet(entityTypes);
  }
}
