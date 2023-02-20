package eu.europeana.indexing;

import eu.europeana.indexing.mongo.MongoIndexer;
import eu.europeana.indexing.solr.SolrIndexer;
import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.solr.connection.SolrProperties;

public class SimpleIndexerFactory {

  public SimpleIndexer getIndexer(SolrProperties solrProperties) {
    return new SolrIndexer(solrProperties);
  }

  public SimpleIndexer getIndexer(MongoProperties mongoProperties) {
    return new MongoIndexer(mongoProperties);
  }
}
