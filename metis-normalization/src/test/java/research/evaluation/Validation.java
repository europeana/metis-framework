package research.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.csv.CSVRecord;
import eu.europeana.normalization.languages.Language;
import eu.europeana.normalization.languages.LanguagesVocabulary;

class Validation {

  private String value;
  private List<Language> normalizedValues;
  private boolean invalidCase;

  public Validation(CSVRecord rec, Function<String, Language> isoCodeLookup) {
    value = rec.get(0);
    String validation = rec.get(4);
    invalidCase = !validation.equals("n") && !validation.equals("y");

    if (validation.equals("n")) {
      System.out.println("Was incorrec case: " + value);
    }

    if (!invalidCase) {
      normalizedValues = new ArrayList<>(2);
      if (validation.equals("y")) {
        for (int i = 1; i <= 3; i++) {
          String v = rec.get(i).trim();
          if (!v.isEmpty()) {
            v = v.substring(0, v.indexOf('('));
            normalizedValues.add(isoCodeLookup.apply(v));
          } else if (i == 1) {
            throw new RuntimeException("empty result: " + value);
          } else {
            break;
          }
        }
      } else if (validation.equals("n")) {
        for (int i = 5; i <= 7; i++) {
          String v = rec.get(i).trim();
          if (!v.isEmpty()) {
            v = v.substring(0, v.indexOf('('));
            normalizedValues.add(isoCodeLookup.apply(v));
          } else if (i == 5) {
            throw new RuntimeException("uncorrected wrong case: " + value);
          } else {
            break;
          }
        }
      }
    }
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public List<Language> getNormalizedValues() {
    return normalizedValues;
  }

  public void setNormalizedValues(List<Language> normalizedValues) {
    this.normalizedValues = normalizedValues;
  }

  public boolean isInvalidCase(LanguagesVocabulary languagesVocabulary) {
    if (invalidCase) {
      return true;
    }
    for (Language l : normalizedValues) {
      if (l.getNormalizedLanguageId(languagesVocabulary) == null) {
        return true;
      }
    }
    return false;
  }

  public void setInvalidCase(boolean invalidCase) {
    this.invalidCase = invalidCase;
  }


}
