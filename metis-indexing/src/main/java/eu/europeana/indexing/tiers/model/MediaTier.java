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

  /**
   * Get the Enum representation given a string value
   * @param value the string value
   * @return the enum representation
   */
  public static MediaTier getEnum(String value){
    MediaTier result = null;
    for(MediaTier tier: MediaTier.values()) {
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
