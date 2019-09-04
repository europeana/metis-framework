package eu.europeana.indexing;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.fullbean.StringToFullBeanConverter;
import eu.europeana.indexing.tiers.ClassifierFactory;
import eu.europeana.indexing.utils.RdfTierUtils;
import eu.europeana.indexing.utils.RdfWrapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link Indexer}.
 * 
 * @author jochen
 *
 */
class IndexerImpl implements Indexer {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexerImpl.class);

  private final AbstractConnectionProvider connectionProvider;

  private final IndexingSupplier<StringToFullBeanConverter> stringToRdfConverterSupplier;
  
  /**
   * Constructor.
   * 
   * @param connectionProvider The connection provider for this indexer.
   */
  IndexerImpl(AbstractConnectionProvider connectionProvider) {
    this(connectionProvider, StringToFullBeanConverter::new);
  }

  /**
   * Constructor for testing purposes.
   * 
   * @param connectionProvider The connection provider for this indexer.
   * @param stringToRdfConverterSupplier Supplies an instance of {@link StringToFullBeanConverter}
   *        used to parse strings to instances of {@link RDF}. Will be called once during every
   *        index.
   */
  IndexerImpl(AbstractConnectionProvider connectionProvider,
      IndexingSupplier<StringToFullBeanConverter> stringToRdfConverterSupplier) {
    this.connectionProvider = connectionProvider;
    this.stringToRdfConverterSupplier = stringToRdfConverterSupplier;
  }

  private void indexRecords(List<RDF> records, Date recordDate, boolean preserveUpdateAndCreateTimesFromRdf)
      throws IndexingException {
    LOGGER.info("Processing {} records...", records.size());
    try {
      final FullBeanPublisher publisher =
          connectionProvider.getFullBeanPublisher(preserveUpdateAndCreateTimesFromRdf);
      for (RDF record : records) {
        preprocessRecord(record);
        publisher.publish(new RdfWrapper(record), recordDate);
      }
      LOGGER.info("Successfully processed {} records.", records.size());
    } catch (IndexingException e) {
      LOGGER.warn("Error while indexing a record.", e);
      throw e;
    }
  }

  private static void preprocessRecord(RDF rdf) throws IndexingException {

    // Perform the tier classification
    final RdfWrapper rdfWrapper = new RdfWrapper(rdf);
    RdfTierUtils.setTier(rdf, ClassifierFactory.getMediaClassifier().classify(rdfWrapper));
    RdfTierUtils.setTier(rdf, ClassifierFactory.getMetadataClassifier().classify(rdfWrapper));

  }

  @Override
  public void indexRdfs(List<RDF> records, Date recordDate, boolean preserveUpdateAndCreateTimesFromRdf) throws IndexingException {
    indexRecords(records, recordDate, preserveUpdateAndCreateTimesFromRdf);
  }

  @Override
  public void indexRdf(RDF record, Date recordDate,
      boolean preserveUpdateAndCreateTimesFromRdf) throws IndexingException {
    indexRdfs(Collections.singletonList(record), recordDate, preserveUpdateAndCreateTimesFromRdf);
  }

  @Override
  public void index(List<String> records, Date recordDate, boolean preserveUpdateAndCreateTimesFromRdf) throws IndexingException {
    LOGGER.info("Parsing {} records...", records.size());
    final StringToFullBeanConverter stringToRdfConverter = stringToRdfConverterSupplier.get();
    final List<RDF> wrappedRecords = new ArrayList<>(records.size());
    for (String record : records) {
      wrappedRecords.add(stringToRdfConverter.convertStringToRdf(record));
    }
    indexRecords(wrappedRecords, recordDate, preserveUpdateAndCreateTimesFromRdf);
  }

  @Override
  public void index(String record, Date recordDate, boolean preserveUpdateAndCreateTimesFromRdf) throws IndexingException {
    index(Collections.singletonList(record), recordDate, preserveUpdateAndCreateTimesFromRdf);
  }

  @Override
  public void close() throws IOException {
    this.connectionProvider.close();
  }

  @Override
  public void triggerFlushOfPendingChanges(boolean blockUntilComplete) throws IndexerRelatedIndexingException {
    try {
      this.connectionProvider.triggerFlushOfPendingChanges(blockUntilComplete);
    } catch (SolrServerException | IOException e) {
      throw new IndexerRelatedIndexingException("Error while flushing changes.", e);
    }
  }

  @Override
  public boolean remove(String rdfAbout) throws IndexerRelatedIndexingException {
    try {
      return this.connectionProvider.getIndexedRecordRemover().removeRecord(rdfAbout);
    } catch (IndexerRelatedIndexingException e) {
      LOGGER.warn("Error while removing a record.", e);
      throw e;
    }
  }

  @Override
  public int removeAll(String datasetId) throws IndexerRelatedIndexingException {
    try {
      return this.connectionProvider.getIndexedRecordRemover().removeDataset(datasetId);
    } catch (IndexerRelatedIndexingException e) {
      LOGGER.warn("Error while removing a dataset.", e);
      throw e;
    }
  }

  /**
   * Similar to the Java interface {@link Supplier}, but one that may throw an
   * {@link IndexerRelatedIndexingException}.
   * 
   * @author jochen
   *
   * @param <T> The type of the object to be supplied.
   */
  @FunctionalInterface
  interface IndexingSupplier<T> {

    /**
     * Gets a result.
     * 
     * @return A result.
     * @throws IndexerRelatedIndexingException In case something went wrong while getting the result.
     */
    T get() throws IndexerRelatedIndexingException;
  }
}
