package eu.europeana.indexing.common.contract;

/**
 * Implementations of this interface access, index and query EDM records in a persistence database.
 *
 * @param <R> The type of the record that instances can query.
 */
public interface QueryableRecordPersistence<R> extends RecordPersistence {

  /**
   * Get the record with the given ID.
   * @param rdfAbout The ID of the record to retrieve.
   * @return The record. Can be null if no such record exists.
   */
  R getRecord(String rdfAbout);

}
