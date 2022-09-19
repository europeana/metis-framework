package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.DCMI_PERIOD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import eu.europeana.normalization.dates.extraction.DcmiPeriod;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link DcmiPeriodDateExtractor} class
 */
class DcmiPeriodDateExtractorTest {

  private static Stream<Arguments> dcmiPeriodData() {
    return Stream.of(
        Arguments.of("name=The Great Depression; start=1929; end=1939;",
            "1929", "1939", "The Great Depression", true),
        Arguments.of("name=The Great Depression; start=; end=1939;",
            "", "1939", "The Great Depression", false), //TODO: check this case for dcmperiod probably should be accepted
        Arguments.of("name=The Great Depression; start=1929; end=;",
            "1929", "", "The Great Depression", false), //TODO: check this case for dcmperiod probably should be accepted
        Arguments.of("name=Haagse International Arts Festival, 2000; start=2000-01-26; end=2000-02-20;",
            "2000-01-26", "2000-02-20", "Haagse International Arts Festival, 2000", true),
        //TODO: support dates without seconds 1999-09-25T14:20+10:00
        Arguments.of("start=1999-09-25T14:20:00+10:00; end=1999-09-25T16:40:00+10:00; scheme=W3C-DTF;",
            "1999-09-25T14:20:00", "1999-09-25T16:40:00", null, true),
        Arguments.of("start=1999-09-25T14:20:00+10:00;  scheme=W3C-DTF;",
            "1999-09-25T14:20:00", null, null, true),
        Arguments.of("end=1999-09-25T16:40:00+10:00; scheme=W3C-DTF;",
            null, "1999-09-25T16:40:00", null, true),
        Arguments.of("end=1998-09-25T16:40:00+10:00; start=1998-1986 scheme=W3C-DTF;",
            "..", "1998-09-25T16:40:00", null, false)
        //TODO: support scheme=Geological timescale
        //        Arguments.of("start=Cambrian period; scheme=Geological timescale; name=Phanerozoic Eon;",
        //            "Cambrian period", null, null, true)
    );
  }

  @ParameterizedTest
  @MethodSource("dcmiPeriodData")
  @DisplayName("Decode DCMI Period")
  void decodePeriod(String actualDcmiPeriod,
      String expectedStartDate,
      String expectedEndDate,
      String expectedName,
      Boolean isSuccess) {
    DcmiPeriod dcmiPeriod = DcmiPeriodDateExtractor.decodePeriod(actualDcmiPeriod);
    if (isSuccess) {
      assertEquals(expectedStartDate, dcmiPeriod.getStart() != null ? dcmiPeriod.getStart().toString() : null);
      assertEquals(expectedEndDate, dcmiPeriod.getEnd() != null ? dcmiPeriod.getEnd().toString() : null);
      assertEquals(expectedName, dcmiPeriod.getName());
    } else {
      assertNull(dcmiPeriod);
    }
  }

  private static Stream<Arguments> extractData() {
    return Stream.of(
        Arguments.of("name=The Great Depression; start=1929; end=1939;",
            "1929", "1939", DCMI_PERIOD, true),
        Arguments.of("name=Haagse International Arts Festival, 2000; start=2000-01-26; end=2000-02-20;",
            "2000-01-26", "2000-02-20", DCMI_PERIOD, true),
        Arguments.of("start=1998-09-25T14:20:00+10:00; end=1998-09-25T16:40:00+10:00; scheme=W3C-DTF;",
            "1998-09-25T14:20:00", "1998-09-25T16:40:00", DCMI_PERIOD, true),
        Arguments.of("start=1998-09-25T14:20:00+10:00;  scheme=W3C-DTF;",
            "1998-09-25T14:20:00", "..", DCMI_PERIOD, true),
        Arguments.of("end=1998-09-25T16:40:00+10:00; scheme=W3C-DTF;",
            "..", "1998-09-25T16:40:00", DCMI_PERIOD, true),
        Arguments.of("end=1998-09-25T16:40+10:00; start=1998/01/01 scheme=W3C-DTF;",
            "..", "1998-09-25T16:40:00", DCMI_PERIOD, false)
    );
  }

  @ParameterizedTest
  @MethodSource("extractData")
  @DisplayName("Extract DCMI Period")
  void extract(String actualDcmiPeriod,
      String expectedStartDate,
      String expectedEndDate,
      DateNormalizationExtractorMatchId expectedMatchId,
      Boolean isSuccess) {
    DcmiPeriodDateExtractor periodDateExtractor = new DcmiPeriodDateExtractor();
    DateNormalizationResult result = periodDateExtractor.extract(actualDcmiPeriod);
    if (isSuccess) {
      IntervalEdtfDate interval = (IntervalEdtfDate) result.getEdtfDate();
      assertEquals(expectedStartDate, interval.getStart() != null ? interval.getStart().toString() : null);
      assertEquals(expectedEndDate, interval.getEnd() != null ? interval.getEnd().toString() : null);
      assertEquals(expectedMatchId, result.getDateNormalizationExtractorMatchId());
      assertTrue(result.isCompleteDate());
    } else {
      assertNull(result);
    }
  }
}
