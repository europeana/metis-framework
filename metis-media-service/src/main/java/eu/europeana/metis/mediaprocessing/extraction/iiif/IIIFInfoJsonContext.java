package eu.europeana.metis.mediaprocessing.extraction.iiif;

import java.util.List;

/**
 * The type Iiif info json context.
 */
public class IIIFInfoJsonContext {
  private List<String> values;

  /**
   * Instantiates a new Iiif info json context.
   *
   * @param value the value
   */
  public IIIFInfoJsonContext(String value) {
    this.values = List.of(value);
  }

  /**
   * Instantiates a new Iiif info json context.
   *
   * @param values the values
   */
  public IIIFInfoJsonContext(List<String> values) {
    this.values = values;
  }

  /**
   * Gets values.
   *
   * @return the values
   */
  public List<String> getValues() {
    return values;
  }
}
