package eu.europeana.metis.exception;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A wrapper class for exceptions thrown in the system.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-10
 */
@XmlRootElement(name = "error")
@XmlAccessorType(XmlAccessType.FIELD)
public class StructuredExceptionWrapper {

  @XmlElement
  private String errorMessage;

  /**
   * Constructor that is json friendly and is displayed to the client if an exception was thrown.
   *
   * @param errorMessage the error message that will be shown to the user
   */
  public StructuredExceptionWrapper(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
