package eu.europeana.indexing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.fullbean.StringToFullBeanConverter;

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

  @Override
  public void indexRdfs(List<RDF> records, boolean preserveUpdateAndCreateTimesFromRdf) throws IndexingException {
    LOGGER.info("Processing {} records...", records.size());
    try {
      final FullBeanPublisher publisher =
          connectionProvider.getFullBeanPublisher(preserveUpdateAndCreateTimesFromRdf);
      for (RDF record : records) {
        publisher.publish(record);
      }
      LOGGER.info("Successfully processed {} records.", records.size());
    } catch (IndexingException e) {
      LOGGER.warn("Error while indexing a record.", e);
      throw e;
    }
  }

  @Override
  public void indexRdf(RDF record, boolean preserveUpdateAndCreateTimesFromRdf) throws IndexingException {
    indexRdfs(Collections.singletonList(record), preserveUpdateAndCreateTimesFromRdf);
  }

  @Override
  public void index(List<String> records, boolean preserveUpdateAndCreateTimesFromRdf) throws IndexingException {
    LOGGER.info("Parsing {} records...", records.size());
    final StringToFullBeanConverter stringToRdfConverter = stringToRdfConverterSupplier.get();
    final List<RDF> rdfs = new ArrayList<>(records.size());
    for (String record : records) {
      rdfs.add(stringToRdfConverter.convertStringToRdf(record));
    }
    indexRdfs(rdfs, preserveUpdateAndCreateTimesFromRdf);
  }

  @Override
  public void index(String record, boolean preserveUpdateAndCreateTimesFromRdf) throws IndexingException {
    index(Collections.singletonList(record), preserveUpdateAndCreateTimesFromRdf);
  }

  @Override
  public void close() throws IOException {
    this.connectionProvider.close();
  }

  @Override
  public void triggerFlushOfPendingChanges(boolean blockUntilComplete) throws IndexingException {
    try {
      this.connectionProvider.triggerFlushOfPendingChanges(blockUntilComplete);
    } catch (SolrServerException | IOException e) {
      throw new IndexingException("Error while flushing changes.", e);
    }
  }

  @Override
  public int removeAll(String datasetId) throws IndexingException {
    try {
      return this.connectionProvider.getDatasetRemover().removeDataset(datasetId);
    } catch (IndexingException e) {
      LOGGER.warn("Error while removing a dataset.", e);
      throw e;
    }
  }

  /**
   * Similar to the Java interface {@link Supplier}, but one that may throw an
   * {@link IndexingException}.
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
     * @throws IndexingException In case something went wrong while getting the result.
     */
    T get() throws IndexingException;
  }
}
