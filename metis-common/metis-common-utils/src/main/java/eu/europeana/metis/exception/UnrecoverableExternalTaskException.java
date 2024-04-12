package eu.europeana.metis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Type of ExternalTaskException indicating that there is no sense of retrying failed operation. Because the result will be always
 * negative. For example when the task does not exist on server.
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Unrecoverable task")
public class UnrecoverableExternalTaskException extends GenericMetisException {

  public UnrecoverableExternalTaskException(String message, Throwable cause) {
    super(message, cause);
  }
}
