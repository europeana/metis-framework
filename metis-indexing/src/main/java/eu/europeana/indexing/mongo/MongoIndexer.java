package eu.europeana.indexing.mongo;

import eu.europeana.indexing.AbstractConnectionProvider;
import eu.europeana.indexing.IndexerImpl.IndexingSupplier;
import eu.europeana.indexing.SettingsConnectionProvider;
import eu.europeana.indexing.SimpleIndexer;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.fullbean.StringToFullBeanConverter;
import eu.europeana.indexing.solr.SolrIndexer;
import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.schema.jibx.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoIndexer implements SimpleIndexer {

  private static final Logger LOGGER = LoggerFactory.getLogger(SolrIndexer.class);
  private final AbstractConnectionProvider connectionProvider;
  private final IndexingSupplier<StringToFullBeanConverter> stringToRdfConverterSupplier;

  public MongoIndexer(MongoProperties<SetupRelatedIndexingException> properties) throws SetupRelatedIndexingException {
    this.connectionProvider = new SettingsConnectionProvider(properties);
    this.stringToRdfConverterSupplier = StringToFullBeanConverter::new;
  }

  @Override
  public void indexRecord(RDF record) {
    // Sanity checks
    if (record == null) {
      throw new IllegalArgumentException("Input RDF cannot be null.");
    }

  }

  @Override
  public void indexRecord(String record) {

  }
}
