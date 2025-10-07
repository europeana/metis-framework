package eu.europeana.indexing.common.contract;

/**
 * Implementations of this interface access, index and query EDM tombstone records in a persistence
 * database.
 *
 * @param <T> The type of the tombstone that instances can query.
 */
public interface QueryableTombstonePersistence<T> extends TombstonePersistence {

  /**
   * Get the tombstone with the given ID.
   *
   * @param rdfAbout The ID of the tombstone to retrieve.
   * @return The tombstone. Can be null if no such tombstone exists.
   */
  T getTombstone(String rdfAbout);

}
