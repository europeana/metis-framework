package eu.europeana.indexing.tiers.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * This enum contains the different values of the media tier.
 */
public enum MediaTier implements Tier {

  T0(0, "0"), T1(1, "1"), T2(2, "2"), T3(3, "3"), T4(4, "4");

  private final int level;
  private final String stringRepresentation;

  MediaTier(int level, String stringRepresentation) {
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

  public static MediaTier getEnum(String value){
    switch(value){
      case "1":
        return MediaTier.T1;
      case "2":
        return MediaTier.T2;
      case "3":
        return MediaTier.T3;
      case "4":
        return MediaTier.T4;
      case "0":
        return MediaTier.T0;
      default:
        throw new IllegalArgumentException("No such value " + value + " exists");
    }
  }
}
