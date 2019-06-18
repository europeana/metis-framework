package eu.europeana.indexing.tiers.model;

/**
 * This enum contains the different values of the metadata tier.
 */
public enum MetadataTier implements Tier {

  T0("0", 0), TA("A", 1), TB("B", 2), TC("C", 3);

  private final String label;
  private final int level;

  MetadataTier(String label, int level) {
    this.label = label;
    this.level = level;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public int getLevel() {
    return level;
  }
}
