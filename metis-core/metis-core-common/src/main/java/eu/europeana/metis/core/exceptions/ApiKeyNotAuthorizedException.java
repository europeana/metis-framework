package eu.europeana.metis.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Not authorized")
public class ApiKeyNotAuthorizedException extends Exception {

  private static final long serialVersionUID = -3332292346834265371L;

  public ApiKeyNotAuthorizedException(String apiKey) {
    super("API key " + apiKey + " not authorized");
  }

  public ApiKeyNotAuthorizedException(String apiKey, String message) {
    super("API key " + apiKey + " not authorized: " + message);
  }
}
