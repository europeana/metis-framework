package eu.europeana.normalization.dates.extraction.extractors;

import static org.junit.jupiter.params.provider.Arguments.of;

import eu.europeana.normalization.dates.DateNormalizationExtractorMatchId;
import eu.europeana.normalization.dates.DateNormalizationResult;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EdtfRangeDateExtractorTest implements DateExtractorTest {

  private static final EdtfRangeDateExtractor EDTF_RANGE_DATE_EXTRACTOR = new EdtfRangeDateExtractor();

  private void assertExtract(String input, String expected) {
    final DateNormalizationResult dateNormalizationResult = EDTF_RANGE_DATE_EXTRACTOR.extractDateProperty(input);
    assertDateNormalizationResult(dateNormalizationResult, expected, DateNormalizationExtractorMatchId.EDTF);
  }

  @ParameterizedTest
  @MethodSource
  void dateIntervalRepresentationLevel0(String input, String expected) {
    assertExtract(input, expected);
  }

  @ParameterizedTest
  @MethodSource
  @DisplayName("Letter-prefixed calendar year interval")
  void letterPrefixedCalendarYearIntervalLevel1(String input, String expected) {
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
        of("1989 / 1990", "1989/1990"),
        //Dash not valid
        of("1989-1990", null),
        //Missing digits
        of("989-1990", null),
        of("1989-990", null)
    );
  }

  private static Stream<Arguments> letterPrefixedCalendarYearIntervalLevel1() {
    return Stream.of(
        of("Y-123456789/Y-123456788", "Y-123456789/Y-123456788"),
        //Non prefixed
        of("-123456789/-123456788", null)
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
        of(".. / 1989-11-01", "../1989-11-01"),
        of("../ 1989-11-01", "../1989-11-01"),
        of(".. /1989-11-01", "../1989-11-01"),

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
        of("1989-11-01 / ..", "1989-11-01/.."),
        of("1989-11-01 /..", "1989-11-01/.."),
        of("1989-11-01/ ..", "1989-11-01/.."),
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
        of(" / 1989-11-01", "../1989-11-01"),
        of("/ 1989-11-01", "../1989-11-01"),
        of(" /1989-11-01", "../1989-11-01"),

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
        of("1989-11-01 / ", "1989-11-01/.."),
        of("1989-11-01 /", "1989-11-01/.."),
        of("1989-11-01/ ", "1989-11-01/.."),
        of("/", null)
    );
  }
}