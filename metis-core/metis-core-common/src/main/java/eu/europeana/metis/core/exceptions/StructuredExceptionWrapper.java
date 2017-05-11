package eu.europeana.metis.core.exceptions;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-10
 */
@JacksonXmlRootElement(localName="error")
public class StructuredExceptionWrapper {
  @JacksonXmlProperty
  private String message;

  public StructuredExceptionWrapper(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
