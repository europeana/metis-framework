package eu.europeana.normalization.dates.extraction;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS_XX;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import java.util.regex.Pattern;

/**
 * Enum with all the acceptable date patterns used for numeric dates.
 * <p>This is the main general enum. Furthermore the method
 * {@link #generatePattern(String, DateNormalizationExtractorMatchId, int)} can be used to generate other enums and gives more
 * control on the date delimiters used, the option of XX dates and the order of the year, month, day of the date</p>
 */
public enum NumericWithMissingPartsPattern implements NumericPattern {
  YMD(NumericWithMissingPartsPattern.DELIMITERS, NUMERIC_ALL_VARIANTS, 1, 2, 3),
  DMY(NumericWithMissingPartsPattern.DELIMITERS, NUMERIC_ALL_VARIANTS, 3, 2, 1),

  YMD_XX(NumericWithMissingPartsPattern.DELIMITERS, NUMERIC_ALL_VARIANTS_XX, 1, 2, 3),
  DMY_XX(NumericWithMissingPartsPattern.DELIMITERS, NUMERIC_ALL_VARIANTS_XX, 3, 2, 1);

  private static final String DELIMITERS = "[\\-./]";
  /**
   * For the 3 digits we make sure there is no question mark in front, using a lookahead
   */
  private static final String YEAR = "(\\d{3}(?!\\?)|\\d{4})";
  /**
   * For the 3 digits we make sure there is no question mark in front, using a lookahead
   */
  private static final String YEAR_XX = "(\\d{2}(?:XX|UU|--|\\?\\?)|\\d{3}(?!\\?)[XU]|\\d{4})";

  private final Pattern pattern;
  private final DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId;
  private final int yearIndex;
  private final int monthIndex;
  private final int dayIndex;

  NumericWithMissingPartsPattern(String dateDelimiters, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId,
      int yearIndex, int monthIndex, int dayIndex) {
    this.dateNormalizationExtractorMatchId = dateNormalizationExtractorMatchId;
    this.yearIndex = yearIndex;
    this.monthIndex = monthIndex;
    this.dayIndex = dayIndex;

    this.pattern = NumericWithMissingPartsPattern.generatePattern(dateDelimiters, dateNormalizationExtractorMatchId, yearIndex);
  }

  // TODO: 28/09/2022 Perhaps the missing and XX can be combined in one and then identified from the UNKNOWN_CHARACTERS cleanup??
  public static Pattern generatePattern(String dateDelimiters,
      DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId, int yearIndex) {
    final String optionalQuestionMark = "\\??";
    final String year;
    final String delimiterDigits;
    final String digitsDelimiter;
    if (dateNormalizationExtractorMatchId == NUMERIC_ALL_VARIANTS) {
      year = YEAR;
      delimiterDigits = "(?:" + dateDelimiters + "(\\d{1,2}))?";
      digitsDelimiter = "(?:(\\d{1,2})" + dateDelimiters + ")?";
    } else {
      year = YEAR_XX;
      delimiterDigits = "(?:" + dateDelimiters + "(\\d{2}|XX|UU|(?<!-)--|\\?\\?))?";
      digitsDelimiter = "(?:(\\d{2}|XX|UU|--(?!-)|\\?\\?)" + dateDelimiters + ")?";
    }

    final String dateRegex;
    if (yearIndex == 1) {
      dateRegex = year + delimiterDigits + delimiterDigits;
    } else {
      dateRegex = digitsDelimiter + digitsDelimiter + year;
    }

    return compile(optionalQuestionMark + dateRegex + optionalQuestionMark, CASE_INSENSITIVE);
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
