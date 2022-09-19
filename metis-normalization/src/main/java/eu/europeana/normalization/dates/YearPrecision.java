package eu.europeana.normalization.dates;

/**
 * Enum indicating the year precision that can be used to adjust a year.
 */
public enum YearPrecision {
  MILLENNIUM(1000), CENTURY(100), DECADE(10);

  final int duration;

  YearPrecision(int duration) {
    this.duration = duration;
  }

  public int getDuration() {
    return duration;
  }
}
