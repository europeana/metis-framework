package eu.europeana.metis.debias.detect.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The type DeBias exception.
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "DeBias detection internal server error")
public class DeBiasInternalServerException extends RuntimeException {

  @Serial
  private static final long serialVersionUID = -5671884493038169899L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()} method.
   */
  public DeBiasInternalServerException(String message) {
    super(message);
  }

  /**
   * Instantiates a new DeBias exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public DeBiasInternalServerException(String message, Throwable cause) {
    super(message, cause);
  }
}
