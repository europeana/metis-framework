package eu.europeana.indexing.common.contract;

import eu.europeana.indexing.common.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.common.exception.IndexingException;
import eu.europeana.indexing.common.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.common.exception.SetupRelatedIndexingException;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.utils.DepublicationReason;

/**
 * Implementations of this interface access and index EDM tombstone records to a persistence database.
 */
public interface TombstonePersistence {

  /**
   * This indexes a tombstone from a live record according to the provided settings. If no live
   * record was found, no tombstone will be created. The live record will not be removed.
   *
   * @param rdfAbout The live record ID to turn into a tombstone.
   * @param reason The reason for depublishing the record.
   * @return Returns <code>true</code> if the caller can/should proceed with removing the live
   * record. This is the case when an old record was found and a tombstone was created, or if the
   * reason does not allow the creation of tombstones and hence no tombstone was created. Returns
   * <code>false</code> otherwise.
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link SetupRelatedIndexingException} in case an error occurred during indexing setup</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   * @throws UnsupportedOperationException if this indexer is not suitable to create tombstones
   * for existing live records.
   */
  boolean indexTombstoneForLiveRecord(String rdfAbout, DepublicationReason reason)
      throws IndexingException;

  /**
   * This indexes a tombstone according to the provided settings. Sets the current time as the
   * record updated date, and tries to compute the updatedDate from the current state in the DB.
   *
   * @param rdfRecord RDF to publish.
   * @param reason The reason for depublishing the record.
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link SetupRelatedIndexingException} in case an error occurred during indexing setup</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   * @throws IllegalArgumentException if the depublication reason does not allow creating
   * tombstones.
   */
  void indexTombstone(RDF rdfRecord, DepublicationReason reason) throws IndexingException;

  /**
   * This indexes a tombstone according to the provided settings. Sets the current time as the
   * record updated date, and tries to compute the updatedDate from the current state in the DB.
   *
   * @param rdfRecord String representation of the RDF to publish.
   * @param reason The reason for depublishing the record.
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link SetupRelatedIndexingException} in case an error occurred during indexing setup</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   * @throws IllegalArgumentException if the depublication reason does not allow creating
   * tombstones.
   */
  void indexTombstone(String rdfRecord, DepublicationReason reason) throws IndexingException;

}
