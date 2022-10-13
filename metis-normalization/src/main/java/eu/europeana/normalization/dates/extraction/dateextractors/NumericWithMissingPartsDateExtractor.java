package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS_XX;
import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.YearPrecision;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Patterns for numeric dates with variations in the separators of date components.
 * <p>For Patterns pay attentions on the use of {@link Matcher#matches()} or {@link Matcher#find()} in this method.</p>
 */
public class NumericWithMissingPartsDateExtractor implements DateExtractor {

  /**
   * The start of the string can be one or three question marks but not two.
   */
  private static final Pattern STARTING_UNCERTAIN_PATTERN = compile("^(?:\\?(?!\\?)|\\?{3})");
  /**
   * The end of the string can be one or three question marks but not two.
   */
  private static final Pattern ENDING_UNCERTAIN_PATTERN = compile("(?:(?<!\\?)\\?|\\?{3})$");

  private static final String DELIMITERS = "[\\-./]";
  private static final String OPTIONAL_QUESTION_MARK = "\\??";

  // TODO: 28/09/2022 Perhaps the missing and XX can be combined in one and then identified from the UNKNOWN_CHARACTERS cleanup??
  /**
   * For the 3 digits we make sure there is no question mark in front, using a lookahead
   */
  private static final String YEAR = "(\\d{3}(?!\\?)|\\d{4})";
  private static final String DELIMITER_DIGITS = "(?:" + DELIMITERS + "(\\d{1,2}))?";
  private static final String DIGITS_DELIMITER = "(?:(\\d{1,2})" + DELIMITERS + ")?";

  /**
   * Those are characters that indicate unknowns on year, month or day
   */
  private static final String UNKNOWN_CHARACTERS_REGEX = "[XU?-]";
  /**
   * For the 3 digits we make sure there is no question mark in front, using a lookahead
   */
  private static final String YEAR_XX = "(\\d{2}(?:XX|UU|--|\\?\\?)|\\d{3}(?!\\?)[XU]|\\d{4})";
  /**
   * For the double dash case we make sure there isn't a third dash with a lookbehind
   */
  private static final String DELIMITER_DIGITS_XX = "(?:" + DELIMITERS + "(\\d{2}|XX|UU|(?<!-)--|\\?\\?))?";
  /**
   * For the double dash case we make sure there isn't a third dash with a lookahead
   */
  private static final String DIGITS_DELIMITER_XX = "(?:(\\d{2}|XX|UU|--(?!-)|\\?\\?)" + DELIMITERS + ")?";

  /**
   * Enum with all the acceptable date patterns used in the surrounding class.
   */
  private enum NumericWithMissingPartsPattern {
    YMD(compile(OPTIONAL_QUESTION_MARK + YEAR + DELIMITER_DIGITS + DELIMITER_DIGITS + OPTIONAL_QUESTION_MARK,
        CASE_INSENSITIVE), NUMERIC_ALL_VARIANTS, 1, 2, 3),

    DMY(compile(OPTIONAL_QUESTION_MARK + DIGITS_DELIMITER + DIGITS_DELIMITER + YEAR + OPTIONAL_QUESTION_MARK, CASE_INSENSITIVE),
        NUMERIC_ALL_VARIANTS, 3, 2, 1),

    YMD_XX(compile(OPTIONAL_QUESTION_MARK + YEAR_XX + DELIMITER_DIGITS_XX + DELIMITER_DIGITS_XX + OPTIONAL_QUESTION_MARK,
        CASE_INSENSITIVE), NUMERIC_ALL_VARIANTS_XX, 1, 2, 3),

    DMY_XX(compile(OPTIONAL_QUESTION_MARK + DIGITS_DELIMITER_XX + DIGITS_DELIMITER_XX + YEAR_XX + OPTIONAL_QUESTION_MARK,
        CASE_INSENSITIVE), NUMERIC_ALL_VARIANTS_XX, 3, 2, 1);

    private final Pattern pattern;
    private final DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId;
    private final int yearIndex;
    private final int monthIndex;
    private final int dayIndex;

    NumericWithMissingPartsPattern(Pattern pattern, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId,
        int yearIndex, int monthIndex, int dayIndex) {
      this.pattern = pattern;
      this.dateNormalizationExtractorMatchId = dateNormalizationExtractorMatchId;
      this.yearIndex = yearIndex;
      this.monthIndex = monthIndex;
      this.dayIndex = dayIndex;
    }

    public Pattern getPattern() {
      return pattern;
    }

    public DateNormalizationExtractorMatchId getDateNormalizationExtractorMatchId() {
      return dateNormalizationExtractorMatchId;
    }

    public int getYearIndex() {
      return yearIndex;
    }

    public int getMonthIndex() {
      return monthIndex;
    }

    public int getDayIndex() {
      return dayIndex;
    }
  }

  public DateNormalizationResult extract(String inputValue) {
    final String sanitizedValue = inputValue.replaceAll("\\s", " ").trim();
    final boolean uncertain =
        STARTING_UNCERTAIN_PATTERN.matcher(sanitizedValue).find() || ENDING_UNCERTAIN_PATTERN.matcher(sanitizedValue).find();

    DateNormalizationResult dateNormalizationResult = null;
    for (NumericWithMissingPartsPattern numericWithMissingPartsPattern : NumericWithMissingPartsPattern.values()) {
      final Matcher matcher = numericWithMissingPartsPattern.getPattern().matcher(sanitizedValue);
      if (matcher.matches()) {
        EdtfDatePart edtfDatePart = extractDate(numericWithMissingPartsPattern, matcher, uncertain);
        dateNormalizationResult = new DateNormalizationResult(
            numericWithMissingPartsPattern.getDateNormalizationExtractorMatchId(), inputValue,
            new InstantEdtfDate(edtfDatePart));
        break;
      }
    }

    return dateNormalizationResult;
  }

  private EdtfDatePart extractDate(
      NumericWithMissingPartsPattern numericWithMissingPartsPattern, Matcher matcher, boolean uncertain) {
    final String year = getYear(numericWithMissingPartsPattern, matcher);
    final String month = getMonth(numericWithMissingPartsPattern, matcher);
    final String day = getDay(numericWithMissingPartsPattern, matcher);

    final String yearSanitized = getFieldSanitized(year);
    final String monthSanitized = getFieldSanitized(month);
    final String daySanitized = getFieldSanitized(day);

    final EdtfDatePart edtfDatePart = new EdtfDatePart();
    final int unknownYearCharacters = year.length() - yearSanitized.length();
    edtfDatePart.setYearPrecision(YearPrecision.getYearPrecisionByOrdinal(unknownYearCharacters));
    edtfDatePart.setYear(adjustYearWithPrecision(yearSanitized, edtfDatePart));
    edtfDatePart.setMonth(Integer.parseInt(monthSanitized));
    edtfDatePart.setDay(Integer.parseInt(daySanitized));
    edtfDatePart.setUncertain(uncertain);
    return edtfDatePart;
  }

  private int adjustYearWithPrecision(String yearSanitized, EdtfDatePart edtfDatePart) {
    return Integer.parseInt(yearSanitized) * Optional.ofNullable(edtfDatePart.getYearPrecision()).map(YearPrecision::getDuration)
                                                     .orElse(1);
  }

  private String getFieldSanitized(String stringField) {
    return StringUtils.defaultIfEmpty(stringField.toUpperCase(Locale.US).replaceAll(UNKNOWN_CHARACTERS_REGEX, ""),
        "0");
  }

  /**
   * Get the year from the matcher.
   *
   * @param numericWithMissingPartsPattern the pattern that contains the indices
   * @param matcher the matcher
   * @return the year
   */
  private String getYear(NumericWithMissingPartsPattern numericWithMissingPartsPattern, Matcher matcher) {
    return matcher.group(numericWithMissingPartsPattern.getYearIndex());
  }

  /**
   * Checks if the month is null and if the day is not null it will get its value instead.
   * <p>That occurs with the DMY pattern when there is no day e.g. 11-1989</p>
   *
   * @param numericWithMissingPartsPattern the pattern that contains the indices
   * @param matcher the matcher
   * @return the month
   */
  private String getMonth(NumericWithMissingPartsPattern numericWithMissingPartsPattern, Matcher matcher) {
    return ofNullable(ofNullable(matcher.group(numericWithMissingPartsPattern.getMonthIndex()))
        .orElseGet(() -> matcher.group(numericWithMissingPartsPattern.getDayIndex()))).orElse("0");
  }

  /**
   * Checks if month is null, it then returns the default value, otherwise gets the value of day.
   *
   * @param numericWithMissingPartsPattern the pattern that contains the indices
   * @param matcher the matcher
   * @return the day
   */
  private String getDay(NumericWithMissingPartsPattern numericWithMissingPartsPattern, Matcher matcher) {
    return ofNullable(matcher.group(numericWithMissingPartsPattern.getMonthIndex()))
        .map(optional -> matcher.group(numericWithMissingPartsPattern.getDayIndex()))
        .orElse("0");
  }
}
