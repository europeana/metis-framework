package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.DCMI_PERIOD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link DcmiPeriodDateExtractor} class
 */
class DcmiPeriodDateExtractorTest {

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
}
