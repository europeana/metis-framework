package eu.europeana.enrichment.rest.client.exceptions;

/**
 * Exception indicating a problem arising during dereferencing.
 */
public class DereferenceException extends Exception {

  private static final long serialVersionUID = -4429750421981318176L;

  /**
   * Constructor.
   *
   * @param message The message.
   * @param cause The cause.
   */
  public DereferenceException(String message, Throwable cause) {
    super(message, cause);
  }
}
