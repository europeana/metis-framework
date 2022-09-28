package eu.europeana.normalization.dates;

/**
 * Enum indicating the year precision that can be used to adjust a year.
 */
public enum YearPrecision {
  DECADE(10), CENTURY(100), MILLENNIUM(1000);

  static final YearPrecision[] values = YearPrecision.values();
  final int duration;

  YearPrecision(int duration) {
    this.duration = duration;
  }

  public int getDuration() {
    return duration;
  }

  public static YearPrecision getYearPrecisionByOrdinal(int ordinal) {
    final YearPrecision yearPrecision;
    if (ordinal < 1 || ordinal > values().length) {
      yearPrecision = null;
    } else {
      yearPrecision = values()[ordinal - 1];
    }
    return yearPrecision;
  }
}
