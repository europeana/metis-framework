package eu.europeana.metis.harvesting.oaipmh;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.ReportingIteration;
import java.io.Closeable;
import java.util.function.Predicate;

public interface OaiRecordHeaderIterator extends Closeable {

  void forEachFiltered(ReportingIteration<OaiRecordHeader> action, Predicate<OaiRecordHeader> filter)
          throws HarvesterException;

  default void forEach(ReportingIteration<OaiRecordHeader> action) throws HarvesterException {
    forEachFiltered(action, header -> true);
  }

  default void forEachNonDeleted(ReportingIteration<OaiRecordHeader> action) throws HarvesterException {
    forEachFiltered(action, Predicate.not(OaiRecordHeader::isDeleted));
  }
}
