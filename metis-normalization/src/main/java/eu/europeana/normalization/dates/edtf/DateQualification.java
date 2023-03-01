package eu.europeana.normalization.dates.edtf;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Date qualification characters according to <a href="https://www.loc.gov/standards/datetime/">Extended Date/Time Format (EDTF)
 * Specification</a>
 */
public enum DateQualification {

  NO_QUALIFICATION(""),
  UNCERTAIN("?"),
  APPROXIMATE("~"),
  UNCERTAIN_APPROXIMATE("%");

  public static final Pattern CHECK_QUALIFICATION_PATTERN = Pattern.compile("^[^\\?~%]*([\\?~%]?)$");
  private final String character;

  DateQualification(String character) {
    this.character = character;
  }

  /**
   * Get the enum value based on the character provided.
   * <p>It will return a matched enum value or {@link #NO_QUALIFICATION}.</p>
   *
   * @param character the provided character
   * @return the enum value
   */
  public static DateQualification fromCharacter(String character) {
    return Arrays.stream(DateQualification.values()).filter(value -> value.character.equals(character)).findFirst().orElse(
        NO_QUALIFICATION);
  }

  public String getCharacter() {
    return character;
  }
}
