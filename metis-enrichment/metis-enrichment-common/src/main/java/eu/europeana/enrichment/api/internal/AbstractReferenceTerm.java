package eu.europeana.enrichment.api.internal;

import java.net.URL;
import java.util.Optional;

/**
 * This class is a basic implementation of {@link ReferenceTerm} that leaves the details of the
 * candidate types unimplemented.
 */
public abstract class AbstractReferenceTerm implements ReferenceTerm {

  private final URL reference;

  public AbstractReferenceTerm(URL reference) {
    this.reference = reference;
  }

  @Override
  public URL getReference() {
    return reference;
  }

  public String getReferenceAsString() {
    return Optional.ofNullable(reference).map(URL::toString).orElse(null);
  }
}
