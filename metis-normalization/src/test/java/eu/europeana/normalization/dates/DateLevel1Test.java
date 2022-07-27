package eu.europeana.normalization.dates;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests to validate EDTF format parser Level 1
 *
 * @see <a href="https://www.loc.gov/standards/datetime/">EDTF library specification</a>
 */
public class DateLevel1Test {

  private static Stream<Arguments> date() {
    return Stream.of(Arguments.of());
  }

  @ParameterizedTest
  @MethodSource("date")
  void letterPrefixedCalendarYear() {

  }

  @ParameterizedTest
  @MethodSource("date")
  void seasons() {

  }

  @ParameterizedTest
  @MethodSource("date")
  void qualificationOfADate() {

  }

  @ParameterizedTest
  @MethodSource("date")
  void unspecifiedDigitsFromTheRight() {

  }

  @ParameterizedTest
  @MethodSource("date")
  void negativeCalendarYear() {

  }

}
