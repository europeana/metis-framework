package eu.europeana.metis.harvesting.oaipmh;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.ReportingIteration;
import java.io.Closeable;
import java.util.function.Predicate;

/**
 * Implementations of this interface allow iterative access to records harvested using OAI-PMH. The
 * iterator needs to be closed after use.
 */
public interface OaiRecordHeaderIterator extends Closeable {

  /**
   * Iterate through the records while applying a filter (potentially skipping some records).
   *
   * @param action The iteration to perform. It needs to return a result.
   * @param filter The filter to apply (only records that return true will be sent to the action).
   * @throws HarvesterException In case there was a problem while harvesting.
   */
  void forEachFiltered(ReportingIteration<OaiRecordHeader> action,
          Predicate<OaiRecordHeader> filter)
          throws HarvesterException;

  /**
   * Iterate through all the records.
   *
   * @param action The iteration to perform. It needs to return a result.
   * @throws HarvesterException In case there was a problem while harvesting.
   */
  default void forEach(ReportingIteration<OaiRecordHeader> action) throws HarvesterException {
    forEachFiltered(action, header -> true);
  }

  /**
   * Iterate through all non-deleted records.
   *
   * @param action The iteration to perform. It needs to return a result.
   * @throws HarvesterException In case there was a problem while harvesting.
   */
  default void forEachNonDeleted(ReportingIteration<OaiRecordHeader> action)
          throws HarvesterException {
    forEachFiltered(action, Predicate.not(OaiRecordHeader::isDeleted));
  }
}
