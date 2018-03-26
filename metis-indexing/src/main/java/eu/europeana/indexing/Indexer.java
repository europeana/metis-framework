package eu.europeana.indexing;

/**
 * This interface allows access to this library's indexing functionality.
 * 
 * @author jochen
 */
public interface Indexer {

  /**
   * This method indexes a record, publishing it to the provided data stores.
   * 
   * @param record The record to index.
   * @throws IndexingException In case a problem occurred during indexing.
   */
  public void index(String record) throws IndexingException;

}
