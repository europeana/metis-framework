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
 */
public class Cleaner {

  private final List<CleanOperation> cleaningPatterns1stTime;
  private final List<CleanOperation> cleaningPatterns2ndTime;
  private final List<CleanOperation> cleaningPatternsGeneric;

  public Cleaner() {
    cleaningPatterns1stTime = List.of(
        CleanOperation.INITIAL_TEXT_A,
        CleanOperation.INITIAL_TEXT_B,
        CleanOperation.ENDING_TEXT,
        CleanOperation.SQUARE_BRACKETS_AND_CIRCA,
        CleanOperation.SQUARE_BRACKETS,
        CleanOperation.CIRCA,
        CleanOperation.SQUARE_BRACKET_END,
        CleanOperation.ENDING_DOT
    );

    cleaningPatterns2ndTime = List.of(
        CleanOperation.ENDING_TEXT_SQUARE_BRACKETS,
        CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA,
        CleanOperation.PARENTHESES_FULL_VALUE
    );

    cleaningPatternsGeneric = List.of(
        CleanOperation.SQUARE_BRACKETS_AND_CIRCA,
        CleanOperation.SQUARE_BRACKETS,
        CleanOperation.CIRCA,
        CleanOperation.ENDING_TEXT
    );
  }

  public CleanResult clean1stTime(String value) {
    return clean(cleaningPatterns1stTime, value);
  }

  public CleanResult clean2ndTime(String value) {
    return clean(cleaningPatterns2ndTime, value);
  }

  public CleanResult cleanGenericProperty(String value) {
    return clean(cleaningPatternsGeneric, value);
  }

  private CleanResult clean(List<CleanOperation> cleanOperations, String value) {
    for (CleanOperation cleaningOperationId : cleanOperations) {
      final Matcher matcher = cleaningOperationId.getCleanPattern().matcher(value);
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
