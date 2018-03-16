package eu.europeana.normalization.languages;

import static org.junit.Assert.assertEquals;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import eu.europeana.normalization.languages.LanguageMatch.Type;
import eu.europeana.normalization.settings.AmbiguityHandling;
import eu.europeana.normalization.util.NormalizationConfigurationException;

public class LanguageMatcherTest {

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

  @BeforeClass
  public static void prepareTests() throws NormalizationConfigurationException {

    final Language language1 = new Language();
    language1.setIso6391(LANGUAGE_1_CODE_1);
    language1.setIso6392b(LANGUAGE_1_CODE_2B);
    language1.setIso6392t(LANGUAGE_1_CODE_2T);
    language1.setIso6393(LANGUAGE_1_CODE_3);
    language1.setAuthorityCode(LANGUAGE_1_CODE_NAL);
    language1.addOriginalNames(
        Arrays.asList(new LanguageLabel(LANGUAGE_1_LABEL_1, LABEL_LANGUAGE_1, LABEL_SCRIPT)));
    language1.addLabels(
        Arrays.asList(new LanguageLabel(LANGUAGE_1_LABEL_2, LABEL_LANGUAGE_2, LABEL_SCRIPT)));
    language1.addLabels(
        Arrays.asList(new LanguageLabel(LANGUAGE_1_LABEL_3, LABEL_LANGUAGE_1, LABEL_SCRIPT)));
    language1.addLabels(
        Arrays.asList(new LanguageLabel(LANGUAGE_1_LABEL_3, LABEL_LANGUAGE_2, LABEL_SCRIPT)));
    language1.addLabels(
        Arrays.asList(new LanguageLabel(AMBIGUOUS_LABEL, LABEL_LANGUAGE_1, LABEL_SCRIPT)));
    language1
        .addLabels(Arrays.asList(new LanguageLabel(SHORT_LABEL, LABEL_LANGUAGE_1, LABEL_SCRIPT)));

    final Language language2 = new Language();
    language2.setIso6393(LANGUAGE_2_CODE_3);
    language2.addOriginalNames(
        Arrays.asList(new LanguageLabel(LANGUAGE_2_LABEL_1, LABEL_LANGUAGE_1, LABEL_SCRIPT)));
    language2.addLabels(
        Arrays.asList(new LanguageLabel(LANGUAGE_2_LABEL_2, LABEL_LANGUAGE_2, LABEL_SCRIPT)));
    language2.addLabels(
        Arrays.asList(new LanguageLabel(LANGUAGE_2_LABEL_3, LABEL_LANGUAGE_1, LABEL_SCRIPT)));
    language2.addLabels(
        Arrays.asList(new LanguageLabel(LANGUAGE_2_LABEL_3, LABEL_LANGUAGE_2, LABEL_SCRIPT)));
    language2.addLabels(
        Arrays.asList(new LanguageLabel(AMBIGUOUS_LABEL, LABEL_LANGUAGE_1, LABEL_SCRIPT)));

    final Language language3 = new Language();
    language3.setIso6393(LANGUAGE_3_CODE_3);
    language3.addLabels(
        Arrays.asList(new LanguageLabel(AMBIGUOUS_LABEL, LABEL_LANGUAGE_2, LABEL_SCRIPT)));

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
  public void testSingleMatches() {

    // Create matcher
    final LanguageMatcher matcher = new LanguageMatcher(4, AmbiguityHandling.NO_MATCH,
        LanguagesVocabulary.ISO_639_3, languages, Function.identity());

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

    // Match locale
    assertSingleMatch(matcher, LANGUAGE_1_CODE_1 + "-XX", LANGUAGE_1_CODE_3, Type.CODE_MATCH);
  }

  @Test
  public void testMultipleMatches() {

    // Create matcher
    final LanguageMatcher matcher = new LanguageMatcher(4, AmbiguityHandling.NO_MATCH,
        LanguagesVocabulary.ISO_639_3, languages, Function.identity());

    // Match multiple existing codes
    final List<LanguageMatch> matchCodes =
        matcher.match(String.join(" ", LANGUAGE_1_CODE_1, LANGUAGE_1_CODE_3, LANGUAGE_2_CODE_3));
    assertEquals(3, matchCodes.size());
    assertMatch(matchCodes.get(0), LANGUAGE_1_CODE_1, LANGUAGE_1_CODE_3, Type.CODE_MATCH);
    assertMatch(matchCodes.get(1), LANGUAGE_1_CODE_3, LANGUAGE_1_CODE_3, Type.CODE_MATCH);
    assertMatch(matchCodes.get(2), LANGUAGE_2_CODE_3, LANGUAGE_2_CODE_3, Type.CODE_MATCH);

    // Match multiple existing labels
    final List<LanguageMatch> matchLabels =
        matcher.match(String.join(" ", LANGUAGE_1_LABEL_1, LANGUAGE_2_LABEL_1, LANGUAGE_2_LABEL_3));
    assertEquals(3, matchLabels.size());
    assertMatch(matchLabels.get(0), LANGUAGE_1_LABEL_1, LANGUAGE_1_CODE_3, Type.LABEL_MATCH);
    assertMatch(matchLabels.get(1), LANGUAGE_2_LABEL_1, LANGUAGE_2_CODE_3, Type.LABEL_MATCH);
    assertMatch(matchLabels.get(2), LANGUAGE_2_LABEL_3, LANGUAGE_2_CODE_3, Type.LABEL_MATCH);

    // Match mix of existing codes and labels
    final List<LanguageMatch> matchMix =
        matcher.match(String.join(" ", LANGUAGE_1_CODE_1, LANGUAGE_2_LABEL_1));
    assertEquals(2, matchMix.size());
    assertMatch(matchMix.get(0), LANGUAGE_1_CODE_1, LANGUAGE_1_CODE_3, Type.CODE_MATCH);
    assertMatch(matchMix.get(1), LANGUAGE_2_LABEL_1, LANGUAGE_2_CODE_3, Type.LABEL_MATCH);
  }

  @Test
  public void testAmbiguousLabels() {

    // Test ambiguity handling NO_MATCH.
    final LanguageMatcher matcherChooseNone = new LanguageMatcher(4, AmbiguityHandling.NO_MATCH,
        LanguagesVocabulary.ISO_639_3, languages, Function.identity());
    assertSingleMatch(matcherChooseNone, AMBIGUOUS_LABEL, null, Type.NO_MATCH);

    // Test ambiguity handling FIRST_MATCH.
    final LanguageMatcher matcherChooseFirst =
        new LanguageMatcher(4, AmbiguityHandling.CHOOSE_FIRST, LanguagesVocabulary.ISO_639_3,
            languages, Function.identity());
    assertSingleMatch(matcherChooseFirst, AMBIGUOUS_LABEL, LANGUAGE_1_CODE_3, Type.LABEL_MATCH);
  }


  @Test
  public void testNonexistingCodesAndLabels() {

    // Test what happens when a nonexisting code is matched.
    final LanguageMatcher matcher = new LanguageMatcher(4, AmbiguityHandling.NO_MATCH,
        LanguagesVocabulary.ISO_639_3, languages, Function.identity());
    assertSingleMatch(matcher, NONEXISTING_CODE, null, Type.NO_MATCH);
    assertSingleMatch(matcher, NONEXISTING_LABEL, null, Type.NO_MATCH);
  }

  @Test
  public void testLanguageWithoutCode() {

    // Test what happens when a language doesn't have a code in the target vocabulary
    final LanguageMatcher matcher = new LanguageMatcher(4, AmbiguityHandling.NO_MATCH,
        LanguagesVocabulary.ISO_639_1, languages, Function.identity());
    assertSingleMatch(matcher, LANGUAGE_1_CODE_1, LANGUAGE_1_CODE_1, Type.CODE_MATCH);
    assertSingleMatch(matcher, LANGUAGE_1_CODE_3, LANGUAGE_1_CODE_1, Type.CODE_MATCH);
    assertSingleMatch(matcher, LANGUAGE_1_LABEL_1, LANGUAGE_1_CODE_1, Type.LABEL_MATCH);
    assertSingleMatch(matcher, LANGUAGE_2_CODE_3, null, Type.NO_MATCH);
    assertSingleMatch(matcher, LANGUAGE_2_LABEL_1, null, Type.NO_MATCH);
  }

  @Test(expected = RuntimeException.class)
  public void testLanguageCodeWithBadCharacters() {
    final Language language = new Language();
    language.setIso6391("A1a");
    new LanguageMatcher(4, AmbiguityHandling.NO_MATCH, LanguagesVocabulary.ISO_639_1,
        createLanguages(language), Function.identity());
  }

  @Test(expected = RuntimeException.class)
  public void testLanguageCodeOfWrongLength() {
    final Language language = new Language();
    language.setIso6391("aaaa");
    new LanguageMatcher(4, AmbiguityHandling.NO_MATCH, LanguagesVocabulary.ISO_639_1,
        createLanguages(language), Function.identity());
  }

  @Test(expected = RuntimeException.class)
  public void testConflictingLanguageCodes() {
    final Language language1 = new Language();
    language1.setIso6391("aaa");
    language1.setIso6393("aaa");
    final Language language2 = new Language();
    language2.setIso6391("aaa");
    language2.setIso6393("aab");
    new LanguageMatcher(4, AmbiguityHandling.NO_MATCH, LanguagesVocabulary.ISO_639_3,
        createLanguages(language1, language2), Function.identity());
  }

  @Test
  public void testShortLanguageLabels() {
    final LanguageMatcher matcherShort = new LanguageMatcher(SHORT_LABEL.length(),
        AmbiguityHandling.NO_MATCH, LanguagesVocabulary.ISO_639_3, languages, Function.identity());
    assertSingleMatch(matcherShort, SHORT_LABEL, LANGUAGE_1_CODE_3, Type.LABEL_MATCH);
    final LanguageMatcher matcherLong = new LanguageMatcher(SHORT_LABEL.length() + 1,
        AmbiguityHandling.NO_MATCH, LanguagesVocabulary.ISO_639_3, languages, Function.identity());
    assertSingleMatch(matcherLong, SHORT_LABEL, null, Type.NO_MATCH);
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
