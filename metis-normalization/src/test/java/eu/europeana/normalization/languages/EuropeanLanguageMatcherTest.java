package eu.europeana.normalization.languages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.normalization.languages.LanguageMatch.Type;
import eu.europeana.normalization.settings.AmbiguityHandling;
import eu.europeana.normalization.util.LanguageTag;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

// TODO JV: this class tests a class called LanguageMatcher. Why add 'European' to this class name?
class EuropeanLanguageMatcherTest {

  private static final String LANGUAGE_1_CODE_1 = "ca";
  private static final String LANGUAGE_1_CODE_2B = "cab";
  private static final String LANGUAGE_1_CODE_2T = "cat";
  private static final String LANGUAGE_1_CODE_3 = "caz";
  private static final String LANGUAGE_1_CODE_NAL = "can";
  private static final String LANGUAGE_2_CODE_3 = "cb";
  private static final String LANGUAGE_3_CODE_3 = "cc";

  private static final String LANGUAGE_1_LABEL_1 = "Label1A";
  private static final String LANGUAGE_1_LABEL_2 = "Label1B";
  private static final String LANGUAGE_1_LABEL_3 = "Label1C";
  private static final String LANGUAGE_2_LABEL_1 = "Label2A";
  private static final String LANGUAGE_2_LABEL_2 = "Label2B";
  private static final String LANGUAGE_2_LABEL_3 = "Label2C";

  private static final String AMBIGUOUS_LABEL = "Label3A";
  private static final String SHORT_LABEL = "SL";
  private static final String NONEXISTING_CODE = "xxx";
  private static final String NONEXISTING_LABEL = "XXXXXX";

  private static final String LABEL_LANGUAGE_1 = "lla";
  private static final String LABEL_LANGUAGE_2 = "llb";
  private static final String LABEL_SCRIPT = "latin";

  private static Languages languages;

  private static final String SPLIT_CHARACTER = " ";
  private static final Function<String, List<LanguageTag>> NORMALIZER = tags -> Stream
      .of(tags.split(SPLIT_CHARACTER)).map(tag -> new LanguageTag(tag, tag, null))
      .collect(Collectors.toList());

  @BeforeAll
  static void prepareTests() {

    final Language language1 = new Language();
    language1.setIso6391(LANGUAGE_1_CODE_1);
    language1.setIso6392b(LANGUAGE_1_CODE_2B);
    language1.setIso6392t(LANGUAGE_1_CODE_2T);
    language1.setIso6393(LANGUAGE_1_CODE_3);
    language1.setAuthorityCode(LANGUAGE_1_CODE_NAL);
    language1.addOriginalNames(Collections
        .singletonList(new LanguageLabel(LANGUAGE_1_LABEL_1, LABEL_LANGUAGE_1, LABEL_SCRIPT)));
    language1.addLabels(Collections
        .singletonList(new LanguageLabel(LANGUAGE_1_LABEL_2, LABEL_LANGUAGE_2, LABEL_SCRIPT)));
    language1.addLabels(Collections
        .singletonList(new LanguageLabel(LANGUAGE_1_LABEL_3, LABEL_LANGUAGE_1, LABEL_SCRIPT)));
    language1.addLabels(Collections
        .singletonList(new LanguageLabel(LANGUAGE_1_LABEL_3, LABEL_LANGUAGE_2, LABEL_SCRIPT)));
    language1.addLabels(Collections
        .singletonList(new LanguageLabel(AMBIGUOUS_LABEL, LABEL_LANGUAGE_1, LABEL_SCRIPT)));
    language1.addLabels(Collections
        .singletonList(new LanguageLabel(SHORT_LABEL, LABEL_LANGUAGE_1, LABEL_SCRIPT)));

    final Language language2 = new Language();
    language2.setIso6393(LANGUAGE_2_CODE_3);
    language2.addOriginalNames(Collections
        .singletonList(new LanguageLabel(LANGUAGE_2_LABEL_1, LABEL_LANGUAGE_1, LABEL_SCRIPT)));
    language2.addLabels(Collections
        .singletonList(new LanguageLabel(LANGUAGE_2_LABEL_2, LABEL_LANGUAGE_2, LABEL_SCRIPT)));
    language2.addLabels(Collections
        .singletonList(new LanguageLabel(LANGUAGE_2_LABEL_3, LABEL_LANGUAGE_1, LABEL_SCRIPT)));
    language2.addLabels(Collections
        .singletonList(new LanguageLabel(LANGUAGE_2_LABEL_3, LABEL_LANGUAGE_2, LABEL_SCRIPT)));
    language2.addLabels(Collections
        .singletonList(new LanguageLabel(AMBIGUOUS_LABEL, LABEL_LANGUAGE_1, LABEL_SCRIPT)));

    final Language language3 = new Language();
    language3.setIso6393(LANGUAGE_3_CODE_3);
    language3.addLabels(Collections
        .singletonList(new LanguageLabel(AMBIGUOUS_LABEL, LABEL_LANGUAGE_2, LABEL_SCRIPT)));

    languages = createLanguages(language1, language2, language3);
  }

