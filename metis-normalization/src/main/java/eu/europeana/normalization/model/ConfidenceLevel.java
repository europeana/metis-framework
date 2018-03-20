package eu.europeana.normalization.model;

/**
 * A categorization of confidence percentages.
 * 
 * @author jochen
 */
public enum ConfidenceLevel {

  /** A confidence of 100% **/
  CERTAIN(1, null),

  /** A confidence of between 98% and 100% **/
  VERY_HIGH(0.98F, CERTAIN),

  /** A confidence of between 95% and 98% **/
  HIGH(0.95F, VERY_HIGH),

  /** A confidence of between 90% and 95% **/
  GOOD(0.90F, HIGH),

  /** A confidence of between 80% and 90% **/
  FAIR(0.80F, GOOD),

  /** A confidence of between 50% and 80% **/
  POOR(0.50F, FAIR),

  /** A confidence of between 0% and 50% **/
  GUESS(0, POOR);

  private static final ConfidenceLevel LOWEST_LEVEL = GUESS;

  /** This is the minimum of the interval, inclusive. **/
  private final float min;

  /** This is the next higher level. **/
  private final ConfidenceLevel higherLevel;

  ConfidenceLevel(float min, ConfidenceLevel higherLevel) {
    this.min = min;
    this.higherLevel = higherLevel;
  }

  /**
   * Get the confidence level that corresponds to the given confidence.
   * 
   * @param confidence The confidence.
   * @return The corresponding confidence level. Is not null.
   * @throws IllegalArgumentException If the confidence is not a number between 0 and 1 (inclusive).
   */
  public static ConfidenceLevel getForConfidence(float confidence) {

    // Sanity check
    if (confidence < 0 || confidence > 1) {
      throw new IllegalArgumentException(
          "Confidence must be between 0 and 1 (not " + confidence + ").");
    }

    // Find the highest level that is still acceptable.
    ConfidenceLevel acceptableLevel = LOWEST_LEVEL;
    while (acceptableLevel.higherLevel != null) {
      if (acceptableLevel.higherLevel.min > confidence) {
        break;
      }
      acceptableLevel = acceptableLevel.higherLevel;
    }

    // Return the level.
    return acceptableLevel;
  }

  /**
   * Retrieves the upper bound on the percentage interval that this confidence level represents.
   * This upper bound is exclusive (i.e. if the confidence is in this level, it cannot be equal to
   * this upper bound). Note: for the highest level there is no upper bound: null will be returned.
   * 
   * @return the upper bound.
   */
  public Float getMax() {
    return higherLevel == null ? null : higherLevel.min;
  }

  /**
   * Retrieves the lower bound on the percentage interval that this confidence level represents.
   * This upper bound is inclusive (i.e. if the confidence is in this level, it can be equal to this
   * lower bound).
   * 
   * @return the lower bound.
   */
  public float getMin() {
    return min;
  }
}
