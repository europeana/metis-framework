package eu.europeana.metis.debias.detect.model.error;

import java.util.Collections;
import java.util.List;

/**
 * The type Input.
 */
public class Input {

  /**
   * The Values.
   */
  List<String> values;

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
