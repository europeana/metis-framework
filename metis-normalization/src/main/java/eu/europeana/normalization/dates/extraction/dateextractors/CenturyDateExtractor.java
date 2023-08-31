package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.YearPrecision.CENTURY;
import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static eu.europeana.normalization.dates.edtf.DateQualification.UNCERTAIN;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.DateNormalizationResultStatus;
import eu.europeana.normalization.dates.edtf.AbstractEdtfDate;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDateBuilder;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import eu.europeana.normalization.dates.extraction.RomanToNumber;
import eu.europeana.normalization.dates.sanitize.DateFieldSanitizer;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class CenturyDateExtractor extends AbstractDateExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String NUMERIC_10_TO_21_ENDING_DOTS_REGEX = "(1\\d|2[0-1])\\.{2}";
  private static final String NUMERIC_1_TO_21_SUFFIXED_REGEX = "(2?1st|2nd|3rd|(?:1\\d|[4-9]|20)th)\\scentury";
  private static final String ROMAN_1_TO_21_REGEX = "(X?(?:IX|IV|VI{0,3}|I{1,3})|X|XXI?)";
  private static final String CENTURY_PREFIX = "(?:(?:s|sec|saec)\\s|(?:s|sec|saec)\\.\\s?)?";
  private static final String QUESTION_MARK = "\\??";

  private enum PatternCenturyDateOperation {
    PATTERN_YYYY(
        compile(QUESTION_MARK + NUMERIC_10_TO_21_ENDING_DOTS_REGEX + QUESTION_MARK, CASE_INSENSITIVE),
        Integer::parseInt, DateNormalizationExtractorMatchId.CENTURY_NUMERIC),
    PATTERN_ENGLISH(
        compile(QUESTION_MARK + NUMERIC_1_TO_21_SUFFIXED_REGEX + QUESTION_MARK, CASE_INSENSITIVE),
        century -> (Integer.parseInt(century.substring(0, century.length() - 2)) - 1),
        DateNormalizationExtractorMatchId.CENTURY_NUMERIC),
    PATTERN_ROMAN(
        compile(QUESTION_MARK + CENTURY_PREFIX + ROMAN_1_TO_21_REGEX + QUESTION_MARK, CASE_INSENSITIVE),
        century -> (RomanToNumber.romanToDecimal(century) - 1),
        DateNormalizationExtractorMatchId.CENTURY_ROMAN),
    PATTERN_ROMAN_RANGE(
        compile(QUESTION_MARK + CENTURY_PREFIX + ROMAN_1_TO_21_REGEX + "\\s?-\\s?" + ROMAN_1_TO_21_REGEX + QUESTION_MARK,
            CASE_INSENSITIVE), century -> (RomanToNumber.romanToDecimal(century) - 1),
        DateNormalizationExtractorMatchId.CENTURY_RANGE_ROMAN);

    private final Pattern pattern;
    private final ToIntFunction<String> centuryExtractorFunction;
    private final DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId;

    PatternCenturyDateOperation(Pattern pattern, ToIntFunction<String> centuryExtractorFunction,
        DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
      this.pattern = pattern;
      this.centuryExtractorFunction = centuryExtractorFunction;
      this.dateNormalizationExtractorMatchId = dateNormalizationExtractorMatchId;
    }

    public Pattern getPattern() {
      return pattern;
    }

    public ToIntFunction<String> getCenturyExtractorFunction() {
      return centuryExtractorFunction;
    }

    public DateNormalizationExtractorMatchId getDateNormalizationExtractorMatchId() {
      return dateNormalizationExtractorMatchId;
    }
  }

  @Override
  public DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      boolean flexibleDateBuild) {
    return Arrays.stream(PatternCenturyDateOperation.values())
                 .map(operation -> extract(inputValue, requestedDateQualification, operation, flexibleDateBuild))
                 .filter(dateNormalizationResult -> dateNormalizationResult.getDateNormalizationResultStatus()
                     == DateNormalizationResultStatus.MATCHED).findFirst()
                 .orElse(DateNormalizationResult.getNoMatchResult(inputValue));
  }

  private DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      PatternCenturyDateOperation patternCenturyDateOperation,
      boolean allowSwitchMonthDay) {
    DateNormalizationResult dateNormalizationResult = DateNormalizationResult.getNoMatchResult(inputValue);
    try {
      final String sanitizedValue = DateFieldSanitizer.cleanSpacesAndTrim(inputValue);
      final DateQualification dateQualification = computeDateQualification(requestedDateQualification, () ->
          (sanitizedValue.startsWith("?") || sanitizedValue.endsWith("?")) ? UNCERTAIN : NO_QUALIFICATION);

      final Matcher matcher = patternCenturyDateOperation.getPattern().matcher(sanitizedValue);
      if (matcher.matches()) {
        AbstractEdtfDate abstractEdtfDate;
        InstantEdtfDateBuilder startDatePartBuilder = extractEdtfDatePart(patternCenturyDateOperation, matcher, 1);
        InstantEdtfDate startEdtfDate = startDatePartBuilder.withDateQualification(dateQualification)
                                                            .withFlexibleDateBuild(allowSwitchMonthDay).build();

        boolean isInterval = matcher.groupCount() == 2;
        if (isInterval) {
          InstantEdtfDateBuilder endDatePartBuilder = extractEdtfDatePart(patternCenturyDateOperation, matcher, 2);
          InstantEdtfDate endEdtfDate = endDatePartBuilder.withDateQualification(dateQualification)
                                                          .withFlexibleDateBuild(allowSwitchMonthDay).build();
          abstractEdtfDate = new IntervalEdtfDateBuilder(startEdtfDate, endEdtfDate).withFlexibleDateBuild(allowSwitchMonthDay)
                                                                                    .build();
        } else {
          abstractEdtfDate = startEdtfDate;
        }

        dateNormalizationResult = new DateNormalizationResult(patternCenturyDateOperation.getDateNormalizationExtractorMatchId(),
            inputValue, abstractEdtfDate);
      }
    } catch (DateExtractionException e) {
      LOGGER.warn("Failed instance extraction!", e);
    }
    return dateNormalizationResult;
  }

  private InstantEdtfDateBuilder extractEdtfDatePart(PatternCenturyDateOperation patternCenturyDateOperation,
      Matcher matcher, int group) {
    final String century = matcher.group(group);
    return new InstantEdtfDateBuilder(patternCenturyDateOperation.getCenturyExtractorFunction().applyAsInt(century))
        .withYearPrecision(CENTURY);
  }
}
