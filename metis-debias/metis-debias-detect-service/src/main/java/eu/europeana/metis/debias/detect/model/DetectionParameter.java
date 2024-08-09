package eu.europeana.metis.debias.detect.model;

import java.util.List;

public class DetectionParameter {
  private String language;
  private List<String> values;

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }
}
