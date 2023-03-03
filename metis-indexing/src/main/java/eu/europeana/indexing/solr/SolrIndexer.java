package eu.europeana.indexing.solr;

import eu.europeana.indexing.AbstractConnectionProvider;
import eu.europeana.indexing.FullBeanPublisher;
import eu.europeana.indexing.IndexerImpl.IndexingSupplier;
import eu.europeana.indexing.SettingsConnectionProvider;
import eu.europeana.indexing.SimpleIndexer;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.fullbean.StringToFullBeanConverter;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.solr.connection.SolrProperties;
import java.time.Instant;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolrIndexer implements SimpleIndexer {

  private static final Logger LOGGER = LoggerFactory.getLogger(SolrIndexer.class);
  private final AbstractConnectionProvider connectionProvider;
  private final IndexingSupplier<StringToFullBeanConverter> stringToRdfConverterSupplier;

  public SolrIndexer(SolrProperties<SetupRelatedIndexingException> properties) throws SetupRelatedIndexingException {
    this.connectionProvider = new SettingsConnectionProvider(properties);
    this.stringToRdfConverterSupplier = StringToFullBeanConverter::new;
  }

  @Override
  public void indexRecord(RDF rdfRecord) {
    // Sanity checks
    if (rdfRecord == null) {
      throw new IllegalArgumentException("Input RDF cannot be null.");
    }
    LOGGER.info("Processing {} record...", rdfRecord);
    final FullBeanPublisher publisher = connectionProvider.getFullBeanPublisher(false);
    try {
      publisher.publishSolr(new RdfWrapper(rdfRecord), Date.from(Instant.now()));
    } catch (IndexingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void indexRecord(String stringRdfRecord) {
    try {
      final RDF rdfRecord = stringToRdfConverterSupplier.get().convertStringToRdf(stringRdfRecord);
      indexRecord(rdfRecord);
    } catch (IndexingException e) {
      LOGGER.error("Error indexing record", e);
    }
  }
}
