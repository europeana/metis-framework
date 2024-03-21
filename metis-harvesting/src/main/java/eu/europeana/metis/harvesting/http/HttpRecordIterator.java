package eu.europeana.metis.harvesting.http;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.HarvestingIterator;
import java.nio.file.Path;

/**
 * Implementations of this interface provide iterative access to the decompressed results of a HTTP
 * (compressed archive) harvest. Closing this iterator cleans up the downloaded and decompressed
 * files.
 * @deprecated Use {@link HarvestingIterator} instead.
 */
@Deprecated
public interface HttpRecordIterator extends HarvestingIterator<Path, Path> {

  /**
   * Returns the extracted directory used to create the iterator if there is any
   *
   * @deprecated (see TODO explanation)
   *
   * TODO JV This method is (I think) only used for computing the relative file path
   *  (by way of harvesting ID) in the Sandbox. We should make that the return value of
   *  {@link eu.europeana.metis.harvesting.http.HttpHarvester.ArchiveEntry#getHarvestingIdentifier()}
   *  so that we don't have to expose this directory. It is also less of a security risk.
   *
   * @return The extracted directory as a string if there is any, empty string if there is none
   */
  @Deprecated
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