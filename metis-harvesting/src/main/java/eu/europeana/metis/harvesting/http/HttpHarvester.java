package eu.europeana.metis.harvesting.http;

import eu.europeana.metis.harvesting.HarvesterException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Implementations of this interface provide the functionality to harvest from HTTP (compressed
 * archive).
 */
public interface HttpHarvester {

  /**
   * Harvest from HTTP (compressed archive).
   *
   * @param archiveUrl The URL location of the compressed archive. The URL can use either the
   * http(s) protocol or the file protocol.
   * @param downloadDirectory The directory to which we download and extract the archive. Note: the
   * class does not clean up the downloaded or decompressed files. The caller is responsible for
   * providing a directory that is safe (i.e. on the right file system).
   * @return An iterator that provides access to the decompressed records.
   * @throws HarvesterException In case there was an issue during the harvest.
   */
  HttpRecordIterator harvestRecords(String archiveUrl, String downloadDirectory)
          throws HarvesterException;

  /**
   * Harvest from HTTP (compressed archive). This is a convenience method for {@link
   * #harvestRecords(String, String)} that copies the input stream to a temporary file (in the
   * system's temporary directory) first. An attempt will be made to remove the temporary file
   * before this method returns.
   *
   * @param inputStream The input stream containing the compressed archive.
   * @param compressedFileType The type of the archive.
   * @param action The action to be performed.
   * @throws HarvesterException In case there was an issue during the harvest.
   */
  void harvestRecords(InputStream inputStream, CompressedFileExtension compressedFileType,
          Consumer<ArchiveEntry> action) throws HarvesterException;

  /**
   * Method to set up the maximum number of iterations through records during harvesting.
   * If there is none, the harvesting iterate through all records.
   *
   * @param maxOfIterations The maximum number of iterations
   */
  void setMaxNumberOfIterations(int maxOfIterations);

  /**
   * It creates a {@link HttpRecordIterator} with a InputStream into a temporary file directory.
   * It is needed to use the {@link HttpRecordIterator#deleteIteratorContent()} method if this method is used.
   * @param input The input stream from which we create the iterator
   * @param compressedFileType The type of compressed file type
   * @return A HttpRecordIterator based on a temporary file location
   * @throws HarvesterException In case there is an issue while using the input stream
   */
  HttpRecordIterator createTemporaryHttpHarvestIterator(InputStream input, CompressedFileExtension compressedFileType) throws HarvesterException;

  /**
   * An object representing an entry in a file archive.
   */
  interface ArchiveEntry {

    /**
     * @return The name of the entry. This is the file name (including extension, excluding the
     * path).
     */
    String getEntryName();

    /**
     * @return The content of the entry (in memory).
     */
    ByteArrayInputStream getEntryContent();
  }
}
