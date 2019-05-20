package eu.europeana.normalization.languages;

import eu.europeana.normalization.languages.LanguageMatch.Type;
import eu.europeana.normalization.settings.AmbiguityHandling;
import eu.europeana.normalization.util.LanguageTag;
import eu.europeana.normalization.util.LanguageTagValueNormalizer;
import eu.europeana.normalization.util.NormalizationConfigurationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Provides the matching algorithms for matching dc:language values with codes and labels in the
 * Languages NAL
 *
 * @author Nuno Freire (nfreire@gmail.com)
 */
public class LanguageMatcher {

  private static final Pattern LANGUAGE_CODE_MATCHER = Pattern.compile("\\A\\p{Alpha}{2,3}\\Z");

  private final int minimumLabelLength;
  private final AmbiguityHandling ambiguityHandling;
  private final List<LanguagesVocabulary> targetVocabularies;
  private final Function<String, List<LanguageTag>> languageTagValueNormalizer;

  private final Map<String, String> isoCodes = new HashMap<>();
  private final Map<String, String> unambiguousLabels = new HashMap<>();
  private final HashMap<String, List<String>> ambiguousLabels = new HashMap<>();

  /**
   * Constructor.
   *
   * @param minimumLabelLength The minimum label length for indexing.
   * @param ambiguityHandling The minimum ambiguity handling for matching.
   * @param targetVocabularies The target vocabularies in which to return the match results. The
   * language code for a matching language will be decided by the first vocabulary in this list that
   * contains a code for that language. The list must contain at least one vocabulary.
   * @throws NormalizationConfigurationException In case the list of languages could not be
   * configured properly.
   */
  public LanguageMatcher(int minimumLabelLength, AmbiguityHandling ambiguityHandling,
      List<LanguagesVocabulary> targetVocabularies)
      throws NormalizationConfigurationException {
    this(minimumLabelLength, ambiguityHandling, targetVocabularies, Languages.getLanguages(),
        LanguageTagValueNormalizer::normalize);
  }

  /**
   * Constructor for Mocking.
   *
   * @param minimumLabelLength The minimum label length for indexing.
   * @param ambiguityHandling The minimum ambiguity handling for matching.
   * @param targetVocabularies The target vocabularies in which to return the match results. The
   * language code for a matching language will be decided by the first vocabulary in this list that
   * contains a code for that language. The list must contain at least one vocabulary.
   * @param languages The language vocabulary.
   * @param languageTagValueNormalizer The function that normalizes a language tag value.
   */
  LanguageMatcher(int minimumLabelLength, AmbiguityHandling ambiguityHandling,
      List<LanguagesVocabulary> targetVocabularies, Languages languages,
      Function<String, List<LanguageTag>> languageTagValueNormalizer) {
    this.minimumLabelLength = minimumLabelLength;
    this.ambiguityHandling = ambiguityHandling;
    this.targetVocabularies = new ArrayList<>(targetVocabularies);
    this.languageTagValueNormalizer = languageTagValueNormalizer;
    languages.getActiveLanguages().forEach(this::index);
  }

  private void index(Language language) {

    // Get the normalized language ID.
    final String languageId = targetVocabularies.stream().map(language::getNormalizedLanguageId)
        .filter(Objects::nonNull).findFirst().orElse(null);
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

    // Normalize the label. If it is split, we don't add it to the index: it can never be matched anyway.
    final List<LanguageTag> normalizationResult = languageTagValueNormalizer.apply(label);
    if (normalizationResult.size() != 1) {
      return;
    }

    // Add the label to the index.
    final String normalizedLabel = normalizationResult.get(0).getNormalizedInput();
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

    // Normalize the code.
    final List<LanguageTag> normalization = languageTagValueNormalizer.apply(code);
    if (normalization.size() != 1 || normalization.get(0).getSubTag() != null) {
      throw new IllegalStateException(
          "Empty ISO code, ISO code with spaces or ISO code with subtag detected: " + code);
    }

    // Add the code to the code index.
    final String normalizedCode = normalization.get(0).getLanguageCode();
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
    return languageTagValueNormalizer.apply(input).stream().map(this::matchNormalizedWord)
        .collect(Collectors.toList());
  }

  private LanguageMatch matchNormalizedWord(LanguageTag languageTag) {

    // The result.
    LanguageMatch result = null;

    // First try to match the code. Preserve the subtag if there is one.
    final String codeMatch = isoCodes.get(languageTag.getLanguageCode());
    if (codeMatch != null) {
      result = new LanguageMatch(languageTag.getNormalizedInput(),
          codeMatch + (languageTag.getSubTag() == null ? "" : languageTag.getSubTag()),
          Type.CODE_MATCH);
    }

    // If that doesn't work, we try to match an unambiguous label.
    if (result == null) {
      final String unambiguousMatch = unambiguousLabels.get(languageTag.getNormalizedInput());
      if (unambiguousMatch != null) {
        result = new LanguageMatch(languageTag.getNormalizedInput(), unambiguousMatch, Type.LABEL_MATCH);
      }
    }

    // Finally, we try to match an ambiguous label.
    if (result == null) {
      final List<String> ambiguousMatch = ambiguousLabels.get(languageTag.getNormalizedInput());
      if (ambiguousMatch != null) {
        final String match = ambiguityHandling.resolveAmbiguousMatch(ambiguousMatch);
        result = new LanguageMatch(languageTag.getNormalizedInput(), match, match == null ? Type.NO_MATCH : Type.LABEL_MATCH);
      }
    }

    // If nothing worked, we return an empty result.
    if (result == null) {
      result = new LanguageMatch(languageTag.getNormalizedInput(), null, Type.NO_MATCH);
    }
    return result;
  }
}
