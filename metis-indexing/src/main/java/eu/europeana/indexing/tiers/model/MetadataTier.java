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
    switch (value){
    case "A":
      return MetadataTier.TA;
    case "B":
      return MetadataTier.TB;
    case "C":
      return MetadataTier.TC;
    case "0":
      return MetadataTier.T0;
    default:
      throw new IllegalArgumentException("Nu such value " + value + " exists");
    }
  }
}
