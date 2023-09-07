package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.YearPrecision.CENTURY;
import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static eu.europeana.normalization.dates.edtf.DateQualification.UNCERTAIN;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDateBuilder;
import eu.europeana.normalization.dates.extraction.DateExtractionException;
import eu.europeana.normalization.dates.extraction.RomanToNumber;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extractor that matches a century with Roman numerals
 * <p>The range of values this accepts are from 1-21 including.
 * The Roman numerals may also be preceded by an abbreviation of century, for example ‘s. XIX’.</p>
 * <p>Examples of some cases:
 * <ul>
 *   <li>
 *     Value = s. XX | Outcome = 19XX
 *     Value = s. XXI | Outcome = 20XX
 *   </li>
 * </ul>
 * </p>
 */
public class CenturyRomanDateExtractor extends AbstractDateExtractor {

  private static final String OPTIONAL_QUESTION_MARK = "\\??";
  private static final String CENTURY_PREFIX = "(?:(?:s|sec|saec)\\s|(?:s|sec|saec)\\.\\s?)?";
  private static final String ROMAN_1_TO_21_REGEX = "(X?(?:IX|IV|VI{0,3}|I{1,3})|X|XXI?)";
  private static final Pattern ROMAN_2_TO_21_PATTERN = compile(
      OPTIONAL_QUESTION_MARK + CENTURY_PREFIX + ROMAN_1_TO_21_REGEX + OPTIONAL_QUESTION_MARK, CASE_INSENSITIVE);

  @Override
  public DateNormalizationResult extract(String inputValue, DateQualification requestedDateQualification,
      boolean flexibleDateBuild) throws DateExtractionException {
    DateNormalizationResult dateNormalizationResult = DateNormalizationResult.getNoMatchResult(inputValue);
    final DateQualification dateQualification = computeDateQualification(requestedDateQualification, () ->
        (inputValue.startsWith("?") || inputValue.endsWith("?")) ? UNCERTAIN : NO_QUALIFICATION);
    final Matcher matcher = ROMAN_2_TO_21_PATTERN.matcher(inputValue);
    if (matcher.matches()) {
      final int century = RomanToNumber.romanToDecimal(matcher.group(1)) - 1;
      final InstantEdtfDateBuilder instantEdtfDateBuilder =
          new InstantEdtfDateBuilder(century).withYearPrecision(CENTURY).withDateQualification(dateQualification);
      dateNormalizationResult = new DateNormalizationResult(DateNormalizationExtractorMatchId.CENTURY_ROMAN,
          inputValue, instantEdtfDateBuilder.build());
    }
    return dateNormalizationResult;
  }
}
