package eu.europeana.metis.harvesting;

/**
 * Implementations of this interface allow iterative access to records as they are being harvested.
 * The iterator needs to be closed after use.
 *
 * @param <R> The type of the record to harvest.
 * @param <C> The type of the object on which filtering is to be applied.
 */
public interface FullRecordHarvestingIterator<R extends FullRecord, C> extends
    HarvestingIterator<R, C> {

}
