package eu.europeana.normalization.dates.sanitize;

import java.util.List;
import java.util.regex.Matcher;

/**
 * Class with functionality for sanitizing date string values.
 * <p>
 * The methods contain the implementation to for the detection of punctuation marks and abbreviations that signal uncertain and
 * approximate dates.
 * </p>
 * <p>
 * This class also detects some patterns frequently used for adding notes to the dates, and removes such notes.
 * </p>
 * <p>Before any pattern is ran, multiple space characters are replaced by a single literal space character and the string is
 * trimmed.</p>
 */
public class DateFieldSanitizer {

  private final List<SanitizeOperation> dateSanitizePatterns1stGroup;
  private final List<SanitizeOperation> dateSanitizePatterns2ndGroup;
  private final List<SanitizeOperation> genericSanitizePatternsGroup;

  /**
   * Default constructor.
   * <p>It initializes all lists.</p>
   */
  public DateFieldSanitizer() {
    dateSanitizePatterns1stGroup = List.of(
        SanitizeOperation.STARTING_TEXT_UNTIL_FIRST_COLON,
        SanitizeOperation.STARTING_PARENTHESES,
        SanitizeOperation.ENDING_PARENTHESES,
        SanitizeOperation.CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA,
        SanitizeOperation.CAPTURE_VALUE_IN_SQUARE_BRACKETS,
        SanitizeOperation.STARTING_CIRCA,
        SanitizeOperation.ENDING_CLOSING_SQUARE_BRACKET,
        SanitizeOperation.ENDING_DOT
    );

    dateSanitizePatterns2ndGroup = List.of(
        SanitizeOperation.ENDING_SQUARE_BRACKETS,
        SanitizeOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA,
        SanitizeOperation.CAPTURE_VALUE_IN_PARENTHESES
    );

    genericSanitizePatternsGroup = List.of(
        SanitizeOperation.CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA,
        SanitizeOperation.CAPTURE_VALUE_IN_SQUARE_BRACKETS,
        SanitizeOperation.STARTING_CIRCA,
        SanitizeOperation.ENDING_PARENTHESES
    );
  }

  /**
   * Clean operations used for the date property first sanitization trial for the provided value.
   *
   * @param value the value to sanitize
   * @return the sanitize result
   */
  public SanitizedDate sanitize1stTimeDateProperty(String value) {
    return sanitize(dateSanitizePatterns1stGroup, value);
  }

  /**
   * Clean operations used for the date property second sanitization trial for the provided value.
   *
   * @param value the value to sanitize
   * @return the sanitize result
   */
  public SanitizedDate sanitize2ndTimeDateProperty(String value) {
    return sanitize(dateSanitizePatterns2ndGroup, value);
  }

  /**
   * Clean operations used for the generic sanitization trial for the provided value.
   *
   * @param value the value to sanitize
   * @return the sanitize result
   */
  public SanitizedDate sanitizeGenericProperty(String value) {
    return sanitize(genericSanitizePatternsGroup, value);
  }

  private SanitizedDate sanitize(List<SanitizeOperation> sanitizeOperations, String inputValue) {
    final String cleanedValue = cleanSpacesAndTrim(inputValue);
    for (SanitizeOperation sanitizeOperation : sanitizeOperations) {
      final Matcher matcher = sanitizeOperation.getSanitizePattern().matcher(cleanedValue);
      if (sanitizeOperation.getMatchingCheck().test(matcher)) {
        final String sanitizedValue = sanitizeOperation.getReplaceOperation().apply(matcher);
        if (sanitizeOperation.getIsOperationSuccessful().test(sanitizedValue)) {
          return new SanitizedDate(sanitizeOperation, sanitizedValue);
        }
      }
    }
    return null;
  }

  private String cleanSpacesAndTrim(String inputValue) {
    return inputValue.replaceAll("\\s", " ").trim();
  }
}
