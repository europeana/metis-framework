package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.BRIEF_DATE_RANGE;
import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationResult;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BriefRangeDateExtractorTest implements DateExtractorTest {

  private static final BriefRangeDateExtractor BRIEF_RANGE_DATE_EXTRACTOR = new BriefRangeDateExtractor();

  private void assertExtract(String input, String expected) {
    final DateNormalizationResult dateNormalizationResult = BRIEF_RANGE_DATE_EXTRACTOR.extractDateProperty(input,
        NO_QUALIFICATION);
    assertDateNormalizationResult(dateNormalizationResult, expected, BRIEF_DATE_RANGE);
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
