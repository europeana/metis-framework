package eu.europeana.enrichment.api.internal;

import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import java.util.Set;

/**
 * Implementations of this interface capture the content of a term (to be enriched).
 */
public interface TermContext {

  /**
   * Return the field types in which this term occurs.
   *
   * @return The field types.
   */
  Set<FieldType<? extends AboutType>> getFieldTypes();

  /**
   * Returns whether the term value is equal to the given value (literal or reference).
   * @param resourceOrLiteralType The value to compare with.
   * @return Whether this term's value is equal to that of the given value.
   */
  boolean valueEquals(ResourceOrLiteralType resourceOrLiteralType);
}
