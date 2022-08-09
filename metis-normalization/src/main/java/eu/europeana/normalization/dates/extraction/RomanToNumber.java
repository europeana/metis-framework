package eu.europeana.normalization.dates.extraction;

import java.util.HashMap;
import java.util.Map;

/**
 * Auxiliary class for converting roman numerals to decimal.
 */
public final class RomanToNumber {

  private static final Map<Character, Integer> numbersMap;

  static {
    numbersMap = new HashMap<>();
    numbersMap.put('I', 1);
    numbersMap.put('V', 5);
    numbersMap.put('X', 10);
    numbersMap.put('L', 50);
    numbersMap.put('C', 100);
    numbersMap.put('D', 500);
    numbersMap.put('M', 1000);
  }

  private RomanToNumber() {
  }

  /**
   * Converts from roman numeral to a decimal
   *
   * @param value the roman value
   * @return the decimal value
   */
  public static int romanToDecimal(String value) {
    int result = 0;
    for (int i = 0; i < value.length(); i++) {
      char character = value.charAt(i);      // Current Roman Character
      if (i > 0 && numbersMap.get(character) > numbersMap.get(value.charAt(i - 1))) {
        result += numbersMap.get(character) - 2 * numbersMap.get(value.charAt(i - 1));
      } else {
        result += numbersMap.get(character);
      }
    }

    return result;
  }
}
