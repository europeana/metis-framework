package eu.europeana.indexing.solr.tiers.model;

import eu.europeana.indexing.utils.RdfWrapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
    this.classifiers = new ArrayList<>(classifiers);
    if (this.classifiers.isEmpty()) {
      throw new IllegalArgumentException("Combined classifier expects at least one algorithm.");
    }
  }

  @Override
  public T classify(RdfWrapper entity) {
    return classifiers.stream().map(algorithm -> algorithm.classify(entity))
        .min(Comparator.comparing(Tier::getLevel)).orElseThrow(IllegalStateException::new);
  }
}

