package eu.europeana.normalization.dates.cleaning;

import static java.util.regex.Pattern.compile;

import java.util.EnumSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Enum containing all cleaning operations.
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
public enum CleanOperation {

  STARTING_TEXT_UNTIL_FIRST_COLON(compile("^[^:]+:\\s?"),
      Matcher::find, matcher -> matcher.replaceFirst(""), StringUtils::isNotEmpty),
  STARTING_PARENTHESES(compile("^\\(.+\\)\\s?"),
      Matcher::find, matcher -> matcher.replaceFirst(""), StringUtils::isNotEmpty),
  ENDING_PARENTHESES(compile("\\s?\\(.+\\)$"),
      Matcher::find, matcher -> matcher.replaceFirst(""), StringUtils::isNotEmpty),
  ENDING_SQUARE_BRACKETS(compile("\\s?\\[.+]$"),
      Matcher::find, matcher -> matcher.replaceFirst(""), s -> true),
  ENDING_DOT(compile("\\s?\\.$"),
      Matcher::find, matcher -> matcher.replaceFirst(""), StringUtils::isNotEmpty),
  SQUARE_BRACKETS(compile("\\[([^]]+)]"),
      Matcher::find, matcher -> matcher.replaceAll("$1"), s -> true),
  STARTING_CIRCA(compile("^" + CleanOperation.CIRCA_REGEX, Pattern.CASE_INSENSITIVE),
      Matcher::find, matcher -> matcher.replaceAll(""), s -> true),
  STARTING_SQUARE_BRACKETS_WITH_CIRCA(compile("\\[" + CleanOperation.CIRCA_REGEX + "([^]]+)]", Pattern.CASE_INSENSITIVE),
      Matcher::find, matcher -> matcher.replaceAll("$1"), s -> true),
  CLOSING_SQUARE_BRACKET(compile("\\s?]$"),
      Matcher::find, matcher -> matcher.replaceAll(""), s -> true),
  CAPTURE_VALUE_IN_PARENTHESES(compile("\\s?\\((.+)\\)\\s?"),
      Matcher::matches, matcher -> matcher.replaceAll("$1"), s -> true),
  CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA(
      compile("\\s?\\(" + CleanOperation.CIRCA_REGEX + "(.+)\\)\\s?", Pattern.CASE_INSENSITIVE),
      Matcher::matches, matcher -> matcher.replaceAll("$1"), s -> true);

  private static final String CIRCA_REGEX = "(?:(?:circa|CA|C)\\s|(?:CA\\.|C\\.)\\s?)";
  private static final EnumSet<CleanOperation> APPROXIMATE_CLEAN_OPERATION_IDS_FOR_DATE_PROPERTY = EnumSet.of(
      CleanOperation.STARTING_CIRCA, CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA,
      CleanOperation.CAPTURE_VALUE_IN_PARENTHESES_WITH_CIRCA);
  private static final EnumSet<CleanOperation> APPROXIMATE_CLEAN_OPERATION_IDS_FOR_GENERIC_PROPERTY = EnumSet.of(
      CleanOperation.STARTING_CIRCA, CleanOperation.STARTING_SQUARE_BRACKETS_WITH_CIRCA);

  private final Pattern cleanPattern;
  private final Predicate<Matcher> matchingCheck;
  private final Function<Matcher, String> replaceOperation;
  private final Predicate<String> isOperationSuccessful;


  CleanOperation(Pattern cleanPattern, Predicate<Matcher> matchingCheck, Function<Matcher, String> replaceOperation,
      Predicate<String> isOperationSuccessful) {
    this.cleanPattern = cleanPattern;
    this.matchingCheck = matchingCheck;
    this.replaceOperation = replaceOperation;
    this.isOperationSuccessful = isOperationSuccessful;
  }

  public Pattern getCleanPattern() {
    return cleanPattern;
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
   * Check if provided clean operation id is part of the approximate clean operations for date properties.
   *
   * @param cleanOperation the clean operation
   * @return true if it is, false otherwise
   */
  public static boolean isApproximateCleanOperationIdForDateProperty(CleanOperation cleanOperation) {
    return APPROXIMATE_CLEAN_OPERATION_IDS_FOR_DATE_PROPERTY.contains(cleanOperation);
  }

  /**
   * Check if provided clean operation id is part of the approximate clean operations for generic properties.
   *
   * @param cleanOperation the clean operation
   * @return true if it is, false otherwise
   */
  public static boolean isApproximateCleanOperationIdForGenericProperty(CleanOperation cleanOperation) {
    return APPROXIMATE_CLEAN_OPERATION_IDS_FOR_GENERIC_PROPERTY.contains(cleanOperation);
  }
}
