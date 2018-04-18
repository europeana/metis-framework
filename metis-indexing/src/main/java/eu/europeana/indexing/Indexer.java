package eu.europeana.indexing;

import java.io.Closeable;
import java.util.List;
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
  public void index(String record) throws IndexingException;

  /**
   * This method indexes a list of records, publishing it to the provided data stores.
   * 
   * @param records The record to index.
   * @throws IndexingException In case a problem occurred during indexing.
   */
  public void index(List<String> records) throws IndexingException;

}
