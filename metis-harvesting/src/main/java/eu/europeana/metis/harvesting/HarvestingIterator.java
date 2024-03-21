package eu.europeana.metis.harvesting;

import java.io.Closeable;
import java.util.function.Predicate;

/**
 * Implementations of this interface allow iterative access to records as they are being harvested.
 * The iterator needs to be closed after use.
 *
 * @param <R> The type of the record to harvest.
 * @param <C> The type of the object on which filtering is to be applied.
 */
public interface HarvestingIterator<R, C> extends Closeable {

  /**
   * Iterate through the records while applying a filter (potentially skipping some records).
   *
   * @param action The iteration to perform. It needs to return a result.
   * @param filter The filter to apply (only records that return true will be sent to the action).
   * @throws HarvesterException In case there was a problem while harvesting.
   */
  void forEachFiltered(ReportingIteration<R> action, Predicate<C> filter) throws HarvesterException;

  /**
   * Iterate through all the records.
   *
   * @param action The iteration to perform. It needs to return a result.
   * @throws HarvesterException In case there was a problem while harvesting.
   */
  default void forEach(ReportingIteration<R> action) throws HarvesterException {
    forEachFiltered(action, header -> true);
  }

  /**
   * Attempts to count the number of records. This method may make assumptions, and any result is
   * only indicative. Server requests or other IO operations may be performed in order to perform
   * this count, so this method is to be used sparingly.
   *
   * @return The number of records. Or null if the number could not be determined.
   * @throws HarvesterException In case something went wrong.
   */
  Integer countRecords() throws HarvesterException;
}
