package eu.europeana.enrichment.rest.client.exceptions;

public class SerializationException extends Exception {

  /**
   * Constructor.
   *
   * @param message
   * @param cause
   */
  public SerializationException(String message, Throwable cause) {
    super(message, cause);
  }

}
