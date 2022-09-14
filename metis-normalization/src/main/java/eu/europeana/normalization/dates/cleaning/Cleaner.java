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

  private final List<CleanOperation> cleaningPatterns1stTimeDateProperty;
  private final List<CleanOperation> cleaningPatterns2ndTimeDateProperty;
  private final List<CleanOperation> cleaningPatternsGenericProperty;

  /**
   * Default constructor.
   * <p>It initializes all cleaning lists.</p>
   */
  public Cleaner() {
    cleaningPatterns1stTimeDateProperty = List.of(
        CleanOperation.INITIAL_TEXT_A,
        CleanOperation.INITIAL_TEXT_B,
        CleanOperation.ENDING_TEXT,
        CleanOperation.SQUARE_BRACKETS_AND_CIRCA,
        CleanOperation.SQUARE_BRACKETS,
        CleanOperation.CIRCA,
        CleanOperation.SQUARE_BRACKET_END,
        CleanOperation.ENDING_DOT
    );

    cleaningPatterns2ndTimeDateProperty = List.of(
        CleanOperation.ENDING_TEXT_SQUARE_BRACKETS,
        CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA,
        CleanOperation.PARENTHESES_FULL_VALUE
    );

    cleaningPatternsGenericProperty = List.of(
        CleanOperation.SQUARE_BRACKETS_AND_CIRCA,
        CleanOperation.SQUARE_BRACKETS,
        CleanOperation.CIRCA,
        CleanOperation.ENDING_TEXT
    );
  }

  /**
   * Clean operations used for the date property first cleanup trial for the provided value.
   *
   * @param value the value to clean
   * @return the clean result
   */
  public CleanResult clean1stTimeDateProperty(String value) {
    return clean(cleaningPatterns1stTimeDateProperty, value);
  }

  /**
   * Clean operations used for the date property second cleanup trial for the provided value.
   *
   * @param value the value to clean
   * @return the clean result
   */
  public CleanResult clean2ndTimeDateProperty(String value) {
    return clean(cleaningPatterns2ndTimeDateProperty, value);
  }

  /**
   * Clean operations used for the generic cleanup trial for the provided value.
   *
   * @param value the value to clean
   * @return the clean result
   */
  public CleanResult cleanGenericProperty(String value) {
    return clean(cleaningPatternsGenericProperty, value);
  }

  private CleanResult clean(List<CleanOperation> cleanOperations, String inputValue) {
    final String sanitizedValue = inputValue.replaceAll("\\s", " ").trim();
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
}
