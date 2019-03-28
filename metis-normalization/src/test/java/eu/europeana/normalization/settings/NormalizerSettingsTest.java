package eu.europeana.normalization.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.normalization.languages.LanguagesVocabulary;
import eu.europeana.normalization.util.NormalizationConfigurationException;
import org.junit.jupiter.api.Test;

class NormalizerSettingsTest {

  @Test
  void testNonNullValues() throws NormalizationConfigurationException {

    // Check default values.
    final NormalizerSettings settings = new NormalizerSettings();
    assertEquals(NormalizerSettings.DEFAULT_CLEAN_MARKUP_TAGS_MODE,
        settings.getCleanMarkupTagsMode());
    assertEquals(NormalizerSettings.DEFAULT_LANGUAGE_AMBIGUITY_HANDLING,
        settings.getLanguageAmbiguityHandling());
    assertEquals(NormalizerSettings.DEFAULT_MIN_LANGUAGE_LABEL_LENGTH,
        settings.getMinLanguageLabelLength());
    assertEquals(NormalizerSettings.DEFAULT_MINIMUM_CONFIDENCE, settings.getMinimumConfidence(),
        0.000001);
    assertEquals(NormalizerSettings.DEFAULT_VOCABULARY, settings.getTargetLanguageVocabulary());

    // Check setters
    NormalizerSettings newSettings = settings.setCleanMarkupTagsMode(CleanMarkupTagsMode.HTML_ONLY);
    assertEquals(CleanMarkupTagsMode.HTML_ONLY, settings.getCleanMarkupTagsMode());
    assertSame(settings, newSettings);
    newSettings = settings.setLanguageAmbiguityHandling(AmbiguityHandling.CHOOSE_FIRST);
    assertEquals(AmbiguityHandling.CHOOSE_FIRST, settings.getLanguageAmbiguityHandling());
    assertSame(settings, newSettings);
    assertSame(settings, newSettings);
    newSettings = settings.setMinLanguageLabelLength(6);
    assertEquals(6, settings.getMinLanguageLabelLength());
    assertSame(settings, newSettings);
    newSettings = settings.setMinimumConfidence(0.1F);
    assertEquals(0.1F, settings.getMinimumConfidence(), 0.000001);
    assertSame(settings, newSettings);
    newSettings = settings.setTargetLanguageVocabulary(LanguagesVocabulary.ISO_639_2B);
    assertEquals(LanguagesVocabulary.ISO_639_2B, settings.getTargetLanguageVocabulary());
    assertSame(settings, newSettings);

    // Check negative label length
    settings.setMinLanguageLabelLength(-6);
    assertEquals(0, settings.getMinLanguageLabelLength());
  }

  @Test
  void testSetMinimumConfidenceToNegativeValue() {
    assertThrows(NormalizationConfigurationException.class,
        () -> new NormalizerSettings().setMinimumConfidence(-1.0F));
  }

  @Test
  void testSetMinimumConfidenceToLargeValue() {
    assertThrows(NormalizationConfigurationException.class,
        () -> new NormalizerSettings().setMinimumConfidence(2.0F));
  }

  @Test
  void testSetTargetLanguageVocabularyToNull() {
    assertThrows(NormalizationConfigurationException.class,
        () -> new NormalizerSettings().setTargetLanguageVocabulary(null));
  }

  @Test
  void testSetLanguageAmbiguityHandlingToNull() {
    assertThrows(NormalizationConfigurationException.class,
        () -> new NormalizerSettings().setLanguageAmbiguityHandling(null));
  }

  @Test
  void testSetCleanMarkupTagsModeToNull() {
    assertThrows(NormalizationConfigurationException.class,
        () -> new NormalizerSettings().setCleanMarkupTagsMode(null));
  }

}
