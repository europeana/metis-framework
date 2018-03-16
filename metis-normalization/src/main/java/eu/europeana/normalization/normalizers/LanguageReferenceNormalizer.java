package eu.europeana.normalization.normalizers;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import eu.europeana.normalization.languages.LanguageMatch;
import eu.europeana.normalization.languages.LanguageMatch.Type;
import eu.europeana.normalization.languages.LanguageMatcher;
import eu.europeana.normalization.settings.LanguageElement;
import eu.europeana.normalization.util.XpathQuery;

/**
 * This normalizer normalizes language references. It uses the functionality in
 * {@link LanguageMatcher}.
 */
public class LanguageReferenceNormalizer implements ValueNormalizeAction {

  protected static final float CONFIDENCE_SINGLE_CODE_EQUALS = 1.0F;
  protected static final float CONFIDENCE_SINGLE_CODE_KNOWN = 0.98F;
  protected static final float CONFIDENCE_LABELS_OR_CODES_MATCHES = 0.95F;
  protected static final float CONFIDENCE_LABELS_AND_CODES_MATCHES = 0.85F;

  private final float minimumConfidence;
  private final LanguageMatcher matcher;
  private final XpathQuery elementsToNormalize;

  /**
   * Constructor.
   * 
   * @param languageMatcher A language matcher.
   * @param minimumConfidence The minimum confidence to apply to normalizations.
   * @param elementsToNormalize The elements to normalize.
   */
  public LanguageReferenceNormalizer(LanguageMatcher languageMatcher, float minimumConfidence,
      LanguageElement[] elementsToNormalize) {
    this.matcher = languageMatcher;
    this.elementsToNormalize = XpathQuery.combine(Stream.of(elementsToNormalize)
        .map(LanguageElement::getElementQuery).toArray(XpathQuery[]::new));
    this.minimumConfidence = minimumConfidence;
  }

  @Override
  public List<NormalizedValueWithConfidence> normalizeValue(String label) {

    // Match the input.
    final List<LanguageMatch> matches = matcher.match(label);

    // Determine and check the confidence (also checks against empty match)
    final Float confidence = determineConfidence(matches);
    if (confidence == null || confidence < minimumConfidence) {
      return Collections.emptyList();
    }

    // Return the result.
    return matches.stream().map(LanguageMatch::getMatch).distinct()
        .map(language -> new NormalizedValueWithConfidence(language, confidence))
        .collect(Collectors.toList());
  }

  /**
   * Determine the confidence in a match result.
   * 
   * @param matches The match result of a given input string.
   * @return The confidence in the match. Or null if matching did not succeed.
   */
  Float determineConfidence(List<LanguageMatch> matches) {

    // Sanity check
    if (matches.isEmpty()) {
      return null;
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
      confidence = CONFIDENCE_LABELS_OR_CODES_MATCHES;
    } else {
      confidence = CONFIDENCE_LABELS_AND_CODES_MATCHES;
    }

    // Done
    return confidence;
  }


  @Override
  public RecordNormalizeAction getAsRecordNormalizer() {
    return new ValueNormalizeActionWrapper(this, elementsToNormalize);
  }
}
