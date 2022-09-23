package eu.europeana.normalization.dates.extraction.dateextractors;

import static java.util.Optional.ofNullable;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Patterns for numeric dates with variations in the separators of date components
 */
public class NumericWithMissingPartsDateExtractor implements DateExtractor {

  private static final String DELIMITERS = "[\\-./]";
  private static final String YEAR = "(\\d{3}(?!\\?)|\\d{4})";
  private static final String DELIMITER_DIGITS1_2 = "(?:" + DELIMITERS + "(\\d{1,2}))?";
  private static final String DIGITS1_2_DELIMITER = "(?:(\\d{1,2})" + DELIMITERS + ")?";

  private enum NumericWithMissingPartsPattern {
    YMD(Pattern.compile("^\\??" + YEAR + DELIMITER_DIGITS1_2 + DELIMITER_DIGITS1_2 + "\\??$", Pattern.CASE_INSENSITIVE), 1, 2, 3),
    DMY(Pattern.compile("^\\??" + DIGITS1_2_DELIMITER + DIGITS1_2_DELIMITER + YEAR + "\\??$", Pattern.CASE_INSENSITIVE), 3, 2, 1);

    private final Pattern pattern;
    private final int yearIndex;
    private final int monthIndex;
    private final int dayIndex;

    NumericWithMissingPartsPattern(Pattern pattern, int yearIndex, int monthIndex, int dayIndex) {
      this.pattern = pattern;
      this.yearIndex = yearIndex;
      this.monthIndex = monthIndex;
      this.dayIndex = dayIndex;
    }

    public Pattern getPattern() {
      return pattern;
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
    final boolean uncertain = sanitizedValue.startsWith("?") || sanitizedValue.endsWith("?");

    DateNormalizationResult dateNormalizationResult = null;
    for (NumericWithMissingPartsPattern numericWithMissingPartsPattern : NumericWithMissingPartsPattern.values()) {
      final Matcher matcher = numericWithMissingPartsPattern.getPattern().matcher(sanitizedValue);
      if (matcher.matches()) {
        EdtfDatePart edtfDatePart = extractDate(numericWithMissingPartsPattern, matcher, uncertain);
        dateNormalizationResult = new DateNormalizationResult(DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS, inputValue,
            new InstantEdtfDate(edtfDatePart));
      }
    }

    return dateNormalizationResult;
  }

  private EdtfDatePart extractDate(
      NumericWithMissingPartsPattern numericWithMissingPartsPattern, Matcher matcher, boolean uncertain) {
    final String year = matcher.group(numericWithMissingPartsPattern.getYearIndex());
    final String month = getMonth(numericWithMissingPartsPattern, matcher);
    final String day = getDay(numericWithMissingPartsPattern, matcher);
    final EdtfDatePart edtfDatePart = new EdtfDatePart();
    edtfDatePart.setYear(Integer.parseInt(year));
    edtfDatePart.setMonth(Integer.parseInt(month));
    edtfDatePart.setDay(Integer.parseInt(day));
    edtfDatePart.setUncertain(uncertain);
    return edtfDatePart;
  }

  /**
   * Checks if the month is null and if the day is not null it will get its value instead.
   * <p>That occurs with the DMY pattern when there is no day e.g. 11-1989</p>
   *
   * @param numericWithMissingPartsPattern the group indices
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
   * @param numericWithMissingPartsPattern the group indices
   * @param matcher the matcher
   * @return the day
   */
  private String getDay(NumericWithMissingPartsPattern numericWithMissingPartsPattern, Matcher matcher) {
    return ofNullable(matcher.group(numericWithMissingPartsPattern.getMonthIndex()))
        .map(optional -> matcher.group(numericWithMissingPartsPattern.getDayIndex()))
        .orElse("0");
  }
}
