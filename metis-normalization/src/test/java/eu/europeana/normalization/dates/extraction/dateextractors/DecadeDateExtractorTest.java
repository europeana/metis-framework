package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.DECADE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
      if (actual.contains("?")) {
        assertTrue(dateNormalizationResult.getEdtfDate().isUncertain());
      } else {
        assertFalse(dateNormalizationResult.getEdtfDate().isUncertain());
      }
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
        // TODO: 26/09/2022 This is fine since it contains two question marks at the end and not just one question mark
        of("180??", "180X?", DECADE),
        of("?180x", "180X?", DECADE),
        of("?180u", "180X?", DECADE),
        of("?180u?", "180X?", DECADE),

        of("222u", "222X", DECADE),
        of("180-?", null, null), //This is an ambiguous case because hyphen can be used as a separator
        // TODO: 26/09/2022 Update to support hyphen as well
        of("180-", null, null),//Non u, x or ?
        of("180?", null, null), //Only one question mark not supported
        of("1800", null, null), //Too many digits
        of("18??", null, null), //Too few digits
        of("18--", null, null), //Too few digits
        of("18..", null, null), //Too few digits
        of("1...", null, null) //Too few digits
    );
  }

}