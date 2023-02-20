package eu.europeana.indexing;

import eu.europeana.metis.schema.jibx.RDF;

public interface SimpleIndexer {

  void indexRecord(RDF record);

  void indexRecord(String record);
}
