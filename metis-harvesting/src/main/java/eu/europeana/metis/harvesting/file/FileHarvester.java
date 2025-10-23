package eu.europeana.metis.harvesting.file;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.HarvestingIterator;
import java.nio.file.Path;

public interface FileHarvester {

  HarvestingIterator<Path, Path> createHarvestIterator(Path archivePath, Path downloadDirectory)
      throws HarvesterException;
}
