package eu.europeana.enrichment.rest.client.exceptions;

public class DereferenceException extends Exception{

  // TODO: Similar/Same to DereferenceOrEnrichException class. So we can distinguish where the exception happens.

  /**
   * Constructor.
   *
   * @param message
   * @param cause
   */
  public DereferenceException(String message, Throwable cause) {
    super(message, cause);
  }

}
