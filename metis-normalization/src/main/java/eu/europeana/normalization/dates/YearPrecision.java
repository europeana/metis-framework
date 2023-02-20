package eu.europeana.normalization.dates;

/**
 * Enum indicating the year precision that can be used to adjust a year.
 */
public enum YearPrecision {
  DECADE(10),
  CENTURY(100),
  MILLENNIUM(1000);

  static final YearPrecision[] values = YearPrecision.values();
  final int duration;

  YearPrecision(int duration) {
    this.duration = duration;
  }

  public int getDuration() {
    return duration;
  }

  /**
   * Get a year precision objects based on the ordinal or null if below one or above the total amount of values.
   * <p>This method can be useful in cases were we want to get the year precision duration based on the unknown digits of a
   * year. For example if we have a year 198X and we know that we have one X, the ordinal 1 will give the
   * {@link YearPrecision#DECADE}.</p>
   *
   * @param ordinal the ordinal
   * @return the year precision
   */
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
