package eu.europeana.indexing.tiers.model;

import eu.europeana.indexing.utils.RdfWrapper;

/**
 * Implementations of this class can classify RDF entities and award them a tier.
 *
 * @param <T> The type of the tier for which this classifier can be used.
 */
public interface TierClassifier<T extends Tier> {

  /**
   * Classify the entity and award it a tier.
   *
   * @param entity The entity to classify.
   * @return The tier. Is not null.
   */
  T classify(RdfWrapper entity);

}
