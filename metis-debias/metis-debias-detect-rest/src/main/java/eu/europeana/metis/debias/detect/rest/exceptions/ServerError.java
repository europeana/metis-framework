package eu.europeana.metis.debias.detect.rest.exceptions;

/**
 * The type Server error.
 */
public class ServerError {

  private int statusCode;
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
   * @param statusCode the status code
   * @param errorMessage the error message
   */
  public ServerError(int statusCode, String errorMessage) {
    this.statusCode = statusCode;
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

  /**
   * Gets status code.
   *
   * @return the status code
   */
  public int getStatusCode() {
    return statusCode;
  }

  /**
   * Sets status code.
   *
   * @param statusCode the status code
   */
  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }
}
