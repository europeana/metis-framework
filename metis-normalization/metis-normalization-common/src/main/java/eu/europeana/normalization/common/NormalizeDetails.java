/* NormalizeResult.java - created on 11/05/2016, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.normalization.common;

import eu.europeana.normalization.common.model.ConfidenceLevel;
import java.util.ArrayList;
import java.util.List;


/**
 * Detailed informatio about the normalized value, such as the confidence that the normalization is
 * correct
 *
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 11/05/2016
 */
public class NormalizeDetails {

  protected String normalizedValue;
  protected float confidence;

  /**
   * Creates a new instance of this class.
   */
  public NormalizeDetails(String normalizedValue, float confidence) {
    super();
    this.normalizedValue = normalizedValue;
    this.confidence = confidence;
  }

  /**
   * @param normalizeds
   * @param confidence
   * @return
   */
  public static List<NormalizeDetails> newList(List<String> normalizeds, float confidence) {
    List<NormalizeDetails> res = new ArrayList<>();
    for (String v : normalizeds) {
      res.add(new NormalizeDetails(v, confidence));
    }
    return res;
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
    return ConfidenceLevel.fromScore(confidence);
  }

}
