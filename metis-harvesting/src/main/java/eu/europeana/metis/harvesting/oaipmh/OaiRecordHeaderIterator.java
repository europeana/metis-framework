package eu.europeana.metis.harvesting.oaipmh;

import eu.europeana.metis.harvesting.HarvesterException;
import java.io.Closeable;
import java.util.function.Predicate;

public interface OaiRecordHeaderIterator extends Closeable {

  void forEachFiltered(Predicate<OaiRecordHeader> action, Predicate<OaiRecordHeader> filter)
          throws HarvesterException;

  default void forEach(Predicate<OaiRecordHeader> action) throws HarvesterException {
    forEachFiltered(action, header -> true);
  }

  default void forEachNonDeleted(Predicate<OaiRecordHeader> action) throws HarvesterException {
    forEachFiltered(action, Predicate.not(OaiRecordHeader::isDeleted));
  }
}
