package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.LinkCheckingException;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import java.io.Closeable;
import java.time.Duration;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * <p>
 * This class is a utilities object providing the functionality of a pool of media processor
 * objects, all created from the same factory/settings. This class is designed to save the time
 * otherwise spent on repeatedly creating and destroying media processors.
 * </p>
 * <p>
 * This pool can have an unlimited number of indexers, that will be created automatically whenever
 * they are needed (i.e. when all other indexers are in use already). This thread pool implements
 * the automatic destruction of idle indexers, given a certain permissible idle time.
 * </p>
 * <p>
 * This class is used through one of the inner subclasses that this class provides.
 * </p>
 * <p>
 * This class is thread-safe.
 * </p>
 *
 * @param <I> The input type of the processor.
 * @param <O> The output type of the processor.
 * @param <E> The exception type thrown by processing tasks.
 * @param <T> The type of the processor managed by this pool.
 */
public abstract class AbstractMediaProcessorPool<I, O, E extends Exception, T extends PoolableProcessor<I, O, E>> implements
    Closeable {

  private static final int MAX_IDLE_TIME_FOR_PROCESSOR_IN_SECONDS = 300;
  private static final int IDLE_TIME_CHECK_INTERVAL_IN_SECONDS = 60;

  private final GenericObjectPool<T> pool;

  private AbstractMediaProcessorPool(MediaProcessorFactory processorFactory) {

    // Create indexer pool with default options.
    pool = new GenericObjectPool<>(
        new PooledProcessorFactory<>(processorFactory, this::createProcessor));

    // Set custom options for the size of the pool: no max or min number of processor objects.
    pool.setMaxIdle(-1);
    pool.setMinIdle(-1);
    pool.setMaxTotal(-1);

    // Set custom options for pool regarding eviction (when processor has been idle for some time).
    pool.setSoftMinEvictableIdleTimeMillis(-1);
    pool.setMinEvictableIdleTimeMillis(convertSecsToMillis(MAX_IDLE_TIME_FOR_PROCESSOR_IN_SECONDS));
    pool.setTimeBetweenEvictionRunsMillis(convertSecsToMillis(IDLE_TIME_CHECK_INTERVAL_IN_SECONDS));
  }

  private static long convertSecsToMillis(long seconds) {
    return Duration.ofSeconds(seconds).toMillis();
  }

  /**
   * This method provides access to the pool. It takes one processor from the pool and processes the
   * given input.
   *
   * @param input The input to process.
   * @return The result of processing the given input.
   * @throws E In case a problem occurred while processing the input.
   */
  public O processTask(I input) throws MediaProcessorException, E {

    // Obtain indexer from the pool.
    final T processor;
    try {
      processor = pool.borrowObject();
    } catch (Exception e) {
      throw new MediaProcessorException("Error while obtaining processor from the pool.", e);
    }

    // Perform indexing and release indexer.
    try {
      return processor.processTask(input);
    } finally {
      pool.returnObject(processor);
    }
  }

  abstract T createProcessor(MediaProcessorFactory processorFactory) throws MediaProcessorException;

  @Override
  public void close() {
    this.pool.close();
  }

  private static class PooledProcessorFactory<T extends Closeable> extends
      BasePooledObjectFactory<T> {

    private final MediaProcessorFactory processorFactory;
    private final ProcessorCreator<T> processorCreator;

    PooledProcessorFactory(MediaProcessorFactory processorFactory,
        ProcessorCreator<T> processorCreator) {
      this.processorFactory = processorFactory;
      this.processorCreator = processorCreator;
    }

    @Override
    public T create() throws MediaProcessorException {
      return processorCreator.create(processorFactory);
    }

    @Override
    public PooledObject<T> wrap(T processor) {
      return new DefaultPooledObject<>(processor);
    }

    @Override
    public void destroyObject(PooledObject<T> pooledProcessor) throws Exception {
      pooledProcessor.getObject().close();
      super.destroyObject(pooledProcessor);
    }
  }

  private interface ProcessorCreator<T> {

    T create(MediaProcessorFactory factory) throws MediaProcessorException;
  }

  /**
   * A {@link AbstractMediaProcessorPool} for {@link MediaExtractor} instances.
   */
  public static class MediaExtractorPool extends
      AbstractMediaProcessorPool<RdfResourceEntry, ResourceExtractionResult, MediaExtractionException, MediaExtractor> {

    /**
     * Constructor.
     *
     * @param processorFactory The processor factory with which to create the media extractor.
     */
    public MediaExtractorPool(MediaProcessorFactory processorFactory) {
      super(processorFactory);
    }

    @Override
    MediaExtractor createProcessor(MediaProcessorFactory processorFactory)
        throws MediaProcessorException {
      return processorFactory.createMediaExtractor();
    }
  }

  /**
   * A {@link AbstractMediaProcessorPool} for {@link LinkChecker} instances.
   */
  public static class LinkCheckerPool extends
      AbstractMediaProcessorPool<String, Void, LinkCheckingException, LinkChecker> {

    /**
     * Constructor.
     *
     * @param processorFactory The processor factory with which to create the link checker.
     */
    public LinkCheckerPool(MediaProcessorFactory processorFactory) {
      super(processorFactory);
    }

    @Override
    LinkChecker createProcessor(MediaProcessorFactory processorFactory)
        throws MediaProcessorException {
      return processorFactory.createLinkChecker();
    }
  }
}