  private static Languages createLanguages(Language... languagesToAdd) {
    final Languages result = Mockito.mock(Languages.class);
    Mockito.doReturn(Arrays.asList(languagesToAdd)).when(result).getActiveLanguages();
    Mockito.doThrow(new IllegalStateException("Should not be looking at deprecated languages!"))
        .when(result).getDeprecatedLanguages();
    return result;
  }

  @Test
  void testSingleMatches() {

    // Create matcher
    final LanguageMatcher matcher = new LanguageMatcher(4, AmbiguityHandling.NO_MATCH,
        Collections.singletonList(LanguagesVocabulary.ISO_639_3), languages, NORMALIZER);

    // Match single existing codes
    assertSingleMatch(matcher, LANGUAGE_1_CODE_1, LANGUAGE_1_CODE_3, Type.CODE_MATCH);
    assertSingleMatch(matcher, LANGUAGE_1_CODE_2B, LANGUAGE_1_CODE_3, Type.CODE_MATCH);
    assertSingleMatch(matcher, LANGUAGE_1_CODE_2T, LANGUAGE_1_CODE_3, Type.CODE_MATCH);
    assertSingleMatch(matcher, LANGUAGE_1_CODE_3, LANGUAGE_1_CODE_3, Type.CODE_MATCH);
    assertSingleMatch(matcher, LANGUAGE_1_CODE_NAL, LANGUAGE_1_CODE_3, Type.CODE_MATCH);
    assertSingleMatch(matcher, LANGUAGE_2_CODE_3, LANGUAGE_2_CODE_3, Type.CODE_MATCH);
    assertSingleMatch(matcher, LANGUAGE_3_CODE_3, LANGUAGE_3_CODE_3, Type.CODE_MATCH);

    // Match single existing labels that exist and are not ambiguous
    assertSingleMatch(matcher, LANGUAGE_1_LABEL_1, LANGUAGE_1_CODE_3, Type.LABEL_MATCH);
    assertSingleMatch(matcher, LANGUAGE_1_LABEL_2, LANGUAGE_1_CODE_3, Type.LABEL_MATCH);
    assertSingleMatch(matcher, LANGUAGE_1_LABEL_3, LANGUAGE_1_CODE_3, Type.LABEL_MATCH);
    assertSingleMatch(matcher, LANGUAGE_2_LABEL_1, LANGUAGE_2_CODE_3, Type.LABEL_MATCH);
    assertSingleMatch(matcher, LANGUAGE_2_LABEL_2, LANGUAGE_2_CODE_3, Type.LABEL_MATCH);
    assertSingleMatch(matcher, LANGUAGE_2_LABEL_3, LANGUAGE_2_CODE_3, Type.LABEL_MATCH);
  }

  @Test
  void testMultipleMatches() {

    // Create matcher
    final LanguageMatcher matcher = new LanguageMatcher(4, AmbiguityHandling.NO_MATCH,
        Collections.singletonList(LanguagesVocabulary.ISO_639_3), languages, NORMALIZER);

    // Match multiple existing codes
    final List<LanguageMatch> matchCodes =
        matcher.match(String.join(SPLIT_CHARACTER, LANGUAGE_1_CODE_1, LANGUAGE_1_CODE_3, LANGUAGE_2_CODE_3));
    assertEquals(3, matchCodes.size());
    assertMatch(matchCodes.get(0), LANGUAGE_1_CODE_1, LANGUAGE_1_CODE_3, Type.CODE_MATCH);
    assertMatch(matchCodes.get(1), LANGUAGE_1_CODE_3, LANGUAGE_1_CODE_3, Type.CODE_MATCH);
    assertMatch(matchCodes.get(2), LANGUAGE_2_CODE_3, LANGUAGE_2_CODE_3, Type.CODE_MATCH);

    // Match multiple existing labels
    final List<LanguageMatch> matchLabels =
        matcher.match(String.join(SPLIT_CHARACTER, LANGUAGE_1_LABEL_1, LANGUAGE_2_LABEL_1, LANGUAGE_2_LABEL_3));
    assertEquals(3, matchLabels.size());
    assertMatch(matchLabels.get(0), LANGUAGE_1_LABEL_1, LANGUAGE_1_CODE_3, Type.LABEL_MATCH);
    assertMatch(matchLabels.get(1), LANGUAGE_2_LABEL_1, LANGUAGE_2_CODE_3, Type.LABEL_MATCH);
    assertMatch(matchLabels.get(2), LANGUAGE_2_LABEL_3, LANGUAGE_2_CODE_3, Type.LABEL_MATCH);

    // Match mix of existing codes and labels
    final List<LanguageMatch> matchMix =
        matcher.match(String.join(SPLIT_CHARACTER, LANGUAGE_1_CODE_1, LANGUAGE_2_LABEL_1));
    assertEquals(2, matchMix.size());
    assertMatch(matchMix.get(0), LANGUAGE_1_CODE_1, LANGUAGE_1_CODE_3, Type.CODE_MATCH);
    assertMatch(matchMix.get(1), LANGUAGE_2_LABEL_1, LANGUAGE_2_CODE_3, Type.LABEL_MATCH);
  }

