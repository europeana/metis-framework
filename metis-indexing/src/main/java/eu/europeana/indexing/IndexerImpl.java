package eu.europeana.indexing;

import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.fullbean.StringToFullBeanConverter;
import eu.europeana.indexing.tiers.ClassifierFactory;
import eu.europeana.indexing.utils.RdfTierUtils;
import eu.europeana.indexing.utils.RdfWrapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link Indexer}.
 *
 * @author jochen
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
   * used to parse strings to instances of {@link RDF}. Will be called once during every index.
   */
  IndexerImpl(AbstractConnectionProvider connectionProvider,
      IndexingSupplier<StringToFullBeanConverter> stringToRdfConverterSupplier) {
    this.connectionProvider = connectionProvider;
    this.stringToRdfConverterSupplier = stringToRdfConverterSupplier;
  }

  private void indexRecords(List<RDF> records, IndexingProperties properties)
          throws IndexingException {
    if (properties.isPerformRedirects() && connectionProvider.getRecordRedirectDao() == null) {
      throw new SetupRelatedIndexingException(
          "Record redirect dao has not been initialized and performing redirects is requested");
    }
    LOGGER.info("Processing {} records...", records.size());
    final FullBeanPublisher publisher =
        connectionProvider.getFullBeanPublisher(properties.isPreserveUpdateAndCreateTimesFromRdf());

    for (RDF record : records) {
      preprocessRecord(record, properties.isPerformTierCalculation());
      if (properties.isPerformRedirects()) {
        publisher.publishWithRedirects(new RdfWrapper(record), properties.getRecordDate(),
                properties.getDatasetIdsForRedirection());
      } else {
        publisher.publish(new RdfWrapper(record), properties.getRecordDate(),
                properties.getDatasetIdsForRedirection());
      }
    }

    LOGGER.info("Successfully processed {} records.", records.size());
  }

  private static void preprocessRecord(RDF rdf, boolean performTierCalculation)
          throws IndexingException {

    // Perform the tier classification
    if (performTierCalculation) {
      final RdfWrapper rdfWrapper = new RdfWrapper(rdf);
      RdfTierUtils.setTier(rdf, ClassifierFactory.getMediaClassifier().classify(rdfWrapper));
      RdfTierUtils.setTier(rdf, ClassifierFactory.getMetadataClassifier().classify(rdfWrapper));
    }
  }

  @Override
  public void indexRdfs(List<RDF> records, IndexingProperties indexingProperties)
          throws IndexingException {
    indexRecords(records, indexingProperties);
  }

  @Override
  public void indexRdf(RDF record, IndexingProperties indexingProperties) throws IndexingException {
    indexRdfs(List.of(record), indexingProperties);
  }

  @Override
  public void index(List<String> records, IndexingProperties indexingProperties)
          throws IndexingException {
    LOGGER.info("Parsing {} records...", records.size());
    final StringToFullBeanConverter stringToRdfConverter = stringToRdfConverterSupplier.get();
    final List<RDF> wrappedRecords = new ArrayList<>(records.size());
    for (String record : records) {
      wrappedRecords.add(stringToRdfConverter.convertStringToRdf(record));
    }
    indexRecords(wrappedRecords, indexingProperties);
  }

  @Override
  public void index(String record, IndexingProperties indexingProperties) throws IndexingException {
    index(List.of(record), indexingProperties);
  }

  @Override
  public void index(InputStream record, IndexingProperties indexingProperties)
          throws IndexingException {
    final StringToFullBeanConverter stringToRdfConverter = stringToRdfConverterSupplier.get();
    indexRdf(stringToRdfConverter.convertToRdf(record), indexingProperties);
  }

  @Override
  public void close() throws IOException {
    this.connectionProvider.close();
  }

  @Override
  public void triggerFlushOfPendingChanges(boolean blockUntilComplete)
      throws IndexerRelatedIndexingException {
    try {
      this.connectionProvider.triggerFlushOfPendingChanges(blockUntilComplete);
    } catch (SolrServerException | IOException e) {
      throw new IndexerRelatedIndexingException("Error while flushing changes.", e);
    }
  }

  @Override
  public boolean remove(String rdfAbout) throws IndexerRelatedIndexingException {
    return this.connectionProvider.getIndexedRecordAccess().removeRecord(rdfAbout);
  }

  @Override
  public int removeAll(String datasetId, Date maxRecordDate)
      throws IndexerRelatedIndexingException {
    // TODO: 8/26/20 Update removeAll method to return long instead of int, it will affect clients
    return Math.toIntExact(
        this.connectionProvider.getIndexedRecordAccess().removeDataset(datasetId, maxRecordDate));
  }

  @Override
  public Stream<String> getRecordIds(String datasetId, Date maxRecordDate) {
    return this.connectionProvider.getIndexedRecordAccess().getRecordIds(datasetId, maxRecordDate);
  }

  @Override
  public long countRecords(String datasetId, Date maxRecordDate) {
    return this.connectionProvider.getIndexedRecordAccess().countRecords(datasetId, maxRecordDate);
  }

  @Override
  public long countRecords(String datasetId) {
    return this.connectionProvider.getIndexedRecordAccess().countRecords(datasetId);
  }

  /**
   * Similar to the Java interface {@link Supplier}, but one that may throw an {@link
   * IndexerRelatedIndexingException}.
   *
   * @param <T> The type of the object to be supplied.
   * @author jochen
   */
  @FunctionalInterface
  interface IndexingSupplier<T> {

    /**
     * Gets a result.
     *
     * @return A result.
     * @throws IndexerRelatedIndexingException In case something went wrong while getting the
     * result.
     */
    T get() throws IndexerRelatedIndexingException;
  }
}
