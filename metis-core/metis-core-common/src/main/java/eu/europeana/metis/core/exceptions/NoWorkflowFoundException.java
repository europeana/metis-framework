package eu.europeana.metis.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-29
 */
@ResponseStatus(value= HttpStatus.NOT_FOUND, reason="No userWorkflow found")
public class NoWorkflowFoundException extends Exception {
  private static final long serialVersionUID = -3332292346834265371L;
  public NoWorkflowFoundException(String message){
    super(message);
  }
}


