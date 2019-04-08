package eu.europeana.normalization.settings;

import eu.europeana.normalization.languages.LanguagesVocabulary;
import eu.europeana.normalization.util.NormalizationConfigurationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * This object contains all the settings that are needed to set up normalization.
 *
 * @author jochen
 */
public class NormalizerSettings {

  protected static final float DEFAULT_MINIMUM_CONFIDENCE = 0.95F;
  protected static final List<LanguagesVocabulary> DEFAULT_DC_LANGUAGE_TARGET_VOCABULARIES = Collections
      .singletonList(LanguagesVocabulary.ISO_639_3);
  protected static final int DEFAULT_MIN_LANGUAGE_LABEL_LENGTH = 4;
  protected static final AmbiguityHandling DEFAULT_LANGUAGE_AMBIGUITY_HANDLING =
      AmbiguityHandling.NO_MATCH;
  protected static final CleanMarkupTagsMode DEFAULT_CLEAN_MARKUP_TAGS_MODE =
      CleanMarkupTagsMode.HTML_ONLY;

  private float minimumConfidence = DEFAULT_MINIMUM_CONFIDENCE;
  private List<LanguagesVocabulary> targetDcLanguageVocabularies = DEFAULT_DC_LANGUAGE_TARGET_VOCABULARIES;
  private int minLanguageLabelLength = DEFAULT_MIN_LANGUAGE_LABEL_LENGTH;
  private AmbiguityHandling languageAmbiguityHandling = DEFAULT_LANGUAGE_AMBIGUITY_HANDLING;
  private CleanMarkupTagsMode cleanMarkupTagsMode = DEFAULT_CLEAN_MARKUP_TAGS_MODE;

  /**
   * Sets the minimal confidence that is required for changes to be made.
   *
   * @param minimumConfidence The minimal confidence. Must be a value between 0 and 1 (inclusive).
   * @return This instance, so that the setter methods can be concatenated easily.
   * @throws NormalizationConfigurationException In case the value is outside the accepted range.
   */
  public NormalizerSettings setMinimumConfidence(float minimumConfidence)
      throws NormalizationConfigurationException {
    if (minimumConfidence < 0 || minimumConfidence > 1) {
      throw new NormalizationConfigurationException(
          "Minimum confidence must be a number between 0 and 1 (inclusive).", null);
    }
    this.minimumConfidence = minimumConfidence;
    return this;
  }

  /**
   * @return The minimal confidence that is required for changes to be made. The default is {@value
   * #DEFAULT_MINIMUM_CONFIDENCE}.
   */
  public float getMinimumConfidence() {
    return minimumConfidence;
  }

  /**
   * Sets the target vocabularies for dc:language normalization.
   *
   * @param targetDcLanguageVocabularies The target language vocabularies. Cannot be null, empty or
   * have null values.
   * @return This instance, so that the setter methods can be concatenated easily.
   * @throws NormalizationConfigurationException If the provided value is null.
   */
  public NormalizerSettings setTargetDcLanguageVocabularies(
      List<LanguagesVocabulary> targetDcLanguageVocabularies)
      throws NormalizationConfigurationException {
    if (targetDcLanguageVocabularies == null || targetDcLanguageVocabularies.isEmpty()
        || targetDcLanguageVocabularies.stream().anyMatch(Objects::isNull)) {
      throw new NormalizationConfigurationException(
          "Provided vocabulary list is null, empty or has null entries", null);
    }
    this.targetDcLanguageVocabularies = new ArrayList<>(targetDcLanguageVocabularies);
    return this;
  }

  /**
   * @return The target vocabularies for dc:language normalization. The default is {@link
   * #DEFAULT_DC_LANGUAGE_TARGET_VOCABULARIES}.
   */
  public List<LanguagesVocabulary> getTargetDcLanguageVocabularies() {
    return Collections.unmodifiableList(targetDcLanguageVocabularies);
  }

  /**
   * Sets the minimum label length for text to be considered a language name (i.e. label).
   *
   * @param minLanguageLabelLength The minimum language label length. If negative, a value of zero
   * will be applied.
   * @return This instance, so that the setter methods can be concatenated easily.
   */
  public NormalizerSettings setMinLanguageLabelLength(int minLanguageLabelLength) {
    this.minLanguageLabelLength = Math.max(minLanguageLabelLength, 0);
    return this;
  }

  /**
   * @return The minimum label length for text to be considered a language name (i.e. label). The
   * default is {@value #DEFAULT_MIN_LANGUAGE_LABEL_LENGTH}.
   */
  public int getMinLanguageLabelLength() {
    return minLanguageLabelLength;
  }

  /**
   * Sets the ambiguity handling strategy for language matching.
   *
   * @param languageAmbiguityHandling The ambiguity handling strategy. Cannot be null.
   * @return This instance, so that the setter methods can be concatenated easily.
   * @throws NormalizationConfigurationException If the provided value is null.
   */
  public NormalizerSettings setLanguageAmbiguityHandling(
      AmbiguityHandling languageAmbiguityHandling) throws NormalizationConfigurationException {
    if (languageAmbiguityHandling == null) {
      throw new NormalizationConfigurationException("Provided ambiguity strategy is null", null);
    }
    this.languageAmbiguityHandling = languageAmbiguityHandling;
    return this;
  }

  /**
   * @return The ambiguity handling strategy for language matching. The default is {@link
   * #DEFAULT_LANGUAGE_AMBIGUITY_HANDLING}.
   */
  public AmbiguityHandling getLanguageAmbiguityHandling() {
    return languageAmbiguityHandling;
  }

  /**
   * Sets the mode for mark-up tag cleaning.
   *
   * @param cleanMarkupTagsMode The mark-up cleaning mode.
   * @return This instance, so that the setter methods can be concatenated easily.
   * @throws NormalizationConfigurationException If the provided value is null.
   */
  public NormalizerSettings setCleanMarkupTagsMode(CleanMarkupTagsMode cleanMarkupTagsMode)
      throws NormalizationConfigurationException {
    if (cleanMarkupTagsMode == null) {
      throw new NormalizationConfigurationException("Provided mode is null", null);
    }
    this.cleanMarkupTagsMode = cleanMarkupTagsMode;
    return this;
  }

  /**
   * @return The mode for mark-up tag cleaning. The default is {@link #DEFAULT_CLEAN_MARKUP_TAGS_MODE}.
   */
  public CleanMarkupTagsMode getCleanMarkupTagsMode() {
    return cleanMarkupTagsMode;
  }
}
