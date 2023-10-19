package eu.europeana.normalization.dates.extraction.extractors;

import static eu.europeana.normalization.dates.YearPrecision.CENTURY;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extractor that matches a century with a decimal numerals.
 * <p>The range of values this accepts are from 1-21 including.</p>
 * <p>Examples of some cases:
 * <ul>
 *   <li>
 *     Value = 18.. | Outcome = 18XX
 *     Value = 1st century | Outcome = 00XX
 *   </li>
 * </ul>
 * </p>
 */
public class CenturyNumericDateExtractor extends AbstractDateExtractor {

  private static final String NUMERIC_10_TO_21_ENDING_DOTS_REGEX = "(1\\d|2[0-1])\\.{2}";
  private static final String NUMERIC_1_TO_21_SUFFIXED_REGEX = "(2?1st|2nd|3rd|(?:1\\d|[4-9]|20)th)\\scentury";

  private enum CenturyNumericDatePattern {
    PATTERN_YYYY(
        compile(OPTIONAL_QUESTION_MARK_REGEX + NUMERIC_10_TO_21_ENDING_DOTS_REGEX + OPTIONAL_QUESTION_MARK_REGEX,
            CASE_INSENSITIVE),
        Integer::parseInt, DateNormalizationExtractorMatchId.CENTURY_NUMERIC),
    PATTERN_ENGLISH(
        compile(OPTIONAL_QUESTION_MARK_REGEX + NUMERIC_1_TO_21_SUFFIXED_REGEX + OPTIONAL_QUESTION_MARK_REGEX, CASE_INSENSITIVE),
        century -> (Integer.parseInt(century.substring(0, century.length() - 2)) - 1),
        DateNormalizationExtractorMatchId.CENTURY_NUMERIC);

    private final Pattern pattern;
    private final ToIntFunction<String> centuryExtractorFunction;
    private final DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId;

    CenturyNumericDatePattern(Pattern pattern, ToIntFunction<String> centuryExtractorFunction,
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
  public DateNormalizationResult extract(String inputValue, boolean allowDayMonthSwap) throws DateExtractionException {
    DateNormalizationResult dateNormalizationResult = DateNormalizationResult.getNoMatchResult(inputValue);
    for (CenturyNumericDatePattern centerNumericDatePattern : CenturyNumericDatePattern.values()) {
      final Matcher matcher = centerNumericDatePattern.getPattern().matcher(inputValue);
      if (matcher.matches()) {
        final String century = matcher.group(1);
        InstantEdtfDateBuilder instantEdtfDateBuilder = new InstantEdtfDateBuilder(
            centerNumericDatePattern.getCenturyExtractorFunction().applyAsInt(century))
            .withYearPrecision(CENTURY);
        InstantEdtfDate instantEdtfDate = instantEdtfDateBuilder.withDateQualification(getQualification(inputValue))
                                                                .withAllowDayMonthSwap(allowDayMonthSwap).build();
        dateNormalizationResult =
            new DateNormalizationResult(centerNumericDatePattern.getDateNormalizationExtractorMatchId(), inputValue,
                instantEdtfDate);
        break;
      }
    }
    return dateNormalizationResult;
  }
}
