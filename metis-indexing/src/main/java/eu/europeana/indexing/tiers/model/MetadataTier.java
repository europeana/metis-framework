package eu.europeana.indexing.tiers.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * This enum contains the different values of the metadata tier.
 */
public enum MetadataTier implements Tier {

  T0(0, "0"), TA(1, "A"), TB(2, "B"), TC(3, "C");

  private final int level;
  private final String stringRepresentation;

  MetadataTier(int level, String stringRepresentation) {
    this.level = level;
    this.stringRepresentation = stringRepresentation;
  }

  @Override
  public int getLevel() {
    return level;
  }

  @Override
  @JsonValue
  public String toString() {
    return stringRepresentation;
  }

  public static MetadataTier getEnum(String value){
    MetadataTier result = null;
    for(MetadataTier tier: MetadataTier.values()) {
      if(tier.stringRepresentation.equals(value)) {
        result = tier;
        break;
      }
    }

    if(result == null){
      throw new IllegalArgumentException("Nu such value " + value + " exists");
    } else {
      return result;
    }
  }
}
