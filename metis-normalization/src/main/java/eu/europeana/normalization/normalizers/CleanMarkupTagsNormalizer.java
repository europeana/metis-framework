package eu.europeana.normalization.normalizers;

import java.util.Collections;
import java.util.List;
import eu.europeana.normalization.settings.CleanMarkupTagsMode;

/**
 * This normalizer removes markup (HTML) tags from text values.
 *
 * @author Nuno Freire (nfreire@gmail.com)
 */
public class CleanMarkupTagsNormalizer extends EdmValueNormalizer {

  private final CleanMarkupTagsMode mode;

  /**
   * Constructor.
   * 
   * @param mode The mode of this normalizer.
   */
  public CleanMarkupTagsNormalizer(CleanMarkupTagsMode mode) {
    this.mode = mode;
  }

  @Override
  public List<NormalizedValueWithConfidence> normalizeValue(String htmlText) {
    final String result = mode.getCleaner().apply(htmlText);
    if (result.length() == 0) {
      return Collections.emptyList();
    }
    return Collections.singletonList(new NormalizedValueWithConfidence(result, 1));
  }
}
