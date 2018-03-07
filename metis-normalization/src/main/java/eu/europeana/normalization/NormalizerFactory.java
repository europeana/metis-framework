package eu.europeana.normalization;

import eu.europeana.normalization.common.language.LanguagesVocabulary;
import eu.europeana.normalization.util.NormalizationConfigurationException;

public class NormalizerFactory {

  // TODO JOCHEN are these sensible defaults? Particularly the conficence?
  private static final LanguagesVocabulary DEFAULT_VOCABULARY = LanguagesVocabulary.ISO_639_3;
  private static final float DEFAULT_MINIMAL_CONFIDENCE = 0.95f;

  private NormalizerFactory() {}

  public static Normalizer getNormalizer() throws NormalizationConfigurationException {
    return getNormalizer(DEFAULT_VOCABULARY, DEFAULT_MINIMAL_CONFIDENCE);
  }

  // TODO JOCHEN how to provide these settings? Config file, or like this?
  public static Normalizer getNormalizer(LanguagesVocabulary targetLanguageVocabulary,
      float minimumConfidence) throws NormalizationConfigurationException {
    return new NormalizerImpl(targetLanguageVocabulary, minimumConfidence);
  }
}
