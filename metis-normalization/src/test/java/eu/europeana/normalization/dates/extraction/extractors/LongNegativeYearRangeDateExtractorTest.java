package eu.europeana.normalization.dates.extraction.extractors;

import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LongNegativeYearRangeDateExtractorTest implements DateExtractorTest {

  private static final LongNegativeYearRangeDateExtractor LONG_NEGATIVE_YEAR_RANGE_DATE_EXTRACTOR = new LongNegativeYearRangeDateExtractor();

  @ParameterizedTest
  @MethodSource
  void extract(String input, String expected) {
    assertExtract(input, expected);
  }

  void assertExtract(String input, String expected) {
    final DateNormalizationResult dateNormalizationResult = LONG_NEGATIVE_YEAR_RANGE_DATE_EXTRACTOR.extractDateProperty(input,
        NO_QUALIFICATION);
    assertDateNormalizationResult(dateNormalizationResult, expected, DateNormalizationExtractorMatchId.LONG_NEGATIVE_YEAR);
  }

  private static Stream<Arguments> extract() {
    return Stream.of(
        of("-12345/-12344", "Y-12345/Y-12344"),
        of("-123456/-123455", "Y-123456/Y-123455"),
        of("-1234567/-1234566", "Y-1234567/Y-1234566"),
        of("-12345678/-12345677", "Y-12345678/Y-12345677"),
        of("-123456789/-123456788", "Y-123456789/Y-123456788"),

        //Uncertain
        of("-12345?/-12344", "Y-12345?/Y-12344"),
        of("-12345/-12344?", "Y-12345/Y-12344?"),
        of("-12345?/-12344?", "Y-12345?/Y-12344?"),

        //Dash
        of("-12345--12344", null),
        of("-123456--123455", null),
        of("-1234567--1234566", null),
        of("-12345678--12345677", null),
        of("-123456789--123456788", null),

        //Future dates are not valid
        of("123456788/123456789", null),
        //Less digits
        of("-1234/-1233", null),
        //Greater digits
        of("-1234567890/-1234567889", null)
    );
  }
}