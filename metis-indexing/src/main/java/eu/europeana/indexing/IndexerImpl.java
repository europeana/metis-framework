package eu.europeana.indexing;

import static java.lang.String.format;

import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.fullbean.StringToFullBeanConverter;
import eu.europeana.indexing.tiers.model.TierResults;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.utils.DepublicationReason;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link Indexer}.
 */
public class IndexerImpl implements Indexer {

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
   * @param stringToRdfConverterSupplier Supplies an instance of {@link StringToFullBeanConverter} used to convert a string to an
   * instance of {@link RDF}. Will be called once during every index.
   */
  IndexerImpl(AbstractConnectionProvider connectionProvider,
      IndexingSupplier<StringToFullBeanConverter> stringToRdfConverterSupplier) {
    this.connectionProvider = connectionProvider;
    this.stringToRdfConverterSupplier = stringToRdfConverterSupplier;
  }

  @Override
  public void indexRdfs(List<RDF> records, IndexingProperties indexingProperties)
      throws IndexingException {
    indexRecords(records, indexingProperties, tiers -> true);
  }

  @Override
  public void index(List<String> records, IndexingProperties indexingProperties)
      throws IndexingException {
    LOGGER.info("Parsing {} records...", records.size());
    final StringToFullBeanConverter stringToRdfConverter = stringToRdfConverterSupplier.get();
    final List<RDF> wrappedRecords = new ArrayList<>(records.size());
    for (String stringRdfRecord : records) {
      wrappedRecords.add(stringToRdfConverter.convertStringToRdf(stringRdfRecord));
    }
    indexRecords(wrappedRecords, indexingProperties, tiers -> true);
  }

  @Override
  public void index(InputStream rdfInputStream, IndexingProperties indexingProperties)
      throws IndexingException {
    final StringToFullBeanConverter stringToRdfConverter = stringToRdfConverterSupplier.get();
    indexRdf(stringToRdfConverter.convertToRdf(rdfInputStream), indexingProperties);
  }

  @Override
  public TierResults indexAndGetTierCalculations(InputStream recordContent,
      IndexingProperties indexingProperties) throws IndexingException {
    final RDF rdfRecord = stringToRdfConverterSupplier.get().convertToRdf(recordContent);
    final List<TierResults> result = new ArrayList<>();
    indexRecords(List.of(rdfRecord), indexingProperties, result::add);
    return result.getFirst();
  }

  @Override
  public void indexRdf(RDF rdf, IndexingProperties indexingProperties) throws IndexingException {
    indexRdfs(List.of(rdf), indexingProperties);
  }

  @Override
  public void index(String rdfString, IndexingProperties indexingProperties) throws IndexingException {
    index(List.of(rdfString), indexingProperties);
  }

  @Override
  public void index(String stringRdfRecord, IndexingProperties indexingProperties,
      Predicate<TierResults> tierResultsConsumer) throws IndexingException {
    final RDF rdfRecord = stringToRdfConverterSupplier.get().convertStringToRdf(stringRdfRecord);
    indexRecords(List.of(rdfRecord), indexingProperties, tierResultsConsumer);
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
  public FullBeanImpl getTombstone(String rdfAbout) {
    return this.connectionProvider.getIndexedRecordAccess().getTombstoneFullbean(rdfAbout);
  }

  @Override
  public boolean indexTombstone(String rdfAbout, DepublicationReason depublicationReason) throws IndexingException {
    if (depublicationReason == DepublicationReason.LEGACY) {
      throw new IndexerRelatedIndexingException(
          format("Depublication reason %s, is not allowed", depublicationReason));
    }
    final FullBeanImpl publishedFullbean = this.connectionProvider.getIndexedRecordAccess().getFullbean(rdfAbout);
    if (publishedFullbean != null) {
      final FullBeanPublisher publisher = connectionProvider.getFullBeanPublisher(true);
      final FullBeanImpl tombstoneFullbean = TombstoneUtil.prepareTombstoneFullbean(publishedFullbean, depublicationReason);
      try {
        publisher.publishTombstone(tombstoneFullbean, tombstoneFullbean.getTimestampCreated());
      } catch (IndexingException e) {
        throw new IndexerRelatedIndexingException("Could not create tombstone record '" + rdfAbout + "'.", e);
      }
    }
    return publishedFullbean != null;
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

  private void indexRecords(List<RDF> records, IndexingProperties properties,
      Predicate<TierResults> tierResultsConsumer) throws IndexingException {
    if (properties.isPerformRedirects() && connectionProvider.getRecordRedirectDao() == null) {
      throw new SetupRelatedIndexingException(
          "Record redirect dao has not been initialized and performing redirects is requested");
    }
    LOGGER.info("Processing {} records...", records.size());
    final FullBeanPublisher publisher =
        connectionProvider.getFullBeanPublisher(properties.isPreserveUpdateAndCreateTimesFromRdf());

    for (RDF rdfRecord : records) {
      if (tierResultsConsumer.test(IndexerPreprocessor.preprocessRecord(rdfRecord, properties))) {
        if (properties.isPerformRedirects()) {
          publisher.publishWithRedirects(new RdfWrapper(rdfRecord), properties.getRecordDate(),
              properties.getDatasetIdsForRedirection());
        } else {
          publisher.publish(new RdfWrapper(rdfRecord), properties.getRecordDate(),
              properties.getDatasetIdsForRedirection());
        }
      }
    }

    LOGGER.info("Successfully processed {} records.", records.size());
  }

  /**
   * Similar to the Java interface {@link Supplier}, but one that may throw an {@link IndexerRelatedIndexingException}.
   *
   * @param <T> The type of the object to be supplied.
   * @author jochen
   */
  @FunctionalInterface
  public interface IndexingSupplier<T> {

    /**
     * Gets a result.
     *
     * @return A result.
     * @throws IndexerRelatedIndexingException In case something went wrong while getting the result.
     */
    T get() throws IndexerRelatedIndexingException;
  }
}

