package eu.europeana.normalization.normalizers;

import java.util.List;

/**
 * An instance of this class performs a normalize action on string values and provide feedback of
 * their actions.
 */
public interface ValueNormalizeAction extends NormalizeAction {

  /**
   * This method normalizes the given value and attaches a confidence level.
   * 
   * @param value The value to normalize.
   * @return The normalized value.
   */
  List<NormalizedValueWithConfidence> normalizeValue(String value);

}
