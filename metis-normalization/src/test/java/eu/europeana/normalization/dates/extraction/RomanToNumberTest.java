package eu.europeana.normalization.dates.extraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit test for {@link RomanToNumber} class
 */
class RomanToNumberTest {

  private static Stream<Arguments> numberData() {
    return Stream.of(
        of("I", 1, true),
        of("II", 2, true),
        of("III", 3, true),
        //Arguments.of("IIII", 4, false), //TODO: this is not valid
        of("IV", 4, true),
        of("V", 5, true),
        of("VI", 6, true),
        of("VII", 7, true),
        //Arguments.of("VIII", 8, true),   //TODO: this is not valid
        of("IX", 9, true),
        of("X", 10, true),
        //Arguments.of("VV", 10, false), //TODO: this is not valid
        of("XI", 11, true),
        of("XII", 12, true),
        of("XIII", 13, true),
        of("XIV", 14, true),
        //Arguments.of("XIIII", 14, false), //TODO: this is not valid
        of("XV", 15, true),
        of("XVI", 16, true),
        of("XVII", 17, true),
        of("XVIII", 18, true),
        of("XIX", 19, true),
        of("XX", 20, true),
        of("XXI", 21, true),
        //Arguments.of("XVVI", 21, true), //TODO: this is not valid
        of("XXII", 22, true),
        of("XXIII", 23, true),
        of("XXIV", 24, true),
        of("XXV", 25, true),
        of("XXVI", 26, true),
        of("XXVII", 27, true),
        of("XXVIII", 28, true),
        of("XXIX", 29, true),
        of("XXX", 30, true),
        of("XXXI", 31, true),
        of("XXXIV", 34, true),
        of("XXXV", 35, true),
        //Arguments.of("VXL", 35, false), //TODO: this is not valid
        of("XL", 40, true),
        of("XLI", 41, true),
        //Arguments.of("XXXXI", 41, false), //TODO: this is not valid
        of("XLII", 42, true),
        of("XLIII", 43, true),
        of("XLIV", 44, true),
        of("XLV", 45, true),
        of("XLVI", 46, true),
        of("XLVII", 47, true),
        of("XLVIII", 48, true),
        //Arguments.of("XLIVV", 49, false), //TODO: this is not valid
        of("XLIX", 49, true),
        of("L", 50, true),
        of("LI", 51, true),
        of("LII", 52, true),
        of("MCMLXXVI", 1976, true),
        of("MCMXCVIII", 1998, true),
        of("MMXXII", 2022, true)
    );
  }

  @ParameterizedTest
  @MethodSource("numberData")
  void romanToDecimal(String romanNumber, Integer expectedNumber, Boolean isSuccess) {
    if (isSuccess) {
      assertEquals(expectedNumber, RomanToNumber.romanToDecimal(romanNumber));
    } else {
      assertNotEquals(expectedNumber, RomanToNumber.romanToDecimal(romanNumber));
    }
  }
}
