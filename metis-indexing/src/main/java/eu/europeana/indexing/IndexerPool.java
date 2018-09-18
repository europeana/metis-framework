package eu.europeana.indexing;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.indexing.exception.IndexingException;
import java.io.Closeable;
import java.time.Duration;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * <p>
 * This class is a utilities object providing the functionality of a pool of indexer objects, all
 * created from the same factory/settings. This class is designed to save the time otherwise spent
 * on repeatedly creating and destroying * indexers.
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
   * @param record The record to index (can be parsed to RDF).
   * @param preserveUpdateAndCreateTimesFromRdf This determines whether this indexer should use the
   * updated and created times from the incoming RDFs, or whether it computes its own.
   * @throws IndexingException In case a problem occurred during indexing.
   * indexer.
   */
  public void index(String record, boolean preserveUpdateAndCreateTimesFromRdf)
      throws IndexingException {
    indexRecord(indexer -> indexer.index(record, preserveUpdateAndCreateTimesFromRdf));
  }

  /**
   * <p>
   * This method indexes a single record, using a free indexer in the pool.
   * </p>
   *
   * @param record The record to index.
   * @param preserveUpdateAndCreateTimesFromRdf This determines whether this indexer should use the
   * updated and created times from the incoming RDFs, or whether it computes its own.
   * @throws IndexingException In case a problem occurred during indexing.
   * indexer.
   */
  public void indexRdf(RDF record, boolean preserveUpdateAndCreateTimesFromRdf)
      throws IndexingException {
    indexRecord(indexer -> indexer.indexRdf(record, preserveUpdateAndCreateTimesFromRdf));
  }

  private void indexRecord(IndexTask indexTask) throws IndexingException {

    // Obtain indexer from the pool.
    final Indexer indexer;
    try {
      indexer = pool.borrowObject();
    } catch (IndexingException e) {
      throw e;
    } catch (Exception e) {
      throw new IndexingException("Error while obtaining indexer from the pool.", e);
    }

    // Perform indexing and release indexer.
    try {
      indexTask.performTask(indexer);
    } finally {
      pool.returnObject(indexer);
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

  private static class PooledIndexerFactory extends BasePooledObjectFactory<Indexer> {

    private final IndexerFactory indexerFactory;

    public PooledIndexerFactory(IndexerFactory indexerFactory) {
      this.indexerFactory = indexerFactory;
    }

    @Override
    public Indexer create() throws IndexingException {
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
