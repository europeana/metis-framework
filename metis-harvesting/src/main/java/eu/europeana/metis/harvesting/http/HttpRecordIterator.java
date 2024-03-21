package eu.europeana.metis.harvesting.http;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.HarvestingIterator;
import java.nio.file.Path;

/**
 * Implementations of this interface provide iterative access to the decompressed results of a HTTP
 * (compressed archive) harvest. Closing this iterator cleans up the downloaded and decompressed
 * files.
 */
public interface HttpRecordIterator extends HarvestingIterator<Path, Path> {

  /**
   * Returns the extracted directory used to create the iterator if there is any
   *
   * @return The extracted directory as a string if there is any, empty string if there is none
   */
  String getExtractedDirectory();

  /**
   * @deprecated Use {@link #close()} instead.
   */
  @Deprecated
  void deleteIteratorContent();

  /**
   * @deprecated Use {@link #countRecords()} instead.
   */
  @Deprecated
  default int getExpectedRecordCount() throws HarvesterException {
    return countRecords();
  }
}