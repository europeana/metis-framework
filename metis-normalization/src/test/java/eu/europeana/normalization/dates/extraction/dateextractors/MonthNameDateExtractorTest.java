package eu.europeana.normalization.dates.extraction.dateextractors;

import static eu.europeana.normalization.dates.DateNormalizationExtractorMatchId.MONTH_NAME;
import static eu.europeana.normalization.dates.edtf.DateQualification.NO_QUALIFICATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.DateNormalizationResultStatus;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MonthNameDateExtractorTest {

  private static final MonthNameDateExtractor PATTERN_MONTH_NAME_DATE_EXTRACTOR = new MonthNameDateExtractor();

  @ParameterizedTest
  @MethodSource
  void extractDayMonthYear(String input, String expected) {
    assertExtract(input, expected, MONTH_NAME);
  }

  @ParameterizedTest
  @MethodSource
  void extractMonthDayYear(String input, String expected) {
    assertExtract(input, expected, MONTH_NAME);
  }

  @ParameterizedTest
  @MethodSource
  void extractMonthYear(String input, String expected) {
    assertExtract(input, expected, MONTH_NAME);
  }

  void assertExtract(String input, String expected, DateNormalizationExtractorMatchId dateNormalizationExtractorMatchId) {
    final DateNormalizationResult dateNormalizationResult = PATTERN_MONTH_NAME_DATE_EXTRACTOR.extractDateProperty(input,
        NO_QUALIFICATION);
    if (expected == null) {
      assertEquals(DateNormalizationResultStatus.NO_MATCH, dateNormalizationResult.getDateNormalizationResultStatus());
    } else {
      final String actual = dateNormalizationResult.getEdtfDate().toString();
      assertEquals(expected, actual);
      assertEquals(NO_QUALIFICATION, dateNormalizationResult.getEdtfDate().getDateQualification());
      assertEquals(dateNormalizationExtractorMatchId, dateNormalizationResult.getDateNormalizationExtractorMatchId());
    }
  }

  private static Stream<Arguments> extractDayMonthYear() {

    return Stream.of(
        //DAY-MONTH-YEAR
        of("01 November 1989", "1989-11-01"),
        of("32 November 1989", null),
        of("01.November.1989", "1989-11-01"),
        of("01,November,1989", "1989-11-01"),
        //Combination of separators
        of("01 November.1989", "1989-11-01"),
        of("01 November,1989", "1989-11-01"),
        of("01.November 1989", "1989-11-01"),
        of("01.November,1989", "1989-11-01"),
        of("01,November 1989", "1989-11-01"),
        of("01,November.1989", "1989-11-01"),

        //Some other languages or name formats
        of("01 nov. 1989", "1989-11-01"),
        of("01 ное 1989", "1989-11-01"),
        of("01 Νοεμβρίου 1989", "1989-11-01"),
        of("01 January 1989", "1989-01-01"),

        //Incorrect month
        of("99 November 9989", null),
        of("99 November 9989", null),

        //Too few digits on year
        of("1 January 989", null),
        of("1.January.989", null),
        of("1,January,989", null),
        //Too many digits on year
        of("01 January 12345", null),
        //Too many digits on day
        of("123 January 1234", null),

        //Other invalids
        //Double spaces should not match
        of("1989  November  01", null),
        //Double dots should not match
        of("1989..November..01", null),
        //Double commas should not match
        of("1989,,November,,01", null)
    );
  }

  private static Stream<Arguments> extractMonthDayYear() {

    return Stream.of(
        //DAY-MONTH-YEAR
        of("November 01 1989", "1989-11-01"),
        of("November 32 1989", null),
        of("November.01.1989", "1989-11-01"),
        of("November,01,1989", "1989-11-01"),
        //Combination of separators
        of("November 01.1989", "1989-11-01"),
        of("November 01,1989", "1989-11-01"),
        of("November.01 1989", "1989-11-01"),
        of("November.01,1989", "1989-11-01"),
        of("November,01 1989", "1989-11-01"),
        of("November,01.1989", "1989-11-01"),

        //Some other languages or name formats
        of("nov. 01 1989", "1989-11-01"),
        of("ное 01 1989", "1989-11-01"),
        of("Νοεμβρίου 01 1989", "1989-11-01"),
        of("January 01 1989", "1989-01-01"),

        //Incorrect month
        of("November 99 9989", null),
        of("November 99 9989", null),

        //Too few digits on year
        of("January 1 989", null),
        of("January.1.989", null),
        of("January,1,989", null),
        //Too many digits on year
        of("January 01 12345", null),
        //Too many digits on day
        of("January 123 1234", null),

        //Other invalids
        //Double spaces should not match
        of("November  01  1989", null),
        //Double dots should not match
        of("November..01..1989", null),
        //Double commas should not match
        of("November,,01,,1989", null)
    );
  }

  // TODO: 29/08/2023 Add "of" cases other formats of month name and further tests
  private static Stream<Arguments> extractMonthYear() {

    return Stream.of(
        //MONTH-YEAR
        //        of("November 1989", "1989-11"),
        //        of("November.1989", "1989-11"),
        //        of("November,1989", "1989-11"),
        //
        //        //Some other languages or name formats
        //        of("nov. 1989", "1989-11"),
        //        of("ное 1989", "1989-11"),
        //        of("Νοεμβρίου 1989", "1989-11"),
        //        of("January 1989", "1989-01"),
        //Incorrect month year
        of("November 9989", null),
        of("November 9989", null)
        //Too few digits on year
        //        of("January 989", null),
        //        of("January.989", null),
        //        of("January,989", null),
        //        //Too many digits on year
        //        of("January 12345", null),
        //
        //        //Other invalids
        //        //Double spaces should not match
        //        of("November  1989", null),
        //        //Double dots should not match
        //        of("November..1989", null),
        //        //Double commas should not match
        //        of("November,,1989", null)
    );
  }

}
