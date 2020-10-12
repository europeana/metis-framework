package eu.europeana.metis.core.util;

import dev.morphia.query.Sort;
import java.util.function.Function;

/**
 * Defines the sorting directions.
 */
public enum SortDirection {

  /**
   * Ascending sort.
   */
  ASCENDING(Sort::ascending),

  /**
   * Descending sort.
   */
  DESCENDING(Sort::descending);

  private final Function<String, Sort> sortCreator;

  SortDirection(Function<String, Sort> sortCreator) {
    this.sortCreator = sortCreator;
  }

  /**
   * Creates a MongoDB sort based on the given database field
   *
   * @param databaseField The database field.
   * @return The MongoDB sort.
   */
  public Sort createSort(String databaseField) {
    return sortCreator.apply(databaseField);
  }
}
