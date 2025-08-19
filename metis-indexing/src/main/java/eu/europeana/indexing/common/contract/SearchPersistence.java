package eu.europeana.indexing.common.contract;

import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.Date;

/**
 * Implementations of this interface access and index EDM records in a search database.
 */
public interface SearchPersistence extends Persistence {

  /**
   * This indexes for search according to the provided settings.
   *
   * @param rdfWrapper RDF to publish.
   * @param preserveUpdateAndCreateTimesFromRdf This regulates whether we should preserve (use) the
   * updated and created dates that are set in the input record or if they should be recomputed
   * using any equivalent record that is currently in the database.
   * @param updatedDate The date that should be added as updated date in the saved data.
   * If this is null, the current instant ({@link Date#Date()}) is used.
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  void saveRecord(RdfWrapper rdfWrapper, boolean preserveUpdateAndCreateTimesFromRdf,
      Date updatedDate) throws IndexingException;

  /**
   * This indexes for search with the given values for the updated and created date.
   * @param rdfWrapper RDF to publish.
   * @param updatedDate The new value for the updated date. Can be null.
   * @param createdDate An override value for the creation date. Can be null.
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  void saveRecord(RdfWrapper rdfWrapper, Date updatedDate, Date createdDate)
      throws IndexingException;

  /**
   * This indexes for search according to the provided settings. Sets the current time as the
   * record updated date, and tries to compute the updatedDate from the current state in the DB.
   *
   * @param rdfRecord RDF to publish.
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  void saveRecord(RDF rdfRecord) throws IndexingException;

  /**
   * This indexes for search according to the provided settings. Sets the current time as the
   * record updated date, and tries to compute the updatedDate from the current state in the DB.
   *
   * @param rdfRecord String representation of the RDF to publish.
   * @throws IndexingException which can be one of:
   * <ul>
   * <li>{@link IndexerRelatedIndexingException} In case an error occurred during publication.</li>
   * <li>{@link RecordRelatedIndexingException} in case an error occurred related to record
   * contents</li>
   * </ul>
   */
  void saveRecord(String rdfRecord) throws IndexingException;

  /**
   * Removes the record with the given ID.
   * @param rdfAbout The ID of the record to remove.
   * @throws IndexerRelatedIndexingException In case of issues.
   */
  void removeRecord(String rdfAbout) throws IndexerRelatedIndexingException;

  /**
   * Removes a dataset.
   * @param datasetId The ID of the dataset to remove.
   * @param maxUpdatedDate If not null, only removes records that have been updated strictly before
   *                       this date.
   * @throws IndexerRelatedIndexingException In case of issues.
   */
  void removeDataset(String datasetId, Date maxUpdatedDate) throws IndexerRelatedIndexingException;
}
