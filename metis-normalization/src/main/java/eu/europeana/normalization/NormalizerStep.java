package eu.europeana.normalization;

import eu.europeana.normalization.languages.LanguageMatcher;
import eu.europeana.normalization.normalizers.CleanIRIViolationsNormalizer;
import eu.europeana.normalization.normalizers.CleanMarkupTagsNormalizer;
import eu.europeana.normalization.normalizers.CleanSpaceCharactersNormalizer;
import eu.europeana.normalization.normalizers.DatesNormalizer;
import eu.europeana.normalization.normalizers.DcLanguageNormalizer;
import eu.europeana.normalization.normalizers.NormalizeAction;
import eu.europeana.normalization.normalizers.RemoveDuplicateStatementNormalizer;
import eu.europeana.normalization.normalizers.XmlLangNormalizer;
import eu.europeana.normalization.settings.NormalizerSettings;
import eu.europeana.normalization.util.NormalizationConfigurationException;

/**
 * This class lists the supported normalization steps.
 *
 * @author jochen
 */
public enum NormalizerStep {

  /**
   * Clean spaces. See {@link CleanSpaceCharactersNormalizer}.
   **/
  CLEAN_SPACE_CHARACTERS(settings -> new CleanSpaceCharactersNormalizer()),

  /**
   * Clean markup tags. See {@link CleanMarkupTagsNormalizer}.
   **/
  CLEAN_MARKUP_TAGS(settings -> new CleanMarkupTagsNormalizer(settings.getCleanMarkupTagsMode())),

  /**
   * Clean IRI Violations.
   */
  CLEAN_IRI_VIOLATIONS(settings -> new CleanIRIViolationsNormalizer()),

  /**
   * Remove duplicate statements. See {@link RemoveDuplicateStatementNormalizer}.
   **/
  REMOVE_DUPLICATE_STATEMENTS(settings -> new RemoveDuplicateStatementNormalizer()),

  /**
   * Dates normalization
   */
  DATES_NORMALIZER(settings -> new DatesNormalizer()),

  /**
   * Normalize language references. See {@link DcLanguageNormalizer}.
   **/
  NORMALIZE_DC_LANGUAGE_REFERENCES(settings -> {
    final LanguageMatcher languageMatcher = new LanguageMatcher(
        settings.getMinLanguageLabelLength(), settings.getLanguageAmbiguityHandling(),
        settings.getTargetDcLanguageVocabularies());
    return new DcLanguageNormalizer(languageMatcher, settings.getMinimumConfidence());
  }),

  /**
   * Normalize language references. See {@link XmlLangNormalizer}.
   **/
  NORMALIZE_XML_LANG_REFERENCES(settings -> {
    final LanguageMatcher languageMatcher = new LanguageMatcher(
        settings.getMinLanguageLabelLength(), settings.getLanguageAmbiguityHandling(),
        settings.getTargetXmlLangVocabularies());
    return new XmlLangNormalizer(languageMatcher, settings.getMinimumConfidence());
  });

  private final ActionCreator actionCreator;

  NormalizerStep(ActionCreator actionCreator) {
    this.actionCreator = actionCreator;
  }

  /**
   * Creates an action that performs this step.
   *
   * @param settings The settings to be applied.
   * @return The action.
   * @throws NormalizationConfigurationException In case there was an issue configuring the action.
   */
  NormalizeAction createAction(NormalizerSettings settings)
      throws NormalizationConfigurationException {
    return this.actionCreator.create(settings);
  }

  @FunctionalInterface
  private interface ActionCreator {

    NormalizeAction create(NormalizerSettings settings) throws NormalizationConfigurationException;
  }
}
