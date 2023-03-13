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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EdtfDateExtractorTest {

  private final EdtfDateExtractor edtfDateExtractor = new EdtfDateExtractor();

  // TODO: 01/03/2023 Possible reuse of the test code here for all extractors
  private void assertExtract(String input, String expected) {
    final DateNormalizationResult dateNormalizationResult = edtfDateExtractor.extractDateProperty(input, NO_QUALIFICATION);
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
  void dateIntervalRepresentationLevel0(String input, String expected) {
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


  @ParameterizedTest
  @MethodSource
  @DisplayName("Open time interval")
  void openTimeIntervalLevel1(String input, String expected) {
    assertExtract(input, expected);
  }


  @ParameterizedTest
  @MethodSource
  @DisplayName("Unknown time interval")
  void unknownTimeIntervalLevel1(String input, String expected) {
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

  private static Stream<Arguments> dateIntervalRepresentationLevel0() {
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

  private static Stream<Arguments> letterPrefixedCalendarYearLevel1() {
    return Stream.of(
        //Future dates are not valid
        of("Y170000002", null),
        of("Y-170000002", "Y-170000002"),
        //Overflow, max is +-999999999
        of("Y1700000002", null),
        of("Y-1700000002", null),
        //Too low values
        of("Y0", null),
        of("Y1", null),
        of("Y-1", null),
        of("Y", null)
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

  private static Stream<Arguments> openTimeIntervalLevel1() {
    return Stream.of(
        //Open start
        of("../1989-11-01", "../1989-11-01"),
        of("../1989-11", "../1989-11"),
        of("../1989", "../1989"),
        of("../1989-11-01~", "../1989-11-01~"),
        of("../1989-11~", "../1989-11~"),
        of("../1989~", "../1989~"),
        of("../1989-11-01?", "../1989-11-01?"),
        of("../1989-11?", "../1989-11?"),
        of("../1989?", "../1989?"),
        of("../1989-11-01%", "../1989-11-01%"),
        of("../1989-11%", "../1989-11%"),
        of("../1989%", "../1989%"),
        of(".. / 1989-11-01", null),
        of("../ 1989-11-01", null),
        of(".. /1989-11-01", null),

        //Open end
        of("1989-11-01/..", "1989-11-01/.."),
        of("1989-11/..", "1989-11/.."),
        of("1989/..", "1989/.."),
        of("1989-11-01~/..", "1989-11-01~/.."),
        of("1989-11~/..", "1989-11~/.."),
        of("1989~/..", "1989~/.."),
        of("1989-11-01?/..", "1989-11-01?/.."),
        of("1989-11?/..", "1989-11?/.."),
        of("1989?/..", "1989?/.."),
        of("1989-11-01%/..", "1989-11-01%/.."),
        of("1989-11%/..", "1989-11%/.."),
        of("1989%/..", "1989%/.."),
        of("1989-11-01 / ..", null),
        of("1989-11-01 /..", null),
        of("1989-11-01/ ..", null),
        of("../..", null)
    );
  }


  private static Stream<Arguments> unknownTimeIntervalLevel1() {
    return Stream.of(
        //Unknown start
        of("/1989-11-01", "../1989-11-01"),
        of("/1989-11", "../1989-11"),
        of("/1989", "../1989"),
        of("/1989-11-01~", "../1989-11-01~"),
        of("/1989-11~", "../1989-11~"),
        of("/1989~", "../1989~"),
        of("/1989-11-01?", "../1989-11-01?"),
        of("/1989-11?", "../1989-11?"),
        of("/1989?", "../1989?"),
        of("/1989-11-01%", "../1989-11-01%"),
        of("/1989-11%", "../1989-11%"),
        of("/1989%", "../1989%"),
        of(" / 1989-11-01", null),
        of("/ 1989-11-01", null),
        of(" /1989-11-01", null),

        //Unknown end
        of("1989-11-01/", "1989-11-01/.."),
        of("1989-11/", "1989-11/.."),
        of("1989/", "1989/.."),
        of("1989-11-01~/", "1989-11-01~/.."),
        of("1989-11~/", "1989-11~/.."),
        of("1989~/", "1989~/.."),
        of("1989-11-01?/", "1989-11-01?/.."),
        of("1989-11?/", "1989-11?/.."),
        of("1989?/", "1989?/.."),
        of("1989-11-01%/", "1989-11-01%/.."),
        of("1989-11%/", "1989-11%/.."),
        of("1989%/", "1989%/.."),
        of("1989-11-01 / ", null),
        of("1989-11-01 /", null),
        of("1989-11-01/ ", null),
        of("/", null)
    );
  }
}