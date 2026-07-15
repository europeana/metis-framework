package eu.europeana.indexing;

import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.tiers.model.TierResults;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.utils.DepublicationReason;
import java.io.Closeable;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class is a utilities object providing the functionality of a pool of indexer objects, all created from the same
 * factory/settings. This class is designed to save the time otherwise spent on repeatedly creating and destroying indexers.
 * </p>
 * <p>
 * This pool can have an unlimited number of indexers, that will be created automatically whenever they are needed (i.e. when all
 * other indexers are in use already). This thread pool implements the automatic destruction of idle indexers, given a certain
 * permissible idle time.
 * </p>
 * <p>
 * This class is thread-safe.
 * </p>
 */
public class IndexerPool implements Closeable {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexerPool.class);

  private final GenericObjectPool<Indexer<?>> pool;

  /**
   * Constructor.
   *
   * @param indexingSettings The  settings with which to create the indexer instances in the pool.
   * @param maxIdleTimeForIndexerInSecs The idle time after which an indexer is eligible for destruction, in seconds.
   * @param idleTimeCheckIntervalInSecs The interval with which we check the idle time of indexers to decide whether to destroy
   * them, in seconds.
   */
  public IndexerPool(IndexingSettings indexingSettings, long maxIdleTimeForIndexerInSecs,
      long idleTimeCheckIntervalInSecs) {
    this(IndexerFactory.create(indexingSettings), maxIdleTimeForIndexerInSecs,
        idleTimeCheckIntervalInSecs);
  }

  /**
   * Constructor.
   *
   * @param indexerFactory The factory from which to create the indexer instances in the pool.
   * @param maxIdleTimeForIndexerInSecs The idle time after which an indexer is eligible for destruction, in seconds.
   * @param idleTimeCheckIntervalInSecs The interval with which we check the idle time of indexers to decide whether to destroy
   * them, in seconds.
   */
  public IndexerPool(IndexerFactory<?> indexerFactory, long maxIdleTimeForIndexerInSecs,
      long idleTimeCheckIntervalInSecs) {

    // Create indexer pool with default options.
    pool = new GenericObjectPool<>(new PooledIndexerFactory(indexerFactory));

    // Set custom options for the size of the pool: no max or min number of indexer objects.
    pool.setMaxIdle(-1);
    pool.setMinIdle(-1);
    pool.setMaxTotal(-1);

    // Set custom options for indexer pool regarding eviction (when indexer has been idle for some time).
    pool.setSoftMinEvictableIdleTimeMillis(-1);
    pool.setMinEvictableIdleTimeMillis(convertSecsToMillis(maxIdleTimeForIndexerInSecs));
    pool.setTimeBetweenEvictionRunsMillis(convertSecsToMillis(idleTimeCheckIntervalInSecs));
  }

  private static long convertSecsToMillis(long seconds) {
    return Duration.ofSeconds(seconds).toMillis();
  }

  /**
   * <p>
   * This method indexes a single record, using a free indexer in the pool.
   * </p>
   *
   * @param stringRdfRecord The record to index (can be parsed to RDF).
   * @param indexingProperties The properties of this indexing operation.
   * @throws IndexingException In case a problem occurred during indexing. indexer.
   */
  public void index(String stringRdfRecord, IndexingProperties indexingProperties) throws IndexingException {
    executeIndexCommand(indexer -> {
      indexer.index(stringRdfRecord, indexingProperties);
      return true;
    });
  }

  /**
   * Index tombstone.
   *
   * @param rdfAbout the rdf about
   * @param depublicationReason the depublication reason.
   * @return the boolean result of the tombstoning.
   * @throws IndexingException the indexing exception.
   */
  public boolean indexTombstone(String rdfAbout, DepublicationReason depublicationReason) throws IndexingException {
    return executeIndexCommand(indexer -> indexer.indexTombstone(rdfAbout, depublicationReason));
  }

  /**
   * Remove tombstone boolean.
   *
   * @param rdfAbout the rdf about
   * @return the boolean
   * @throws IndexingException the indexing exception
   */
  public boolean removeTombstone(String rdfAbout) throws IndexingException {
    return executeIndexCommand(indexer -> indexer.removeTombstone(rdfAbout));
  }

  /**
   * <p>
   * This method indexes a single record, using a free indexer in the pool.
   * </p>
   *
   * @param stringRdfRecord The record to index.
   * @param indexingProperties The properties of this indexing operation.
   * @throws IndexingException In case a problem occurred during indexing. indexer.
   */
  public void indexRdf(RDF stringRdfRecord, IndexingProperties indexingProperties) throws IndexingException {
    executeIndexCommand(indexer -> {
      indexer.indexRdf(stringRdfRecord, indexingProperties);
      return true;
    });
  }

  /**
   * This method removes a single record, using a free indexer in the pool
   *
   * @param stringRdfRecord The record to be removed.
   * @return the boolean result of the record removal.
   * @throws IndexingException In case something went wrong.
   */
  public boolean removeRecord(String stringRdfRecord) throws IndexingException {
    return executeIndexCommand(indexer -> indexer.remove(stringRdfRecord));
  }

  /**
   * This method indexes a record provided in the form of an InputStream and calculates its tier classifications, such as media
   * tier and metadata tier values. It uses an indexer from the pool to perform the operation and returns the tier result values.
   *
   * @param recordContent The InputStream containing the record content to be indexed.
   * @param indexingProperties The properties and parameters applied during the indexing operation.
   * @return A {@code TierResults} object that encapsulates the calculated media and metadata tier classifications.
   * @throws IndexingException If any issue occurs while performing the indexing operation.
   */
  public TierResults indexAndGetTierCalculations(InputStream recordContent, IndexingProperties indexingProperties)
      throws IndexingException {
    return executeIndexCommand(indexer -> indexer.indexAndGetTierCalculations(recordContent, indexingProperties));
  }

  /**
   * Removes all records from the specified dataset that were created/updated less than the maximum record date.
   *
   * @param datasetId The identifier of the dataset from which records will be removed.
   * @param maxRecordDate The maximum date of records to be removed; any record with a creation date on or before this date will
   * be removed.
   * @return The number of records that were successfully removed.
   * @throws IndexingException If an error occurs during the removal operation.
   */
  public int removeAll(String datasetId, Instant maxRecordDate) throws IndexingException {
    return executeIndexCommand(indexer -> indexer.removeAll(datasetId, maxRecordDate == null ? null : Date.from(maxRecordDate)));
  }

  /**
   * Counts the number of records in the specified dataset that have a timestamp updated less than the given maximum record date.
   * If the maximum record date is null, all records in the dataset will be counted.
   *
   * @param datasetId The identifier of the dataset from which records will be counted. Must not be null.
   * @param maxRecordDate The maximum date used as a cutoff for filtering records. If null, all records in the dataset will be
   * included in the count.
   * @return The total number of records that satisfy the criteria.
   * @throws IndexingException If an error occurs while performing the counting operation.
   */
  public long countRecords(String datasetId, Instant maxRecordDate) throws IndexingException {
    return executeIndexCommand(
        indexer -> indexer.countRecords(datasetId, maxRecordDate == null ? null : Date.from(maxRecordDate)));
  }

  private <R> R executeIndexCommand(IndexTask<R> indexTask) throws IndexingException {

    // Obtain indexer from the pool.
    final Indexer<?> indexer;
    try {
      indexer = pool.borrowObject();
    } catch (IndexingException e) {
      throw e;
    } catch (Exception e) {
      throw new IndexerRelatedIndexingException("Error while obtaining indexer from the pool.", e);
    }

    R taskResult = null;
    // Perform indexing and release indexer.
    try {
      taskResult = indexTask.performTask(indexer);
    } catch (IndexerRelatedIndexingException e) {
      invalidateAndSwallowException(indexer);
      throw new IndexerRelatedIndexingException("Invalidation done", e);
    } catch (IndexingException e) {
      //If any other indexing exception occurs we want to return the indexer to the pool
      pool.returnObject(indexer);
      if (e instanceof SetupRelatedIndexingException) {
        throw new SetupRelatedIndexingException("Pool object returned and an exception occurred", e);
      } else if (e instanceof RecordRelatedIndexingException) {
        throw new RecordRelatedIndexingException("Pool object returned and an exception occurred", e);
      }
    }

    // Return indexer to the pool if it has not been invalidated.
    pool.returnObject(indexer);
    return taskResult;
  }

  private void invalidateAndSwallowException(Indexer<?> indexer) {
    try {
      pool.invalidateObject(indexer);
    } catch (Exception e) {
      LOGGER.warn("Problem invalidating the indexer.", e);
    }
  }

  @Override
  public void close() {
    this.pool.close();
  }

  @FunctionalInterface
  private interface IndexTask<R> {

    R performTask(Indexer<?> indexer) throws IndexingException;
  }

  private static class PooledIndexerFactory extends BasePooledObjectFactory<Indexer<?>> {

    private final IndexerFactory<?> indexerFactory;

    public PooledIndexerFactory(IndexerFactory<?> indexerFactory) {
      this.indexerFactory = indexerFactory;
    }

    @Override
    public Indexer<?> create() throws SetupRelatedIndexingException {
      return indexerFactory.getIndexer();
    }

    @Override
    public PooledObject<Indexer<?>> wrap(Indexer indexer) {
      return new DefaultPooledObject<Indexer<?>>(indexer);
    }

    @Override
    public void destroyObject(PooledObject<Indexer<?>> pooledIndexer) throws Exception {
      pooledIndexer.getObject().close();
      super.destroyObject(pooledIndexer);
    }
  }
}
