package eu.europeana.metis.harvesting.http;

import eu.europeana.metis.harvesting.HarvesterException;

/**
 * Implementations of this interface provide the functionality to harvest from HTTP (compressed
 * archive).
 */
public interface HttpHarvester {

  /**
   * Harvest from HTTP (compressed archive).
   *
   * @param archiveUrl The URL location of the compressed archive.
   * @param downloadDirectory The directory to which we download and extract the archive. Note: the
   * class does not clean up the downloaded or decompressed files.
   * @return An iterator that provides access to the decompressed records.
   * @throws HarvesterException In case there was an issue during the harvest.
   */
  HttpRecordIterator harvestRecords(String archiveUrl, String downloadDirectory)
          throws HarvesterException;

}
