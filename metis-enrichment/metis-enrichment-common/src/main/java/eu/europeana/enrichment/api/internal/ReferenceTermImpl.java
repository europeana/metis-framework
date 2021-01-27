package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import java.net.URL;
import java.util.Collections;
import java.util.Objects;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ReferenceTermImpl that = (ReferenceTermImpl) o;
    return Objects.equals(getCandidateTypes(), that.getCandidateTypes()) && Objects
            .equals(getReference(), that.getReference());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getCandidateTypes(), getReference());
  }
}
