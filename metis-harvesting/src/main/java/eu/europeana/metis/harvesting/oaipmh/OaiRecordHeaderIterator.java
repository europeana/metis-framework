package eu.europeana.metis.harvesting.oaipmh;

import eu.europeana.metis.harvesting.HarvesterException;
import java.io.Closeable;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface OaiRecordHeaderIterator extends Closeable {

  void forEachFiltered(Consumer<OaiRecordHeader> action, Predicate<OaiRecordHeader> filter)
          throws HarvesterException;

  default void forEach(Consumer<OaiRecordHeader> action) throws HarvesterException {
    forEachFiltered(action, header -> true);
  }

  default void forEachNonDeleted(Consumer<OaiRecordHeader> action) throws HarvesterException {
    forEachFiltered(action, Predicate.not(OaiRecordHeader::isDeleted));
  }
}
