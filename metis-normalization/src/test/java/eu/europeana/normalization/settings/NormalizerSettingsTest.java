package eu.europeana.normalization.settings;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import eu.europeana.normalization.languages.LanguagesVocabulary;
import eu.europeana.normalization.util.NormalizationConfigurationException;

public class NormalizerSettingsTest {

  @Test
  public void testNonNullValues() throws NormalizationConfigurationException {

    // Check default values.
    final NormalizerSettings settings = new NormalizerSettings();
    assertEquals(NormalizerSettings.DEFAULT_CLEAN_MARKUP_TAGS_MODE,
        settings.getCleanMarkupTagsMode());
    assertEquals(NormalizerSettings.DEFAULT_LANGUAGE_AMBIGUITY_HANDLING,
        settings.getLanguageAmbiguityHandling());
    assertArrayEquals(NormalizerSettings.DEFAULT_LANGUAGE_ELEMENTS,
        settings.getLanguageElementsToNormalize());
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
    newSettings = settings.setLanguageElementsToNormalize(LanguageElement.XML_LANG);
    assertArrayEquals(new LanguageElement[] {LanguageElement.XML_LANG},
        settings.getLanguageElementsToNormalize());
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

  @Test(expected = NormalizationConfigurationException.class)
  public void testSetMinimumConfidenceToNegativeValue() throws NormalizationConfigurationException {
    new NormalizerSettings().setMinimumConfidence(-1.0F);
  }

  @Test(expected = NormalizationConfigurationException.class)
  public void testSetMinimumConfidenceToLargeValue() throws NormalizationConfigurationException {
    new NormalizerSettings().setMinimumConfidence(2.0F);
  }

  @Test(expected = NormalizationConfigurationException.class)
  public void testSetLanguageElementsToNormalizeToNull()
      throws NormalizationConfigurationException {
    new NormalizerSettings().setLanguageElementsToNormalize((LanguageElement[]) null);
  }

  @Test(expected = NormalizationConfigurationException.class)
  public void testSetTargetLanguageVocabularyToNull() throws NormalizationConfigurationException {
    new NormalizerSettings().setTargetLanguageVocabulary(null);
  }

  @Test(expected = NormalizationConfigurationException.class)
  public void testSetLanguageAmbiguityHandlingToNull() throws NormalizationConfigurationException {
    new NormalizerSettings().setLanguageAmbiguityHandling(null);
  }

  @Test(expected = NormalizationConfigurationException.class)
  public void testSetCleanMarkupTagsModeToNull() throws NormalizationConfigurationException {
    new NormalizerSettings().setCleanMarkupTagsMode(null);
  }

}
