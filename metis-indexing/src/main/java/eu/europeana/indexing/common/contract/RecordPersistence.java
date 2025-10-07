package eu.europeana.indexing.common.contract;

import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.Date;
import java.util.stream.Stream;

/**
 * Implementations of this interface access and index EDM records in a persistence database.
 */
public interface RecordPersistence extends Persistence {

  /**
   * A wrapper for the dates computed while persisting records.
   *
   * @param updatedDate The updated date that is computed for the record.
   * @param createdDate The created date that is computed for the record.
   */
  record ComputedDates(Date updatedDate, Date createdDate) { }

  /**
   * This saves a record according to the provided settings.
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
  ComputedDates saveRecord(RdfWrapper rdfWrapper,
      boolean preserveUpdateAndCreateTimesFromRdf, Date recordDate, Date createdDate)
      throws IndexingException;

  /**
   * This saves a record according to the provided settings. Sets the current time as the
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
  ComputedDates saveRecord(RDF rdfRecord) throws IndexingException;

  /**
   * This saves a record according to the provided settings. Sets the current time as the
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
  ComputedDates saveRecord(String rdfRecord) throws IndexingException;

  /**
   * Counts the record in a dataset.
   *
   * @param datasetId      The ID of the dataset to count.
   * @param maxUpdatedDate If not null, only counts records that have been updated strictly before
   *                       this date.
   * @throws IndexerRelatedIndexingException In case of issues.
   * @return The number of records in the dataset.
   */
  long countRecords(String datasetId, Date maxUpdatedDate) throws IndexerRelatedIndexingException;

  /**
   * Return all record IDs that belong to the given dataset.
   *
   * @param datasetId The ID of the dataset to search. Is not null.
   * @param maxUpdatedDate If not null, only include IDs for records that have been updated strictly
   *                       before this date.
   * @param batchSize - size of the batch during traversing DB data. To not set it use overloaded
   * method without this parameter. Anyway DB returns data in constrained buffers which means
   * in practice about 300-400 thousands of records in one batch max.
   *
   * @return The record IDs in a stream.
   * @throws IndexerRelatedIndexingException In case of issues.
   */
  Stream<String> getRecordIds(String datasetId, Date maxUpdatedDate, int batchSize)
      throws IndexerRelatedIndexingException;

  /**
   * Return all record IDs that belong to the given dataset.
   *
   * @param datasetId The ID of the dataset to search. Is not null.
   * @param maxUpdatedDate If not null, only include IDs for records that have been updated strictly
   *                       before this date.
   *
   * @return The record IDs in a stream.
   * @throws IndexerRelatedIndexingException In case of issues.
   */
  Stream<String> getRecordIds(String datasetId, Date maxUpdatedDate)
      throws IndexerRelatedIndexingException;

    /**
     * Removes the record with the given ID.
     *
     * @param rdfAbout The ID of the record to remove.
     * @return Whether the remove was successful.
     * @throws IndexerRelatedIndexingException In case of issues.
     */
  boolean removeRecord(String rdfAbout) throws IndexerRelatedIndexingException;

  /**
   * Removes a dataset.
   *
   * @param datasetId      The ID of the dataset to remove.
   * @param maxUpdatedDate If not null, only removes records that have been updated strictly before
   *                       this date.
   * @throws IndexerRelatedIndexingException In case of issues.
   * @return The number of records that were removed.
   */
  long removeDataset(String datasetId, Date maxUpdatedDate) throws IndexerRelatedIndexingException;
}
