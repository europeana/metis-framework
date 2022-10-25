package eu.europeana.metis.harvesting.http;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.ReportingIteration;
import eu.europeana.metis.harvesting.ReportingIteration.IterationResult;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementations of this interface provide iterative access to the decompressed results of a HTTP
 * (compressed archive) harvest. Note: the class does not clean up the downloaded or decompressed
 * files.
 */
public interface HttpRecordIterator {

  /**
   * Returns the extracted directory used to create the iterator if there is any
   * @return The extracted directory as a string if there is any, empty string if there is none
   */
  String getExtractedDirectory();

  void deleteIteratorContent();

  /**
   * Iterate through the decompressed records.
   *
   * @param action The iteration action to be executed for each harvested record.
   * @throws HarvesterException In case something went wrong during the iteration.
   */
  void forEach(ReportingIteration<Path> action) throws HarvesterException;

  /**
   * Count the number of decompressed records.
   *
   * @return The number of decompressed records.
   * @throws HarvesterException In case something went wrong during the counting.
   */
  default int getExpectedRecordCount() throws HarvesterException {
    final AtomicInteger counter = new AtomicInteger(0);
    forEach(path -> {
      counter.incrementAndGet();
      return IterationResult.CONTINUE;
    });
    return counter.get();
  }
}
