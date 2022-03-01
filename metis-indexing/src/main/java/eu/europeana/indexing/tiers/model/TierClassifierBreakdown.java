package eu.europeana.indexing.tiers.model;

import eu.europeana.indexing.utils.RdfWrapper;

/**
 * Implementations of this class can classify RDF entities breakdowns that may contain a tier value.
 *
 * @param <S> The breakdown of the tier classification
 */
public interface TierClassifierBreakdown<S extends TierProvider<MetadataTier>> {

  /**
   * Analyze an entity to get the breakdown that may contain a tier value
   *
   * @param entity the entity to analyze
   * @return the breakdown of the analysis
   */
  S classifyBreakdown(RdfWrapper entity);
}
