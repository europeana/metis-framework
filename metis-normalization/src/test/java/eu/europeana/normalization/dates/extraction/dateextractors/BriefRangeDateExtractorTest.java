package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.edtf.DateBoundaryType.OPEN;
import static eu.europeana.normalization.dates.edtf.DateBoundaryType.UNKNOWN;
import static eu.europeana.normalization.dates.edtf.DateQualification.APPROXIMATE;
import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static eu.europeana.normalization.dates.edtf.DateQualification.UNCERTAIN;
import static eu.europeana.normalization.dates.edtf.DateQualification.UNCERTAIN_APPROXIMATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
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

  private static final BriefRangeDateExtractor BRIEF_RANGE_DATE_EXTRACTOR = new BriefRangeDateExtractor();

  private void assertExtract(String input, String expected) {
    final DateNormalizationResult dateNormalizationResult = BRIEF_RANGE_DATE_EXTRACTOR.extractDateProperty(input,
        NO_QUALIFICATION);
    if (expected == null) {
      assertEquals(DateNormalizationResultStatus.NO_MATCH, dateNormalizationResult.getDateNormalizationResultStatus());
    } else {
      AbstractEdtfDate edtfDate = dateNormalizationResult.getEdtfDate();
      if (edtfDate instanceof IntervalEdtfDate) {
        final String actual = dateNormalizationResult.getEdtfDate().toString();
        assertEquals(expected, actual);
      } else {
        assertEdtfDate(expected, (InstantEdtfDate) dateNormalizationResult.getEdtfDate());
      }
      assertEquals(expected, edtfDate.toString());
      assertEquals(DateNormalizationExtractorMatchId.BRIEF_DATE_RANGE,
          dateNormalizationResult.getDateNormalizationExtractorMatchId());
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
        //Slash
        of("1989/90", "1989/1990"),
        of("1989/90?", "1989/1990?"),
        of("-1989/-88", "-1989/-1988"),
        of("-1989/-88?", "-1989/-1988?"),
        of("-1989/-13", "-1989/-1913"),

        //Dash not supported
        of("1989-90", null),
        of("1989-90?", null),
        of("1989-90", null),
        of("989-90", null),

        //End date lower rightmost two digits than start year
        of("1989/89", null),
        of("1989/88", null),
        of("1989-89", null),
        of("1989-88", null),

        //More than two digits on end year not allowed
        of("1989/990", null),
        of("1989-990", null),

        //End year cannot be lower or equal than +-12
        of("1900/01", null),
        of("1900/12", null),
        of("-1989/-12", null),

        //Less than three digits on start year
        of("89-90", null)
    );
  }

}
