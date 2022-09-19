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
 * Extractor that matches a century with a Roman numeral, for example ‘XVI’.
 * <p>The Roman numerals may also be preceded by an abbreviation of century, for example ‘s. IXX’.</p>
 * <p>Also supports ranges.</p>
 */
public class PatternCenturyDateExtractor implements DateExtractor {

  enum PatternCenturyDateOperation {
    PATTERN_YYYY(
        compile("\\s*(?<startsWithQuestionMark>\\?)?(?<century1>\\d{2})\\.{2}(?<endsWithQuestionMark>\\?)?\\s*",
            CASE_INSENSITIVE), century -> Integer.parseInt(century) * 100,
        DateNormalizationExtractorMatchId.CENTURY_NUMERIC),
    PATTERN_ENGLISH(
        compile(
            "\\s*(?<startsWithQuestionMark>\\?)?(?<century1>[12]?\\d)(st|nd|rd|th)\\s+century(?<endsWithQuestionMark>\\?)?\\s*",
            CASE_INSENSITIVE), century -> (Integer.parseInt(century) - 1) * 100,
        DateNormalizationExtractorMatchId.CENTURY_NUMERIC),
    PATTERN_ROMAN(
        compile(
            "\\s*(?<startsWithQuestionMark>\\?)?(s\\s|s\\.|sec\\.?|saec\\.?)\\s*(?<century1>I{1,3}|IV|VI{0,3}|I?X|XI{1,3}|XIV|XVI{0,3}|I?XX|XXI)(?<endsWithQuestionMark>\\?)?\\s*",
            CASE_INSENSITIVE), century -> (RomanToNumber.romanToDecimal(century) - 1) * 100,
        DateNormalizationExtractorMatchId.CENTURY_ROMAN),
    PATTERN_ROMAN_CLEAN(
        compile(
            "\\s*(?<startsWithQuestionMark>\\?)?(?<century1>I{1,3}|IV|VI{0,3}|I?X|XI{1,3}|XIV|XVI{0,3}|I?XX|XXI)(?<endsWithQuestionMark>\\?)?\\s*",
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
      "\\s*(?<startsWithQuestionMark>\\?)?(s\\.?|sec\\.?|saec\\.?)\\s*(?<century1>[XIV]{1,5})\\s*-\\s*(?<century2>[XIV]{1,5})(?<endsWithQuestionMark>\\?)?\\s*",
      CASE_INSENSITIVE);

  @Override
  public DateNormalizationResult extract(String inputValue) {

    DateNormalizationResult dateNormalizationResult =
        Arrays.stream(PatternCenturyDateOperation.values())
              .map(operation -> extractInstance(inputValue, operation))
              .filter(Objects::nonNull).findFirst().orElse(null);

    // TODO: 22/07/2022 This could be possible merged with the non-range cases
    if (dateNormalizationResult == null) {
      Matcher matcher;
      matcher = PATTERN_ROMAN_RANGE.matcher(inputValue);
      if (matcher.matches()) {
        EdtfDatePart startDatePart = new EdtfDatePart();
        startDatePart.setYearPrecision(YearPrecision.CENTURY);
        final String startCentury = matcher.group("century1");
        startDatePart.setYear((RomanToNumber.romanToDecimal(startCentury) - 1) * 100);
        EdtfDatePart endDatePart = new EdtfDatePart();
        endDatePart.setYearPrecision(YearPrecision.CENTURY);
        final String endCentury = matcher.group("century2");
        endDatePart.setYear((RomanToNumber.romanToDecimal(endCentury) - 1) * 100);
        if (isUncertainString(matcher)) {
          startDatePart.setUncertain(true);
          endDatePart.setUncertain(true);
        }
        dateNormalizationResult = new DateNormalizationResult(DateNormalizationExtractorMatchId.CENTURY_RANGE_ROMAN, inputValue,
            new IntervalEdtfDate(new InstantEdtfDate(startDatePart), new InstantEdtfDate(endDatePart)));
      }
    }
    return dateNormalizationResult;
  }

  private DateNormalizationResult extractInstance(String inputValue, PatternCenturyDateOperation patternCenturyDateOperation) {
    final Matcher matcher = patternCenturyDateOperation.getPattern().matcher(inputValue);
    DateNormalizationResult dateNormalizationResult = null;
    if (matcher.matches()) {
      EdtfDatePart datePart = new EdtfDatePart();
      datePart.setYearPrecision(YearPrecision.CENTURY);
      final String century = matcher.group("century1");
      datePart.setYear(patternCenturyDateOperation.getCenturyAdjustmentFunction().applyAsInt(century));
      if (isUncertainString(matcher)) {
        datePart.setUncertain(true);
      }
      dateNormalizationResult = new DateNormalizationResult(patternCenturyDateOperation.getDateNormalizationExtractorMatchId(),
          inputValue, new InstantEdtfDate(datePart));
    }
    return dateNormalizationResult;
  }

  private boolean isUncertainString(Matcher matcher) {
    return matcher.group("startsWithQuestionMark") != null || matcher.group("endsWithQuestionMark") != null;
  }

}
