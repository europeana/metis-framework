package eu.europeana.metis.mediaprocessing;

import java.io.Closeable;

/**
 * This is the interface for processors that can be subject to the pooling as implemented in {@link
 * AbstractMediaProcessorPool}.
 *
 * @param <I> The input type of the processor.
 * @param <O> The output type of the processor.
 * @param <E> The exception type thrown by processing tasks.
 */
public interface PoolableProcessor<I, O, E extends Exception> extends Closeable {

  /**
   * Processes the task that this processor provides.
   *
   * @param input The input to process.
   * @return The result of processing the given input.
   * @throws E In case a problem occurred while processing the input.
   */
  O processTask(I input) throws E;

}
