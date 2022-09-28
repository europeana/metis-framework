package eu.europeana.normalization.dates.sanitize;

import static java.util.regex.Pattern.compile;

import java.util.EnumSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Enum containing all sanitize operations.
 * <p>The patterns expect the original value to be sanitized from multiple space characters and leading and trailing spaces
 * removed. Some operation could have been combined, but they are structured intentionally separately so that they can be applied
 * separately in different order.</p>
 * <p>
 * In detail each enum value contains:
 *   <ul>
 *     <li>The compiled regex pattern</li>
 *     <li>The matching check operation {@link Matcher#find()} or {@link Matcher#matches()}</li>
 *     <li>The replacing operation of how a matched operation should replace the original value {@link Matcher#replaceFirst(String)} or {@link Matcher#replaceAll(String)}</li>
 *     <li>The condition that indicated whether the generated value from the previous replacing operation is successful</li>
 *   </ul>
 * </p>
 */
public enum SanitizeOperation {

  STARTING_TEXT_UNTIL_FIRST_COLON(compile("^[^:]*:\\s?"),
      Matcher::find, matcher -> matcher.replaceFirst(""), StringUtils::isNotEmpty),
  STARTING_PARENTHESES(compile("^\\(.+\\)\\s?"),
      Matcher::find, matcher -> matcher.replaceFirst(""), StringUtils::isNotEmpty),
  ENDING_PARENTHESES(compile("\\s?\\(.+\\)$"),
      Matcher::find, matcher -> matcher.replaceFirst(""), StringUtils::isNotEmpty),
  ENDING_SQUARE_BRACKETS(compile("\\s?\\[.+]$"),
      Matcher::find, matcher -> matcher.replaceFirst(""), s -> true),
  ENDING_DOT(compile("\\s?\\.$"),
      Matcher::find, matcher -> matcher.replaceFirst(""), StringUtils::isNotEmpty),
  CAPTURE_VALUE_IN_SQUARE_BRACKETS(compile("\\[([^]]+)]"),
      Matcher::find, matcher -> matcher.replaceAll("$1"), s -> true),
  STARTING_CIRCA(compile("^" + SanitizeOperation.CIRCA_REGEX, Pattern.CASE_INSENSITIVE),
      Matcher::find, matcher -> matcher.replaceAll(""), s -> true),
  CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA(
      compile("\\[" + SanitizeOperation.CIRCA_REGEX + "([^]]+)]", Pattern.CASE_INSENSITIVE),
      Matcher::find, matcher -> matcher.replaceAll("$1"), s -> true),
  ENDING_CLOSING_SQUARE_BRACKET(compile("\\s?]$"),
      Matcher::find, matcher -> matcher.replaceAll(""), s -> true),
  CAPTURE_VALUE_IN_PARENTHESES(compile("\\s?\\((.+)\\)\\s?"),
      Matcher::matches, matcher -> matcher.replaceAll("$1"), s -> true),
  CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA(
      compile("\\s?\\(" + SanitizeOperation.CIRCA_REGEX + "(.+)\\)\\s?", Pattern.CASE_INSENSITIVE),
      Matcher::matches, matcher -> matcher.replaceAll("$1"), s -> true);

  private static final String CIRCA_REGEX = "(?:(?:circa|CA|C)\\s|(?:CA\\.|C\\.)\\s?)";
  private static final EnumSet<SanitizeOperation> APPROXIMATE_CLEAN_OPERATION_IDS_FOR_DATE_PROPERTY = EnumSet.of(
      SanitizeOperation.STARTING_CIRCA, SanitizeOperation.CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA,
      SanitizeOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA);
  private static final EnumSet<SanitizeOperation> APPROXIMATE_CLEAN_OPERATION_IDS_FOR_GENERIC_PROPERTY = EnumSet.of(
      SanitizeOperation.STARTING_CIRCA, SanitizeOperation.CAPTURE_VALUE_IN_SQUARE_BRACKETS_WITH_CIRCA);

  private final Pattern sanitizePattern;
  private final Predicate<Matcher> matchingCheck;
  private final Function<Matcher, String> replaceOperation;
  private final Predicate<String> isOperationSuccessful;


  SanitizeOperation(Pattern sanitizePattern, Predicate<Matcher> matchingCheck, Function<Matcher, String> replaceOperation,
      Predicate<String> isOperationSuccessful) {
    this.sanitizePattern = sanitizePattern;
    this.matchingCheck = matchingCheck;
    this.replaceOperation = replaceOperation;
    this.isOperationSuccessful = isOperationSuccessful;
  }

  public Pattern getSanitizePattern() {
    return sanitizePattern;
  }

  public Predicate<Matcher> getMatchingCheck() {
    return matchingCheck;
  }

  public Function<Matcher, String> getReplaceOperation() {
    return replaceOperation;
  }

  public Predicate<String> getIsOperationSuccessful() {
    return isOperationSuccessful;
  }

  /**
   * Check if provided sanitize operation is part of the approximate sanitize operations for date properties.
   *
   * @param sanitizeOperation the sanitize operation
   * @return true if it is, false otherwise
   */
  public static boolean isApproximateSanitizeOperationForDateProperty(SanitizeOperation sanitizeOperation) {
    return APPROXIMATE_CLEAN_OPERATION_IDS_FOR_DATE_PROPERTY.contains(sanitizeOperation);
  }

  /**
   * Check if provided sanitize operation is part of the approximate sanitize operations for generic properties.
   *
   * @param sanitizeOperation the sanitize operation
   * @return true if it is, false otherwise
   */
  public static boolean isApproximateSanitizeOperationForGenericProperty(SanitizeOperation sanitizeOperation) {
    return APPROXIMATE_CLEAN_OPERATION_IDS_FOR_GENERIC_PROPERTY.contains(sanitizeOperation);
  }
}
