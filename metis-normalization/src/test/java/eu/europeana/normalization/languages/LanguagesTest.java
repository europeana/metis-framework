package eu.europeana.normalization.languages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import eu.europeana.normalization.util.NormalizationConfigurationException;

public class LanguagesTest {

  @Test
  public void testLoadLanguages() throws NormalizationConfigurationException {

    // Load the languages
    final Languages languages = Languages.getLanguages();

    // Test that the second time we load we get the same instance.
    assertSame(languages, Languages.getLanguages());

    // Find Dutch - a language with lots of information.
    final List<Language> dutchList = languages.getActiveLanguages().stream()
        .filter(language -> language.getAuthorityCode().equals("NLD")).collect(Collectors.toList());
    assertEquals(1, dutchList.size());
    final Language dutch = dutchList.get(0);

    // Check the other codes.
    assertEquals("nl", dutch.getIso6391());
    assertEquals("dut", dutch.getIso6392b());
    assertEquals("nld", dutch.getIso6392t());
    assertEquals("nld", dutch.getIso6393());

    // Check the original name.
    final Set<String> originalNames =
        dutch.getOriginalNames().stream().filter(label -> label.getLanguage().equals("nld"))
            .map(LanguageLabel::getLabel).collect(Collectors.toSet());
    assertTrue(originalNames.contains("Nederlands"));

    // No alternative name known.
    assertTrue(dutch.getAlternativeNames().isEmpty());

    // Check labels for English
    final Set<String> labels =
        dutch.getLabels().stream().filter(label -> label.getLanguage().equals("eng"))
            .map(LanguageLabel::getLabel).collect(Collectors.toSet());
    assertTrue(labels.contains("Dutch"));
  }

}
