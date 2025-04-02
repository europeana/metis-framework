package eu.europeana.enrichment.api.external.exceptions;

import java.io.Serial;

/**
 * The type Entity API client exception.
 */
public class EntityApiException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -272263706753226938L;

  /**
   * Instantiates a new Entity API client exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public EntityApiException(String message, Throwable cause) {
    super(message, cause);
  }
}
