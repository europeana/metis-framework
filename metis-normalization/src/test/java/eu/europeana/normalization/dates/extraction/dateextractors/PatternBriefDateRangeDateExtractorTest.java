package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.edtf.IntervalEdtfDate.DATES_SEPARATOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.DateNormalizationResultStatus;
import eu.europeana.normalization.dates.edtf.AbstractEdtfDate;
import eu.europeana.normalization.dates.edtf.DateEdgeType;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PatternBriefDateRangeDateExtractorTest {

  private final PatternBriefDateRangeDateExtractor patternBriefDateRangeDateExtractor = new PatternBriefDateRangeDateExtractor();

  private void extract(String input, String expected) {
    final DateNormalizationResult dateNormalizationResult = patternBriefDateRangeDateExtractor.extractDateProperty(input);
    if (expected == null) {
      assertEquals(DateNormalizationResultStatus.NO_MATCH, dateNormalizationResult.getDateNormalizationResultStatus());
    } else {
      AbstractEdtfDate edtfDate = dateNormalizationResult.getEdtfDate();
      if (edtfDate instanceof IntervalEdtfDate) {
        String startPart = expected.substring(0, expected.indexOf(DATES_SEPARATOR));
        String endPart = expected.substring(expected.indexOf(DATES_SEPARATOR) + 1);
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
    assertEquals(expected.contains("?"), instantEdtfDate.getDateQualification() == DateQualification.UNCERTAIN);
    assertEquals(expected.contains("~"), instantEdtfDate.getDateQualification() == DateQualification.APPROXIMATE);
    assertEquals(expected.contains("%"), instantEdtfDate.getDateQualification() == DateQualification.UNCERTAIN_APPROXIMATE);
    assertEquals(expected.equals(DateEdgeType.OPEN.getSerializedRepresentation()),
        instantEdtfDate.getDateEdgeType() == DateEdgeType.OPEN || instantEdtfDate.getDateEdgeType() == DateEdgeType.UNKNOWN);
  }

  @ParameterizedTest
  @MethodSource
  void extractBrief(String input, String expected) {
    extract(input, expected);
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
        of("1900-12", null)

    );
  }

}