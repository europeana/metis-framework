package eu.europeana.normalization.dates.extraction.dateextractors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PatternDateExtractorYyyyMmDdSpacesDateExtractorTest {
  // TODO: 16/12/2022 Prepared some tests for future implementation. To be further improved with another ticket

  private static final PatternDateExtractorYyyyMmDdSpacesDateExtractor PATTERN_DATE_EXTRACTOR_YYYY_MM_DD_SPACES_DATE_EXTRACTOR = new PatternDateExtractorYyyyMmDdSpacesDateExtractor();

  @ParameterizedTest
  @MethodSource
  void extractYMD(String input, String expected) {
    extract(input, expected);
  }

  @ParameterizedTest
  @MethodSource
  void extractDMY(String input, String expected) {
    extract(input, expected);
  }

  void extract(String input, String expected) {
    final DateNormalizationResult dateNormalizationResult = PATTERN_DATE_EXTRACTOR_YYYY_MM_DD_SPACES_DATE_EXTRACTOR.extract(
        input);
    if (expected == null) {
      assertNull(dateNormalizationResult);
    } else {
      final String actual = dateNormalizationResult.getEdtfDate().toString();
      assertEquals(expected, actual);
      assertEquals(DateNormalizationExtractorMatchId.YYYY_MM_DD_SPACES,
          dateNormalizationResult.getDateNormalizationExtractorMatchId());
    }
  }

  private static Stream<Arguments> extractYMD() {

    return Stream.of(
        of("1989 11 01", "1989-11-01"),
        of("1989 1 1", "1989-01-01"),
        of("1989 1", null),
        of("1989", null)
    );
  }

  private static Stream<Arguments> extractDMY() {

    return Stream.of(
        of("01 11 1989", "1989-11-01"),
        of("1 1 1989", "1989-01-01"),
        of("1 1989", null),
        of("1989", null)
    );
  }
}