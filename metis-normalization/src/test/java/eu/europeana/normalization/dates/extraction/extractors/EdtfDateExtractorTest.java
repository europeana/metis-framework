package eu.europeana.normalization.dates.extraction.extractors;

import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EdtfDateExtractorTest implements DateExtractorTest {

  private static final EdtfDateExtractor EDTF_DATE_EXTRACTOR = new EdtfDateExtractor();

  private void assertExtract(String input, String expected) {
    final DateNormalizationResult dateNormalizationResult = EDTF_DATE_EXTRACTOR.extractDateProperty(input);
    assertDateNormalizationResult(dateNormalizationResult, expected, DateNormalizationExtractorMatchId.EDTF);
  }

  @ParameterizedTest
  @MethodSource
  @DisplayName("[year][“-”][month][“-”][day] Complete representation")
  void completeDateRepresentationLevel0(String input, String expected) {
    assertExtract(input, expected);
  }

  @ParameterizedTest
  @MethodSource
  @DisplayName("[year][“-”][month] Reduced precision for year and month")
  void reducedPrecisionForYearAndMonthLevel0(String input, String expected) {
    assertExtract(input, expected);
  }

  @ParameterizedTest
  @MethodSource
  @DisplayName("[year] Reduced precision for year")
  void reducedPrecisionForYearLevel0(String input, String expected) {
    assertExtract(input, expected);
  }

  @ParameterizedTest
  @MethodSource
  @DisplayName("Letter-prefixed calendar year")
  void letterPrefixedCalendarYearLevel1(String input, String expected) {
    assertExtract(input, expected);
  }

  @ParameterizedTest
  @MethodSource
  @DisplayName("The characters '?', '~' and '%' are used to mean \"uncertain\", \"approximate\", and \"uncertain\" as well as \"approximate\", respectively")
  void dateQualificationLevel1(String input, String expected) {
    assertExtract(input, expected);
  }

  @ParameterizedTest
  @MethodSource
  @DisplayName("Negative Calendar Year")
  void negativeCalendarYearLevel1(String input, String expected) {
    assertExtract(input, expected);
  }

  private static Stream<Arguments> completeDateRepresentationLevel0() {
    return Stream.of(
        of("1989-11-01", "1989-11-01"),
        of("0989-11-01", "0989-11-01"),
        of("0989-11-01", "0989-11-01"),
        //Digits missing on year
        of("198-11-01", null),
        //Digits missing on month or day
        of("1989-11-1", null),
        of("1989-1-01", null),
        //Anything other than hyphen "-" is not valid
        of("1989/11/01", null),

        //Complete representations for calendar date and (local) time of day
        of("1989-11-01T23:59:59", "1989-11-01"),
        of("1989-11-01T23:59", "1989-11-01"),
        of("1989-11-01T23", "1989-11-01"),
        of("1989-11-01T", "1989-11-01"),
        of("1989-11-01T23:59:5", "1989-11-01"),
        of("1989-11-01T23:5:59", "1989-11-01"),
        of("1989-11-01t23:59:59", null),
        of("1989-11-01 23:59:59", null),

        //Complete representations for calendar date and UTC time of day
        of("1989-11-01T23:59:59Z", "1989-11-01"),
        of("1989-11-01t23:59:59Z", null),
        of("1989-11-01 23:59:59Z", null),

        //Date and time with time shift in hours (only)
        of("1989-11-01T23:59:59-04", "1989-11-01"),
        of("1989-11-01T23:59:59+04", "1989-11-01"),
        of("1989-11-01t23:59:59-04", null),
        of("1989-11-01 23:59:59-04", null),

        //Date and time with time shift in hours and minutes
        of("1989-11-01T23:59:59-04:44", "1989-11-01"),
        of("1989-11-01T23:59:59+04:44", "1989-11-01"),
        of("1989-11-01t23:59:59-04:44", null),
        of("1989-11-01 23:59:59-04:44", null)
    );
  }

  private static Stream<Arguments> reducedPrecisionForYearAndMonthLevel0() {
    return Stream.of(
        of("1989-11", "1989-11"),
        of("0989-11", "0989-11"),
        //Digits missing on year
        of("198-11", null),
        //Digits missing on month
        of("1989-1", null),
        //Anything other than hyphen "-" is not valid
        of("1989/11", null)
    );
  }

  private static Stream<Arguments> reducedPrecisionForYearLevel0() {
    return Stream.of(
        of("1989", "1989"),
        of("0989", "0989"),
        //Digits missing on year
        of("198", null)
    );
  }

  private static Stream<Arguments> letterPrefixedCalendarYearLevel1() {
    return Stream.of(
        of("Y-123456789", "Y-123456789"),
        //Non prefixed
        of("-123456789", null),
        //Future dates are not valid
        of("Y123456789", null),
        //Month and day not valid
        of("Y123456789/11/01", null),
        //Overflow, max is +-999999999
        of("Y1234567890", null),
        of("Y-1234567890", null),
        //Too low values
        of("Y0", null),
        of("Y1", null),
        of("Y-1", null),
        of("Y", null),
        of("YnonValidNumber", null)
    );
  }

  private static Stream<Arguments> dateQualificationLevel1() {
    return Stream.of(
        of("1989?", "1989?"),
        of("1989~", "1989~"),
        of("1989-11?", "1989-11?"),
        of("1989-11~", "1989-11~"),
        of("1989-11-01%", "1989-11-01%")
    );
  }

  private static Stream<Arguments> negativeCalendarYearLevel1() {
    return Stream.of(
        of("-1989", "-1989"),
        of("-9999", "-9999"),
        of("-0989", "-0989"),
        of("-11989", null)
    );
  }
}