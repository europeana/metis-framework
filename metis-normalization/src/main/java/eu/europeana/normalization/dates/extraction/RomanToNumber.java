package eu.europeana.normalization.dates.extraction;

import java.util.Locale;

/**
 * Auxiliary class for converting roman numerals to decimal.
 */
public final class RomanToNumber {

  enum ROMAN {
    I('I', 1),
    V('V', 5),
    X('X', 10),
    L('L', 50),
    C('C', 100),
    D('D', 500),
    M('M', 1000),
    ;

    private final char text;
    private final int value;

    ROMAN(char text, int value) {
      this.text = text;
      this.value = value;
    }

    public char getText() {
      return text;
    }

    public int getValue() {
      return value;
    }
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
      if (i > 0 && ROMAN.valueOf(String.valueOf(character)).getValue() > ROMAN.valueOf(
          String.valueOf(upperCasedValue.charAt(i - 1))).getValue()) {
        result +=
            ROMAN.valueOf(String.valueOf(character)).getValue() - 2 * ROMAN.valueOf(String.valueOf(upperCasedValue.charAt(i - 1)))
                                                                           .getValue();
      } else {
        result += ROMAN.valueOf(String.valueOf(character)).getValue();
      }
    }

    return result;
  }
}
