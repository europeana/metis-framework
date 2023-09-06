package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.LONG_NEGATIVE_YEAR;
import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.DateNormalizationResultStatus;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PatternLongNegativeYearDateExtractorTest {

  private static final PatternLongNegativeYearDateExtractor PATTERN_LONG_NEGATIVE_YEAR_DATE_EXTRACTOR = new PatternLongNegativeYearDateExtractor();

  @ParameterizedTest
  @MethodSource
  void extract(String input, String expected) {
    assertExtract(input, expected);
  }

  void assertExtract(String input, String expected) {
    final DateNormalizationResult dateNormalizationResult = PATTERN_LONG_NEGATIVE_YEAR_DATE_EXTRACTOR.extractDateProperty(input,
        NO_QUALIFICATION);
    if (expected == null) {
      assertEquals(DateNormalizationResultStatus.NO_MATCH, dateNormalizationResult.getDateNormalizationResultStatus());
    } else {
      final String actual = dateNormalizationResult.getEdtfDate().toString();
      assertEquals(expected, actual);
      assertEquals(NO_QUALIFICATION, dateNormalizationResult.getEdtfDate().getDateQualification());
      assertEquals(LONG_NEGATIVE_YEAR, dateNormalizationResult.getDateNormalizationExtractorMatchId());
    }
  }

  private static Stream<Arguments> extract() {

    return Stream.of(
        of("-12345", "Y-12345"),
        of("-123456", "Y-123456"),
        of("-1234567", "Y-1234567"),
        of("-12345678", "Y-12345678"),
        of("-123456789", "Y-123456789"),

        //Future dates are not valid
        of("Y123456789", null),
        //Less digits
        of("-1234", null),
        //Greater digits
        of("-1234567890", null)
    );
  }

}