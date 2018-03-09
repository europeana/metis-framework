package eu.europeana.normalization.common.language;


import java.util.ArrayList;
import java.util.List;
import eu.europeana.normalization.common.NormalizeDetails;
import eu.europeana.normalization.common.RecordNormalization;
import eu.europeana.normalization.common.ValueNormalization;
import eu.europeana.normalization.common.language.nal.AmbiguousLabelMatchException;
import eu.europeana.normalization.common.language.nal.EuropeanLanguagesNal;
import eu.europeana.normalization.common.language.nal.LanguageMatcher;
import eu.europeana.normalization.common.normalizers.ValueToRecordNormalizationWrapper;
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
  private final LanguageMatcher normalizer;
  private SupportedOperations operations = SupportedOperations.ALL;

  /**
   * Creates a new instance of this class.
   * 
   * @throws NormalizationConfigurationException
   */
  public LanguageNormalizer(LanguagesVocabulary targetVocab, Float minimumConfidence)
      throws NormalizationConfigurationException {
    super();
    EuropeanLanguagesNal matchingVocab = new EuropeanLanguagesNal();
    matchingVocab.setTargetVocabulary(targetVocab);
    normalizer = new LanguageMatcher(matchingVocab);

  }

  public List<String> normalize(String value) {
    List<NormalizeDetails> normalizeDetailedRes = normalizeDetailed(value);
    List<String> res = new ArrayList<>(normalizeDetailedRes.size());
    for (NormalizeDetails dtl : normalizeDetailedRes) {
      res.add(dtl.getNormalizedValue());
    }
    return res;
  }

  public List<NormalizeDetails> normalizeDetailed(String lbl) {
    List<NormalizeDetails> res = new ArrayList<>();

    String normalized = normalizer.findIsoCodeMatch(lbl, lbl);
    if (normalized != null && normalized.equalsIgnoreCase(lbl)) {
      res.add(new NormalizeDetails(normalized, 1));
    } else if (normalized != null) {
      res.add(new NormalizeDetails(normalized, 0.98f));
    } else {
      List<String> normalizeds;
      try {
        normalizeds = normalizer.findLabelMatches(lbl);
        if (!normalizeds.isEmpty()) {
          res.addAll(NormalizeDetails.newList(normalizeds, 0.95f));
        } else {
          normalizeds = normalizer.findLabelAllWordMatches(lbl);
          if (!normalizeds.isEmpty()) {
            res.addAll(NormalizeDetails.newList(normalizeds, 0.95f));
          } else {
            normalizeds = normalizer.findLabelWordMatches(lbl);
            if (!normalizeds.isEmpty()) {
              res.addAll(NormalizeDetails.newList(normalizeds, 0.85f));
            }
          }
        }
      } catch (AmbiguousLabelMatchException e) {
        res.add(new NormalizeDetails(e.getAmbigouosMatches().get(0),
            1f / (float) e.getAmbigouosMatches().size()));
      }
    }
    if (minimumConfidence != null) {
      for (int i = 0; i < res.size(); i++) {
        NormalizeDetails n = res.get(i);
        if (n.getConfidence() < minimumConfidence) {
          res.remove(i);
          i--;
        }
      }
    }
    return res;
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
