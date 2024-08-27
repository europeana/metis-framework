package eu.europeana.metis.debias.detect.rest.exceptions;

/**
 * The type Server error.
 */
public class ServerError {

  private String errorMessage;

  /**
   * Instantiates a new Server error.
   */
  public ServerError() {
    // Required for serialization and deserialization
  }

  /**
   * Instantiates a new Server error.
   *
   * @param errorMessage the error message
   */
  public ServerError(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  /**
   * Gets error message.
   *
   * @return the error message
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * Sets error message.
   *
   * @param errorMessage the error message
   */
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
