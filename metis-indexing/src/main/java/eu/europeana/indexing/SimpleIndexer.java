package eu.europeana.indexing;

import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.metis.schema.jibx.RDF;

/**
 * The interface Simple indexer.
 */
public interface SimpleIndexer {

  /**
   * Index record.
   *
   * @param record the record
   * @throws IndexingException the indexing exception
   */
  void indexRecord(RDF record) throws IndexingException;

  /**
   * Index record.
   *
   * @param record the record
   * @throws IndexingException the indexing exception
   */
  void indexRecord(String record) throws IndexingException;
}
