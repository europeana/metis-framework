package eu.europeana.normalization.dates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.extraction.dateextractors.PatternEdtfDateExtractor;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DateLevel0Test {

  private PatternEdtfDateExtractor patternEdtfDateExtractor = new PatternEdtfDateExtractor();

  private void extract(String input, String expected) {
    final DateNormalizationResult dateNormalizationResult = patternEdtfDateExtractor.extractDateProperty(input);
    if (expected == null) {
      assertNull(dateNormalizationResult);
    } else {
      assertEquals(expected, dateNormalizationResult.getEdtfDate().toString());
    }
  }

  @ParameterizedTest
  @MethodSource
  @DisplayName("[year][“-”][month][“-”][day] Complete representation")
  void completeDateRepresentation(String input, String expected) {
    extract(input, expected);
  }

  @ParameterizedTest
  @MethodSource
  @DisplayName("[year][“-”][month] Reduced precision for year and month")
  void reducedPrecisionForYearAndMonth(String input, String expected) {
    extract(input, expected);
  }

  @ParameterizedTest
  @MethodSource
  @DisplayName("[year] Reduced precision for year")
  void reducedPrecisionForYear(String input, String expected) {
    extract(input, expected);
  }

  @ParameterizedTest
  @MethodSource
  void dateAndTimeRepresentation(String input, String expected) {
    extract(input, expected);
  }

  @ParameterizedTest
  @MethodSource
  void dateIntervalRepresentation(String input, String expected) {
    extract(input, expected);
  }

  private static Stream<Arguments> completeDateRepresentation() {
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
        of("1989/11/01", null)
    );
  }

  private static Stream<Arguments> reducedPrecisionForYearAndMonth() {
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

  private static Stream<Arguments> reducedPrecisionForYear() {
    return Stream.of(
        of("1989", "1989"),
        of("0989", "0989"),
        //Digits missing on year
        of("198", null)
    );
  }

  private static Stream<Arguments> dateAndTimeRepresentation() {
    return Stream.of(
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

        //Date and time with timeshift in hours (only)
        of("1989-11-01T23:59:59-04", "1989-11-01"),
        of("1989-11-01T23:59:59+04", "1989-11-01"),
        of("1989-11-01t23:59:59-04", null),
        of("1989-11-01 23:59:59-04", null),

        //Date and time with timeshift in hours and minutes
        of("1989-11-01T23:59:59-04:44", "1989-11-01"),
        of("1989-11-01T23:59:59+04:44", "1989-11-01"),
        of("1989-11-01t23:59:59-04:44", null),
        of("1989-11-01 23:59:59-04:44", null)
    );
  }

  private static Stream<Arguments> dateIntervalRepresentation() {
    return Stream.of(
        of("1989/1990", "1989/1990"),
        of("1989-11/1990-11", "1989-11/1990-11"),
        of("1989-11-01/1990-11-01", "1989-11-01/1990-11-01"),
        of("1989-11-01/1990-11", "1989-11-01/1990-11"),
        of("1989-11-01/1990", "1989-11-01/1990"),
        of("1989/1990-11", "1989/1990-11"),
        of("1989/1990-11-01", "1989/1990-11-01"),
        of("1989-00/1990-00", null),
        of("1989-00-00/1990-00-00", null),
        //Spaces not valid
        of("1989 / 1990", null),
        //Dash not valid
        of("1989-1990", null),
        //Missing digits
        of("989-1990", null),
        of("1989-990", null)
    );
  }
}
