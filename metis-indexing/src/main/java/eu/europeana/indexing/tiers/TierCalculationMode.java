package eu.europeana.indexing.tiers;

/**
 * The enum Tier calculation mode.
 */
public enum TierCalculationMode {
  /**
   * Overwrite tier calculation mode. Always perform tier calculation, overwriting any existing tiers
   */
  OVERWRITE,
  /**
   * Skip tier calculation mode. Do not perform any tier calculation
   */
  SKIP,
  /**
   * The Initialise. Perform tier calculation only if no tiers exist
   */
  INITIALISE
}
