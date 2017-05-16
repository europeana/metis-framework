package eu.europeana.metis.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "No API key found")
public class NoApiKeyFoundException extends Exception {

  private static final long serialVersionUID = -3332292346834265371L;

  public NoApiKeyFoundException(String apiKey) {
    super("No API key found with name: " + apiKey);
  }
}

