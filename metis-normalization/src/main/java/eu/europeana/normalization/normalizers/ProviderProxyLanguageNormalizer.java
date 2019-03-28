package eu.europeana.normalization.normalizers;

import eu.europeana.normalization.languages.LanguageMatch;
import eu.europeana.normalization.languages.LanguageMatch.Type;
import eu.europeana.normalization.languages.LanguageMatcher;
import eu.europeana.normalization.normalizers.ValueNormalizeActionWrapper.CopySettings;
import eu.europeana.normalization.util.Namespace;
import eu.europeana.normalization.util.Namespace.Element;
import eu.europeana.normalization.util.XpathQuery;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This normalizer normalizes language references in the provider proxy. It uses the functionality
 * in {@link LanguageMatcher}. It doesn't overwrite the values, but instead copies them to the
 * Europeana proxy.
 */
public class ProviderProxyLanguageNormalizer implements ValueNormalizeAction {

  protected static final float CONFIDENCE_SINGLE_CODE_EQUALS = 1.0F;
  protected static final float CONFIDENCE_SINGLE_CODE_KNOWN = 0.98F;
  protected static final float CONFIDENCE_LABELS_OR_CODES_MATCHES = 0.95F;
  protected static final float CONFIDENCE_LABELS_AND_CODES_MATCHES = 0.85F;

  private static final Element DC_LANGUAGE = Namespace.DC.getElement("language");
  private static final Element ORE_PROXY = Namespace.ORE.getElement("Proxy");
  private static final Element EDM_EUROPEANA_PROXY = Namespace.EDM.getElement("europeanaProxy");

  private static final XpathQuery PROVIDER_PROXY_LANGUAGES = new XpathQuery(
      "/%s/%s[not(%s='true')]/%s", XpathQuery.RDF_TAG, ORE_PROXY, EDM_EUROPEANA_PROXY, DC_LANGUAGE);
  private static final XpathQuery EUROPEANA_PROXY = new XpathQuery("/%s/%s[%s='true']",
      XpathQuery.RDF_TAG, ORE_PROXY, EDM_EUROPEANA_PROXY);
  private final float minimumConfidence;
  private final LanguageMatcher matcher;

  /**
   * Constructor.
   * 
   * @param languageMatcher A language matcher.
   * @param minimumConfidence The minimum confidence to apply to normalizations.
   */
  public ProviderProxyLanguageNormalizer(LanguageMatcher languageMatcher, float minimumConfidence) {
    this.matcher = languageMatcher;
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
    final Set<Type> matchTypes = matches.stream().map(LanguageMatch::getType)
        .collect(Collectors.toSet());
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
    final CopySettings copySettings = new CopySettings(EUROPEANA_PROXY, DC_LANGUAGE);
    return new ValueNormalizeActionWrapper(this, copySettings, PROVIDER_PROXY_LANGUAGES);
  }
}
