package eu.europeana.indexing.tiers.model;

import java.util.ArrayList;
import java.util.List;
import eu.europeana.indexing.utils.RdfWrapper;

/**
 * This tier classifier combines various classifiers. When asked to classify a record, it calls all
 * the containing classifiers and awards the record the minimum of the resulting tiers.
 *
 * @param <T> The type of the tier that this classifier returns.
 */
public class CombinedClassifier<T extends Tier> implements TierClassifier<T> {

  private final List<TierClassifier<T>> classifiers;

  /**
   * Constructor.
   *
   * @param classifiers The classifiers that this classifier combines.
   */
  public CombinedClassifier(List<TierClassifier<T>> classifiers) {
    if (classifiers == null || classifiers.isEmpty()) {
      throw new IllegalArgumentException("Combined classifier expects at least one algorithm.");
    }
    this.classifiers = new ArrayList<>(classifiers);
  }

  @Override
  public T classify(RdfWrapper entity) {
    return classifiers.stream().map(algorithm -> algorithm.classify(entity))
        .reduce(Tier::min).orElseThrow(IllegalStateException::new);
  }
}

