package eu.europeana.normalization.dates;

import static eu.europeana.normalization.dates.edtf.IntervalEdtfDate.DATES_SEPARATOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.edtf.AbstractEdtfDate;
import eu.europeana.normalization.dates.edtf.DateEdgeType;
import eu.europeana.normalization.dates.edtf.DateQualification;
import eu.europeana.normalization.dates.edtf.InstantEdtfDate;
import eu.europeana.normalization.dates.edtf.IntervalEdtfDate;
import eu.europeana.normalization.dates.extraction.dateextractors.PatternEdtfDateExtractor;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests to validate EDTF format patternEdtfDateExtractor Level 1
 *
 * @see <a href="https://www.loc.gov/standards/datetime/">EDTF library specification</a>
 */
class DateLevel1Test {

  private final PatternEdtfDateExtractor patternEdtfDateExtractor = new PatternEdtfDateExtractor();

  private void extract(String input, String expected) {
    final DateNormalizationResult dateNormalizationResult = patternEdtfDateExtractor.extractDateProperty(input);
    if (expected == null) {
      assertNull(dateNormalizationResult);
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
  @DisplayName("Letter-prefixed calendar year")
  void letterPrefixedCalendarYear(String input, String expected) {
    extract(input, expected);
  }

  @ParameterizedTest
  @MethodSource
  @DisplayName("The characters '?', '~' and '%' are used to mean \"uncertain\", \"approximate\", and \"uncertain\" as well as \"approximate\", respectively")
  void dateQualification(String input, String expected) {
    extract(input, expected);
  }


  @ParameterizedTest
  @MethodSource
  @DisplayName("The character 'X' may be used in place of one or more rightmost digits to indicate that the value of that digit is unspecified")
  void unspecifiedDigitsFromTheRight(String input, String expected) {
    extract(input, expected);
  }

  @ParameterizedTest
  @MethodSource
  @DisplayName("Negative Calendar Year")
  void negativeCalendarYear(String input, String expected) {
    extract(input, expected);
  }


  @ParameterizedTest
  @MethodSource
  @DisplayName("Open start time interval")
  void openStartTimeInterval(String input, String expected) {
    extract(input, expected);
  }

  @ParameterizedTest
  @MethodSource
  @DisplayName("Open end time interval")
  void openEndTimeInterval(String input, String expected) {
    extract(input, expected);
  }


  @ParameterizedTest
  @MethodSource
  @DisplayName("Time interval with unknown start")
  void timeIntervalWithUnknownStart(String input, String expected) {
    extract(input, expected);
  }

  @ParameterizedTest
  @MethodSource
  @DisplayName("Time interval with unknown end")
  void timeIntervalWithUnknownEnd(String input, String expected) {
    extract(input, expected);
  }

  private static Stream<Arguments> letterPrefixedCalendarYear() {
    return Stream.of(
        of("Y170000002", "Y170000002"),
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

  private static Stream<Arguments> dateQualification() {
    return Stream.of(
        of("1989?", "1989?"),
        of("1989~", "1989~"),
        of("1989-11?", "1989-11?"),
        of("1989-11~", "1989-11~"),
        of("1989-11-01%", "1989-11-01%")
    );
  }

  private static Stream<Arguments> unspecifiedDigitsFromTheRight() {
    return Stream.of(
        of("201X", null),
        of("20XX", null),
        of("1989-XX", null),
        of("1989-11-XX", null),
        of("1989-XX-XX", null)
    );
  }

  private static Stream<Arguments> negativeCalendarYear() {
    return Stream.of(
        of("-1989", "-1989"),
        of("-9999", "-9999"),
        of("-0989", "-0989"),
        of("-11989", null)
    );
  }


  private static Stream<Arguments> openStartTimeInterval() {
    return Stream.of(
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
        of(".. /1989-11-01", null)
    );
  }

  private static Stream<Arguments> openEndTimeInterval() {
    return Stream.of(
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
        of("1989-11-01/ ..", null)
    );
  }


  private static Stream<Arguments> timeIntervalWithUnknownStart() {
    return Stream.of(
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
        of(" /1989-11-01", null)
    );
  }

  private static Stream<Arguments> timeIntervalWithUnknownEnd() {
    return Stream.of(
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
        of("1989-11-01/ ", null)
    );
  }


}
