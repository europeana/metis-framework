package eu.europeana.metis.harvesting.oaipmh;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.HarvestingIterator;
import eu.europeana.metis.harvesting.ReportingIteration;
import java.util.function.Predicate;

/**
 * Implementations of this interface allow iterative access to records harvested using OAI-PMH. The
 * iterator needs to be closed after use.
 */
public interface OaiRecordHeaderIterator extends HarvestingIterator<OaiRecordHeader, OaiRecordHeader> {

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
