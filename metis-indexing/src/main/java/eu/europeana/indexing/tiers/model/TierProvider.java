package eu.europeana.indexing.tiers.model;

/**
 * Implementations of this interface contain a tier that can be retrieved.
 *
 * @param <T> The type of the tier
 */
public interface TierProvider<T extends Tier> {

  /**
   * Get the tier for object.
   *
   * @return the tier value
   */
  T getTier();
}
