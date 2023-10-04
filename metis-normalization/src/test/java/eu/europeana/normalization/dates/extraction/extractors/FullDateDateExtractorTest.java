package eu.europeana.normalization.dates.extraction.extractors;

import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FullDateDateExtractorTest implements DateExtractorTest {

  private static final FullDateDateExtractor PATTERN_FORMATTED_FULL_DATE_DATE_EXTRACTOR = new FullDateDateExtractor();

  @ParameterizedTest
  @MethodSource
  void extract(String input, String expected) {
    assertExtract(input, expected);
  }

  void assertExtract(String input, String expected) {
    final DateNormalizationResult dateNormalizationResult = PATTERN_FORMATTED_FULL_DATE_DATE_EXTRACTOR.extractDateProperty(input);
    assertDateNormalizationResult(dateNormalizationResult, expected, DateNormalizationExtractorMatchId.FORMATTED_FULL_DATE);
  }

  private static Stream<Arguments> extract() {
    return Stream.of(
        of("Wed Nov 01 01:00:00 CEST 1989", "1989-11-01"),
        of("Τετ Νοε 01 01:00:00 CEST 1989", "1989-11-01"),
        of("1989-11-01 04:05:06 UTC", "1989-11-01"),
        of("1989-11-01 04:05:06 UTC+01", "1989-11-01"),
        of("1989-11-01 04:05:06 UTC-01", "1989-11-01"),
        of("1989-11-01 01:02:03 UTC", "1989-11-01"),
        of("1989-11-01 01:02:03", "1989-11-01"),

        //Invalids
        of("Wed Nov 01 01:00:00 CEST", null),
        of("Wed Nov 01 01:00:00", null),
        of("1989-11-01 01:02:03+01", null),
        of("1989-11-01 01:02", null),
        of("1989-11-01 01", null),
        of("1989-11-01", null)
    );
  }

}