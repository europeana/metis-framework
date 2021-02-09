package eu.europeana.metis.harvesting.http;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.ReportingIteration;
import eu.europeana.metis.harvesting.ReportingIteration.IterationResult;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

public interface HttpRecordIterator {

  void forEach(ReportingIteration<Path> action) throws HarvesterException;

  default int getExpectedRecordCount() throws HarvesterException {
    final AtomicInteger counter = new AtomicInteger(0);
    forEach(path -> {
      counter.incrementAndGet();
      return IterationResult.CONTINUE;
    });
    return counter.get();
  }
}
