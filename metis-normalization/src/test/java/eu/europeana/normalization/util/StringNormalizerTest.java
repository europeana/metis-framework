package eu.europeana.normalization.util;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class StringNormalizerTest {

  @Test
  public void testNormalize() {

    // Test lower case
    assertEquals("abcdefg", StringNormalizer.normalize("ABCDEFG"));

    // Test transliteration
    assertEquals("aa vv g g dd ee thth", StringNormalizer.normalize("Αα Ββ Γ γ Δδ Εε Θθ"));
    assertEquals("a b v g d e z", StringNormalizer.normalize("А Б В Г Д Е Ж"));

    // Test spaces and trimming
    assertEquals("a b c", StringNormalizer.normalize(" a \n\r b     c\t"));

    // Test punctuation
    assertEquals("a b", StringNormalizer.normalize("a!@#$%^&*()_[]{};'\\:\"|,./<>?b"));

    // Test accents
    assertEquals("eeeee tieng viet", StringNormalizer.normalize("eèéêë tiếng Việt"));
  }
}
