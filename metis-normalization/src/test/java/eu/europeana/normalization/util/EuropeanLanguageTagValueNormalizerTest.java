package eu.europeana.normalization.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

// TODO JV: this class tests a class called LanguageTagValueNormalizer. Why add 'European' to this class name?
class EuropeanLanguageTagValueNormalizerTest {

  @Test
  void testNormalize() {

    // Test one single word
    final List<LanguageTag> singleWord = LanguageTagValueNormalizer.normalize("ABCDEFG");
    assertEquals(1, singleWord.size());
    assertLanguageTag(singleWord.get(0), "abcdefg", null, "abcdefg");

    // Test multiple words with subtags
    final List<LanguageTag> multipleWords = LanguageTagValueNormalizer.normalize("AB CD-EF G");
    assertEquals(3, multipleWords.size());
    assertLanguageTag(multipleWords.get(0), "ab", null, "ab");
    assertLanguageTag(multipleWords.get(1), "cd", "-EF", "cd-ef");
    assertLanguageTag(multipleWords.get(2), "g", null, "g");

    // Test spaces and trimming
    final List<LanguageTag> spacesAndTrimming = LanguageTagValueNormalizer
        .normalize(" a \n\r b     c\td\t");
    assertEquals(4, spacesAndTrimming.size());
    assertLanguageTag(spacesAndTrimming.get(0), "a", null, "a");
    assertLanguageTag(spacesAndTrimming.get(1), "b", null, "b");
    assertLanguageTag(spacesAndTrimming.get(2), "c", null, "c");
    assertLanguageTag(spacesAndTrimming.get(3), "d", null, "d");

    // Test special characters
    final List<LanguageTag> specialChars = LanguageTagValueNormalizer
        .normalize("a-!@#$%^&*()_[]-{};'\\:\"|,./<>?-b");
    assertEquals(2, specialChars.size());
    assertLanguageTag(specialChars.get(0), "a", null, "a-");
    assertLanguageTag(specialChars.get(1), "b", null, "-b");
  }

  @Test
  void testNormalizeWord() {

    // Test splitting and trimming
    assertSingleWordNormalization("ab-bc", "ab", "-bc", "ab-bc");
    assertSingleWordNormalization("a-b-c", "a", "-b-c", "a-b-c");
    assertSingleWordNormalization("-a-b", "a", "-b", "-a-b");
    assertSingleWordNormalization("-aa", "aa", null, "-aa");
    assertSingleWordNormalization("a-", "a", null, "a-");
    assertSingleWordNormalization("a--bcde", "a", "--bcde", "a--bcde");
    assertSingleWordNormalization("abc--", "abc", null, "abc--");
    assertNull(LanguageTagValueNormalizer.normalizeWord("-"));
    assertNull(LanguageTagValueNormalizer.normalizeWord("--"));

    // Test transliteration, diacritics and conversion to lower case
    assertSingleWordNormalization("ab-BC", "ab", "-BC", "ab-bc");
    assertSingleWordNormalization("ΑαΒβΓ-γΔδΕεΘθ", "aavvg", "-γΔδΕεΘθ", "aavvg-gddeethth");
    assertSingleWordNormalization("АБВГ-ДЕЖ", "abvg", "-ДЕЖ", "abvg-dez");
    assertSingleWordNormalization("tiếng-Việt-eèéêë", "tieng", "-Việt-eèéêë", "tieng-viet-eeeee");

  }

  private void assertSingleWordNormalization(String input, String expectedCode,
      String expectedSubtag,
      String expectedLabel) {
    assertLanguageTag(LanguageTagValueNormalizer.normalizeWord(input), expectedCode, expectedSubtag,
        expectedLabel);
  }

  private void assertLanguageTag(LanguageTag result, String expectedCode, String expectedSubtag,
      String expectedLabel) {
    assertNotNull(result);
    assertEquals(expectedCode, result.getLanguageCode());
    assertEquals(expectedSubtag, result.getSubTag());
    assertEquals(expectedLabel, result.getNormalizedInput());
  }
}
