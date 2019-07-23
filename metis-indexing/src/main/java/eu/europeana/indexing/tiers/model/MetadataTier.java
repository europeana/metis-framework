package eu.europeana.indexing.tiers.model;

/**
 * This enum contains the different values of the metadata tier.
 */
public enum MetadataTier implements Tier {

  T0(0), TA(1), TB(2), TC(3);

  private final int level;

  MetadataTier(int level) {
    this.level = level;
  }

  @Override
  public int getLevel() {
    return level;
  }
}
