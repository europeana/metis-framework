package eu.europeana.normalization.dates.edtf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.ParseException;
import java.time.format.DateTimeParseException;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class Iso8601ParserTest {

  private Iso8601Parser iso8601Parser = new Iso8601Parser();

  @ParameterizedTest
  @MethodSource
  @DisplayName("[year][“-”][month][“-”][day] Complete representation")
  void completeDateRepresentation(String input, String expected) throws ParseException {
    parse(input, expected);
  }

  @ParameterizedTest
  @MethodSource
  @DisplayName("[year][“-”][month] Reduced precision for year and month")
  void reducedPrecisionForYearAndMonth(String input, String expected) throws ParseException {
    parse(input, expected);
  }

  @ParameterizedTest
  @MethodSource
  @DisplayName("[year] Reduced precision for year")
  void reducedPrecisionForYear(String input, String expected) throws ParseException {
    parse(input, expected);
  }

  @ParameterizedTest
  @MethodSource
  void dateAndTimeRepresentation(String input, String expected) throws ParseException {
    parse(input, expected);
  }

  //  EDTF Level 0 adopts representations of a time interval where both the start and end are dates: start and end date only; that is, both start and duration, and duration and end, are excluded. Time of day is excluded.

  private void parse(String input, String expected) throws ParseException {
    if (expected == null) {
      assertThrows(DateTimeParseException.class, () -> iso8601Parser.parseDatePart(input));
    } else {
      assertEquals(expected, iso8601Parser.parseDatePart(input).toString());
    }
  }

  private static Stream<Arguments> completeDateRepresentation() {
    return Stream.of(
        Arguments.of("1989-11-01", "1989-11-01"),
        Arguments.of("0989-11-01", "0989-11-01"),
        Arguments.of("0989-11-01", "0989-11-01"),
        //Digits missing on year
        Arguments.of("198-11-01", null),
        //Digits missing on month or day
        Arguments.of("1989-11-1", null),
        Arguments.of("1989-1-01", null),
        //Anything other than hyphen "-" is not valid
        Arguments.of("1989/11/01", null)
    );
  }

  private static Stream<Arguments> reducedPrecisionForYearAndMonth() {
    return Stream.of(
        Arguments.of("1989-11", "1989-11"),
        Arguments.of("0989-11", "0989-11"),
        //Digits missing on year
        Arguments.of("198-11", null),
        //Digits missing on month
        Arguments.of("1989-1", null),
        //Anything other than hyphen "-" is not valid
        Arguments.of("1989/11", null)
    );
  }

  private static Stream<Arguments> reducedPrecisionForYear() {
    return Stream.of(
        Arguments.of("1989", "1989"),
        Arguments.of("0989", "0989"),
        //Digits missing on year
        Arguments.of("198", null)
    );
  }

  private static Stream<Arguments> dateAndTimeRepresentation() {
    return Stream.of(
        //Complete representations for calendar date and (local) time of day
        Arguments.of("1989-11-01T23:59:59", "1989-11-01"),
        Arguments.of("1989-11-01T23:59", "1989-11-01"),
        Arguments.of("1989-11-01T23", "1989-11-01"),
        Arguments.of("1989-11-01T", "1989-11-01"),
        Arguments.of("1989-11-01T23:59:5", "1989-11-01"),
        Arguments.of("1989-11-01T23:5:59", "1989-11-01"),
        Arguments.of("1989-11-01t23:59:59", null),
        Arguments.of("1989-11-01 23:59:59", null),

        //Complete representations for calendar date and UTC time of day
        Arguments.of("1989-11-01T23:59:59Z", "1989-11-01"),
        Arguments.of("1989-11-01t23:59:59Z", null),
        Arguments.of("1989-11-01 23:59:59Z", null),

        //Date and time with timeshift in hours (only)
        Arguments.of("1989-11-01T23:59:59-04", "1989-11-01"),
        Arguments.of("1989-11-01T23:59:59+04", "1989-11-01"),
        Arguments.of("1989-11-01t23:59:59-04", null),
        Arguments.of("1989-11-01 23:59:59-04", null),

        //Date and time with timeshift in hours and minutes
        Arguments.of("1989-11-01T23:59:59-04:44", "1989-11-01"),
        Arguments.of("1989-11-01T23:59:59+04:44", "1989-11-01"),
        Arguments.of("1989-11-01t23:59:59-04:44", null),
        Arguments.of("1989-11-01 23:59:59-04:44", null)
    );
  }

}