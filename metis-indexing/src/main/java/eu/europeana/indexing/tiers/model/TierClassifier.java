package eu.europeana.indexing.tiers.model;

import eu.europeana.indexing.utils.RdfWrapper;

/**
 * Implementations of this class can classify RDF entities and award them a tier.
 *
 * @param <T> The type of the tier for which this classifier can be used.
 */
public interface TierClassifier<T extends Tier, S> {

  TierClassification<T, S> classify(RdfWrapper entity);

  class TierClassification<T extends Tier, S> {

    final T tier;
    final S classification;

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
