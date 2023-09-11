package eu.europeana.normalization.dates.extraction.extractors;

import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DecadeDateExtractorTest implements DateExtractorTest {

  private static final DecadeDateExtractor DECADE_DATE_EXTRACTOR = new DecadeDateExtractor();

  @ParameterizedTest
  @MethodSource
  void extract(String input, String expected) {
    final DateNormalizationResult dateNormalizationResult = DECADE_DATE_EXTRACTOR.extractDateProperty(input, NO_QUALIFICATION);
    assertDateNormalizationResult(dateNormalizationResult, expected, DateNormalizationExtractorMatchId.DECADE);
  }

  private static Stream<Arguments> extract() {
    return Stream.of(
        of("180x", "180X"),
        of("180u", "180X"),
        of("180X", "180X"),
        of("180U", "180X"),
        of("  180u  ", "180X"),
        of("180x?", "180X?"),
        of("180u?", "180X?"),
        of("180??", "180X?"),
        of("?180x", "180X?"),
        of("?180u", "180X?"),
        of("?180x?", "180X?"),
        of("?180u?", "180X?"),
        of("?180??", "180X?"),

        //Future dates not allowed
        of("222u", null),
        //This is an ambiguous case because hyphen can be used as a separator
        of("180-?", null),
        //Ambiguous, possible open end
        of("180-", null),
        of("180s", null),//Non u, x or ?
        of("180?", null), //Only one question mark not supported
        //Too many digits
        of("1800", null),
        of("?1280x", null),
        of("?1280u?", null),
        of("?1280??", null),
        of("1280??", null),

        of("18??", null), //Too few digits
        of("18--", null), //Too few digits
        of("18..", null), //Too few digits
        of("1...", null) //Too few digits
    );
  }

}