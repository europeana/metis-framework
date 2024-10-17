package eu.europeana.metis.debias.detect.model.request;

import java.util.Collections;
import java.util.List;


/**
 * The type Bias input literals.
 */
public class BiasInputLiterals {

  private String language;
  private List<String> values;

  /**
   * Gets language.
   *
   * @return the language 2-letter code ISO 6391
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Sets language.
   *
   * @param language the language 2-letter code ISO 6391
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * Gets values.
   *
   * @return the values
   */
  public List<String> getValues() {
    return values;
  }

  /**
   * Sets values.
   *
   * @param values the values
   */
  public void setValues(List<String> values) {
    if (values != null) {
      this.values = Collections.unmodifiableList(values);
    }
  }
}
