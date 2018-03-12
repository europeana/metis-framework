package eu.europeana.normalization.common;

import java.util.ArrayList;
import java.util.List;
import eu.europeana.normalization.model.ConfidenceLevel;


/**
 * Detailed informatio about the normalized value, such as the confidence that the normalization is
 * correct
 *
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 11/05/2016
 */
public class NormalizeDetails {

  private String normalizedValue;
  private float confidence;

  /**
   * Creates a new instance of this class.
   */
  public NormalizeDetails(String normalizedValue, float confidence) {
    super();
    this.normalizedValue = normalizedValue;
    this.confidence = confidence;
  }

  public String getNormalizedValue() {
    return normalizedValue;
  }

  public void setNormalizedValue(String normalizedValue) {
    this.normalizedValue = normalizedValue;
  }

  public float getConfidence() {
    return confidence;
  }

  public void setConfidence(float confidence) {
    this.confidence = confidence;
  }

  public ConfidenceLevel getConfidenceClass() {
    return ConfidenceLevel.getForConfidence(confidence);
  }

}
