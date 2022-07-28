package eu.europeana.normalization.dates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.normalization.dates.edtf.EdtfParser;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import java.text.ParseException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests to validate EDTF format parser Level 1
 *
 * @see <a href="https://www.loc.gov/standards/datetime/">EDTF library specification</a>
 */
public class DateLevel1Test {

  private EdtfParser parser = new EdtfParser();

  private static Stream<Arguments> letterPrefixedCalendarYear() {
    return Stream.of(Arguments.of("Y170000002", 170000002, true),
        Arguments.of("Y-170000002", -170000002, true),
        Arguments.of("Y1700000002", 1700000002, true),
        //Arguments.of("Y17000000002", 17000000002L, true), //TODO: Long OF Integer?
        Arguments.of("Y0", 0, true),
        Arguments.of("Y1", 1, true),
        Arguments.of("Y-1", -1, true)
        //Arguments.of("Y", 0, false) //TODO: Is this correct to have?
    );
  }

  @ParameterizedTest
  @MethodSource("letterPrefixedCalendarYear")
  @DisplayName("Letter-prefixed calendar year")
  void testLetterPrefixedCalendarYear(String actualDate, Integer expectedYear, Boolean isSuccess) throws ParseException {
    if (isSuccess) {
      InstantEdtfDate actual = (InstantEdtfDate) parser.parse(actualDate);

      assertEquals(expectedYear, actual.getEdtfDatePart().getYear());
    } else {
      assertThrows(ParseException.class, () -> parser.parse(actualDate));
    }
  }

  private static Stream<Arguments> seasons() {
    return Stream.of(
        Arguments.of("2022-21", "Spring, 2022", true), //TODO: Seasons is not supported as Level1
        Arguments.of("2022-22", "Summer, 2022", true),
        Arguments.of("2022-23", "Autumn, 2022", true),
        Arguments.of("2022-24", "Winter, 2022", true),
        Arguments.of("2022-25", null, false)
    );
  }

  @ParameterizedTest
  @MethodSource("seasons")
  @Disabled
  @DisplayName("The values 21, 22, 23, 24 may be used used to signify ' Spring', 'Summer', 'Autumn', 'Winter', respectively")
  void testSeasons(String actualDate, String expectedDate, Boolean isSuccess) throws ParseException {
    if (isSuccess) {
      InstantEdtfDate actual = (InstantEdtfDate) parser.parse(actualDate);

      assertEquals(expectedDate, actual.toString());
    } else {
      assertThrows(ParseException.class, () -> parser.parse(actualDate));
    }
  }

  private static Stream<Arguments> qualificationOfADate() {
    return Stream.of(
        Arguments.of("1986?", 1986, null, null, true, false, true),
        Arguments.of("1986~", 1986, null, null, false, true, true),
        Arguments.of("1986-07?", 1986, 7, null, true, false, true),
        Arguments.of("1986-07~", 1986, 7, null, false, true, true),
        Arguments.of("1986-07-12%", 1986, 7, 12, true, true, true)
    );
  }

  @ParameterizedTest
  @MethodSource("qualificationOfADate")
  @DisplayName("The characters '?', '~' and '%' are used to mean \"uncertain\", \"approximate\", and \"uncertain\" as well as \"approximate\"")
  void testQualificationOfADate(String actualDate,
      Integer expectedYear,
      Integer expectedMonth,
      Integer expectedDay,
      Boolean expectedUncertainty,
      Boolean expectedApproximate,
      Boolean isSuccess) throws ParseException {
    if (isSuccess) {
      InstantEdtfDate actual = (InstantEdtfDate) parser.parse(actualDate);

      assertEquals(expectedYear, actual.getEdtfDatePart().getYear());
      assertEquals(expectedMonth, actual.getEdtfDatePart().getMonth());
      assertEquals(expectedDay, actual.getEdtfDatePart().getDay());
      assertEquals(expectedUncertainty, actual.isUncertain());
      assertEquals(expectedApproximate, actual.isApproximate());
    } else {
      assertThrows(ParseException.class, () -> parser.parse(actualDate));
    }
  }

  private static Stream<Arguments> unspecifiedDigitsFromTheRight() {
    return Stream.of(
        //TODO: check if all this patterns of L1 should be accepted. now they give exception
        Arguments.of("201X", 2010, null, null, false),
        Arguments.of("20XX", 2000, null, null, false),
        Arguments.of("1986-XX", 1986, null, null, false),
        Arguments.of("1986-07-XX", 1986, 7, null, false),
        Arguments.of("1986-XX-XX", 1986, 7, 12, false)
    );
  }

  @ParameterizedTest
  @MethodSource("unspecifiedDigitsFromTheRight")
  @DisplayName("The character 'X' may be used in place of one or more rightmost digits to indicate that the value of that digit is unspecified")
  void testUnspecifiedDigitsFromTheRight(String actualDate,
      Integer expectedYear,
      Integer expectedMonth,
      Integer expectedDay,
      Boolean isSuccess) throws ParseException {
    if (isSuccess) {
      InstantEdtfDate actual = (InstantEdtfDate) parser.parse(actualDate);

      assertEquals(expectedYear, actual.getEdtfDatePart().getYear());
      assertEquals(expectedMonth, actual.getEdtfDatePart().getMonth());
      assertEquals(expectedDay, actual.getEdtfDatePart().getDay());

    } else {
      assertThrows(ParseException.class, () -> parser.parse(actualDate));
    }
  }

  private static Stream<Arguments> negativeCalendarYear() {
    return Stream.of(Arguments.of("-1986", -1986, true),
        Arguments.of("-2086", -2086, true),
        Arguments.of("-9999", -9999, true),
        Arguments.of("-0986", -986, true),
        Arguments.of("-10986", -10986, false));
  }

  @ParameterizedTest
  @MethodSource("negativeCalendarYear")
  void testNegativeCalendarYear(String actualDate,
      Integer expectedYear,
      Boolean isSuccess) throws ParseException {
    if (isSuccess) {
      InstantEdtfDate actual = (InstantEdtfDate) parser.parse(actualDate);

      assertEquals(expectedYear, actual.getEdtfDatePart().getYear());
    } else {
      assertThrows(ParseException.class, () -> parser.parse(actualDate));
    }
  }

}
