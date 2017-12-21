package eu.europeana.metis.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-05-17
 */
@ResponseStatus(value= HttpStatus.CONFLICT, reason="Dataset already exists")
public class DatasetAlreadyExistsException extends Exception {
  private static final long serialVersionUID = -3332292346834265371L;

  public DatasetAlreadyExistsException(String name){
    super("Dataset with name " + name + " already exists");
  }
}
