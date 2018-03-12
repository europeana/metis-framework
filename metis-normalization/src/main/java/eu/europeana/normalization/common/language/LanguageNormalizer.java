package eu.europeana.normalization.common.language;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import eu.europeana.normalization.common.NormalizeDetails;
import eu.europeana.normalization.common.RecordNormalization;
import eu.europeana.normalization.common.ValueNormalization;
import eu.europeana.normalization.common.normalizers.ValueToRecordNormalizationWrapper;
import eu.europeana.normalization.languages.LanguageMatch;
import eu.europeana.normalization.languages.LanguageMatch.Type;
import eu.europeana.normalization.languages.LanguageMatcher;
import eu.europeana.normalization.languages.Languages;
import eu.europeana.normalization.languages.LanguagesVocabulary;
import eu.europeana.normalization.util.Namespace;
import eu.europeana.normalization.util.NormalizationConfigurationException;
import eu.europeana.normalization.util.XpathQuery;

/**
 * The main Class to be used by applications applying this lib's langage normalization techniques
 *
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 16/03/2016
 */
public class LanguageNormalizer implements ValueNormalization {

  private static final XpathQuery DC_LANGUAGE_QUERY = XpathQuery.create("//%s/%s",
      Namespace.ORE.getElement("Proxy"), Namespace.DC.getElement("language"));

  private static final XpathQuery XML_LANG_QUERY = XpathQuery.create("//*[@%s]/@%s",
      Namespace.XML.getElement("lang"), Namespace.XML.getElement("lang"));

  private static final XpathQuery COMBINED_QUERY =
      XpathQuery.combine(DC_LANGUAGE_QUERY, XML_LANG_QUERY);

  public enum SupportedOperations {

    DC_LANGUAGE(DC_LANGUAGE_QUERY), XML_LANG(XML_LANG_QUERY), ALL(COMBINED_QUERY);

    private final XpathQuery languageQuery;

    private SupportedOperations(XpathQuery languageQuery) {
      this.languageQuery = languageQuery;
    }
  }

  private Float minimumConfidence;
  private final LanguageMatcher matcher;
  private SupportedOperations operations = SupportedOperations.ALL;

  /**
   * Creates a new instance of this class.
   * 
   * @throws NormalizationConfigurationException
   */
  public LanguageNormalizer(LanguagesVocabulary targetVocab, Float minimumConfidence)
      throws NormalizationConfigurationException {
    super();
    Languages matchingVocab = Languages.getLanguages();
    matchingVocab.setTargetVocabulary(targetVocab);
    matcher = new LanguageMatcher(matchingVocab);

  }

  public List<String> normalize(String value) {
    List<NormalizeDetails> normalizeDetailedRes = normalizeDetailed(value);
    List<String> res = new ArrayList<>(normalizeDetailedRes.size());
    for (NormalizeDetails dtl : normalizeDetailedRes) {
      res.add(dtl.getNormalizedValue());
    }
    return res;
  }


  public List<NormalizeDetails> normalizeDetailed(String label) {

    // Match the input. If there are no results, we are done.
    final List<LanguageMatch> matches = matcher.match(label);
    if (matches.isEmpty()) {
      return Collections.emptyList();
    }

    // Do some analysis.
    final Set<Type> matchTypes =
        matches.stream().map(LanguageMatch::getType).distinct().collect(Collectors.toSet());
    final boolean justCodeMatches = matchTypes.size() == 1 && matchTypes.contains(Type.CODE_MATCH);
    final boolean justLabelMatches = matchTypes.size() == 1 && matchTypes.contains(Type.LABEL_MATCH);
    final boolean justOneMatch = matches.size()==1;
    final boolean matchesFailed = matchTypes.contains(Type.NO_MATCH);

    // Determine confidence.
    final Float confidence;
    if (matchesFailed) {
      confidence = null;
    } else if (justCodeMatches && justOneMatch) {
      final LanguageMatch match = matches.get(0);
      if (match.getInput().equals(match.getMatch())) {
        confidence = 1.0F;
      } else {
        confidence = 0.98F;
      }
    } else if (justCodeMatches || justLabelMatches) {
      confidence = 0.95F;
    } else {
      confidence = 0.85F;
    }

    // Check the confidence.
    if (confidence == null || confidence < minimumConfidence) {
      return Collections.emptyList();
    }

    // Return the result.
    final Set<String> matchedLanguages =
        matches.stream().map(LanguageMatch::getMatch).distinct().collect(Collectors.toSet());
    return matchedLanguages.stream().map(language -> new NormalizeDetails(language, confidence))
        .collect(Collectors.toList());
  }

  @Override
  public RecordNormalization toEdmRecordNormalizer() {
    return new ValueToRecordNormalizationWrapper(this, false, operations.languageQuery);
  }

  public LanguageNormalizer setOperations(SupportedOperations operations) {
    this.operations = operations;
    return this;
  }
}
