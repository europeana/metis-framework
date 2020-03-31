package eu.europeana.indexing;

import java.io.Closeable;
import java.util.Date;
import java.util.List;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.indexing.exception.IndexingException;

/**
 * <p>
 * This interface allows access to this library's indexing functionality. Note: the object is {@link
 * Closeable} and must be closed after use by calling {@link #close()} (or by using a try block).
 * </p>
 * <p>
 * <b>NOTE:</b> Operations that are provided by this object are <b>not</b> done within a
 * transactions. More details are provided in the documentation for the individual methods.
 * </p>
 *
 * @author jochen
 */
public interface Indexer extends Closeable {

  /**
   * <p>
   * This method indexes a single record, publishing it to the provided data stores.
   * </p>
   * <p>
   * <b>NOTE:</b> this operation should not coincide with a remove operation as this operation is
   * not done within a transaction.
   * </p>
   *
   * @param record The record to index.
   * @param recordDate The date that would represent the created/updated date of a record
   * @param preserveUpdateAndCreateTimesFromRdf This determines whether this indexer should use the
   * updated and created times from the incoming RDFs, or whether it computes its own.
   * @param datasetIdsForRedirection The dataset ids that their records need to be redirected
   * @param performRedirects flag that indicates if redirect should be performed
   * @throws IndexingException In case a problem occurred during indexing.
   */
  void indexRdf(RDF record, Date recordDate, boolean preserveUpdateAndCreateTimesFromRdf,
      List<String> datasetIdsForRedirection, boolean performRedirects)
      throws IndexingException;

  /**
   * <p>
   * This method indexes a list of records, publishing it to the provided data stores.
   * </p>
   * <p>
   * <b>NOTE:</b> this operation should not coincide with a remove operation as this operation is
   * not done within a transaction.
   * </p>
   *
   * @param records The records to index.
   * @param recordDate The date that would represent the created/updated date of a record
   * @param preserveUpdateAndCreateTimesFromRdf This determines whether this indexer should use the
   * updated and created times from the incoming RDFs, or whether it computes its own.
   * @param datasetIdsForRedirection The dataset ids that their records need to be redirected
   * @param performRedirects flag that indicates if redirect should be performed
   * @throws IndexingException In case a problem occurred during indexing.
   */
  void indexRdfs(List<RDF> records, Date recordDate, boolean preserveUpdateAndCreateTimesFromRdf,
      List<String> datasetIdsForRedirection, boolean performRedirects) throws IndexingException;

  /**
   * <p>
   * This method indexes a single record, publishing it to the provided data stores.
   * </p>
   * <p>
   * <b>NOTE:</b> this operation should not coincide with a remove operation as this operation is
   * not done within a transaction.
   * </p>
   *
   * @param record The record to index (can be parsed to RDF).
   * @param recordDate The date that would represent the created/updated date of a record
   * @param preserveUpdateAndCreateTimesFromRdf This determines whether this indexer should use the
   * updated and created times from the incoming RDFs, or whether it computes its own.
   * @param datasetIdsForRedirection The dataset ids that their records need to be redirected
   * @param performRedirects flag that indicates if redirect should be performed
   * @throws IndexingException In case a problem occurred during indexing.
   */
  void index(String record, Date recordDate, boolean preserveUpdateAndCreateTimesFromRdf,
      List<String> datasetIdsForRedirection, boolean performRedirects)
      throws IndexingException;

  /**
   * <p>
   * This method indexes a list of records, publishing it to the provided data stores.
   * </p>
   * <p>
   * <b>NOTE:</b> this operation should not coincide with a remove operation as this operation is
   * not done within a transaction.
   * </p>
   *
   * @param records The records to index (can be parsed to RDF).
   * @param recordDate The date that would represent the created/updated date of a record
   * @param preserveUpdateAndCreateTimesFromRdf This determines whether this indexer should use the
   * updated and created times from the incoming RDFs, or whether it computes its own.
   * @param datasetIdsForRedirection The dataset ids that their records need to be redirected
   * @param performRedirects flag that indicates if redirect should be performed
   * @throws IndexingException In case a problem occurred during indexing.
   */
  void index(List<String> records, Date recordDate, boolean preserveUpdateAndCreateTimesFromRdf,
      List<String> datasetIdsForRedirection, boolean performRedirects) throws IndexingException;

  /**
   * This method will trigger a flush operation on pending changes/updates to the persistent data,
   * causing it to become permanent as well as available to other processes. Calling this method is
   * not obligatory, and indexing will work without it. This just allows the caller to determine the
   * moment when changes are written to disk rather than wait for this to be triggered by the
   * infrastructure/library itself at its own discretion.
   *
   * @param blockUntilComplete If true, the call blocks until the flush is complete.
   * @throws IndexingException In case something went wrong.
   */
  void triggerFlushOfPendingChanges(boolean blockUntilComplete) throws IndexingException;

  /**
   * <p>
   * Removes the record with the given rdf:about value. This method also removes the associated
   * objects (i.e. those objects that are always part of only one record and the removal of which
   * can not invalidate references from other records):
   * <ul>
   * <li>Aggregation</li>
   * <li>EuropeanaAggregation</li>
   * <li>ProvidedCHO</li>
   * <li>Proxy</li>
   * </ul>
   * This does not remove any records that are potentially shared (like web resources, places,
   * concepts etc.).
   * </p>
   * <p>
   * <b>NOTE:</b> this operation should not coincide with indexing operations on the same dataset.
   * They are not put into a transaction and therefore this method may remove what the indexing
   * method just added.
   * </p>
   *
   * @param rdfAbout The ID of the record to remove. Is not null.
   * @return Whether a record was removed.
   * @throws IndexingException In case something went wrong.
   */
  boolean remove(String rdfAbout) throws IndexingException;

  /**
   * <p>
   * Removes all records that belong to a given dataset. This method also removes the associated
   * objects (i.e. those objects that are always part of only one record and the removal of which
   * can not invalidate references from other records):
   * <ul>
   * <li>Aggregation</li>
   * <li>EuropeanaAggregation</li>
   * <li>ProvidedCHO</li>
   * <li>Proxy</li>
   * </ul>
   * This does not remove any records that are potentially shared (like web resources, places,
   * concepts etc.).
   * </p>
   * <p>
   * Please <b>NOTE</b> that the criteria for whether a record or any of the listed dependencies are
   * removed is based on the value of {@link eu.europeana.corelib.definitions.edm.entity.AbstractEdmEntity#getAbout()}
   * and {@link eu.europeana.corelib.definitions.edm.beans.FullBean#getAbout()}. So the value of
   * {@link eu.europeana.corelib.definitions.edm.beans.FullBean#getEuropeanaCollectionName()} does
   * <b>not</b> play any role in determining which records to remove. This should eventually be
   * changed so that the structure of the rdf:about value is taken out of the equation (like it is
   * in {@link #remove(String)}).
   * </p>
   * <p>
   * <b>NOTE:</b> this operation should not coincide with indexing operations on the same dataset.
   * They are not put into a transaction and therefore this method may remove what the indexing
   * method just added.
   * </p>
   *
   * @param datasetId The ID of the dataset to clear. Is not null.
   * @param maxRecordDate The date that all records that have lower timestampUpdated than that date
   * would be removed. If null is provided then all records from that dataset will be removed.
   * @return The number of records that were removed.
   * @throws IndexingException In case something went wrong.
   */
  int removeAll(String datasetId, Date maxRecordDate) throws IndexingException;
}
