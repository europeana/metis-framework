package eu.europeana.normalization.dates.extraction.dateextractors;

import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.compile;

import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.YearPrecision;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.extraction.NumericPartsPattern;
import eu.europeana.normalization.dates.sanitize.DateFieldSanitizer;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Patterns for numeric dates with variations in the separators of date components.
 * <p>For Patterns pay attentions on the use of {@link Matcher#matches()} or {@link Matcher#find()} in this method.</p>
 */
public class NumericPartsDateExtractor implements DateExtractor {

  /**
   * The start of the string can be one or three question marks but not two.
   */
  private static final Pattern STARTING_UNCERTAIN_PATTERN = compile("^(?:\\?(?!\\?)|\\?{3})");
  /**
   * The end of the string can be one or three question marks but not two.
   */
  private static final Pattern ENDING_UNCERTAIN_PATTERN = compile("(?:(?<!\\?)\\?|\\?{3})$");

  /**
   * Those are characters that indicate unknowns on year, month or day
   */
  private static final String UNKNOWN_CHARACTERS_REGEX = "[XU?-]";

  @Override
  public DateNormalizationResult extract(String inputValue) {
    return extract(inputValue, NumericPartsPattern.NUMERIC_SET);
  }

  /**
   * Validates and returns a date normalization result of provided input value.
   *
   * @param inputValue the input value
   * @param numericPatternValues the patterns to check against
   * @return the date normalization result
   */
  public DateNormalizationResult extract(String inputValue, Set<NumericPartsPattern> numericPatternValues) {
    final String sanitizedValue = DateFieldSanitizer.cleanSpacesAndTrim(inputValue);
    final boolean uncertain =
        STARTING_UNCERTAIN_PATTERN.matcher(sanitizedValue).find() || ENDING_UNCERTAIN_PATTERN.matcher(sanitizedValue).find();

    DateNormalizationResult dateNormalizationResult = null;
    for (NumericPartsPattern numericWithMissingPartsPattern : numericPatternValues) {
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
      NumericPartsPattern numericWithMissingPartsPattern, Matcher matcher, boolean uncertain) {
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
  private String getYear(NumericPartsPattern numericWithMissingPartsPattern, Matcher matcher) {
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
  private String getMonth(NumericPartsPattern numericWithMissingPartsPattern, Matcher matcher) {
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
  private String getDay(NumericPartsPattern numericWithMissingPartsPattern, Matcher matcher) {
    return ofNullable(matcher.group(numericWithMissingPartsPattern.getMonthIndex()))
        .map(optional -> matcher.group(numericWithMissingPartsPattern.getDayIndex()))
        .orElse("0");
  }
}
