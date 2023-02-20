package eu.europeana.normalization.dates.extraction.dateextractors;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.YearPrecision;
import eu.europeana.normalization.dates.edtf.AbstractEdtfDate;
import eu.europeana.normalization.dates.edtf.EdtfDatePart;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import eu.europeana.normalization.dates.extraction.RomanToNumber;
import eu.europeana.normalization.dates.sanitize.DateFieldSanitizer;
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
public class CenturyDateExtractor implements DateExtractor {

  private static final String NUMERIC_10_TO_21_ENDING_DOTS_REGEX = "(1\\d|2[0-1])\\.{2}";
  private static final String NUMERIC_1_TO_21_SUFFIXED_REGEX = "(2?1st|2nd|3rd|(?:1\\d|[4-9]|20)th)\\scentury";
  private static final String ROMAN_1_TO_21_REGEX = "(X?(?:IX|IV|VI{0,3}|I{1,3})|X|XXI?)";
  private static final String CENTURY_PREFIX = "(?:(?:s|sec|saec)\\s|(?:s|sec|saec)\\.\\s?)?";
  private static final String QUESTION_MARK = "\\??";

  enum PatternCenturyDateOperation {
    PATTERN_YYYY(
        compile(QUESTION_MARK + NUMERIC_10_TO_21_ENDING_DOTS_REGEX + QUESTION_MARK, CASE_INSENSITIVE),
        century -> Integer.parseInt(century) * 100, DateNormalizationExtractorMatchId.CENTURY_NUMERIC),
    PATTERN_ENGLISH(
        compile(QUESTION_MARK + NUMERIC_1_TO_21_SUFFIXED_REGEX + QUESTION_MARK, CASE_INSENSITIVE),
        century -> (Integer.parseInt(century.substring(0, century.length() - 2)) - 1) * 100,
        DateNormalizationExtractorMatchId.CENTURY_NUMERIC),
    PATTERN_ROMAN(
        compile(QUESTION_MARK + CENTURY_PREFIX + ROMAN_1_TO_21_REGEX + QUESTION_MARK, CASE_INSENSITIVE),
        century -> (RomanToNumber.romanToDecimal(century) - 1) * 100,
        DateNormalizationExtractorMatchId.CENTURY_ROMAN),
    PATTERN_ROMAN_RANGE(
        compile(QUESTION_MARK + CENTURY_PREFIX + ROMAN_1_TO_21_REGEX + "\\s?-\\s?" + ROMAN_1_TO_21_REGEX + QUESTION_MARK,
            CASE_INSENSITIVE), century -> (RomanToNumber.romanToDecimal(century) - 1) * 100,
        DateNormalizationExtractorMatchId.CENTURY_RANGE_ROMAN);

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

  @Override
  public DateNormalizationResult extract(String inputValue) {
    return Arrays.stream(PatternCenturyDateOperation.values())
                 .map(operation -> extractInstance(inputValue, operation))
                 .filter(Objects::nonNull).findFirst().orElse(null);
  }

  private DateNormalizationResult extractInstance(String inputValue, PatternCenturyDateOperation patternCenturyDateOperation) {
    final String sanitizedValue = DateFieldSanitizer.cleanSpacesAndTrim(inputValue);
    final boolean uncertain = sanitizedValue.startsWith("?") || sanitizedValue.endsWith("?");

    final Matcher matcher = patternCenturyDateOperation.getPattern().matcher(sanitizedValue);
    DateNormalizationResult dateNormalizationResult = null;
    if (matcher.matches()) {
      AbstractEdtfDate abstractEdtfDate;
      EdtfDatePart datePart1 = extractEdtfDatePart(patternCenturyDateOperation, uncertain, matcher, 1);

      //Check if we have an interval or instance
      if (matcher.groupCount() == 2) {
        EdtfDatePart datePart2 = extractEdtfDatePart(patternCenturyDateOperation, uncertain, matcher, 2);
        abstractEdtfDate = new IntervalEdtfDate(new InstantEdtfDate(datePart1), new InstantEdtfDate(datePart2));
      } else {
        abstractEdtfDate = new InstantEdtfDate(datePart1);
      }

      dateNormalizationResult = new DateNormalizationResult(patternCenturyDateOperation.getDateNormalizationExtractorMatchId(),
          inputValue, abstractEdtfDate);
    }
    return dateNormalizationResult;
  }

  private EdtfDatePart extractEdtfDatePart(PatternCenturyDateOperation patternCenturyDateOperation, boolean uncertain,
      Matcher matcher, int group) {
    EdtfDatePart datePart = new EdtfDatePart();
    datePart.setYearPrecision(YearPrecision.CENTURY);
    final String century = matcher.group(group);
    datePart.setYear(patternCenturyDateOperation.getCenturyAdjustmentFunction().applyAsInt(century));
    datePart.setUncertain(uncertain);
    return datePart;
  }
}
