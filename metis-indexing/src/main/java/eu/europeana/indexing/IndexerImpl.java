package eu.europeana.indexing;

import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.fullbean.StringToFullBeanConverter;
import eu.europeana.indexing.tiers.ClassifierFactory;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.TierClassifier;
import eu.europeana.indexing.tiers.model.TierResults;
import eu.europeana.indexing.tiers.view.ContentTierBreakdown;
import eu.europeana.indexing.tiers.view.MetadataTierBreakdown;
import eu.europeana.indexing.utils.RdfTierUtils;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.jibx.RDF;
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
 *
 * @author jochen
 */
class IndexerImpl implements Indexer {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexerImpl.class);

  private final AbstractConnectionProvider connectionProvider;

  private final IndexingSupplier<StringToFullBeanConverter> stringToRdfConverterSupplier;
  private final TierClassifier<MediaTier, ContentTierBreakdown> mediaClassifier = ClassifierFactory.getMediaClassifier();
  private final TierClassifier<MetadataTier, MetadataTierBreakdown> metadataClassifier = ClassifierFactory.getMetadataClassifier();

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
  public void index(InputStream recordInputStream, IndexingProperties indexingProperties)
      throws IndexingException {
    final StringToFullBeanConverter stringToRdfConverter = stringToRdfConverterSupplier.get();
    indexRdf(stringToRdfConverter.convertToRdf(recordInputStream), indexingProperties);
  }

  @Override
  public TierResults indexAndGetTierCalculations(InputStream recordContent,
      IndexingProperties indexingProperties) throws IndexingException {
    final RDF rdfRecord = stringToRdfConverterSupplier.get().convertToRdf(recordContent);
    final List<TierResults> result = new ArrayList<>();
    indexRecords(List.of(rdfRecord), indexingProperties, result::add);
    return result.get(0);
  }

  @Override
  public void indexRdf(RDF rdfRecord, IndexingProperties indexingProperties) throws IndexingException {
    indexRdfs(List.of(rdfRecord), indexingProperties);
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
      if(tierResultsConsumer.test(preprocessRecord(rdfRecord, properties))) {
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

  @Override
  public void index(String stringRdfRecord, IndexingProperties indexingProperties) throws IndexingException {
    index(List.of(stringRdfRecord), indexingProperties);
  }

  @Override
  public void index(String stringRdfRecord, IndexingProperties indexingProperties,
                    Predicate<TierResults> tierResultsConsumer) throws IndexingException {
    final RDF rdfRecord = stringToRdfConverterSupplier.get().convertStringToRdf(stringRdfRecord);
    indexRecords(List.of(rdfRecord), indexingProperties, tierResultsConsumer);
  }

  private TierResults preprocessRecord(RDF rdf, IndexingProperties properties)
      throws IndexingException {

    // Perform the tier classification
    final RdfWrapper rdfWrapper = new RdfWrapper(rdf);
    TierResults tierCalculationsResult = new TierResults(null, null);
    if (properties.isPerformTierCalculation() && properties.getTypesEnabledForTierCalculation()
                                                           .contains(rdfWrapper.getEdmType())) {
      tierCalculationsResult = new TierResults(mediaClassifier.classify(rdfWrapper).getTier(),
              metadataClassifier.classify(rdfWrapper).getTier());
      RdfTierUtils.setTier(rdf, tierCalculationsResult.getMediaTier());
      RdfTierUtils.setTier(rdf, tierCalculationsResult.getMetadataTier());
    }

    return tierCalculationsResult;
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
   * Similar to the Java interface {@link Supplier}, but one that may throw an {@link IndexerRelatedIndexingException}.
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
     * @throws IndexerRelatedIndexingException In case something went wrong while getting the result.
     */
    T get() throws IndexerRelatedIndexingException;
  }
}
