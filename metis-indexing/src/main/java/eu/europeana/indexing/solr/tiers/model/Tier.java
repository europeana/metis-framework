package eu.europeana.indexing.solr.tiers.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * This is the interface that is imposed on different tier systems. It is expected that within one
 * tier system, labels as well as levels are unique.
 */
public interface Tier {

  /**
   * @return The label of this tier, which is human readable.
   */
  String getLabel();

  /**
   * @return The level of this tier, where a high level denotes higher quality.
   */
  int getLevel();

  /**
   * Computes the maximum of two tiers by level (see {@link Tier#getLevel()}).
   *
   * @param tier1 The first tier.
   * @param tier2 The second tier.
   * @param <T> The type of the tiers to be compared.
   * @return The tier with the highest level.
   */
  static <T extends Tier> T max(T tier1, T tier2) {
    return Collections.max(Arrays.asList(tier1, tier2), getComparator());
  }

  /**
   * Computes the minimum of two tiers by level (see {@link Tier#getLevel()}).
   *
   * @param tier1 The first tier.
   * @param tier2 The second tier.
   * @param <T> The type of the tiers to be compared.
   * @return The tier with the lowest level.
   */
  static <T extends Tier> T min(T tier1, T tier2) {
    return Collections.min(Arrays.asList(tier1, tier2), getComparator());
  }

  /**
   * Returns a comparator for tiers that compares by level (see {@link Tier#getLevel()}).
   *
   * @param <T> The type of the tiers to be compared.
   * @return The comparator.
   */
  static <T extends Tier> Comparator<T> getComparator() {
    return Comparator.comparing(T::getLevel);
  }
}
