package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link DcmiPeriodDateExtractor} class
 */
class DcmiPeriodDateExtractorTest implements DateExtractorTest {

  private static final DcmiPeriodDateExtractor DCMI_PERIOD_DATE_EXTRACTOR = new DcmiPeriodDateExtractor();

  @ParameterizedTest
  @MethodSource("extractData")
  @DisplayName("Extract DCMI Period")
  void extract(String input, String expected, String expectedLabel) {
    DateNormalizationResult dateNormalizationResult = DCMI_PERIOD_DATE_EXTRACTOR.extractDateProperty(input,
        NO_QUALIFICATION);
    assertDateNormalizationResult(dateNormalizationResult, expected, DateNormalizationExtractorMatchId.DCMI_PERIOD,
        expectedLabel);
  }

  private static Stream<Arguments> extractData() {
    return Stream.of(
        of("name=The Great Depression; start=1929; end=1939;", "1929/1939", "The Great Depression"),
        of("name=Haagse International Arts Festival, 2000; start=2000-01-26; end=2000-02-20;",
            "2000-01-26/2000-02-20", "Haagse International Arts Festival, 2000"),
        of("start=1998-09-25; end=1998-09-25; scheme=W3C-DTF;", "1998-09-25/1998-09-25", null),
        of("start=1998-09-25T14:20:00+10:00;  scheme=W3C-DTF;", "1998-09-25/..", null),
        of("end=1998-09-25T16:40:00+10:00; scheme=W3C-DTF;", "../1998-09-25", null),
        of("end=1998-09-25T16:40+10:00; start=1998/01/01 scheme=W3C-DTF;", "../1998-09-25", null),

        //Scheme checks
        of("name=The Great Depression; start=1929; end=1939; scheme=W3CDTF;", "1929/1939", "The Great Depression"),
        of("name=The Great Depression; start=1929; end=1939; scheme=W3C-DTF;", "1929/1939", "The Great Depression"),
        of("scheme=W3C-DTF; name=The Great Depression; start=1929; end=1939;", "1929/1939", "The Great Depression"),
        of("name=The Great Depression; start=1929; end=1939; scheme=W3C-DTF", "1929/1939", "The Great Depression"),
        of("name=The Great Depression; start=1929; end=1939; scheme=W3C-", null, null, null),

        //double fields should be false
        of("name=The Great Depression; start=1929; end=1939; name=The Great Depression;", null, null, null),
        of("name=The Great Depression; start=1929; end=1939; start=1929;", null, null, null),
        of("name=The Great Depression; end=1939; start=1929; end=1939;", null, null, null),

        //Both start and end null then false
        of("name=The Great Depression; start=; end=;", null, null, null),
        of("name=The Great Depression;", null, null, null),

        //One end bounded
        of("name=The Great Depression; start=; end=1939;", "../1939", "The Great Depression"),
        of("name=The Great Depression; start=1929; end=;", "1929/..", "The Great Depression"),

        //Full date
        of("name=Haagse International Arts Festival, 2000; start=2000-01-26; end=2000-02-20;",
            "2000-01-26/2000-02-20", "Haagse International Arts Festival, 2000"),

        //Full date and time
        of("start=1999-09-25T14:20:00+10:00; end=1999-09-25T16:40:00+10:00; scheme=W3C-DTF;", "1999-09-25/1999-09-25", null),
        of("start=1999-09-25T14:20:00+10:00;  scheme=W3C-DTF;", "1999-09-25/..", null),
        of("end=1999-09-25T16:40:00+10:00; scheme=W3C-DTF;", "../1999-09-25", null),

        //Missing semicolon
        of("end=1998-09-25T16:40:00+10:00; start=1998 scheme=W3C-DTF;", "../1998-09-25", null),

        //Invalid date
        of("end=1998-09-25T16:40+10:00; start=1998-1986; scheme=W3C-DTF;", null, null, null),
        //
        //Spaces at the end of the name are cleaned up
        of("name=The Great Depression          ; start=1929; end=1939;", "1929/1939", "The Great Depression"),

        //Spaces at the beginning of the name are cleaned up
        of("name=     The Great Depression; start=1929; end=1939;", "1929/1939", "The Great Depression"),

        //Name at the beginning without field name
        of("The Great Depression; start=1929; end=1939;", "1929/1939", null),

        //Name at the beginning without field name and spaces at wrapped
        of("   The Great Depression   ; start=1929; end=1939;", "1929/1939", null),

        //Normal case
        of("name=The Great Depression; start=1929; end=1939;", "1929/1939", "The Great Depression")
    );
  }
}
