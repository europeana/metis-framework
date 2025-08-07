package eu.europeana.indexing.common.contract;

import eu.europeana.indexing.common.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.common.exception.IndexingException;
import eu.europeana.indexing.common.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.common.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.Date;

/**
 * Implementations of this interface access and index EDM records in a persistence database.
 * @param <R> The type of the record that this persistence returns.
 */
public interface RecordPersistence<R> {

  /**
   * A wrapper for the dates computed while persisting records.
   *
   * @param updatedDate The updated date that is computed for the record.
   * @param createdDate The created date that is computed for the record.
   */
  record ComputedDates(Date updatedDate, Date createdDate) { }

  /**
   * Get the record with the given ID.
   * @param rdfAbout The ID of the record to retrieve.
   * @return The record. Can be null if no such record exists.
   */
  R getRecord(String rdfAbout);

  /**
   * This indexes for persistence according to the provided settings.
   *
   * @param rdfWrapper RDF to publish.
   * @param preserveUpdateAndCreateTimesFromRdf This regulates whether we should preserve (use) the
   * updated and created dates that are set in the input record or if they should be recomputed
   * using any equivalent record that is currently in the database.
   * @param recordDate The date that would represent the created/updated date of a record
   * @param createdDate The date that would represent the created date if the record already exists,
   * e.g. from a redirected record. Can be null (in which case <code>recordDate</code> would be used).
   * @return The dates that were used for the record.
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link SetupRelatedIndexingException} in case an error occurred during indexing setup</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  ComputedDates indexForPersistence(RdfWrapper rdfWrapper,
      boolean preserveUpdateAndCreateTimesFromRdf, Date recordDate, Date createdDate)
      throws IndexingException;

  /**
   * This indexes for persistence according to the provided settings. Sets the current time as the
   * record updated date, and tries to compute the updatedDate from the current state in the DB.
   *
   * @param rdfRecord RDF to publish.
   * @return The dates that were used for the record.
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link SetupRelatedIndexingException} in case an error occurred during indexing setup</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  ComputedDates indexForPersistence(RDF rdfRecord) throws IndexingException;

  /**
   * This indexes for persistence according to the provided settings. Sets the current time as the
   * record updated date, and tries to compute the updatedDate from the current state in the DB.
   *
   * @param rdfRecord String representation of the RDF to publish.
   * @return The dates that were used for the record.
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link SetupRelatedIndexingException} in case an error occurred during indexing setup</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  ComputedDates indexForPersistence(String rdfRecord) throws IndexingException;

}
