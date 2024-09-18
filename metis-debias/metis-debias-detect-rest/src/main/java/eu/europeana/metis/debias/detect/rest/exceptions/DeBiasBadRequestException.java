package eu.europeana.metis.debias.detect.rest.exceptions;

import java.io.Serial;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The type Debias bad request exception.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "DeBias detection bad request")
public class DeBiasBadRequestException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = -5859207750420173804L;
  /**
   * Instantiates a new Debias bad request exception.
   *
   * @param message the message
   */
  public DeBiasBadRequestException(String message) {
    super(message);
  }

}
