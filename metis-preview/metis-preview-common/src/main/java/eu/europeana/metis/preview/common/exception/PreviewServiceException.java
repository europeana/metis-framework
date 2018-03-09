package eu.europeana.metis.preview.common.exception;

/**
 * Created by erikkonijnenburg on 27/07/2017.
 */
public class PreviewServiceException extends Exception{

  private static final long serialVersionUID = -1615263603028555763L;

  public PreviewServiceException(String message) {
    super(message);
  }

  public PreviewServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
