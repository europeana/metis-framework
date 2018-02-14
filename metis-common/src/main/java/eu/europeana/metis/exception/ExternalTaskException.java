package eu.europeana.metis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-09
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason="Bad Content")
public class ExternalTaskException extends GenericMetisException {
  private static final long serialVersionUID = -3332292346834265371L;

  public ExternalTaskException(String message) {
    super(message);
  }

  public ExternalTaskException(String message, Throwable e) {
    super(message, e);
  }

}