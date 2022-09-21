package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.NUMERIC_ALL_VARIANTS;
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

class NumericWithMissingPartsDateExtractorTest {

  private static final NumericWithMissingPartsDateExtractor NUMERIC_WITH_MISSING_PARTS_DATE_EXTRACTOR = new NumericWithMissingPartsDateExtractor();

  @ParameterizedTest
  @MethodSource
  void extract(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    final DateNormalizationResult dateNormalizationResult = NUMERIC_WITH_MISSING_PARTS_DATE_EXTRACTOR.extract(input);
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
        //YEAR
        of("1989", "1989", NUMERIC_ALL_VARIANTS), //A month and day can be missing
        of("?1989", "1989?", NUMERIC_ALL_VARIANTS),
        of("1989?", "1989?", NUMERIC_ALL_VARIANTS),
        of("?1989?", "1989?", NUMERIC_ALL_VARIANTS),

        //YEAR-MONTH
        of("1989-11", "1989-11", NUMERIC_ALL_VARIANTS),
        of("1989.11", "1989-11", NUMERIC_ALL_VARIANTS),
        of("1989/11", "1989-11", NUMERIC_ALL_VARIANTS),
        of("?1989-11", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("?1989.11", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("?1989/11", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("1989-11?", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("1989.11?", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("1989/11?", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("?1989-11?", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("?1989.11?", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("?1989/11?", "1989-11?", NUMERIC_ALL_VARIANTS),

        //YEAR-MONTH-DAY
        of("1989-11-01", "1989-11-01", NUMERIC_ALL_VARIANTS),
        of("1989-13-32", "1989-13-32", NUMERIC_ALL_VARIANTS),
        //This is working but shouldn't we restrict the month and day ranges???
        of("989-1-1", "0989-01-01", NUMERIC_ALL_VARIANTS), //Some missing digits are allowed
        of("?1989-11-01", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("1989-11-01?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("?1989-11-01?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("1989.11.01", "1989-11-01", NUMERIC_ALL_VARIANTS),
        of("?1989.11.01", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("1989.11.01?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("?1989.11.01?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("989.1.1", "0989-01-01", NUMERIC_ALL_VARIANTS), //Some missing digits are allowed
        of("1989/11/01", "1989-11-01", NUMERIC_ALL_VARIANTS),
        of("?1989/11/01", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("1989/11/01?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("?1989/11/01?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("989/1/1", "0989-01-01", NUMERIC_ALL_VARIANTS), //Some missing digits are allowed

        of("89-01-01", null, null), //Too few digits on year
        of("89.1.1", null, null), //Too few digits on year
        of("1/1/89", null, null), //Too few digits on year
        of("12345-01-01", null, null), //Too many digits on year
        of("1234-123-12", null, null), //Too many digits on month
        of("1234-12-123", null, null), //Too many digits on day

        //MONTH-YEAR
        of("11-1989", "1989-11", NUMERIC_ALL_VARIANTS),
        of("11.1989", "1989-11", NUMERIC_ALL_VARIANTS),
        of("11/1989", "1989-11", NUMERIC_ALL_VARIANTS),
        of("?11-1989", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("?11.1989", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("?11/1989", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("11-1989?", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("11.1989?", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("11/1989?", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("?11-1989?", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("?11.1989?", "1989-11?", NUMERIC_ALL_VARIANTS),
        of("?11/1989?", "1989-11?", NUMERIC_ALL_VARIANTS),

        //DAY-MONTH-YEAR
        of("01-11-1989", "1989-11-01", NUMERIC_ALL_VARIANTS),
        of("32-13-1989", "1989-13-32", NUMERIC_ALL_VARIANTS),
        //This is working but shouldn't we restrict the month and day ranges???
        of("1-1-989", "0989-01-01", NUMERIC_ALL_VARIANTS), //Some missing digits are allowed
        of("?01-11-1989", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("01-11-1989?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("?01-11-1989?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("01.11.1989", "1989-11-01", NUMERIC_ALL_VARIANTS),
        of("?01.11.1989", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("01.11.1989?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("?01.11.1989?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("1.1.989", "0989-01-01", NUMERIC_ALL_VARIANTS), //Some missing digits are allowed
        of("01/11/1989", "1989-11-01", NUMERIC_ALL_VARIANTS),
        of("?01/11/1989", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("01/11/1989?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("?01/11/1989?", "1989-11-01?", NUMERIC_ALL_VARIANTS),
        of("1/1/989", "0989-01-01", NUMERIC_ALL_VARIANTS), //Some missing digits are allowed

        of("01-01-89", null, null), //Too few digits on year
        of("1.1.89", null, null), //Too few digits on year
        of("1/1/89", null, null), //Too few digits on year
        of("01-01-12345", null, null), //Too many digits on year
        of("12-123-1234", null, null), //Too many digits on month
        of("123-12-1234", null, null), //Too many digits on day

        //Other invalids
        of("1989 11 01", null, null), //Spaces should not match
        of("1989--11--01", null, null), //Double dashes should not match
        of("1989..11..01", null, null), //Double dots should not match
        of("1989//11//01", null, null) //Double slashes should not match

    );
  }

}