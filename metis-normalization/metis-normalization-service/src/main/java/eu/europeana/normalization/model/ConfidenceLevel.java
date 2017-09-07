package eu.europeana.normalization.model;

public enum ConfidenceLevel {
//	C100, C95, C90, C80, C50, C0;d

  CERTAIN(1, 1), VERY_HIGH(0.98, 0.100), HIGH(0.95, 0.98), GOOD(0.90, 0.95), FAIR(0.80, 0.90), POOR(
      0.50, 0.80), GUESS(0, 0.50);

  double minThresholdEqualOrHigherThan;
  double maxThresholdLowerThan;

  private ConfidenceLevel(double minThresholdEqualOrHigherThan, double maxThresholdLowerThan) {
    this.minThresholdEqualOrHigherThan = minThresholdEqualOrHigherThan;
    this.maxThresholdLowerThan = maxThresholdLowerThan;
  }

  public static ConfidenceLevel fromScore(float score) {
    return fromScore((double) score);
  }

  public static ConfidenceLevel fromScore(double score) {
    if (score == 1) {
      return ConfidenceLevel.CERTAIN;
    }
    for (int i = values().length - 1; i >= 0; i--) {
      ConfidenceLevel cl = values()[i];
      if (score < cl.maxThresholdLowerThan) {
        return cl;
      }
    }
    throw new IllegalArgumentException("score must be between 0 and 1: " + score);

  }
}
