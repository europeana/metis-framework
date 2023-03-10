package eu.europeana.indexing;

import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.metis.schema.jibx.RDF;

public interface SimpleIndexer {

  void indexRecord(RDF record) throws IndexingException;

  void indexRecord(String record) throws IndexingException;
}
