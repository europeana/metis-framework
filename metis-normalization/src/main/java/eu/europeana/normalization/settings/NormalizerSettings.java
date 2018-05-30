package eu.europeana.normalization.settings;

import java.util.Arrays;
import eu.europeana.normalization.languages.LanguagesVocabulary;
import eu.europeana.normalization.util.NormalizationConfigurationException;

/**
 * This object contains all the settings that are needed to set up normalization.
 * 
 * @author jochen
 *
 */
public class NormalizerSettings {

  protected static final float DEFAULT_MINIMUM_CONFIDENCE = 0.95F;
  protected static final LanguagesVocabulary DEFAULT_VOCABULARY = LanguagesVocabulary.ISO_639_3;
  protected static final LanguageElement[] DEFAULT_LANGUAGE_ELEMENTS =
      {LanguageElement.DC_LANGUAGE};
  protected static final int DEFAULT_MIN_LANGUAGE_LABEL_LENGTH = 4;
  protected static final AmbiguityHandling DEFAULT_LANGUAGE_AMBIGUITY_HANDLING =
      AmbiguityHandling.NO_MATCH;
  protected static final CleanMarkupTagsMode DEFAULT_CLEAN_MARKUP_TAGS_MODE =
      CleanMarkupTagsMode.ALL_MARKUP;

  private float minimumConfidence = DEFAULT_MINIMUM_CONFIDENCE;
  private LanguagesVocabulary targetLanguageVocabulary = DEFAULT_VOCABULARY;
  private LanguageElement[] elementsToNormalize = DEFAULT_LANGUAGE_ELEMENTS;
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
   * 
   * @return The minimal confidence that is required for changes to be made. The default is
   *         {@value #DEFAULT_MINIMUM_CONFIDENCE}.
   */
  public float getMinimumConfidence() {
    return minimumConfidence;
  }

  /**
   * Sets elements to normalizer during language normalization.
   * 
   * @param elementsToNormalize The supported elements for language normalization.
   * @return This instance, so that the setter methods can be concatenated easily.
   * @throws NormalizationConfigurationException If the provided value is null.
   */
  public NormalizerSettings setLanguageElementsToNormalize(LanguageElement... elementsToNormalize)
      throws NormalizationConfigurationException {
    if (elementsToNormalize == null) {
      throw new NormalizationConfigurationException("Provided setting is null", null);
    }
    this.elementsToNormalize = Arrays.copyOf(elementsToNormalize, elementsToNormalize.length);
    return this;
  }

  /**
   * 
   * @return The elements to normalizer during language normalization. The default is
   *         {@value #DEFAULT_LANGUAGE_ELEMENTS}.
   */
  public LanguageElement[] getLanguageElementsToNormalize() {
    return Arrays.copyOf(elementsToNormalize, elementsToNormalize.length);
  }

  /**
   * Sets the target vocabulary for language normalization.
   * 
   * @param targetLanguageVocabulary The target language vocabulary. Cannot be null.
   * @return This instance, so that the setter methods can be concatenated easily.
   * @throws NormalizationConfigurationException If the provided value is null.
   */
  public NormalizerSettings setTargetLanguageVocabulary(
      LanguagesVocabulary targetLanguageVocabulary) throws NormalizationConfigurationException {
    if (targetLanguageVocabulary == null) {
      throw new NormalizationConfigurationException("Provided vocabulary is null", null);
    }
    this.targetLanguageVocabulary = targetLanguageVocabulary;
    return this;
  }

  /**
   * 
   * @return The target vocabulary for language normalization. The default is
   *         {@value #DEFAULT_VOCABULARY}.
   */
  public LanguagesVocabulary getTargetLanguageVocabulary() {
    return targetLanguageVocabulary;
  }

  /**
   * Sets the minimum label length for text to be considered a language name (i.e. label).
   * 
   * @param minLanguageLabelLength The minimum language label length. If negative, a value of zero
   *        will be applied.
   * @return This instance, so that the setter methods can be concatenated easily.
   */
  public NormalizerSettings setMinLanguageLabelLength(int minLanguageLabelLength) {
    this.minLanguageLabelLength = Math.max(minLanguageLabelLength, 0);
    return this;
  }

  /**
   * 
   * @return The minimum label length for text to be considered a language name (i.e. label). The
   *         default is {@value #DEFAULT_MIN_LANGUAGE_LABEL_LENGTH}.
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
   * 
   * @return The ambiguity handling strategy for language matching. The default is
   *         {@value #DEFAULT_LANGUAGE_AMBIGUITY_HANDLING}.
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
   * 
   * @return The mode for mark-up tag cleaning. The default is
   *         {@value #DEFAULT_CLEAN_MARKUP_TAGS_MODE}.
   */
  public CleanMarkupTagsMode getCleanMarkupTagsMode() {
    return cleanMarkupTagsMode;
  }
}
