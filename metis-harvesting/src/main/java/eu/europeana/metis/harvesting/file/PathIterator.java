package eu.europeana.metis.harvesting.file;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.HarvesterRuntimeException;
import eu.europeana.metis.harvesting.ReportingIteration;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.function.Predicate;

/**
 * Iterator over the files in the extracted archive directory.
 */
public class PathIterator extends AbstractFileHarvestIterator<Path> {

  /**
   * Creates PathIterator
   *
   * @param extractedDirectory Path to the extracted directory on which iteration is done
   */
  public PathIterator(Path extractedDirectory) {
    super(extractedDirectory);
  }

  /** Iterate through the files inside the directory while applying a filter (potentially skipping some files).
      *
      * @param action The action that would be performed for every path accepted by filter.
      * @param filter The filter to apply (only records that return true will be sent to the action).
      * @throws HarvesterException In case there was a problem while iterating.
   */
  @Override
  public void forEachFiltered(ReportingIteration<Path> action, Predicate<Path> filter)
      throws HarvesterException {
    forEachPathFiltered(action, filter);
  }

  @Override
  public Iterator<Path> iterator() {
    try {
      return walkFilteredPathsStream().iterator();
    } catch (IOException e) {
      throw new HarvesterRuntimeException("Error walking directory: " + getExtractedDirectory(), e);
    }
  }
}
