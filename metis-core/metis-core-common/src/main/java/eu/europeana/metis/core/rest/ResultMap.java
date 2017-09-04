package eu.europeana.metis.core.rest;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.Map;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-16
 */
public class ResultMap<T> {

  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "results")
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
