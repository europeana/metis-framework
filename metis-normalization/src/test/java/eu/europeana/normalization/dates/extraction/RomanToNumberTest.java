package eu.europeana.normalization.dates.extraction;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit test for {@link RomanToNumber} class
 */
class RomanToNumberTest {

  private static Stream<Arguments> numberData() {
    return Stream.of(Arguments.of("I", 1, true),
        Arguments.of("II", 2, true),
        Arguments.of("III", 3, true),
        //Arguments.of("IIII", 4, false), //TODO: this is not valid
        Arguments.of("IV", 4, true),
        Arguments.of("V", 5, true),
        Arguments.of("VI", 6, true),
        Arguments.of("VII", 7, true),
        //Arguments.of("VIII", 8, true),   //TODO: this is not valid
        Arguments.of("IX", 9, true),
        Arguments.of("X", 10, true),
        //Arguments.of("VV", 10, false), //TODO: this is not valid
        Arguments.of("XI", 11, true),
        Arguments.of("XII", 12, true),
        Arguments.of("XIII", 13, true),
        Arguments.of("XIV", 14, true),
        //Arguments.of("XIIII", 14, false), //TODO: this is not valid
        Arguments.of("XV", 15, true),
        Arguments.of("XVI", 16, true),
        Arguments.of("XVII", 17, true),
        Arguments.of("XVIII", 18, true),
        Arguments.of("XIX", 19, true),
        Arguments.of("XX", 20, true),
        Arguments.of("XXI", 21, true),
        //Arguments.of("XVVI", 21, true), //TODO: this is not valid
        Arguments.of("XXII", 22, true),
        Arguments.of("XXIII", 23, true),
        Arguments.of("XXIV", 24, true),
        Arguments.of("XXV", 25, true),
        Arguments.of("XXVI", 26, true),
        Arguments.of("XXVII", 27, true),
        Arguments.of("XXVIII", 28, true),
        Arguments.of("XXIX", 29, true),
        Arguments.of("XXX", 30, true),
        Arguments.of("XXXI", 31, true),
        Arguments.of("XXXIV", 34, true),
        Arguments.of("XXXV", 35, true),
        //Arguments.of("VXL", 35, false), //TODO: this is not valid
        Arguments.of("XL", 40, true),
        Arguments.of("XLI", 41, true),
        //Arguments.of("XXXXI", 41, false), //TODO: this is not valid
        Arguments.of("XLII", 42, true),
        Arguments.of("XLIII", 43, true),
        Arguments.of("XLIV", 44, true),
        Arguments.of("XLV", 45, true),
        Arguments.of("XLVI", 46, true),
        Arguments.of("XLVII", 47, true),
        Arguments.of("XLVIII", 48, true),
        //Arguments.of("XLIVV", 49, false), //TODO: this is not valid
        Arguments.of("XLIX", 49, true),
        Arguments.of("L", 50, true),
        Arguments.of("LI", 51, true),
        Arguments.of("LII", 52, true),
        Arguments.of("MCMLXXVI", 1976, true),
        Arguments.of("MCMXCVIII", 1998, true),
        Arguments.of("MMXXII", 2022, true)
    );
  }

  @ParameterizedTest
  @MethodSource("numberData")
  void romanToDecimal(String romanNumber, Integer expectedNumber, Boolean isSuccess) {
    if(isSuccess) {
      assertEquals(expectedNumber, RomanToNumber.romanToDecimal(romanNumber));
    } else {
      assertNotEquals(expectedNumber, RomanToNumber.romanToDecimal(romanNumber));
    }
  }
}
