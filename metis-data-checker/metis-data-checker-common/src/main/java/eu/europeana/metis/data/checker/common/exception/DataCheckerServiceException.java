package eu.europeana.metis.data.checker.common.exception;

/**
 * Created by erikkonijnenburg on 27/07/2017.
 */
public class DataCheckerServiceException extends Exception{

  private static final long serialVersionUID = -1615263603028555763L;

  public DataCheckerServiceException(String message) {
    super(message);
  }

  public DataCheckerServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
