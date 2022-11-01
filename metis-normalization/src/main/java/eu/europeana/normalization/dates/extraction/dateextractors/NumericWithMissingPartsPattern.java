package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS;
import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS_XX;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import java.util.regex.Pattern;

/**
 * Enum with all the acceptable date patterns used in the surrounding class.
 */
// TODO: 01/11/2022 Update the constructor similar to the ranges, but we need to support XX cases too first, with a flag perhaps
public enum NumericWithMissingPartsPattern implements NumericPattern {
  YMD(NumericWithMissingPartsPattern.DELIMITERS, false, NUMERIC_ALL_VARIANTS, 1, 2, 3),
  DMY(NumericWithMissingPartsPattern.DELIMITERS, false, NUMERIC_ALL_VARIANTS, 3, 2, 1),

  YMD_XX(NumericWithMissingPartsPattern.DELIMITERS, true, NUMERIC_ALL_VARIANTS_XX, 1, 2, 3),
  DMY_XX(NumericWithMissingPartsPattern.DELIMITERS, true, NUMERIC_ALL_VARIANTS_XX, 3, 2, 1);

  private static final String OPTIONAL_QUESTION_MARK = "\\??";
  private static final String DELIMITERS = "[\\-./]";

  private final Pattern pattern;
  private final DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId;
  private final int yearIndex;
  private final int monthIndex;
  private final int dayIndex;

  NumericWithMissingPartsPattern(String dateDelimiters, boolean isXX,
      DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId,
      int yearIndex, int monthIndex, int dayIndex) {
    this.dateNormalizationExtractorMatchId = dateNormalizationExtractorMatchId;
    this.yearIndex = yearIndex;
    this.monthIndex = monthIndex;
    this.dayIndex = dayIndex;

    this.pattern = NumericWithMissingPartsPattern.generatePattern(dateDelimiters, isXX, yearIndex);
  }

  // TODO: 28/09/2022 Perhaps the missing and XX can be combined in one and then identified from the UNKNOWN_CHARACTERS cleanup??
  public static Pattern generatePattern(String dateDelimiters, boolean isXX, int yearIndex) {
    final String year;
    final String delimiterDigits;
    final String digitsDelimiter;
    if (isXX) {
      year = "(\\d{2}(?:XX|UU|--|\\?\\?)|\\d{3}(?!\\?)[XU]|\\d{4})";
      delimiterDigits = "(?:" + dateDelimiters + "(\\d{2}|XX|UU|(?<!-)--|\\?\\?))?";
      digitsDelimiter = "(?:(\\d{2}|XX|UU|--(?!-)|\\?\\?)" + dateDelimiters + ")?";
    } else {
      year = "(\\d{3}(?!\\?)|\\d{4})";
      delimiterDigits = "(?:" + dateDelimiters + "(\\d{1,2}))?";
      digitsDelimiter = "(?:(\\d{1,2})" + dateDelimiters + ")?";
    }

    final String dateRegex;
    if (yearIndex == 1) {
      dateRegex = year + delimiterDigits + delimiterDigits;
    } else {
      dateRegex = digitsDelimiter + digitsDelimiter + year;
    }

    return compile(
        NumericWithMissingPartsPattern.OPTIONAL_QUESTION_MARK + dateRegex
            + NumericWithMissingPartsPattern.OPTIONAL_QUESTION_MARK, CASE_INSENSITIVE);
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
