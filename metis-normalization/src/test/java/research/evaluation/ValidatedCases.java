package research.evaluation;

import eu.europeana.normalization.languages.Languages;
import eu.europeana.normalization.languages.LanguagesVocabulary;
import eu.europeana.normalization.util.NormalizationConfigurationException;
import eu.europeana.normalization.languages.Language;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ValidatedCases {

  private final Map<String, Validation> validations;
  private EvaluationStats stats;
  private final LanguagesVocabulary targetVocabulary;
  private final Map<String, Language> isoCodeIndex;

  public ValidatedCases(File evaluationFolder, LanguagesVocabulary targetVocabulary)
      throws IOException, NormalizationConfigurationException {
    this.targetVocabulary = targetVocabulary;
    validations = new HashMap<>();
    
    isoCodeIndex = new Hashtable<>();
    for (Language l : Languages.getLanguages().getActiveLanguages()) {
      if (l.getIso6391() != null) {
        isoCodeIndex.put(l.getIso6391(), l);
      }
      if (l.getIso6392b() != null) {
        isoCodeIndex.put(l.getIso6392b(), l);
      }
      if (l.getIso6392t() != null) {
        isoCodeIndex.put(l.getIso6392t(), l);
      }
      if (l.getIso6393() != null) {
        isoCodeIndex.put(l.getIso6393(), l);
      }
      if (l.getAuthorityCode() != null) {
        isoCodeIndex.put(l.getAuthorityCode(), l);
      }
    }

    if (evaluationFolder == null) {
      return;
    }
    for (File f : evaluationFolder.listFiles()) {
      if (f.getName().startsWith("Evaluation") && f.getName().endsWith(".csv")) {
        CSVParser parser = new CSVParser(new FileReader(f), CSVFormat.EXCEL);

        boolean dataReached = false;
        for (CSVRecord rec : parser) {
          if (!dataReached) {
            if (rec.get(0).equals("dc:language value")) {
              dataReached = true;
            }
          } else {
            Validation v = new Validation(rec, this::lookupIsoCode);
            validations.put(v.getValue(), v);
          }
        }

        parser.close();
      }
    }
  }

  public EvaluationStats getStats() {
    return stats;
  }

  public void setStats(EvaluationStats stats) {
    this.stats = stats;
  }

  public void validateLabelMatch(String lbl, List<String> normalizeds, Integer cnt) {
//		if (normalizeds)

    stats.getNormalizationMethodStats().addNormalizedFromExactLabelMatch(cnt);

    Validation validation = validations.get(lbl);
    if (validation != null && !validation.isInvalidCase(getTargetVocab())) {
      if (resultMatchesValidation(normalizeds, validation.getNormalizedValues())) {
        stats.getValidateMatchesCorrectStats().addNormalizedFromExactLabelMatch(cnt);
      } else {
        stats.getValidateMatchesIncorrectStats().addNormalizedFromExactLabelMatch(cnt);
      }
    }
  }

  public void validateLabelWordAllMatch(String lbl, List<String> normalizeds, Integer cnt) {
    stats.getNormalizationMethodStats().addNormalizedFromAllWordsLabelMatch(cnt);

    Validation validation = validations.get(lbl);
    if (validation != null && !validation.isInvalidCase(getTargetVocab())) {
      if (resultMatchesValidation(normalizeds, validation.getNormalizedValues())) {
        stats.getValidateMatchesCorrectStats().addNormalizedFromAllWordsLabelMatch(cnt);
      } else {
        stats.getValidateMatchesIncorrectStats().addNormalizedFromAllWordsLabelMatch(cnt);
      }
    }
  }

  public void validateLabelWordMatch(String lbl, List<String> normalizeds, Integer cnt) {
    stats.getNormalizationMethodStats().addNormalizedFromWordsLabelMatch(cnt);

    Validation validation = validations.get(lbl);
    if (validation != null && !validation.isInvalidCase(getTargetVocab())) {
      if (resultMatchesValidation(normalizeds, validation.getNormalizedValues())) {
        stats.getValidateMatchesCorrectStats().addNormalizedFromWordsLabelMatch(cnt);
      } else {
        stats.getValidateMatchesIncorrectStats().addNormalizedFromWordsLabelMatch(cnt);
      }
    }
  }

  public void validateNoMatch(String lbl, Integer cnt) {
    stats.getNormalizationMethodStats().addNoMatch(cnt);

  }

  public LanguagesVocabulary getTargetVocab() {
    return targetVocabulary;
  }

  public void addCodeMatch(int cnt) {
    stats.addCodeMatch(cnt);

  }

  public void addAlreadyNormalized(int cnt) {
    stats.addAlreadyNormalized(cnt);
  }

  private Language lookupIsoCode(String code) {
    return isoCodeIndex.get(code);
  }
  
  public boolean resultMatchesValidation(List<String> normalizeds,
      List<Language> validatedResult) {
    List<Language> normalizedsEnums = new ArrayList<>(normalizeds.size());
    for (String nid : normalizeds) {
      normalizedsEnums.add(lookupIsoCode(nid));
    }
    return CollectionUtils.isEqualCollection(validatedResult, normalizedsEnums);
  }

  public void addCount(Integer cnt) {
    stats.addCount(cnt);
  }
}
