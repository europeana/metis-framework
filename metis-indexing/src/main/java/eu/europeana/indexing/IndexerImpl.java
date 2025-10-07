package eu.europeana.indexing;

import static eu.europeana.metis.network.ExternalRequestUtil.retryableExternalRequestForNetworkExceptionsThrowing;

import eu.europeana.indexing.common.contract.RecordPersistence.ComputedDates;
import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.tiers.model.TierResults;
import eu.europeana.indexing.utils.RDFDeserializer;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link Indexer}.
 * @param <T> The type of the tombstone that is returned.
 */
public class IndexerImpl<T> implements Indexer<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexerImpl.class);

  private final PersistenceAccessForIndexing<T> persistenceAccess;

  private final IndexingSupplier<RDFDeserializer> stringToRdfConverterSupplier;

  /**
   * Constructor.
   *
   * @param persistenceAccess The connection provider for this indexer.
   */
  IndexerImpl(PersistenceAccessForIndexing<T> persistenceAccess) {
    this(persistenceAccess, RDFDeserializer::new);
  }

  /**
   * Constructor for testing purposes.
   *
   * @param persistenceAccess The connection provider for this indexer.
   * @param stringToRdfConverterSupplier Supplies an instance of {@link RDFDeserializer} used to convert a string to an
   * instance of {@link RDF}. Will be called once during every index.
   */
  IndexerImpl(PersistenceAccessForIndexing<T> persistenceAccess,
      IndexingSupplier<RDFDeserializer> stringToRdfConverterSupplier) {
    this.persistenceAccess = persistenceAccess;
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
    final RDFDeserializer stringToRdfConverter = stringToRdfConverterSupplier.get();
    final List<RDF> wrappedRecords = new ArrayList<>(records.size());
    for (String stringRdfRecord : records) {
      wrappedRecords.add(stringToRdfConverter.convertToRdf(stringRdfRecord));
    }
    indexRecords(wrappedRecords, indexingProperties, tiers -> true);
  }

  @Override
  public void index(InputStream rdfInputStream, IndexingProperties indexingProperties)
      throws IndexingException {
    final RDFDeserializer stringToRdfConverter = stringToRdfConverterSupplier.get();
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
    final RDF rdfRecord = stringToRdfConverterSupplier.get().convertToRdf(stringRdfRecord);
    indexRecords(List.of(rdfRecord), indexingProperties, tierResultsConsumer);
  }

  @Override
  public void close() throws IOException {
    this.persistenceAccess.close();
  }

  @Override
  public void triggerFlushOfPendingChanges(boolean blockUntilComplete)
      throws IndexerRelatedIndexingException {
    this.persistenceAccess.triggerFlushOfPendingChanges(blockUntilComplete);
  }

  @Override
  public boolean remove(String rdfAbout) throws IndexerRelatedIndexingException {
    return retryableExternalRequestForNetworkExceptionsThrowing(() -> {
      this.persistenceAccess.getSearchPersistence().removeRecord(rdfAbout);
      return this.persistenceAccess.getRecordPersistence().removeRecord(rdfAbout);
    });
  }

  @Override
  public T getTombstone(String rdfAbout) throws IndexingException {
    return retryableExternalRequestForNetworkExceptionsThrowing(
        () -> this.persistenceAccess.getTombstonePersistence().getTombstone(rdfAbout));
  }

  @Override
  public boolean removeTombstone(String rdfAbout) throws IndexingException {
    return retryableExternalRequestForNetworkExceptionsThrowing(
        () -> this.persistenceAccess.getTombstonePersistence().removeTombstone(rdfAbout));
  }

  @Override
  public boolean indexTombstone(String rdfAbout, DepublicationReason reason) throws IndexingException {
    return this.persistenceAccess.getTombstonePersistence().saveTombstoneForLiveRecord(rdfAbout, reason);
  }

  @Override
  public int removeAll(String datasetId, Date maxRecordDate)
      throws IndexerRelatedIndexingException {
    final Long totalRemoved = retryableExternalRequestForNetworkExceptionsThrowing(() -> {
      final long count = this.persistenceAccess.getRecordPersistence()
          .removeDataset(datasetId, maxRecordDate);
      this.persistenceAccess.getSearchPersistence().removeDataset(datasetId, maxRecordDate);
      return count;
    });
    return Math.toIntExact(totalRemoved);
  }

  @Override
  public Stream<String> getRecordIds(String datasetId, Date maxRecordDate, int batchSize) throws IndexingException {
    return retryableExternalRequestForNetworkExceptionsThrowing(
        () -> this.persistenceAccess.getRecordPersistence().getRecordIds(datasetId, maxRecordDate, batchSize));
  }

  @Override
  public Stream<String> getRecordIds(String datasetId, Date maxRecordDate) throws IndexingException {
    return retryableExternalRequestForNetworkExceptionsThrowing(
        () -> this.persistenceAccess.getRecordPersistence().getRecordIds(datasetId, maxRecordDate));
  }

  @Override
  public long countRecords(String datasetId, Date maxRecordDate) throws IndexingException {
    return retryableExternalRequestForNetworkExceptionsThrowing(() ->
        this.persistenceAccess.getRecordPersistence().countRecords(datasetId, maxRecordDate));
  }

  @Override
  public long countRecords(String datasetId) throws IndexingException {
    return this.countRecords(datasetId, null);
  }

  private void indexRecord(RdfWrapper rdf, Date recordDate, List<String> datasetIdsToRedirectFrom,
      boolean performRedirects, boolean preserveUpdateAndCreateTimesFromRdf) throws IndexingException {
    final Date createdDate = performRedirects ? this.persistenceAccess.getRedirectPersistence()
        .performRedirection(rdf, recordDate, datasetIdsToRedirectFrom) : null;
    final ComputedDates computedDates = this.persistenceAccess.getRecordPersistence()
        .saveRecord(rdf, preserveUpdateAndCreateTimesFromRdf, recordDate, createdDate);
    this.persistenceAccess.getSearchPersistence().saveRecord(rdf, computedDates.updatedDate(),
        computedDates.createdDate());
  }

  private void indexRecords(List<RDF> records, IndexingProperties properties,
      Predicate<TierResults> tierResultsConsumer) throws IndexingException {
    LOGGER.info("Processing {} records...", records.size());
    for (RDF rdfRecord : records) {
      if (tierResultsConsumer.test(IndexerPreprocessor.preprocessRecord(rdfRecord, properties))) {
        indexRecord(new RdfWrapper(rdfRecord), properties.getRecordDate(),
            properties.getDatasetIdsForRedirection(), properties.isPerformRedirects(),
            properties.isPreserveUpdateAndCreateTimesFromRdf());
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

