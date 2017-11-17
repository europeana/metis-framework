package eu.europeana.metis.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-09-25
 */
@ResponseStatus(value= HttpStatus.CONFLICT, reason="ScheduledWorkflow already exists")
public class ScheduledUserWorkflowAlreadyExistsException extends Exception {
  private static final long serialVersionUID = -3332292346834265371L;

  public ScheduledUserWorkflowAlreadyExistsException(String message){
    super(message);
  }
}