  @Test
  void testCodesWithSubtags() {

    // Create matcher
    final String subTagSeparator = "-";
    final String subTag = subTagSeparator + "sub";
    final LanguageMatcher matcher = new LanguageMatcher(4, AmbiguityHandling.NO_MATCH,
        Collections.singletonList(LanguagesVocabulary.ISO_639_3), languages, tag -> {
      final int separatorIndex = tag.indexOf(subTagSeparator);
      final LanguageTag result;
      if (separatorIndex < 0) {
        result = new LanguageTag(tag, tag, null);
      } else {
        result = new LanguageTag(tag, tag.substring(0, separatorIndex),
            tag.substring(separatorIndex));
      }
      return Collections.singletonList(result);
    });

    // Match single existing codes
    assertSingleMatch(matcher, LANGUAGE_1_CODE_1 + subTag, LANGUAGE_1_CODE_3 + subTag,
        Type.CODE_MATCH);
    assertSingleMatch(matcher, LANGUAGE_1_CODE_3 + subTag, LANGUAGE_1_CODE_3 + subTag,
        Type.CODE_MATCH);
  }

  @Test
  void testAmbiguousLabels() {

    // Test ambiguity handling NO_MATCH.
    final LanguageMatcher matcherChooseNone = new LanguageMatcher(4, AmbiguityHandling.NO_MATCH,
        Collections.singletonList(LanguagesVocabulary.ISO_639_3), languages, NORMALIZER);
    assertSingleMatch(matcherChooseNone, AMBIGUOUS_LABEL, null, Type.NO_MATCH);

    // Test ambiguity handling FIRST_MATCH.
    final LanguageMatcher matcherChooseFirst = new LanguageMatcher(4,
        AmbiguityHandling.CHOOSE_FIRST, Collections.singletonList(LanguagesVocabulary.ISO_639_3),
        languages, NORMALIZER);
    assertSingleMatch(matcherChooseFirst, AMBIGUOUS_LABEL, LANGUAGE_1_CODE_3, Type.LABEL_MATCH);
  }


  @Test
  void testNonexistingCodesAndLabels() {

    // Test what happens when a nonexisting code is matched.
    final LanguageMatcher matcher = new LanguageMatcher(4, AmbiguityHandling.NO_MATCH,
        Collections.singletonList(LanguagesVocabulary.ISO_639_3), languages, NORMALIZER);
    assertSingleMatch(matcher, NONEXISTING_CODE, null, Type.NO_MATCH);
    assertSingleMatch(matcher, NONEXISTING_LABEL, null, Type.NO_MATCH);
  }

  @Test
  void testLanguageWithoutCode() {

    // Test what happens when a language doesn't have a code in the target vocabulary
    final LanguageMatcher matcher = new LanguageMatcher(4, AmbiguityHandling.NO_MATCH,
        Collections.singletonList(LanguagesVocabulary.ISO_639_1), languages, NORMALIZER);
    assertSingleMatch(matcher, LANGUAGE_1_CODE_1, LANGUAGE_1_CODE_1, Type.CODE_MATCH);
    assertSingleMatch(matcher, LANGUAGE_1_CODE_3, LANGUAGE_1_CODE_1, Type.CODE_MATCH);
    assertSingleMatch(matcher, LANGUAGE_1_LABEL_1, LANGUAGE_1_CODE_1, Type.LABEL_MATCH);
    assertSingleMatch(matcher, LANGUAGE_2_CODE_3, null, Type.NO_MATCH);
    assertSingleMatch(matcher, LANGUAGE_2_LABEL_1, null, Type.NO_MATCH);
  }

  @Test
  void testLanguageCodeWithBadCharacters() {
    final Language language = new Language();
    language.setIso6391("A1a");
    assertThrows(RuntimeException.class, () -> new LanguageMatcher(4, AmbiguityHandling.NO_MATCH,
        Collections.singletonList(LanguagesVocabulary.ISO_639_1), createLanguages(language),
        NORMALIZER));
  }

