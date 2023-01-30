package eu.europeana.enrichment.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class LanguageCodeConverterTest {

  @Test
  void convertLanguageCode() {
    final LanguageCodeConverter languageCodeConverter = new LanguageCodeConverter();
    assertEquals("en", languageCodeConverter.convertLanguageCode("eng"));
    assertEquals("en", languageCodeConverter.convertLanguageCode("en"));
    assertNull(languageCodeConverter.convertLanguageCode("english"));
  }

}
