package eu.europeana.normalization.normalizers;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import eu.europeana.normalization.languages.LanguageMatch;
import eu.europeana.normalization.languages.LanguageMatch.Type;
import eu.europeana.normalization.languages.LanguageMatcher;
import eu.europeana.normalization.settings.LanguageElements;

/**
 * This normalizer normalizes language references. It uses the functionality in
 * {@link LanguageMatcher}.
 *
 * @author Nuno Freire (nfreire@gmail.com)
 */
public class LanguageReferenceNormalizer implements ValueNormalizer {

  private static final float CONFIDENCE_SINGLE_CODE_EQUALS = 1.0F;
  private static final float CONFIDENCE_SINGLE_CODE_KNOWN = 0.98F;
  private static final float CONFIDENCE_LABELS_OR_CODES_MATCHED = 0.95F;
  private static final float CONFIDENCE_LABELS_AND_CODES_MATCHED = 0.85F;

  private final float minimumConfidence;
  private final LanguageMatcher matcher;
  private final LanguageElements elementsToNormalize;

  /**
   * Constructor.
   * 
   * @param languageMatcher A language matcher.
   * @param minimumConfidence The minimum confidence to apply to normalizations.
   * @param elementsToNormalize The elements to normalize.
   */
  public LanguageReferenceNormalizer(LanguageMatcher languageMatcher, float minimumConfidence,
      LanguageElements elementsToNormalize) {
    this.matcher = languageMatcher;
    this.elementsToNormalize = elementsToNormalize;
    this.minimumConfidence = minimumConfidence;
  }

  @Override
  public List<NormalizedValueWithConfidence> normalizeValue(String label) {

    // Match the input. If there are no results, we are done.
    final List<LanguageMatch> matches = matcher.match(label);
    if (matches.isEmpty()) {
      return Collections.emptyList();
    }

    // Do some analysis.
    final Set<Type> matchTypes =
        matches.stream().map(LanguageMatch::getType).distinct().collect(Collectors.toSet());
    final boolean justCodeMatches = matchTypes.size() == 1 && matchTypes.contains(Type.CODE_MATCH);
    final boolean justLabelMatches =
        matchTypes.size() == 1 && matchTypes.contains(Type.LABEL_MATCH);
    final boolean justOneMatch = matches.size() == 1;
    final boolean matchesFailed = matchTypes.contains(Type.NO_MATCH);

    // Determine confidence.
    final Float confidence;
    if (matchesFailed) {
      confidence = null;
    } else if (justCodeMatches && justOneMatch) {
      final LanguageMatch match = matches.get(0);
      if (match.getInput().equals(match.getMatch())) {
        confidence = CONFIDENCE_SINGLE_CODE_EQUALS;
      } else {
        confidence = CONFIDENCE_SINGLE_CODE_KNOWN;
      }
    } else if (justCodeMatches || justLabelMatches) {
      confidence = CONFIDENCE_LABELS_OR_CODES_MATCHED;
    } else {
      confidence = CONFIDENCE_LABELS_AND_CODES_MATCHED;
    }

    // Check the confidence.
    if (confidence == null || confidence < minimumConfidence) {
      return Collections.emptyList();
    }

    // Return the result.
    final Set<String> matchedLanguages =
        matches.stream().map(LanguageMatch::getMatch).distinct().collect(Collectors.toSet());
    return matchedLanguages.stream()
        .map(language -> new NormalizedValueWithConfidence(language, confidence))
        .collect(Collectors.toList());
  }

  @Override
  public RecordNormalizer getAsRecordNormalizer() {
    return new ValueNormalizerWrapper(this, elementsToNormalize.getElementQuery());
  }
}
