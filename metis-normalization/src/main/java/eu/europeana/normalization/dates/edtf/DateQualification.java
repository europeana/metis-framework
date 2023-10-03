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

  private static final String CHARACTER_UNCERTAIN = "?";
  private static final String CHARACTER_APPROXIMATE = "~";
  private static final String CHARACTER_UNCERTAIN_APPROXIMATE = "%";
  private static final String QUALIFICATION_CHARACTER_REGEX =
      CHARACTER_UNCERTAIN + CHARACTER_APPROXIMATE + CHARACTER_UNCERTAIN_APPROXIMATE;
  public static final Pattern CHECK_QUALIFICATION_PATTERN = Pattern.compile(
      "^[^" + QUALIFICATION_CHARACTER_REGEX + "]*([" + QUALIFICATION_CHARACTER_REGEX + "])$");

  /**
   * Get the enum values based on the character provided.
   * <p>It will return an empty set or the set with the applicable qualifications.</p>
   *
   * @param character the provided character
   * @return the enum value
   */
  public static Set<DateQualification> fromCharacter(String character) {
    final Set<DateQualification> dateQualifications = EnumSet.noneOf(DateQualification.class);
    if (CHARACTER_UNCERTAIN_APPROXIMATE.equals(character)) {
      dateQualifications.add(DateQualification.UNCERTAIN);
      dateQualifications.add(DateQualification.APPROXIMATE);
    } else if (CHARACTER_UNCERTAIN.equals(character)) {
      dateQualifications.add(DateQualification.UNCERTAIN);
    } else if (CHARACTER_APPROXIMATE.equals(character)) {
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
      character = CHARACTER_UNCERTAIN_APPROXIMATE;
    } else if (dateQualifications.contains(UNCERTAIN)) {
      character = CHARACTER_UNCERTAIN;
    } else if (dateQualifications.contains(APPROXIMATE)) {
      character = CHARACTER_APPROXIMATE;
    } else {
      character = "";
    }
    return character;
  }
}
