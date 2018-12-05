package eu.europeana.enrichment.service.exception;

/**
 * Catched exception identifying zoho access errors
 *
 * @author GordeaS
 */
public class ZohoAccessException extends Exception {

  private static final long serialVersionUID = 8724261367420984595L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the
   * {@link #getMessage()} method.
   * @param th the cause of the exception
   */
  public ZohoAccessException(String message, Throwable th) {
    super(message, th);
  }

  public ZohoAccessException(String message) {
    super(message);
  }
}
