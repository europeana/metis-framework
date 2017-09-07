/* LanguageNormalizer.java - created on 16/03/2016, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.normalization.common.language;


import eu.europeana.normalization.common.NormalizeDetails;
import eu.europeana.normalization.common.RecordNormalization;
import eu.europeana.normalization.common.ValueNormalization;
import eu.europeana.normalization.common.language.nal.AmbiguousLabelMatchException;
import eu.europeana.normalization.common.language.nal.EuropeanLanguagesNal;
import eu.europeana.normalization.common.util.Namespaces;
import eu.europeana.normalization.common.language.nal.LanguageMatcher;
import eu.europeana.normalization.common.normalizers.ValueToRecordNormalizationWrapper;
import eu.europeana.normalization.common.normalizers.ValueToRecordNormalizationWrapper.XpathQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The main Class to be used by applications applying this lib's langage normalization techniques
 *
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 16/03/2016
 */
public class LanguageNormalizer implements ValueNormalization {

  @SuppressWarnings("unused")
  private static java.util.logging.Logger log = java.util.logging.Logger
      .getLogger(LanguageNormalizer.class.getName());

  ;
  Float minimumConfidence;
  LanguageMatcher normalizer;
  SupportedOperations operations = SupportedOperations.ALL;

  /**
   * Creates a new instance of this class.
   */
  public LanguageNormalizer(LanguagesVocabulary targetVocab, Float minimumConfidence) {
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
          // if (!lbl.endsWith("[Metadata]") && !lbl.endsWith("[Resource]")) {// Some invalid values that were
          // present when research was underway. Ingestion will clean these values later
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
    HashMap<String, String> namespacesPrefixes = new HashMap<String, String>() {{
      put("dc", Namespaces.DC);
      put("ore", Namespaces.ORE);
      put("xml", Namespaces.XML);
    }};
    @SuppressWarnings("serial")
    XpathQuery dcLanguageQuery = null;
    switch (operations) {
      case ALL:
        dcLanguageQuery = new XpathQuery(
            namespacesPrefixes, "//ore:Proxy/dc:language | //*[@xml:lang]/@xml:lang");
        break;
      case DC_LANGUAGE:
        dcLanguageQuery = new XpathQuery(
            namespacesPrefixes, "//ore:Proxy/dc:language");
        break;
      case XML_LANG:
        dcLanguageQuery = new XpathQuery(
            namespacesPrefixes, "//*[@xml:lang]/@xml:lang");
        break;
    }
    ValueToRecordNormalizationWrapper dcLanguageNorm = new ValueToRecordNormalizationWrapper(this,
        false, dcLanguageQuery);
    return dcLanguageNorm;
  }

  public LanguageNormalizer setOperations(SupportedOperations operations) {
    this.operations = operations;
    return this;
  }

  public enum SupportedOperations {DC_LANGUAGE, XML_LANG, ALL}

}
