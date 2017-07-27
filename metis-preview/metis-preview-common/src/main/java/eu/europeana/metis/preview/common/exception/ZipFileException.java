package eu.europeana.metis.preview.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by erikkonijnenburg on 27/07/2017.
 */
@ResponseStatus(value= HttpStatus.BAD_REQUEST, reason="Bad file content.")
public class ZipFileException extends Exception {

  private static final long serialVersionUID = -7580405120938844278L;

  public ZipFileException() {
    this("Bad file content.");
  }
  public ZipFileException(String message) {
    super(message);
  }
}
