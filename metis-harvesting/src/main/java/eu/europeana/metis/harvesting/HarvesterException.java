package eu.europeana.metis.harvesting;

/**
 * Exception that can be thrown as the result of a harvesting issue.
 */
public class HarvesterException extends Exception {

  private static final long serialVersionUID = 3937292609814351251L;

  /**
   * Constructor.
   *
   * @param message The message of the exception.
   * @param cause The cause of the exception.
   */
  public HarvesterException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor.
   *
   * @param message The message of the exception.
   */
  public HarvesterException(String message) {
    super(message);
  }
}