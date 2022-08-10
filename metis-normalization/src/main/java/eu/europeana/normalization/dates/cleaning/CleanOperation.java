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
  INITIAL_TEXT_A(compile("^\\s*[^\\s:]+:\\s*"),
      Matcher::find, matcher -> matcher.replaceFirst(""), StringUtils::isNotEmpty),
  INITIAL_TEXT_B(compile("^\\s*\\([^)]+\\)\\s*"),
      Matcher::find, matcher -> matcher.replaceFirst(""), StringUtils::isNotEmpty),
  ENDING_TEXT(compile("\\s*\\(.+\\)\\s*$"),
      Matcher::find, matcher -> matcher.replaceFirst(""), StringUtils::isNotEmpty),
  ENDING_TEXT_SQUARE_BRACKETS(compile("\\s*\\[.+]\\s*$"),
      Matcher::find, matcher -> matcher.replaceFirst(""), s -> true),
  ENDING_DOT(compile("\\s*\\.\\s*$"),
      Matcher::find, matcher -> matcher.replaceFirst(""), StringUtils::isNotEmpty),
  SQUARE_BRACKETS(compile("\\[([^]]+)]"),
      Matcher::find, matcher -> matcher.replaceAll("$1"), s -> true),
  CIRCA(compile("^\\s*(circa|CA\\.?|C\\.)\\s*", Pattern.CASE_INSENSITIVE),
      Matcher::find, matcher -> matcher.replaceAll(""), s -> true),

  // TODO: 22/07/2022 Need to fix this regex, it is reported as dangerous
  SQUARE_BRACKETS_AND_CIRCA(compile("\\[(circa|CA\\.?|C\\.)\\s*([^]]+)]", Pattern.CASE_INSENSITIVE),
      Matcher::find, matcher -> matcher.replaceAll("$2"), s -> true),
  SQUARE_BRACKET_END(compile("\\s*]\\s*$"),
      Matcher::find, matcher -> matcher.replaceAll(""), s -> true),
  PARENTHESES_FULL_VALUE(compile("\\s*\\(([^()]+)\\)\\s*"),
      Matcher::matches, matcher -> matcher.replaceAll("$1"), s -> true),
  PARENTHESES_FULL_VALUE_AND_CIRCA(compile("\\s*\\((circa|CA\\.?|C\\.)([^()]+)\\)\\s*"),
      Matcher::matches, matcher -> matcher.replaceAll("$1"), s -> true);

  private static final EnumSet<CleanOperation> APPROXIMATE_CLEAN_OPERATION_IDS_FOR_DATE_PROPERTY = EnumSet.of(
      CleanOperation.CIRCA, CleanOperation.SQUARE_BRACKETS_AND_CIRCA, CleanOperation.PARENTHESES_FULL_VALUE_AND_CIRCA);
  private static final EnumSet<CleanOperation> APPROXIMATE_CLEAN_OPERATION_IDS_FOR_GENERIC_PROPERTY = EnumSet.of(
      CleanOperation.CIRCA, CleanOperation.SQUARE_BRACKETS_AND_CIRCA);

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

  public static boolean isApproximateCleanOperationIdForDateProperty(CleanOperation cleanOperation) {
    return APPROXIMATE_CLEAN_OPERATION_IDS_FOR_DATE_PROPERTY.contains(cleanOperation);
  }

  public static boolean isApproximateCleanOperationIdForGenericProperty(CleanOperation cleanOperation) {
    return APPROXIMATE_CLEAN_OPERATION_IDS_FOR_GENERIC_PROPERTY.contains(cleanOperation);
  }
}
