package eu.europeana.normalization.normalizers;


import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import eu.europeana.normalization.languages.LanguageMatch;
import eu.europeana.normalization.languages.LanguageMatch.Type;
import eu.europeana.normalization.languages.LanguageMatcher;
import eu.europeana.normalization.languages.Languages;
import eu.europeana.normalization.languages.LanguagesVocabulary;
import eu.europeana.normalization.util.Namespace;
import eu.europeana.normalization.util.NormalizationConfigurationException;
import eu.europeana.normalization.util.XpathQuery;

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

  private static final XpathQuery DC_LANGUAGE_QUERY = XpathQuery.create("//%s/%s",
      Namespace.ORE.getElement("Proxy"), Namespace.DC.getElement("language"));

  private static final XpathQuery XML_LANG_QUERY = XpathQuery.create("//*[@%s]/@%s",
      Namespace.XML.getElement("lang"), Namespace.XML.getElement("lang"));

  private static final XpathQuery COMBINED_QUERY =
      XpathQuery.combine(DC_LANGUAGE_QUERY, XML_LANG_QUERY);


  /**
   * This enum contains the language elements that this normalizer can normalize.
   */
  public enum SupportedElements {

    /** The tag dc:language **/
    DC_LANGUAGE(DC_LANGUAGE_QUERY),

    /** The attribute xml:lang **/
    XML_LANG(XML_LANG_QUERY),

    /** The combination of all elements. **/
    ALL(COMBINED_QUERY);

    private final XpathQuery languageQuery;

    private SupportedElements(XpathQuery languageQuery) {
      this.languageQuery = languageQuery;
    }
  }

  private final float minimumConfidence;
  private final LanguageMatcher matcher;
  private final SupportedElements elements;

  /**
   * Constructor.
   * 
   * @param targetVocabulary The vocabulary to which to normalize language values.
   * @param minimumConfidence The minimum confidence to apply to normalizations.
   * @param elements The elements to normalize.
   * @throws NormalizationConfigurationException In case there is a problem with configuring the
   *         normalization.
   */
  public LanguageReferenceNormalizer(LanguagesVocabulary targetVocabulary, float minimumConfidence,
      SupportedElements elements) throws NormalizationConfigurationException {
    final Languages matchingVocab = Languages.getLanguages();
    matchingVocab.setTargetVocabulary(targetVocabulary);
    this.matcher = new LanguageMatcher(matchingVocab);
    this.elements = elements;
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
    return new ValueNormalizerWrapper(this, elements.languageQuery);
  }
}
