package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import java.net.URL;
import java.util.Set;

/**
 * Represents a reference term: a URL reference in a record with a list of candidate reference
 * types. Two reference terms are equal if and only if they have the same reference and candidate
 * types.
 */
public interface ReferenceTerm {

  Set<EntityType> getCandidateTypes();

  URL getReference();

  /**
   * Two search terms are equal if and only if they have the same reference and candidate types.
   *
   * @param otherObject The other object.
   * @return Whether the other object and this object are equal.
   */
  boolean equals(Object otherObject);

  /**
   * This method is consistent with {@link #equals(Object)} in the sense that if two objects are
   * equal, they will yield the same hash value.
   *
   * @return The object's hash code.
   */
  int hashCode();
}
