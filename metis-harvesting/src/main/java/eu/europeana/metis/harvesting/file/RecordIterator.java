package eu.europeana.metis.harvesting.file;

import eu.europeana.metis.harvesting.FullRecord;
import eu.europeana.metis.harvesting.FullRecordHarvestingIterator;
import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.ReportingIteration;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.function.Predicate;

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
    try {
      return walkFilteredFiles()
          .map(path -> {
            try {
              return getFullRecord(path);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          })
          .iterator();
    } catch (IOException e) {
      throw new RuntimeException("Error walking directory: " + getExtractedDirectory(), e);
    }
  }
}