  @Test
  void testLanguageCodeOfWrongLength() {
    final Language language = new Language();
    language.setIso6391("aaaa");
    assertThrows(RuntimeException.class, () -> new LanguageMatcher(4, AmbiguityHandling.NO_MATCH,
        Collections.singletonList(LanguagesVocabulary.ISO_639_1), createLanguages(language),
        NORMALIZER));
  }

  @Test
  void testConflictingLanguageCodes() {
    final Language language1 = new Language();
    language1.setIso6391("aaa");
    language1.setIso6393("aaa");
    final Language language2 = new Language();
    language2.setIso6391("aaa");
    language2.setIso6393("aab");
    assertThrows(RuntimeException.class, () -> new LanguageMatcher(4, AmbiguityHandling.NO_MATCH,
        Collections.singletonList(LanguagesVocabulary.ISO_639_3),
        createLanguages(language1, language2), NORMALIZER));
  }

  @Test
  void testShortLanguageLabels() {
    final LanguageMatcher matcherShort = new LanguageMatcher(SHORT_LABEL.length(),
        AmbiguityHandling.NO_MATCH, Collections.singletonList(LanguagesVocabulary.ISO_639_3),
        languages, NORMALIZER);
    assertSingleMatch(matcherShort, SHORT_LABEL, LANGUAGE_1_CODE_3, Type.LABEL_MATCH);
    final LanguageMatcher matcherLong = new LanguageMatcher(SHORT_LABEL.length() + 1,
        AmbiguityHandling.NO_MATCH, Collections.singletonList(LanguagesVocabulary.ISO_639_3),
        languages, NORMALIZER);
    assertSingleMatch(matcherLong, SHORT_LABEL, null, Type.NO_MATCH);
  }

  @Test
  void testMultipleVocabularies() {

    // Create matcher
    final LanguageMatcher matcher = new LanguageMatcher(4, AmbiguityHandling.NO_MATCH,
        Arrays.asList(LanguagesVocabulary.ISO_639_1, LanguagesVocabulary.ISO_639_3),
        languages, NORMALIZER);

    // Match language 1: should come from first vocabulary.
    assertSingleMatch(matcher, LANGUAGE_1_CODE_1, LANGUAGE_1_CODE_1, Type.CODE_MATCH);
    assertSingleMatch(matcher, LANGUAGE_1_CODE_2B, LANGUAGE_1_CODE_1, Type.CODE_MATCH);
    assertSingleMatch(matcher, LANGUAGE_1_CODE_2T, LANGUAGE_1_CODE_1, Type.CODE_MATCH);
    assertSingleMatch(matcher, LANGUAGE_1_CODE_3, LANGUAGE_1_CODE_1, Type.CODE_MATCH);
    assertSingleMatch(matcher, LANGUAGE_1_CODE_NAL, LANGUAGE_1_CODE_1, Type.CODE_MATCH);
    assertSingleMatch(matcher, LANGUAGE_1_LABEL_1, LANGUAGE_1_CODE_1, Type.LABEL_MATCH);
    assertSingleMatch(matcher, LANGUAGE_1_LABEL_2, LANGUAGE_1_CODE_1, Type.LABEL_MATCH);
    assertSingleMatch(matcher, LANGUAGE_1_LABEL_3, LANGUAGE_1_CODE_1, Type.LABEL_MATCH);

    // Match language 2: should come from second vocabulary.
    assertSingleMatch(matcher, LANGUAGE_2_CODE_3, LANGUAGE_2_CODE_3, Type.CODE_MATCH);
    assertSingleMatch(matcher, LANGUAGE_2_LABEL_1, LANGUAGE_2_CODE_3, Type.LABEL_MATCH);
    assertSingleMatch(matcher, LANGUAGE_2_LABEL_2, LANGUAGE_2_CODE_3, Type.LABEL_MATCH);
    assertSingleMatch(matcher, LANGUAGE_2_LABEL_3, LANGUAGE_2_CODE_3, Type.LABEL_MATCH);

    // Match nonexisting codes and labels.
    assertSingleMatch(matcher, NONEXISTING_CODE, null, Type.NO_MATCH);
    assertSingleMatch(matcher, NONEXISTING_LABEL, null, Type.NO_MATCH);
  }

  private void assertSingleMatch(final LanguageMatcher matcher, String input, String expectedResult,
      Type expectedMatchType) {
    final List<LanguageMatch> matchResult = matcher.match(input);
    assertEquals(1, matchResult.size());
    assertMatch(matchResult.get(0), input, expectedResult, expectedMatchType);
  }

  private void assertMatch(LanguageMatch matchResult, String input, String expectedResult,
      Type expectedMatchType) {
    assertEquals(input, matchResult.getInput());
    assertEquals(expectedResult, matchResult.getMatch());
    assertEquals(expectedMatchType, matchResult.getType());
  }
}
