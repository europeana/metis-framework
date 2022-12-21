// TODO: 21/12/2022 Rewrite this class without the time part consideration in the results

//package eu.europeana.normalization.dates;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//import eu.europeana.normalization.dates.edtf.EdtfParser;
//import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
//import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
//import java.text.ParseException;
//import java.util.stream.Stream;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.Arguments;
//import org.junit.jupiter.params.provider.MethodSource;
//
///**
// * Unit tests to validate EDTF format parser Level 0
// *
// * @see <a href="https://www.loc.gov/standards/datetime/">EDTF library specification</a>
// */
//public class DateLevel0Test {
//
//  private EdtfParser parser = new EdtfParser();
//
//  private static Stream<Arguments> dateCompleteRepresentation() {
//    return Stream.of(Arguments.of("1986-07-12", 1986, 7, 12, true),
//        Arguments.of("1986-05-08", 1986, 5, 8, true),
//        Arguments.of("1986-2-2", 1986, 2, 2, false),   //TODO: should be this valid or not?
//        Arguments.of("1986-2-21", 1986, 2, 21, false), //TODO: should be this valid or not?
//        Arguments.of("1986/02/02", 1986, 2, 2, false)); //TODO: is this kind of date valid or not yyyy/mm/dd?
//  }
//
//  @ParameterizedTest
//  @MethodSource("dateCompleteRepresentation")
//  @DisplayName("[year][“-”][month][“-”][day] Complete representation")
//  void testDateCompleteRepresentation(String actualDate,
//      Integer expectedYear,
//      Integer expectedMonth,
//      Integer expectedDay,
//      Boolean isSuccess) throws ParseException {
//
//    if (isSuccess) {
//      InstantEdtfDate actual = (InstantEdtfDate) parser.parse(actualDate);
//
//      assertEquals(expectedDay, actual.getEdtfDatePart().getDay());
//      assertEquals(expectedMonth, actual.getEdtfDatePart().getMonth());
//      assertEquals(expectedYear, actual.getEdtfDatePart().getYear());
//    } else {
//      assertThrows(ParseException.class, () -> parser.parse(actualDate));
//    }
//  }
//
//  private static Stream<Arguments> reducedPrecisionForYearAndMonth() {
//    return Stream.of(Arguments.of("1986-07", 1986, 7, true),
//        Arguments.of("1986-11", 1986, 11, true),
//        Arguments.of("1986-5", 1986, 5, false),
//        Arguments.of("1986-13", 1986, 13, true), //TODO: check why this is not an error
//        Arguments.of("1986/1", 1986, 1, false)  //TODO: do we accept slashes yyyy/mm?
//    );
//  }
//
//  @ParameterizedTest
//  @MethodSource("reducedPrecisionForYearAndMonth")
//  @DisplayName("[year][“-”][month] Reduced precision for year and month")
//  void testReducedPrecisionForYearAndMonth(String actualDate,
//      Integer expectedYear,
//      Integer expectedMonth,
//      Boolean isSuccess) throws ParseException {
//    if (isSuccess) {
//      InstantEdtfDate actual = (InstantEdtfDate) parser.parse(actualDate);
//      assertEquals(expectedMonth, actual.getEdtfDatePart().getMonth());
//      assertEquals(expectedYear, actual.getEdtfDatePart().getYear());
//    } else {
//      assertThrows(ParseException.class, () -> parser.parse(actualDate));
//    }
//  }
//
//  private static Stream<Arguments> reducedPrecisionForYear() {
//    return Stream.of(Arguments.of("1000", 1000, true),
//        Arguments.of("1258", 1258, true),
//        Arguments.of("1986", 1986, true),
//        Arguments.of("2022", 2022, true),
//        Arguments.of("9999", 9999, true),
//        Arguments.of("0999", 999, true),
//        Arguments.of("0001", 1, true),    //TODO: if it has four digits it is interpreted as year
//        Arguments.of(" 800", 800, false), //TODO: if it doesn't is valid to test 3 digit years?
//        Arguments.of("800", 800, false)
//    );
//  }
//
//  @ParameterizedTest
//  @MethodSource("reducedPrecisionForYear")
//  @DisplayName("[year] Reduced precision for year")
//  void testReducedPrecisionForYearAndMonth(String actualDate,
//      Integer expectedYear,
//      Boolean isSuccess) throws ParseException {
//    if (isSuccess) {
//      InstantEdtfDate actual = (InstantEdtfDate) parser.parse(actualDate);
//      assertEquals(expectedYear, actual.getEdtfDatePart().getYear());
//    } else {
//      assertThrows(ParseException.class, () -> parser.parse(actualDate));
//    }
//  }
//
//  private static Stream<Arguments> dateAndTimeRepresentation() {
//    return Stream.of(Arguments.of("1986-07-12T23:59:59", 1986, 7, 12, 23, 59, 59, true),
//        Arguments.of("1986-07-12T11:22:55", 1986, 7, 12, 11, 22, 55, true),
//        Arguments.of("1986-07-12T00:00:00", 1986, 7, 12, 0, 0, 0, true),
//        Arguments.of("1986-07-12T23:59", 1986, 7, 12, 23, 59, null, true),
//        Arguments.of("1986-07-12T23", 1986, 7, 12, 23, null, null, true),
//        Arguments.of("1986-07-12T1:20:30", 1986, 7, 12, 1, 20, 30, false),
//        Arguments.of("1986-07-12 11:20:30", 1986, 7, 12, 11, 20, 30, false),
//        Arguments.of("1986-07-12t11:20:30", 1986, 7, 12, 11, 20, 30, false),
//        Arguments.of("1986-07-12T01:1:30", 1986, 7, 12, 1, 1, 30,
//            false)); //TODO: Check the if (false) cases are or not valid to test
//  }
//
//  @ParameterizedTest
//  @MethodSource("dateAndTimeRepresentation")
//  @DisplayName("[date][“T”][time] Complete representations for calendar date and (local) time of day")
//  void testDateAndTime(String actualDate,
//      Integer expectedYear,
//      Integer expectedMonth,
//      Integer expectedDay,
//      Integer expectedHour,
//      Integer expectedMinute,
//      Integer expectedSecond,
//      Boolean isSuccess) throws ParseException {
//    if (isSuccess) {
//      InstantEdtfDate actual = (InstantEdtfDate) parser.parse(actualDate);
//
//      assertEquals(expectedDay, actual.getEdtfDatePart().getDay());
//      assertEquals(expectedMonth, actual.getEdtfDatePart().getMonth());
//      assertEquals(expectedYear, actual.getEdtfDatePart().getYear());
////      assertEquals(expectedHour, actual.getEdtfTimePart().getHour());
////      assertEquals(expectedMinute, actual.getEdtfTimePart().getMinute());
////      assertEquals(expectedSecond, actual.getEdtfTimePart().getSecond());
//    } else {
//      assertThrows(ParseException.class, () -> parser.parse(actualDate));
//    }
//  }
//
//  private static Stream<Arguments> dateAndTimeUTCRepresentation() {
//    return Stream.of(Arguments.of("1986-07-12T23:59:59Z", 1986, 7, 12, 23, 59, 59, true),
//        Arguments.of("1986-07-12T11:22:55Z", 1986, 7, 12, 11, 22, 55, true),
//        Arguments.of("1986-07-12T00:00:00Z", 1986, 7, 12, 0, 0, 0, true),
//        Arguments.of("1986-07-12T23:59Z", 1986, 7, 12, 23, 59, null, false), //TODO: Check what is the outcome of this
//        Arguments.of("1986-07-12T23Z", 1986, 7, 12, 23, null, null, false),  //TODO: Check what is the outcome of this
//        Arguments.of("1986-07-12TZ", 1986, 7, 12, null, null, null, false),  //TODO: Check what are the limits
//        Arguments.of("1986-07-12T1:20:30Z", 1986, 7, 12, 1, 20, 30, false),
//        Arguments.of("1986-07-12 11:20:30Z", 1986, 7, 12, 11, 20, 30, false),
//        Arguments.of("1986-07-12t11:20:30Z", 1986, 7, 12, 11, 20, 30, false),
//        Arguments.of("1986-07-12T01:1:30Z", 1986, 7, 12, 1, 1, 30, false));
//  }
//
//  @ParameterizedTest
//  @MethodSource("dateAndTimeUTCRepresentation")
//  @DisplayName("[dateI][“T”][time][“Z”]Complete representations for calendar date and UTC time of day")
//  void testTimeInterval(String actualDate,
//      Integer expectedYear,
//      Integer expectedMonth,
//      Integer expectedDay,
//      Integer expectedHour,
//      Integer expectedMinute,
//      Integer expectedSecond,
//      Boolean isSuccess) throws ParseException {
//    if (isSuccess) {
//      InstantEdtfDate actual = (InstantEdtfDate) parser.parse(actualDate);
//
//      assertEquals(expectedDay, actual.getEdtfDatePart().getDay());
//      assertEquals(expectedMonth, actual.getEdtfDatePart().getMonth());
//      assertEquals(expectedYear, actual.getEdtfDatePart().getYear());
////      assertEquals(expectedHour, actual.getEdtfTimePart().getHour());
////      assertEquals(expectedMinute, actual.getEdtfTimePart().getMinute());
////      assertEquals(expectedSecond, actual.getEdtfTimePart().getSecond());
//    } else {
//      assertThrows(ParseException.class, () -> parser.parse(actualDate));
//    }
//  }
//
//  private static Stream<Arguments> dateAndTimeShiftRepresentation() {
//    return Stream.of(Arguments.of("1986-07-12T23:59:59-05", 1986, 7, 12, 23, 59, 59, true),
//        Arguments.of("1986-07-12T11:22:55-12", 1986, 7, 12, 11, 22, 55, true),
//        Arguments.of("1986-07-12T00:00:00+08", 1986, 7, 12, 0, 0, 0, true),
//        Arguments.of("1986-07-12T00:00:00+12", 1986, 7, 12, 0, 0, 0, true),
//        Arguments.of("1986-07-12T00:00:00-00", 1986, 7, 12, 0, 0, 0, true),
//        Arguments.of("1986-07-12T00:00:00+00", 1986, 7, 12, 0, 0, 0, true), //TODO: check why this is accepted
//        Arguments.of("1986-07-12T00:00:00+0", 1986, 7, 12, 0, 0, 0, false), //TODO: check this is why is not accepted
//        Arguments.of("1986-07-12T00:00:00-5", 1986, 7, 12, 0, 0, 0, false),
//        Arguments.of("1986-07-12T00:00:00+", 1986, 7, 12, 0, 0, 0, false),
//        Arguments.of("1986-07-12T00:00:00-", 1986, 7, 12, 0, 0, 0, false)
//    );
//  }
//
//  @ParameterizedTest
//  @MethodSource("dateAndTimeShiftRepresentation")
//  @DisplayName("[dateI][“T”][time][shiftHour] Date and time with timeshift in hours (only)")
//  void testDateTimeWithShiftInHour(String actualDate,
//      Integer expectedYear,
//      Integer expectedMonth,
//      Integer expectedDay,
//      Integer expectedHour,
//      Integer expectedMinute,
//      Integer expectedSecond,
//      Boolean isSuccess) throws ParseException {
//    if (isSuccess) {
//      InstantEdtfDate actual = (InstantEdtfDate) parser.parse(actualDate);
//
//      assertEquals(expectedDay, actual.getEdtfDatePart().getDay());
//      assertEquals(expectedMonth, actual.getEdtfDatePart().getMonth());
//      assertEquals(expectedYear, actual.getEdtfDatePart().getYear());
////      assertEquals(expectedHour, actual.getEdtfTimePart().getHour());
////      assertEquals(expectedMinute, actual.getEdtfTimePart().getMinute());
////      assertEquals(expectedSecond, actual.getEdtfTimePart().getSecond());
//    } else {
//      assertThrows(ParseException.class, () -> parser.parse(actualDate));
//    }
//  }
//
//  private static Stream<Arguments> dateAndTimeShiftHourRepresentation() {
//    return Stream.of(Arguments.of("1986-07-12T23:59:59-05:20", 1986, 7, 12, 23, 59, 59, true),
//        Arguments.of("1986-07-12T11:22:55-12:30", 1986, 7, 12, 11, 22, 55, true),
//        Arguments.of("1986-07-12T00:00:00+08:15", 1986, 7, 12, 0, 0, 0, true),
//        Arguments.of("1986-07-12T00:00:00+12:30", 1986, 7, 12, 0, 0, 0, true),
//        Arguments.of("1986-07-12T00:00:00-00:30", 1986, 7, 12, 0, 0, 0, true),
//        Arguments.of("1986-07-12T00:00:00+00:30", 1986, 7, 12, 0, 0, 0, true),
//        Arguments.of("1986-07-12T00:00:00+0:", 1986, 7, 12, 0, 0, 0, false),
//        Arguments.of("1986-07-12T00:00:00-5:30", 1986, 7, 12, 0, 0, 0, false),
//        Arguments.of("1986-07-12T00:00:00+5:15", 1986, 7, 12, 0, 0, 0, false),
//        Arguments.of("1986-07-12T00:00:00-05:", 1986, 7, 12, 0, 0, 0, true) //TODO: check why this is accepted
//    );
//  }
//
//  @ParameterizedTest
//  @MethodSource("dateAndTimeShiftHourRepresentation")
//  @DisplayName("[dateI][“T”][time][shiftHourMinute] Date and time with timeshift in hours and minutes")
//  void testDateTimeWithShiftInHourAndMinutes(String actualDate,
//      Integer expectedYear,
//      Integer expectedMonth,
//      Integer expectedDay,
//      Integer expectedHour,
//      Integer expectedMinute,
//      Integer expectedSecond,
//      Boolean isSuccess) throws ParseException {
//    if (isSuccess) {
//      InstantEdtfDate actual = (InstantEdtfDate) parser.parse(actualDate);
//
//      assertEquals(expectedDay, actual.getEdtfDatePart().getDay());
//      assertEquals(expectedMonth, actual.getEdtfDatePart().getMonth());
//      assertEquals(expectedYear, actual.getEdtfDatePart().getYear());
////      assertEquals(expectedHour, actual.getEdtfTimePart().getHour());
////      assertEquals(expectedMinute, actual.getEdtfTimePart().getMinute());
////      assertEquals(expectedSecond, actual.getEdtfTimePart().getSecond());
//    } else {
//      assertThrows(ParseException.class, () -> parser.parse(actualDate));
//    }
//  }
//
//  private static Stream<Arguments> dateIntervalRepresentation() {
//    return Stream.of(Arguments.of("1986/1998", 1986, null, null, 1998, null, null, true),
//        Arguments.of("1986-07/1998-11", 1986, 7, null, 1998, 11, null, true),
//        Arguments.of("1986-07 / 1998-07", 1986, 7, null, 1998, 7, null, false), //TODO: check if the space between is valid
//        Arguments.of("1986-02-12/1998-07-09", 1986, 2, 12, 1998, 7, 9, true),
//        Arguments.of("1986-07-09/2005", 1986, 7, 9, 2005, null, null, true),
//        Arguments.of("1986/2005-02-22", 1986, null, null, 2005, 2, 22, true),
//        Arguments.of("0986/0998", 986, null, null, 998, null, null, true),
//        Arguments.of("986/998", 986, null, null, 998, null, null, false),
//        Arguments.of("1286/1218", 1286, null, null, 1218, null, null, true), //TODO: check if ranges d1>d2 or d2>d1 are valid
//        Arguments.of("1986-1998", 1986, null, null, 1998, null, null, false),
//        Arguments.of("1986-13/1998-01", 1986, 13, null, 1998, 1, null, true), //TODO: check why this is valid?
//        Arguments.of("1986-00/1998-01", 1986, null, null, 1998, 1, null, true) //TODO: check why this is valid?
//    );
//  }
//
//  @ParameterizedTest
//  @MethodSource("dateIntervalRepresentation")
//  @DisplayName("EDTF Level 0 adopts representations of a time interval")
//  void testDateInterval(String actualDate,
//      Integer expectedStartYear,
//      Integer expectedStartMonth,
//      Integer expectedStartDay,
//      Integer expectedEndYear,
//      Integer expectedEndMonth,
//      Integer expectedEndDay,
//      Boolean isSuccess) throws ParseException {
//    if (isSuccess) {
//      IntervalEdtfDate actual = (IntervalEdtfDate) parser.parse(actualDate);
//
//      assertEquals(expectedStartDay, actual.getStart().getEdtfDatePart().getDay());
//      assertEquals(expectedStartMonth, actual.getStart().getEdtfDatePart().getMonth());
//      assertEquals(expectedStartYear, actual.getStart().getEdtfDatePart().getYear());
//      assertEquals(expectedEndDay, actual.getEnd().getEdtfDatePart().getDay());
//      assertEquals(expectedEndMonth, actual.getEnd().getEdtfDatePart().getMonth());
//      assertEquals(expectedEndYear, actual.getEnd().getEdtfDatePart().getYear());
//    } else {
//      assertThrows(ParseException.class, () -> parser.parse(actualDate));
//    }
//  }
//}
