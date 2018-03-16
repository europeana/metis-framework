package eu.europeana.normalization.normalizers;

import java.util.Collections;
import java.util.List;

/**
 * This normalizer trims text values, removing all space characters before and after a value.
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 */
public class CleanSpaceCharactersNormalizer extends EdmValueNormalizer {

  /**
   * Creates a new instance of this class.
   */
  public CleanSpaceCharactersNormalizer() {
    super();
  }

  @Override
  public List<NormalizedValueWithConfidence> normalizeValue(String value) {
    final String result = value.trim();
    if (result.isEmpty()) {
      return Collections.emptyList();
    }
    return Collections.singletonList(new NormalizedValueWithConfidence(result, 1));
  }
}
