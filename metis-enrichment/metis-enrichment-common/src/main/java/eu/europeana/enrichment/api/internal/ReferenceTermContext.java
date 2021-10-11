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
    return proxyFieldTypes.stream().map(ProxyFieldType::getEntityType).collect(Collectors.toSet());
  }

  public Set<ProxyFieldType> getProxyFieldTypes() {
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
    // Note: avoid using reference URL for equality as it may do a domain name check.
    return Objects.equals(getProxyFieldTypes(), that.getProxyFieldTypes()) && Objects
            .equals(getReferenceAsString(), that.getReferenceAsString());
  }

  @Override
  public int hashCode() {
    // Note: avoid using reference URL for computing the hash as it may do a domain name check.
    return Objects.hash(getProxyFieldTypes(), getReferenceAsString());
  }
}
