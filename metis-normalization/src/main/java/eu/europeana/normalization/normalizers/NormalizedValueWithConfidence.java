package eu.europeana.normalization.normalizers;

import eu.europeana.normalization.model.ConfidenceLevel;

/**
 * The normalized value including the confidence that the normalization is correct. This object is
 * used within the normalization functionality and not exposed to the calling code.
 *
 * @author Nuno Freire (nfreire@gmail.com)
 */
class NormalizedValueWithConfidence {

  private final String normalizedValue;
  private final float confidence;

  /**
   * Constructor.
   * 
   * @param normalizedValue The normalized value
   * @param confidence The confidence in the normalization
   */
  NormalizedValueWithConfidence(String normalizedValue, float confidence) {
    this.normalizedValue = normalizedValue;
    this.confidence = confidence;
  }

  /**
   * 
   * @return The normalized value.
   */
  public String getNormalizedValue() {
    return normalizedValue;
  }

  /**
   * 
   * @return The confidence.
   */
  public float getConfidence() {
    return confidence;
  }

  /**
   * 
   * @return The confidence level that contains the confidence.
   */
  public ConfidenceLevel getConfidenceClass() {
    return ConfidenceLevel.getForConfidence(confidence);
  }
}
