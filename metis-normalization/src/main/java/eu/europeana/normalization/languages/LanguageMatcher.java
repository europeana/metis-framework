package eu.europeana.normalization.languages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import eu.europeana.normalization.languages.LanguageMatch.Type;
import eu.europeana.normalization.settings.AmbiguityHandling;
import eu.europeana.normalization.util.NormalizationConfigurationException;
import eu.europeana.normalization.util.StringNormalizer;

/**
 * Provides the matching algorithms for matching dc:language values with codes and labels in the
 * Languages NAL
 *
 * @author Nuno Freire (nfreire@gmail.com)
 */
public class LanguageMatcher {

  private static final Pattern LOCALE_CODE_PATTERN =
      Pattern.compile("\\A(\\p{Alpha}\\p{Alpha})-\\p{Alpha}\\p{Alpha}\\Z");

  private static final Pattern LANGUAGE_CODE_MATCHER = Pattern.compile("\\A\\p{Alpha}{2,3}\\Z");

  private final int minimumLabelLength;
  private final AmbiguityHandling ambiguityHandling;
  private final LanguagesVocabulary targetVocabulary;
  private final Function<String, String> stringNormalizer;

  private final Map<String, String> isoCodes = new HashMap<>();
  private final Map<String, String> unambiguousLabels = new HashMap<>();
  private final HashMap<String, List<String>> ambiguousLabels = new HashMap<>();

  /**
   * Constructor.
   * 
   * @param minimumLabelLength The minimum label length for indexing.
   * @param ambiguityHandling The minimum ambiguity handling for matching.
   * @param targetVocabulary The target vocabulary in which to return the match results.
   * @throws NormalizationConfigurationException In case the list of languages could not be
   *         configured properly.
   */
  public LanguageMatcher(int minimumLabelLength, AmbiguityHandling ambiguityHandling,
      LanguagesVocabulary targetVocabulary) throws NormalizationConfigurationException {
    this(minimumLabelLength, ambiguityHandling, targetVocabulary, Languages.getLanguages(),
        StringNormalizer::normalize);
  }

  /**
   * Constructor for Mocking.
   * 
   * @param minimumLabelLength The minimum label length for indexing.
   * @param ambiguityHandling The minimum ambiguity handling for matching.
   * @param targetVocabulary The target vocabulary in which to return the match results.
   * @param languages The language vocabulary.
   * @param stringNormalizer The function that normalizes a string, used for indexing.
   */
  LanguageMatcher(int minimumLabelLength, AmbiguityHandling ambiguityHandling,
      LanguagesVocabulary targetVocabulary, Languages languages,
      Function<String, String> stringNormalizer) {
    this.minimumLabelLength = minimumLabelLength;
    this.ambiguityHandling = ambiguityHandling;
    this.targetVocabulary = targetVocabulary;
    this.stringNormalizer = stringNormalizer;
    for (Language l : languages.getActiveLanguages()) {
      index(l);
    }
  }

  private void index(Language language) {

    // Get the normalized language ID.
    final String languageId = language.getNormalizedLanguageId(targetVocabulary);
    if (languageId == null) {
      return;
    }

    // Add all known language codes to the index.
    addCodeToIndex(language.getIso6391(), languageId);
    addCodeToIndex(language.getIso6392b(), languageId);
    addCodeToIndex(language.getIso6392t(), languageId);
    addCodeToIndex(language.getIso6393(), languageId);
    addCodeToIndex(language.getAuthorityCode(), languageId);

    // Add all known language labels to the index.
    for (String label : language.getAllLabels()) {
      if (label.length() >= minimumLabelLength) {
        addLabelToIndex(label, languageId);
      }
    }
  }

  private void addLabelToIndex(String label, String languageId) {
    final String normalizedLabel = stringNormalizer.apply(label);
    if (ambiguousLabels.containsKey(normalizedLabel)) {
      final List<String> alternatives = ambiguousLabels.get(normalizedLabel);
      if (!alternatives.contains(languageId)) {
        alternatives.add(languageId);
      }
    } else if (unambiguousLabels.containsKey(normalizedLabel)) {
      if (!unambiguousLabels.get(normalizedLabel).equals(languageId)) {
        final String oldValue = unambiguousLabels.remove(normalizedLabel);
        final List<String> alternatives = new ArrayList<>();
        alternatives.add(oldValue);
        alternatives.add(languageId);
        ambiguousLabels.put(normalizedLabel, alternatives);
      }
    } else {
      unambiguousLabels.put(normalizedLabel, languageId);
    }
  }

  private void addCodeToIndex(String code, String languageId) {

    // Check for null code.
    if (code == null) {
      return;
    }

    // Check that code satisfies format
    if (!LANGUAGE_CODE_MATCHER.matcher(code.trim()).matches()) {
      throw new IllegalArgumentException("Provided code does not qualify as a code: " + code);
    }

    // Add the code to the code index.
    final String normalizedCode = stringNormalizer.apply(code);
    if (isoCodes.containsKey(normalizedCode) && !isoCodes.get(normalizedCode).equals(languageId)) {
      throw new IllegalStateException("Ambiguous iso code detected: " + normalizedCode);
    }
    isoCodes.put(normalizedCode, languageId);
  }

  /**
   * Match the given input text. This method will tokenize the input into words and then match each
   * individual word. The result is a list of match results, one for each word.
   * 
   * @param input The input text.
   * @return The matches. Does not return null.
   */
  public List<LanguageMatch> match(String input) {

    // First check the locale matcher.
    final String localeMatch = findLocaleMatch(input);
    if (localeMatch != null) {
      return Collections.singletonList(new LanguageMatch(input, localeMatch, Type.CODE_MATCH));
    }

    // Normalize and then split by spaces. We should have non-empty words.
    final String[] words = stringNormalizer.apply(input).split("\\s+");

    // Match words and return the result.
    return Arrays.stream(words).map(this::matchNormalizedWord).collect(Collectors.toList());
  }

  private LanguageMatch matchNormalizedWord(String word) {

    // The result.
    LanguageMatch result = null;

    // First try to match the code.
    final String codeMatch = isoCodes.get(word);
    if (codeMatch != null) {
      result = new LanguageMatch(word, codeMatch, Type.CODE_MATCH);
    }

    // If that doesn't work, we try to match an unambiguous label.
    if (result == null) {
      final String unambiguousMatch = unambiguousLabels.get(word);
      if (unambiguousMatch != null) {
        result = new LanguageMatch(word, unambiguousMatch, Type.LABEL_MATCH);
      }
    }

    // Finally, we try to match an ambiguous label.
    if (result == null) {
      final List<String> ambiguousMatch = ambiguousLabels.get(word);
      if (ambiguousMatch != null) {
        final String match = ambiguityHandling.resolveAmbiguousMatch(ambiguousMatch);
        result = new LanguageMatch(word, match, match == null ? Type.NO_MATCH : Type.LABEL_MATCH);
      }
    }

    // If nothing worked, we return an empty result.
    if (result == null) {
      result = new LanguageMatch(word, null, Type.NO_MATCH);
    }
    return result;
  }

  private String findLocaleMatch(String rawValue) {
    final Matcher matcher = LOCALE_CODE_PATTERN.matcher(rawValue.trim());
    if (matcher.matches()) {
      return isoCodes.get(stringNormalizer.apply(matcher.group(1)));
    }
    return null;
  }
}
