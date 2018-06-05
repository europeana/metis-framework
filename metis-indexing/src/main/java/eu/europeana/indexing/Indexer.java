package eu.europeana.indexing;

import java.io.Closeable;
import java.util.List;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.indexing.exception.IndexingException;

/**
 * This interface allows access to this library's indexing functionality. Note: the object is
 * {@link Closeable} and must be closed after use by calling {@link #close()} (or by using a try
 * block).
 * 
 * @author jochen
 */
public interface Indexer extends Closeable {

  /**
   * This method indexes a single record, publishing it to the provided data stores.
   * 
   * @param record The record to index.
   * @throws IndexingException In case a problem occurred during indexing.
   */
  public void indexRdf(RDF record) throws IndexingException;

  /**
   * This method indexes a list of records, publishing it to the provided data stores.
   * 
   * @param records The records to index.
   * @throws IndexingException In case a problem occurred during indexing.
   */
  public void indexRdfs(List<RDF> records) throws IndexingException;

  /**
   * This method indexes a single record, publishing it to the provided data stores.
   * 
   * @param record The record to index (can be parsed to RDF).
   * @throws IndexingException In case a problem occurred during indexing.
   */
  public void index(String record) throws IndexingException;

  /**
   * This method indexes a list of records, publishing it to the provided data stores.
   * 
   * @param records The records to index (can be parsed to RDF).
   * @throws IndexingException In case a problem occurred during indexing.
   */
  public void index(List<String> records) throws IndexingException;

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
  public void triggerFlushOfPendingChanges(boolean blockUntilComplete) throws IndexingException;
}
