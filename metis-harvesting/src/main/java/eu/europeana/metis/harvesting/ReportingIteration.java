package eu.europeana.metis.harvesting;

import java.io.IOException;

/**
 * Implementations of this interface represent an iteration of a data iterator that also reports on
 * whether to continue.
 *
 * @param <T> The type of the data to iterate over.
 */
@FunctionalInterface
public interface ReportingIteration<T> {

  /**
   * The result of an iteration, indicating whether to proceed.
   */
  enum IterationResult {TERMINATE, CONTINUE}

  /**
   * Perform one iteration for the given data.
   *
   * @param data The data to process.
   * @return Whether to continue processing.
   * @throws IOException in case there was a harvesting related issue. This will cause the remaining
   *                     records not to be processed (as if {@link IterationResult#TERMINATE} was
   *                     passed).
   */
  IterationResult process(T data) throws IOException;
}
