package eu.europeana.enrichment.rest.client.exceptions;

/**
 * Exception indicating a problem arising during enrichment.
 */
public class EnrichmentException extends Exception {

  private static final long serialVersionUID = 1205297107575299710L;

  /**
   * Constructor.
   *
   * @param message The message.
   * @param cause The cause.
   */
  public EnrichmentException(String message, Throwable cause) {
    super(message, cause);
  }
}
