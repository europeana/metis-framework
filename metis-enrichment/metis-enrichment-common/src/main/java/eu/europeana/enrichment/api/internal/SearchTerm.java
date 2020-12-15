package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import java.util.Set;

/**
 * Represents a search term: a text reference (with an optional language) in a record with a list of
 * candidate reference types. Two search terms are equal if and only if they have the same reference
 * (defined by text and language) and candidate types.
 */
public interface SearchTerm {

  Set<EntityType> getCandidateTypes();

  String getTextValue();

  String getLanguage();

  /**
   * Two search terms are equal if and only if they have the same reference (defined by text and
   * language) and candidate types.
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
