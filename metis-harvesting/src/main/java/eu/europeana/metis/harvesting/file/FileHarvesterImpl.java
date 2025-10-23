package eu.europeana.metis.harvesting.file;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.HarvestingIterator;
import eu.europeana.metis.utils.CompressedFileExtension;
import eu.europeana.metis.utils.CompressedFileHandler;
import java.io.IOException;
import java.nio.file.Path;

public class FileHarvesterImpl implements FileHarvester {

  @Override
  public HarvestingIterator<Path, Path> createHarvestIterator(Path archivePath, Path downloadDirectory)
      throws HarvesterException {
    try {
      new CompressedFileHandler().extract(archivePath, downloadDirectory);
    } catch (IOException e) {
      throw new HarvesterException("Failure while extracting file", e);
    }
    Path path = CompressedFileExtension.removeExtension(archivePath);
    return new PathIterator(path);
  }
}
