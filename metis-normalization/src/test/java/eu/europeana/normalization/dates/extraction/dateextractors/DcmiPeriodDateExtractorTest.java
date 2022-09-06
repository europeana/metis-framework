package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.DCMI_PERIOD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        //Scheme checks
        Arguments.of("name=The Great Depression; start=1929; end=1939; scheme=W3CDTF;",
            "1929", "1939", "The Great Depression", true),
        Arguments.of("name=The Great Depression; start=1929; end=1939; scheme=W3C-DTF;",
            "1929", "1939", "The Great Depression", true),
        Arguments.of("scheme=W3C-DTF; name=The Great Depression; start=1929; end=1939;",
            "1929", "1939", "The Great Depression", true),
        Arguments.of("name=The Great Depression; start=1929; end=1939; scheme=W3C-DTF",
            "1929", "1939", "The Great Depression", true),
        Arguments.of("name=The Great Depression; start=1929; end=1939; scheme=W3C-",
            "1929", "1939", "The Great Depression", false),

        //double fields should be false
        Arguments.of("name=The Great Depression; start=1929; end=1939; name=The Great Depression;",
            "1929", "1939", "The Great Depression", false),
        Arguments.of("name=The Great Depression; start=1929; end=1939; start=1929;",
            "1929", "1939", "The Great Depression", false),
        Arguments.of("name=The Great Depression; end=1939; start=1929; end=1939;",
            "1929", "1939", "The Great Depression", false),

        //Both start and end null then false
        Arguments.of("name=The Great Depression; start=; end=;",
            "1929", "1939", "The Great Depression", false),
        Arguments.of("name=The Great Depression;",
            "1929", "1939", "The Great Depression", false),

        //One end bounded
        Arguments.of("name=The Great Depression; start=; end=1939;",
            null, "1939", "The Great Depression", true),
        Arguments.of("name=The Great Depression; start=1929; end=;",
            "1929", null, "The Great Depression", true),

        //Full date
        Arguments.of("name=Haagse International Arts Festival, 2000; start=2000-01-26; end=2000-02-20;",
            "2000-01-26", "2000-02-20", "Haagse International Arts Festival, 2000", true),

        //Full date and time
        Arguments.of("start=1999-09-25T14:20:00+10:00; end=1999-09-25T16:40:00+10:00; scheme=W3C-DTF;",
            "1999-09-25T14:20:00", "1999-09-25T16:40:00", null, true),
        Arguments.of("start=1999-09-25T14:20:00+10:00;  scheme=W3C-DTF;",
            "1999-09-25T14:20:00", null, null, true),
        Arguments.of("end=1999-09-25T16:40:00+10:00; scheme=W3C-DTF;",
            null, "1999-09-25T16:40:00", null, true),

        //Missing semicolon
        Arguments.of("end=1998-09-25T16:40:00+10:00; start=1998 scheme=W3C-DTF;",
            null, "1998-09-25T16:40:00", null, true),

        //Invalid date
        Arguments.of("end=1998-09-25T16:40+10:00; start=1998-1986; scheme=W3C-DTF;",
            "..", "1998-09-25T16:40:00", null, false),

        //Spaces at the end of the name are cleaned up
        Arguments.of("name=The Great Depression          ; start=1929; end=1939;",
            "1929", "1939", "The Great Depression", true),

        //Spaces at the beginning of the name are cleaned up
        Arguments.of("name=The Great Depression; start=1929; end=1939;",
            "1929", "1939", "The Great Depression", true),

        //Normal case
        Arguments.of("name=The Great Depression; start=1929; end=1939;",
            "1929", "1939", "The Great Depression", true)

    );
  }

  @ParameterizedTest
  @MethodSource("dcmiPeriodData")
  @DisplayName("Decode DCMI Period")
  void decodePeriod(String actualDcmiPeriod,
      String expectedName, String expectedStartDate,
      String expectedEndDate,
      Boolean isSuccess) {
    DcmiPeriod dcmiPeriod = DcmiPeriodDateExtractor.extractDcmiPeriod(actualDcmiPeriod);
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
            "The Great Depression", "1929", "1939", true),
        Arguments.of("name=Haagse International Arts Festival, 2000; start=2000-01-26; end=2000-02-20;",
            "Haagse International Arts Festival, 2000", "2000-01-26", "2000-02-20", true),
        Arguments.of("start=1998-09-25T14:20:00+10:00; end=1998-09-25T16:40:00+10:00; scheme=W3C-DTF;",
            null, "1998-09-25T14:20:00", "1998-09-25T16:40:00", true),
        Arguments.of("start=1998-09-25T14:20:00+10:00;  scheme=W3C-DTF;",
            null, "1998-09-25T14:20:00", "..", true),
        Arguments.of("end=1998-09-25T16:40:00+10:00; scheme=W3C-DTF;",
            null, "..", "1998-09-25T16:40:00", true),
        Arguments.of("end=1998-09-25T16:40+10:00; start=1998/01/01 scheme=W3C-DTF;",
            null, "..", "1998-09-25T16:40:00", false),

        //Scheme checks
        Arguments.of("name=The Great Depression; start=1929; end=1939; scheme=W3CDTF;",
            "The Great Depression", "1929", "1939", true),
        Arguments.of("name=The Great Depression; start=1929; end=1939; scheme=W3C-DTF;",
            "The Great Depression", "1929", "1939", true),
        Arguments.of("scheme=W3C-DTF; name=The Great Depression; start=1929; end=1939;",
            "The Great Depression", "1929", "1939", true),
        Arguments.of("name=The Great Depression; start=1929; end=1939; scheme=W3C-DTF",
            "The Great Depression", "1929", "1939", true),
        Arguments.of("name=The Great Depression; start=1929; end=1939; scheme=W3C-",
            "The Great Depression", "1929", "1939", false),

        //double fields should be false
        Arguments.of("name=The Great Depression; start=1929; end=1939; name=The Great Depression;",
            "The Great Depression", "1929", "1939", false),
        Arguments.of("name=The Great Depression; start=1929; end=1939; start=1929;",
            "The Great Depression", "1929", "1939", false),
        Arguments.of("name=The Great Depression; end=1939; start=1929; end=1939;",
            "The Great Depression", "1929", "1939", false),

        //Both start and end null then false
        Arguments.of("name=The Great Depression; start=; end=;",
            "The Great Depression", "1929", "1939", false),
        Arguments.of("name=The Great Depression;",
            "The Great Depression", "1929", "1939", false),

        //One end bounded
        Arguments.of("name=The Great Depression; start=; end=1939;",
            "The Great Depression", "..", "1939", true),
        Arguments.of("name=The Great Depression; start=1929; end=;",
            "The Great Depression", "1929", "..", true),

        //Full date
        Arguments.of("name=Haagse International Arts Festival, 2000; start=2000-01-26; end=2000-02-20;",
            "Haagse International Arts Festival, 2000", "2000-01-26", "2000-02-20", true),

        //Full date and time
        Arguments.of("start=1999-09-25T14:20:00+10:00; end=1999-09-25T16:40:00+10:00; scheme=W3C-DTF;",
            null, "1999-09-25T14:20:00", "1999-09-25T16:40:00", true),
        Arguments.of("start=1999-09-25T14:20:00+10:00;  scheme=W3C-DTF;",
            null, "1999-09-25T14:20:00", "..", true),
        Arguments.of("end=1999-09-25T16:40:00+10:00; scheme=W3C-DTF;",
            null, "..", "1999-09-25T16:40:00", true),

        //Missing semicolon
        Arguments.of("end=1998-09-25T16:40:00+10:00; start=1998 scheme=W3C-DTF;",
            null, "..", "1998-09-25T16:40:00", true),

        //Invalid date
        Arguments.of("end=1998-09-25T16:40+10:00; start=1998-1986; scheme=W3C-DTF;",
            null, "..", "1998-09-25T16:40:00", false),

        //Spaces at the end of the name are cleaned up
        Arguments.of("name=The Great Depression          ; start=1929; end=1939;",
            "The Great Depression", "1929", "1939", true),

        //Spaces at the beginning of the name are cleaned up
        Arguments.of("name=The Great Depression; start=1929; end=1939;",
            "The Great Depression", "1929", "1939", true),

        //Normal case
        Arguments.of("name=The Great Depression; start=1929; end=1939;",
            "The Great Depression", "1929", "1939", true)
    );
  }

  @ParameterizedTest
  @MethodSource("extractData")
  @DisplayName("Extract DCMI Period")
  void extract(String actualDcmiPeriod,
      String expectedLabel, String expectedStartDate,
      String expectedEndDate,
      Boolean isSuccess) {
    DcmiPeriodDateExtractor periodDateExtractor = new DcmiPeriodDateExtractor();
    DateNormalizationResult result = periodDateExtractor.extract(actualDcmiPeriod);
    if (isSuccess) {
      IntervalEdtfDate interval = (IntervalEdtfDate) result.getEdtfDate();
      assertEquals(expectedLabel, interval.getLabel());
      assertEquals(expectedStartDate, interval.getStart() != null ? interval.getStart().toString() : null);
      assertEquals(expectedEndDate, interval.getEnd() != null ? interval.getEnd().toString() : null);
      assertEquals(DCMI_PERIOD, result.getDateNormalizationExtractorMatchId());
      assertTrue(result.isCompleteDate());
    } else {
      assertNull(result);
    }
  }
}
