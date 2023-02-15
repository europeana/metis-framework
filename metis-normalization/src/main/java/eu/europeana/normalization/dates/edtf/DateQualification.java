package eu.europeana.normalization.dates.edtf;

import java.util.Arrays;

/**
 * Date qualification characters according to <a href="https://www.loc.gov/standards/datetime/">Extended Date/Time Format (EDTF)
 * Specification</a>
 */
public enum DateQualification {

  EMPTY('\0'), UNCERTAIN('?'), APPROXIMATE('~'), UNCERTAIN_APPROXIMATE('%');

  private final char character;

  DateQualification(char character) {
    this.character = character;
  }

  public static DateQualification fromCharacter(char character) {
    return Arrays.stream(DateQualification.values()).filter(value -> value.character == character).findFirst().orElse(EMPTY);
  }

  public char getCharacter() {
    return character;
  }
}
