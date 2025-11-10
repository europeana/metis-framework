package eu.europeana.metis.harvesting.file;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.HarvestingIterator;
import eu.europeana.metis.utils.CompressedFileExtension;
import eu.europeana.metis.utils.CompressedFileHandler;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Implementation of the {@link FileHarvester} interface.
 */
public class FileHarvesterImpl implements FileHarvester {
  private final CompressedFileHandler compressedFileHandler = new CompressedFileHandler();

  @Override
  public HarvestingIterator<Path, Path> createHarvestIteratorFromArchive(Path archivePath, Path extractionDirectory)
      throws HarvesterException {
    try {
      compressedFileHandler.extract(archivePath, extractionDirectory);
    } catch (IOException e) {
      throw new HarvesterException("Failure while extracting file", e);
    }
    Path path = CompressedFileExtension.removeExtension(archivePath);
    return new PathIterator(path);
  }
}
