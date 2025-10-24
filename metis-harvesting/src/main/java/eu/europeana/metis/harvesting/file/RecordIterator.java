package eu.europeana.metis.harvesting.file;

import eu.europeana.metis.harvesting.FullRecord;
import eu.europeana.metis.harvesting.FullRecordHarvestingIterator;
import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.HarvesterRuntimeException;
import eu.europeana.metis.harvesting.ReportingIteration;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * An implementation of {@link FullRecordHarvestingIterator} that iterates over {@link FullRecord} entities located within a
 * specific directory.
 */
public class RecordIterator extends AbstractFileHarvestIterator<FullRecord>
    implements FullRecordHarvestingIterator<FullRecord, Path> {

  public RecordIterator(Path extractedDirectory) {
    super(extractedDirectory);
  }

  @Override
  public void forEachFiltered(ReportingIteration<FullRecord> action, Predicate<Path> filter)
      throws HarvesterException {
    forEachFileFiltered(action, filter);
  }

  @Override
  public Iterator<FullRecord> iterator() {
    Stream<Path> pathStream;
    try {
      pathStream = walkFilteredFiles();
    } catch (IOException e) {
      throw new HarvesterRuntimeException("Error walking directory: " + getExtractedDirectory(), e);
    }
    return pathStream
        .map(path -> {
          try {
            return getFullRecord(path);
          } catch (IOException e) {
            throw new HarvesterRuntimeException(e);
          }
        })
        .iterator();
  }
}
