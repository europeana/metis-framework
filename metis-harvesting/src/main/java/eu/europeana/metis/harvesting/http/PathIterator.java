package eu.europeana.metis.harvesting.http;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.ReportingIteration;
import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * Iterator over the files in the extracted archive directory.
 */
public class PathIterator extends AbstractHttpHarvestIterator<Path> {

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

  /**
   *
   * @return Path to the extracted directory on which iteration is done.
   */
  @Override
  public String getExtractedDirectory() {
    return super.getExtractedDirectory();
  }
}
