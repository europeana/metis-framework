package eu.europeana.metis.harvesting.oaipmh;

import eu.europeana.metis.harvesting.HarvestingIterator;

/**
 * Implementations of this interface allow iterative access to records harvested using OAI-PMH. The
 * iterator needs to be closed after use.
 * @deprecated Use {@link HarvestingIterator} instead.
 */
@Deprecated
public interface OaiRecordHeaderIterator extends HarvestingIterator<OaiRecordHeader, OaiRecordHeader> {

}
