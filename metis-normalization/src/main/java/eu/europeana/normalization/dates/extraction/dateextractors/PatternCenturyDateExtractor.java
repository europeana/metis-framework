package eu.europeana.normalization.dates.extraction.dateextractors;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.YearPrecision;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import eu.europeana.normalization.dates.extraction.RomanToNumber;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extractor that matches a century with a decimal or Roman numerals
 * <p>The range of values this accepts are from 1-21 including.</p>
 * <p>Examples of some cases:
 * <ul>
 *   <li>
 *     Value = 18.. | Outcome = 18XX
 *     Value = 1st century | Outcome = 00XX
 *     Value = s. XXI | Outcome = 20XX
 *     Value = s.II-III | Outcome = 01XX/02XX
 *   </li>
 * </ul>
 * </p>
 * <p>The Roman numerals may also be preceded by an abbreviation of century, for example ‘s. XIX’.</p>
 * <p>Also supports ranges.</p>
 */
public class PatternCenturyDateExtractor implements DateExtractor {

  private static final String ROMAN_1_TO_21_REGEX = "(X?(?:IX|IV|VI{0,3}|I{1,3})|X|XXI?)";
  private static final String NUMERIC_10_TO_21_REGEX = "1\\d|2[0-1]";
  private static final String NUMERIC_1_TO_21_SUFFIXED_REGEX = "2?1st|2nd|3rd|(?:1\\d|[4-9]|20)th";
  private static final String CENTURY_PREFIX = "((?:s|sec|saec)\\s|(?:s|sec|saec)\\.\\s?)";

  enum PatternCenturyDateOperation {
    PATTERN_YYYY(compile("\\??(?<century1>" + NUMERIC_10_TO_21_REGEX + ")\\.{2}\\??",
        CASE_INSENSITIVE), century -> Integer.parseInt(century) * 100,
        DateNormalizationExtractorMatchId.CENTURY_NUMERIC),
    PATTERN_ENGLISH(
        compile("\\??(?<century1>" + NUMERIC_1_TO_21_SUFFIXED_REGEX + ")\\s+century\\??",
            CASE_INSENSITIVE), century -> (Integer.parseInt(century.substring(0, century.length() - 2)) - 1) * 100,
        DateNormalizationExtractorMatchId.CENTURY_NUMERIC),
    PATTERN_ROMAN(
        compile("\\??" + CENTURY_PREFIX + "(?<century1>" + ROMAN_1_TO_21_REGEX + ")\\??",
            CASE_INSENSITIVE), century -> (RomanToNumber.romanToDecimal(century) - 1) * 100,
        DateNormalizationExtractorMatchId.CENTURY_ROMAN),
    PATTERN_ROMAN_CLEAN(
        compile("\\??(?<century1>" + ROMAN_1_TO_21_REGEX + ")\\??",
            CASE_INSENSITIVE), century -> (RomanToNumber.romanToDecimal(century) - 1) * 100,
        DateNormalizationExtractorMatchId.CENTURY_ROMAN);

    private final Pattern pattern;
    private final ToIntFunction<String> centuryAdjustmentFunction;
    private final DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId;

    PatternCenturyDateOperation(Pattern pattern, ToIntFunction<String> centuryAdjustmentFunction,
        DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
      this.pattern = pattern;
      this.centuryAdjustmentFunction = centuryAdjustmentFunction;
      this.dateNormalizationExtractorMatchId = dateNormalizationExtractorMatchId;
    }

    public Pattern getPattern() {
      return pattern;
    }

    public ToIntFunction<String> getCenturyAdjustmentFunction() {
      return centuryAdjustmentFunction;
    }

    public DateNormalizationExtractorMatchId getDateNormalizationExtractorMatchId() {
      return dateNormalizationExtractorMatchId;
    }
  }

  private static final Pattern PATTERN_ROMAN_RANGE = compile(
      "\\??" + CENTURY_PREFIX + "(?<century1>" + ROMAN_1_TO_21_REGEX + ")\\s*-\\s*(?<century2>" + ROMAN_1_TO_21_REGEX + ")\\??",
      CASE_INSENSITIVE);

  @Override
  public DateNormalizationResult extract(String inputValue) {

    DateNormalizationResult dateNormalizationResult =
        Arrays.stream(PatternCenturyDateOperation.values())
              .map(operation -> extractInstance(inputValue, operation))
              .filter(Objects::nonNull).findFirst().orElse(null);

    // TODO: 22/07/2022 This could be possible merged with the non-range cases
    if (dateNormalizationResult == null) {
      final boolean uncertain = inputValue.startsWith("?") || inputValue.endsWith("?");
      final String sanitizedValue = inputValue.replaceAll("\\s", " ").trim();

      Matcher matcher;
      matcher = PATTERN_ROMAN_RANGE.matcher(sanitizedValue);
      if (matcher.matches()) {
        EdtfDatePart startDatePart = new EdtfDatePart();
        startDatePart.setYearPrecision(YearPrecision.CENTURY);
        final String startCentury = matcher.group("century1");
        startDatePart.setYear((RomanToNumber.romanToDecimal(startCentury) - 1) * 100);
        EdtfDatePart endDatePart = new EdtfDatePart();
        endDatePart.setYearPrecision(YearPrecision.CENTURY);
        final String endCentury = matcher.group("century2");
        endDatePart.setYear((RomanToNumber.romanToDecimal(endCentury) - 1) * 100);
        startDatePart.setUncertain(uncertain);
        endDatePart.setUncertain(uncertain);
        dateNormalizationResult = new DateNormalizationResult(DateNormalizationExtractorMatchId.CENTURY_RANGE_ROMAN,
            sanitizedValue,
            new IntervalEdtfDate(new InstantEdtfDate(startDatePart), new InstantEdtfDate(endDatePart)));
      }
    }
    return dateNormalizationResult;
  }

  private DateNormalizationResult extractInstance(String inputValue, PatternCenturyDateOperation patternCenturyDateOperation) {
    final boolean uncertain = inputValue.startsWith("?") || inputValue.endsWith("?");
    final String sanitizedValue = inputValue.replaceAll("\\s", " ").trim();

    final Matcher matcher = patternCenturyDateOperation.getPattern().matcher(sanitizedValue);
    DateNormalizationResult dateNormalizationResult = null;
    if (matcher.matches()) {
      EdtfDatePart datePart = new EdtfDatePart();
      datePart.setYearPrecision(YearPrecision.CENTURY);
      final String century = matcher.group("century1");
      datePart.setYear(patternCenturyDateOperation.getCenturyAdjustmentFunction().applyAsInt(century));
      datePart.setUncertain(uncertain);
      dateNormalizationResult = new DateNormalizationResult(patternCenturyDateOperation.getDateNormalizationExtractorMatchId(),
          inputValue, new InstantEdtfDate(datePart));
    }
    return dateNormalizationResult;
  }
}
