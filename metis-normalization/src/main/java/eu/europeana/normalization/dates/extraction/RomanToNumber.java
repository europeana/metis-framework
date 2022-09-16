package eu.europeana.normalization.dates.extraction;

import java.util.HashMap;
import java.util.Locale;
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
   * Converts from roman numeral to a decimal.
   * <p>
   * The provided roman numeral is first converted to upper cased.
   * </p>
   *
   * @param value the roman value
   * @return the decimal value
   */
  public static int romanToDecimal(String value) {
    int result = 0;
    final String upperCasedValue = value.toUpperCase(Locale.US);
    for (int i = 0; i < upperCasedValue.length(); i++) {
      // Current Roman Character
      char character = upperCasedValue.charAt(i);
      if (i > 0 && numbersMap.get(character) > numbersMap.get(upperCasedValue.charAt(i - 1))) {
        result += numbersMap.get(character) - 2 * numbersMap.get(upperCasedValue.charAt(i - 1));
      } else {
        result += numbersMap.get(character);
      }
    }

    return result;
  }
}
