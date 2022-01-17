package eu.europeana.indexing.tiers.model;

import eu.europeana.indexing.utils.RdfWrapper;

/**
 * Implementations of this class can classify RDF entities and award them a tier with a breakdown.
 *
 * @param <T> The type of the tier for which this classifier can be used
 * @param <S> The breakdown of the tier classification
 */
public interface TierClassifier<T extends Tier, S> {

  /**
   * Analyzes and classifies an entity to a tier providing a breakdown.
   *
   * @param entity the entity to classify
   * @return the tier classification with its breakdown
   */
  TierClassification<T, S> classify(RdfWrapper entity);

  /**
   * A class to package the Tier and an object representing the tier calculation.
   *
   * @param <T> the type of the tier
   * @param <S> the tier calculation details
   */
  class TierClassification<T extends Tier, S> {

    private final T tier;
    private final S classification;

    public TierClassification(T tier, S classification) {
      this.tier = tier;
      this.classification = classification;
    }

    public T getTier() {
      return tier;
    }

    public S getClassification() {
      return classification;
    }
  }
}
