package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.edtf.DateBoundaryType.OPEN;
import static eu.europeana.normalization.dates.edtf.DateBoundaryType.UNKNOWN;
import static eu.europeana.normalization.dates.edtf.DateQualification.APPROXIMATE;
import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static eu.europeana.normalization.dates.edtf.DateQualification.UNCERTAIN;
import static eu.europeana.normalization.dates.edtf.DateQualification.UNCERTAIN_APPROXIMATE;
import static eu.europeana.normalization.dates.edtf.IntervalEdtfDate.DATE_INTERVAL_SEPARATOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.DateNormalizationResultStatus;
import eu.europeana.normalization.dates.edtf.AbstractEdtfDate;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BriefRangeDateExtractorTest {

  private final BriefRangeDateExtractor briefRangeDateExtractor = new BriefRangeDateExtractor();

  private void assertExtract(String input, String expected) {
    final DateNormalizationResult dateNormalizationResult = briefRangeDateExtractor.extractDateProperty(input, NO_QUALIFICATION);
    if (expected == null) {
      assertEquals(DateNormalizationResultStatus.NO_MATCH, dateNormalizationResult.getDateNormalizationResultStatus());
    } else {
      AbstractEdtfDate edtfDate = dateNormalizationResult.getEdtfDate();
      if (edtfDate instanceof IntervalEdtfDate) {
        String startPart = expected.substring(0, expected.indexOf(DATE_INTERVAL_SEPARATOR));
        String endPart = expected.substring(expected.indexOf(DATE_INTERVAL_SEPARATOR) + 1);
        InstantEdtfDate start = ((IntervalEdtfDate) edtfDate).getStart();
        InstantEdtfDate end = ((IntervalEdtfDate) edtfDate).getEnd();
        assertEdtfDate(startPart, start);
        assertEdtfDate(endPart, end);
      } else {
        assertEdtfDate(expected, (InstantEdtfDate) dateNormalizationResult.getEdtfDate());
      }
      assertEquals(expected, edtfDate.toString());
    }
  }

  private static void assertEdtfDate(String expected, InstantEdtfDate instantEdtfDate) {
    assertEquals(expected.contains("?"), instantEdtfDate.getDateQualification() == UNCERTAIN);
    assertEquals(expected.contains("~"), instantEdtfDate.getDateQualification() == APPROXIMATE);
    assertEquals(expected.contains("%"), instantEdtfDate.getDateQualification() == UNCERTAIN_APPROXIMATE);
    assertEquals(expected.equals(OPEN.getSerializedRepresentation()),
        instantEdtfDate.getDateBoundaryType() == OPEN || instantEdtfDate.getDateBoundaryType() == UNKNOWN);
  }

  @ParameterizedTest
  @MethodSource
  void extractBrief(String input, String expected) {
    assertExtract(input, expected);
  }

  private static Stream<Arguments> extractBrief() {
    return Stream.of(
        of("1989/90", "1989/1990"),
        of("1989/90?", "1989?/1990?"),
        of("1989-90", "1989/1990"),
        of("1989-90?", "1989?/1990?"),
        of("1900-13", "1900/1913"),

        //End date lower rightmost two digits than start year
        of("1989/89", null),
        of("1989/88", null),
        of("1989-89", null),
        of("1989-88", null),

        //More than two digits on end year not allowed
        of("1989/990", null),
        of("1989-990", null),

        //End year cannot be lower or equal than 12
        of("1900/01", null),
        of("1900-12", null),

        //Less than three digits on start year
        of("89-90", null)
    );
  }

}