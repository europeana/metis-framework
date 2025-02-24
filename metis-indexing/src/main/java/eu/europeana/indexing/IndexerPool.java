package eu.europeana.indexing;

import eu.europeana.indexing.exception.IndexerRelatedIndexingException;
import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.utils.DepublicationReason;
import java.io.Closeable;
import java.time.Duration;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class is a utilities object providing the functionality of a pool of indexer objects, all
 * created from the same factory/settings. This class is designed to save the time otherwise spent
 * on repeatedly creating and destroying indexers.
 * </p>
 * <p>
 * This pool can have an unlimited number of indexers, that will be created automatically whenever
 * they are needed (i.e. when all other indexers are in use already). This thread pool implements
 * the automatic destruction of idle indexers, given a certain permissible idle time.
 * </p>
 * <p>
 * This class is thread-safe.
 * </p>
 */
public class IndexerPool implements Closeable {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexerPool.class);

  private final GenericObjectPool<Indexer> pool;

  /**
   * Constructor.
   *
   * @param indexingSettings The  settings with which to create the indexer instances in the pool.
   * @param maxIdleTimeForIndexerInSecs The idle time after which an indexer is eligible for
   * destruction, in seconds.
   * @param idleTimeCheckIntervalInSecs The interval with which we check the idle time of indexers
   * to decide whether to destroy them, in seconds.
   */
  public IndexerPool(IndexingSettings indexingSettings, long maxIdleTimeForIndexerInSecs,
      long idleTimeCheckIntervalInSecs) {
    this(new IndexerFactory(indexingSettings), maxIdleTimeForIndexerInSecs,
        idleTimeCheckIntervalInSecs);
  }

  /**
   * Constructor.
   *
   * @param indexerFactory The factory from which to create the indexer instances in the pool.
   * @param maxIdleTimeForIndexerInSecs The idle time after which an indexer is eligible for
   * destruction, in seconds.
   * @param idleTimeCheckIntervalInSecs The interval with which we check the idle time of indexers
   * to decide whether to destroy them, in seconds.
   */
  public IndexerPool(IndexerFactory indexerFactory, long maxIdleTimeForIndexerInSecs,
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
    indexRecord(indexer -> indexer.index(stringRdfRecord, indexingProperties));
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
    return indexBoolTask(indexer -> indexer.indexTombstone(rdfAbout, depublicationReason));
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
    indexRecord(indexer -> indexer.indexRdf(stringRdfRecord, indexingProperties));
  }

  /**
   * This method removes a single record, using a free indexer in the pool
   *
   * @param stringRdfRecord The record to be removed.
   * @return the boolean result of the record removal.
   * @throws IndexingException In case something went wrong.
   */
  public boolean remove(String stringRdfRecord) throws IndexingException {
    return indexBoolTask(indexer -> indexer.remove(stringRdfRecord));
  }

  private void indexRecord(IndexTask indexTask) throws IndexingException {

    // Obtain indexer from the pool.
    final Indexer indexer;
    try {
      indexer = pool.borrowObject();
    } catch (IndexingException e) {
      throw e;
    } catch (Exception e) {
      throw new IndexerRelatedIndexingException("Error while obtaining indexer from the pool.", e);
    }

    // Perform indexing and release indexer.
    try {
      indexTask.performTask(indexer);
    } catch (IndexerRelatedIndexingException e) {
      invalidateAndSwallowException(indexer);
      throw e;
    } catch (IndexingException e) {
      //If any other indexing exception occurs we want to return the indexer to the pool
      pool.returnObject(indexer);
      throw e;
    }

    // Return indexer to the pool if it has not been invalidated.
    pool.returnObject(indexer);
  }

  private boolean indexBoolTask(IndexBoolTask indexTask) throws IndexingException {
    // Obtain indexer from the pool.
    final Indexer indexer;
    try {
      indexer = pool.borrowObject();
    } catch (IndexingException e) {
      throw e;
    } catch (Exception e) {
      throw new IndexerRelatedIndexingException("Error while obtaining indexer from the pool.", e);
    }

    // Perform indexing and release indexer.
    boolean result = false;
    try {
      result = indexTask.performTask(indexer);
    } catch (IndexerRelatedIndexingException e) {
      invalidateAndSwallowException(indexer);
      throw new IndexerRelatedIndexingException("Index Task Level 1",e);
    } catch (IndexingException e) {
      //If any other indexing exception occurs we want to return the indexer to the pool
      LOGGER.error("index bool task l2 {}", e.getMessage());
      pool.returnObject(indexer);
      if (e instanceof SetupRelatedIndexingException) {
        throw new SetupRelatedIndexingException("Pool object returned and an exception occurred", e);
      } else if (e instanceof RecordRelatedIndexingException) {
        throw new RecordRelatedIndexingException("Pool object returned and an exception occurred", e);
      }
    }

    // Return indexer to the pool if it has not been invalidated.
    pool.returnObject(indexer);
    return result;
  }

  private void invalidateAndSwallowException(Indexer indexer) {
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
  private interface IndexTask {
    void performTask(Indexer indexer) throws IndexingException;
  }

  @FunctionalInterface
  private interface IndexBoolTask {
    boolean performTask(Indexer indexer) throws IndexingException;
  }

  private static class PooledIndexerFactory extends BasePooledObjectFactory<Indexer> {

    private final IndexerFactory indexerFactory;

    public PooledIndexerFactory(IndexerFactory indexerFactory) {
      this.indexerFactory = indexerFactory;
    }

    @Override
    public Indexer create() throws SetupRelatedIndexingException, IndexerRelatedIndexingException {
      return indexerFactory.getIndexer();
    }

    @Override
    public PooledObject<Indexer> wrap(Indexer indexer) {
      return new DefaultPooledObject<>(indexer);
    }

    @Override
    public void destroyObject(PooledObject<Indexer> pooledIndexer) throws Exception {
      pooledIndexer.getObject().close();
      super.destroyObject(pooledIndexer);
    }
  }
}
