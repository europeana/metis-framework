package eu.europeana.metis.core.rest;

import java.util.Map;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-16
 */
public class ResultMap<T> {

  @XmlElement(name = "results")
  private Map<String, T> results;

  public ResultMap(Map<String, T> results) {
    this.results = results;
  }

  public Map<String, T> getResults() {
    return results;
  }

  public void setResults(Map<String, T> results) {
    this.results = results;
  }
}
