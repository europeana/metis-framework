package eu.europeana.normalization.dates.edtf;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Date qualification characters according to <a href="https://www.loc.gov/standards/datetime/">Extended Date/Time Format (EDTF)
 * Specification</a>
 */
public enum DateQualification {
  UNCERTAIN, APPROXIMATE;

  private static final String UNCERTAIN_CHARACTER = "?";
  private static final String APPROXIMATE_CHARACTER = "~";
  private static final String UNCERTAIN_APPROXIMATE_CHARACTER = "%";
  private static final String CHARACTERS_REGEX = UNCERTAIN_CHARACTER + APPROXIMATE_CHARACTER + UNCERTAIN_APPROXIMATE_CHARACTER;
  public static final Pattern PATTERN = Pattern.compile("^[^" + CHARACTERS_REGEX + "]*([" + CHARACTERS_REGEX + "])$");

  /**
   * Get the enum values based on the character provided.
   * <p>It will return an empty set or the set with the applicable qualifications.</p>
   *
   * @param character the provided character
   * @return the enum value
   */
  public static Set<DateQualification> fromCharacter(String character) {
    final Set<DateQualification> dateQualifications = EnumSet.noneOf(DateQualification.class);
    if (UNCERTAIN_APPROXIMATE_CHARACTER.equals(character)) {
      dateQualifications.add(DateQualification.UNCERTAIN);
      dateQualifications.add(DateQualification.APPROXIMATE);
    } else if (UNCERTAIN_CHARACTER.equals(character)) {
      dateQualifications.add(DateQualification.UNCERTAIN);
    } else if (APPROXIMATE_CHARACTER.equals(character)) {
      dateQualifications.add(DateQualification.APPROXIMATE);
    }
    return dateQualifications;
  }

  /**
   * Get the string representation based on the provided date qualifications.
   *
   * @param dateQualifications the date qualifications
   * @return the string representation
   */
  public static String getCharacterFromQualifications(Set<DateQualification> dateQualifications) {
    final String character;
    if (dateQualifications.contains(UNCERTAIN) && dateQualifications.contains(APPROXIMATE)) {
      character = UNCERTAIN_APPROXIMATE_CHARACTER;
    } else if (dateQualifications.contains(UNCERTAIN)) {
      character = UNCERTAIN_CHARACTER;
    } else if (dateQualifications.contains(APPROXIMATE)) {
      character = APPROXIMATE_CHARACTER;
    } else {
      character = "";
    }
    return character;
  }
}
