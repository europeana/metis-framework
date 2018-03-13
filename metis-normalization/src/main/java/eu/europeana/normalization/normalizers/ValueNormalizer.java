package eu.europeana.normalization.normalizers;

import java.util.List;

/**
 * Instances of this class perform normalizations on string values and provide feedback of their
 * actions. All instances must be able to produce an instance of {@link RecordNormalizer} that
 * reflects the normalization in the context of an EDM DOM tree.
 */
public interface ValueNormalizer {

  /**
   * This method normalizes the given value and attaches a confidence level.
   * 
   * @param value The value to normalize.
   * @return The normalized value.
   */
  List<NormalizedValueWithConfidence> normalizeValue(String value);

  /**
   * This method creates a record normalizer that reflects the normalization of this instance in the
   * context of an EDM DOM tree.
   * 
   * @return The record normalizer.
   */
  RecordNormalizer getAsRecordNormalizer();
}
