package eu.europeana.enrichment.rest.client.exceptions;

public class EnrichmentException extends Exception{

  // TODO: Similar/Same to DereferenceOrEnrichException class. So we can distinguish where the exception happens

  /**
   * Constructor.
   *
   * @param message
   * @param cause
   */
  public EnrichmentException(String message, Throwable cause) {
    super(message, cause);
  }


}
