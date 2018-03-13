package eu.europeana.normalization;

import eu.europeana.normalization.languages.LanguageMatcher;
import eu.europeana.normalization.languages.Languages;
import eu.europeana.normalization.normalizers.ChainedNormalizer;
import eu.europeana.normalization.normalizers.CleanMarkupTagsNormalizer;
import eu.europeana.normalization.normalizers.CleanSpaceCharactersNormalizer;
import eu.europeana.normalization.normalizers.LanguageReferenceNormalizer;
import eu.europeana.normalization.normalizers.RemoveDuplicateStatementNormalizer;
import eu.europeana.normalization.settings.NormalizerSettings;
import eu.europeana.normalization.util.NormalizationConfigurationException;

/**
 * This class creates instances of {@link Normalizer}.
 */
public class NormalizerFactory {

  private final NormalizerSettings settings;

  /**
   * Constructor for default settings.
   */
  public NormalizerFactory() {
    this(new NormalizerSettings());
  }

  /**
   * Constructor.
   * 
   * @param settings The settings to be applied to this normalization.
   */
  public NormalizerFactory(NormalizerSettings settings) {
    this.settings = settings;
  }

  /**
   * This method creates a normalizer.
   * 
   * @return A normalizer.
   * @throws NormalizationConfigurationException In case the normalizer could not be set up.
   */
  public Normalizer getNormalizer() throws NormalizationConfigurationException {

    // Set up the language normalizer.
    final Languages vocabulary = Languages.getLanguages();
    vocabulary.setTargetVocabulary(settings.getTargetLanguageVocabulary());
    final LanguageMatcher languageMatcher = new LanguageMatcher(vocabulary,
        settings.getMinLanguageLabelLength(), settings.getLanguageAmbiguityHandling());
    final LanguageReferenceNormalizer languageNormalizer =
        new LanguageReferenceNormalizer(languageMatcher, settings.getMinimumConfidence(),
            settings.getLanguageElementsToNormalize());

    // Set up the other normalizers
    final CleanSpaceCharactersNormalizer spacesCleaner = new CleanSpaceCharactersNormalizer();
    final CleanMarkupTagsNormalizer markupStatementsCleaner =
        new CleanMarkupTagsNormalizer(settings.getCleanMarkupTagsMode());
    final RemoveDuplicateStatementNormalizer dupStatementsCleaner =
        new RemoveDuplicateStatementNormalizer();

    // Combine the normalizers into one chained record normalizer (note the order).
    final ChainedNormalizer chainedNormalizer = new ChainedNormalizer(
        spacesCleaner.getAsRecordNormalizer(), markupStatementsCleaner.getAsRecordNormalizer(),
        languageNormalizer.getAsRecordNormalizer(), dupStatementsCleaner);

    // Create the normalizer object
    return new NormalizerImpl(chainedNormalizer);
  }
}
