package eu.europeana.enrichment.api.internal;

import java.net.URL;
import java.util.Objects;

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

  @Override
  public final boolean equals(Object otherObject) {
    if (otherObject == this) {
      return true;
    }
    if (!(otherObject instanceof ReferenceTerm)) {
      return false;
    }
    final ReferenceTerm other = (ReferenceTerm) otherObject;
    return Objects.equals(other.getReference(), this.getReference())
            && Objects.equals(other.getCandidateTypes(), this.getCandidateTypes());
  }

  @Override
  public final int hashCode() {
    return Objects.hash(this.getReference(), this.getCandidateTypes());
  }
}
