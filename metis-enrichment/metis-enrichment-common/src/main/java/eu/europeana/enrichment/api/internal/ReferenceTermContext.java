package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import java.net.URL;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is an implementation of {@link ReferenceTerm} that provides context in the sense that it is
 * aware of the field type(s) in which the reference term was found.
 */
public class ReferenceTermContext extends AbstractReferenceTerm {

  private final Set<ProxyFieldType> proxyFieldTypes;

  public ReferenceTermContext(URL reference, Set<ProxyFieldType> proxyFieldTypes) {
    super(reference);
    this.proxyFieldTypes = Set.copyOf(proxyFieldTypes);
  }

  @Override
  public Set<EntityType> getCandidateTypes() {
    return proxyFieldTypes.stream().map(FieldType::getEntityType).collect(Collectors.toSet());
  }

  public Set<FieldType> getProxyFieldTypes() {
    return Collections.unmodifiableSet(proxyFieldTypes);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ReferenceTermContext that = (ReferenceTermContext) o;
    return Objects.equals(getProxyFieldTypes(), that.getProxyFieldTypes()) && Objects
            .equals(getReference(), that.getReference());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getProxyFieldTypes(), getReference());
  }
}
