package eu.europeana.indexing;

import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.mongo.MongoIndexer;
import eu.europeana.indexing.solr.SolrIndexer;
import eu.europeana.metis.common.PropertyHolder;
import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.solr.connection.SolrProperties;

public class SimpleIndexerFactory {

  /**
   *
   * @param properties can be eiather a SolrProperties or MongoProperties object.
   * @return SimpleIndexer pointing to mongo or solr.
   */
  public SimpleIndexer getIndexer(PropertyHolder properties) throws SetupRelatedIndexingException {
    if (properties instanceof SolrProperties) {
      return new SolrIndexer((SolrProperties) properties);
    } else if (properties instanceof MongoProperties) {
      return new MongoIndexer((MongoProperties) properties);
    } else {
      throw new IllegalArgumentException("Invalid property configuration");
    }
  }
}
