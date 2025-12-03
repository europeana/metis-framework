package eu.europeana.metis.harvesting.file;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.HarvestingIterator;
import java.nio.file.Path;

/**
 * Implementations of this interface are used for processing files in archives by extracting their contents to a specified
 * directory and providing an iterator to iterate through the files.
 */
public interface FileHarvester {

  /**
   * Creates a harvesting iterator that iterates through files in an archive after extracting its contents to a specified
   * directory.
   *
   * @param archivePath The path to the archive file to be extracted and iterated over.
   * @param extractionDirectory The path to the directory where the archive will be extracted.
   * @return A harvesting iterator that can iterate over the paths of the files in the extracted directory.
   * @throws HarvesterException If an error occurs during archive extraction or iterator creation.
   */
  HarvestingIterator<Path, Path> createHarvestIteratorFromArchive(Path archivePath, Path extractionDirectory)
      throws HarvesterException;

  /**
   * Converts a partitioned {@link Path} into its canonical path.
   *
   * @param partitionedPath the partitioned {@link Path} to be converted
   * @return the canonical path of the partitioned path
   */
  Path convertPartitionedPathToCanonical(Path partitionedPath);
}
