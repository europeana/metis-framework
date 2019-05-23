package eu.europeana.normalization.util;

/**
 * This class represents a (normalized) language tag. It is available in two forms.
 * <ol>
 * <li>As a language label (equal to the input). This is normalized to allow it to be included or
 * searched in a list of known language labels.</li>
 * <li>As a language code/identifier with a possible subtag. The code is normalized to allow it to
 * be included or searched in a list of known language codes, but the subtag is not normalized so
 * that it can be used in its original form.</li>
 * </ol>
 */
public class LanguageTag {

  private final String normalizedInput;
  private final String languageCode;
  private final String subTag;

  /**
   * Constructor.
   *
   * @param normalizedInput The input, but normalized (for use as a language label).
   * @param languageCode The language code (normalized).
   * @param subTag The sub tag. Can be null.
   */
  public LanguageTag(String normalizedInput, String languageCode, String subTag) {
    this.normalizedInput = normalizedInput;
    this.languageCode = languageCode;
    this.subTag = subTag;
  }

  /**
   * @return The language as a language label (i.e. the input), normalized.
   */
  public String getNormalizedInput() {
    return normalizedInput;
  }

  /**
   * @return The language as a language code, normalized.
   */
  public String getLanguageCode() {
    return languageCode;
  }

  /**
   * @return the subtag (not normalized). Can be null if no subtag was found. If not null, it will
   * include the subtag separator.
   */
  public String getSubTag() {
    return subTag;
  }
}
