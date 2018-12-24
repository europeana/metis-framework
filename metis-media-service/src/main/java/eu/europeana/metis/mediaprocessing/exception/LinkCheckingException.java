package eu.europeana.metis.mediaprocessing.exception;

/**
 * This exception represents a problem that occurred during link checking. 
 */
public class LinkCheckingException extends Exception {

  /** This class implements {@link java.io.Serializable}. **/
  private static final long serialVersionUID = 3926174673354061384L;

  /**
   * Constructor.
   * 
   * @param cause The cause.
   */
  public LinkCheckingException(Throwable cause) {
    super(cause);
  }
}
