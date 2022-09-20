package eu.europeana.normalization.dates.cleaning;

import java.util.List;
import java.util.regex.Matcher;

/**
 * Class with functionality for cleaning string values.
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
public class Cleaner {

  private final List<CleanOperation> cleaningPatterns1stGroupDateProperty;
  private final List<CleanOperation> cleaningPatterns2ndGroupDateProperty;
  private final List<CleanOperation> cleaningPatternsGroupGenericProperty;

  /**
   * Default constructor.
   * <p>It initializes all cleaning lists.</p>
   */
  public Cleaner() {
    cleaningPatterns1stGroupDateProperty = List.of(
        CleanOperation.STARTING_TEXT_UNTIL_FIRST_COLON,
        CleanOperation.STARTING_PARENTHESES,
        CleanOperation.ENDING_PARENTHESES,
        CleanOperation.CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA,
        CleanOperation.CAPTURE_VALUE_IN_SQUARE_BRACKETS,
        CleanOperation.STARTING_CIRCA,
        CleanOperation.ENDING_CLOSING_SQUARE_BRACKET,
        CleanOperation.ENDING_DOT
    );

    cleaningPatterns2ndGroupDateProperty = List.of(
        CleanOperation.ENDING_SQUARE_BRACKETS,
        CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA,
        CleanOperation.CAPTURE_VALUE_IN_PARENTHESES
    );

    cleaningPatternsGroupGenericProperty = List.of(
        CleanOperation.CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA,
        CleanOperation.CAPTURE_VALUE_IN_SQUARE_BRACKETS,
        CleanOperation.STARTING_CIRCA,
        CleanOperation.ENDING_PARENTHESES
    );
  }

  /**
   * Clean operations used for the date property first cleanup trial for the provided value.
   *
   * @param value the value to clean
   * @return the clean result
   */
  public CleanResult clean1stTimeDateProperty(String value) {
    return clean(cleaningPatterns1stGroupDateProperty, value);
  }

  /**
   * Clean operations used for the date property second cleanup trial for the provided value.
   *
   * @param value the value to clean
   * @return the clean result
   */
  public CleanResult clean2ndTimeDateProperty(String value) {
    return clean(cleaningPatterns2ndGroupDateProperty, value);
  }

  /**
   * Clean operations used for the generic cleanup trial for the provided value.
   *
   * @param value the value to clean
   * @return the clean result
   */
  public CleanResult cleanGenericProperty(String value) {
    return clean(cleaningPatternsGroupGenericProperty, value);
  }

  private CleanResult clean(List<CleanOperation> cleanOperations, String inputValue) {
    final String sanitizedValue = cleanSpacesAndTrim(inputValue);
    for (CleanOperation cleaningOperationId : cleanOperations) {
      final Matcher matcher = cleaningOperationId.getCleanPattern().matcher(sanitizedValue);
      if (cleaningOperationId.getMatchingCheck().test(matcher)) {
        final String cleanedValue = cleaningOperationId.getReplaceOperation().apply(matcher);
        if (cleaningOperationId.getIsOperationSuccessful().test(cleanedValue)) {
          return new CleanResult(cleaningOperationId, cleanedValue);
        }
      }
    }
    return null;
  }

  private String cleanSpacesAndTrim(String inputValue) {
    return inputValue.replaceAll("\\s", " ").trim();
  }
}
