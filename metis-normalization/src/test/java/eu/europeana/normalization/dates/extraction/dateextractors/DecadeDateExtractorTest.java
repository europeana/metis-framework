package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.DECADE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DecadeDateExtractorTest {

  private static final DecadeDateExtractor DECADE_DATE_EXTRACTOR = new DecadeDateExtractor();

  @ParameterizedTest
  @MethodSource
  void extract(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    final DateNormalizationResult dateNormalizationResult = DECADE_DATE_EXTRACTOR.extract(input);
    if (expected == null) {
      assertNull(dateNormalizationResult);
    } else {
      final String actual = dateNormalizationResult.getEdtfDate().toString();
      assertEquals(expected, actual);
      assertEquals(actual.contains("?"), dateNormalizationResult.getEdtfDate().isUncertain());
      assertEquals(dateNormalizationExtractorMatchId, dateNormalizationResult.getDateNormalizationExtractorMatchId());
    }
  }

  private static Stream<Arguments> extract() {
    return Stream.of(
        of("180x", "180X", DECADE),
        of("180u", "180X", DECADE),
        of("180X", "180X", DECADE),
        of("180U", "180X", DECADE),
        of("  180u  ", "180X", DECADE),
        of("180x?", "180X?", DECADE),
        of("180u?", "180X?", DECADE),
        of("180??", "180X?", DECADE),
        of("?180x", "180X?", DECADE),
        of("?180u", "180X?", DECADE),
        of("?180x?", "180X?", DECADE),
        of("?180u?", "180X?", DECADE),
        of("?180??", "180X?", DECADE),

        of("222u", "222X", DECADE),
        //This is an ambiguous case because hyphen can be used as a separator
        of("180-?", null, null),
        //Ambiguous, possible open end
        of("180-", null, null),
        of("180s", null, null),//Non u, x or ?
        of("180?", null, null), //Only one question mark not supported
        //Too many digits
        of("1800", null, null),
        of("?1280x", null, null),
        of("?1280u?", null, null),
        of("?1280??", null, null),
        of("1280??", null, null),

        of("18??", null, null), //Too few digits
        of("18--", null, null), //Too few digits
        of("18..", null, null), //Too few digits
        of("1...", null, null) //Too few digits
    );
  }

}