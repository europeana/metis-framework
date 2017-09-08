package research.evaluation;

import eu.europeana.normalization.common.language.LanguagesVocabulary;
import eu.europeana.normalization.common.language.nal.EuropeanLanguagesNal;
import eu.europeana.normalization.common.language.nal.NalLanguage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ValidatedCases {

  private final Map<String, Validation> validations;
  private EvaluationStats stats;
  private EuropeanLanguagesNal europaEuLanguagesNal;

  public ValidatedCases(File evaluationFolder, EuropeanLanguagesNal europaEuLanguagesNal)
      throws IOException {
    this.europaEuLanguagesNal = europaEuLanguagesNal;
    validations = new HashMap<>();
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
            Validation v = new Validation(rec, europaEuLanguagesNal);
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
    return europaEuLanguagesNal.getTargetVocabulary();
  }

  public EuropeanLanguagesNal getLanguagesNal() {
    return europaEuLanguagesNal;
  }

  public void setLanguagesNal(EuropeanLanguagesNal europaEuLanguagesNal) {
    this.europaEuLanguagesNal = europaEuLanguagesNal;
  }

  public void addCodeMatch(int cnt) {
    stats.addCodeMatch(cnt);

  }

  public void addAlreadyNormalized(int cnt) {
    stats.addAlreadyNormalized(cnt);
  }

  public boolean resultMatchesValidation(List<String> normalizeds,
      List<NalLanguage> validatedResult) {
    List<NalLanguage> normalizedsEnums = new ArrayList<>(normalizeds.size());
    for (String nid : normalizeds) {
      normalizedsEnums.add(europaEuLanguagesNal.lookupIsoCode(nid));
    }
    return CollectionUtils.isEqualCollection(validatedResult, normalizedsEnums);
  }

  public void addCount(Integer cnt) {
    stats.addCount(cnt);
  }
}
