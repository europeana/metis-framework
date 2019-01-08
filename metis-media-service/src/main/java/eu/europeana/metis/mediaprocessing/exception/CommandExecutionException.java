package eu.europeana.metis.mediaprocessing.exception;

/**
 * This exception represents a problem that occurred during the execution of a system command.
 */
public class CommandExecutionException extends Exception {

  /**
   * This class implements {@link java.io.Serializable}.
   **/
  private static final long serialVersionUID = 456553694666317036L;

  /**
   * Constructor.
   * 
   * @param message The exception message.
   * @param cause The cause.
   */
  public CommandExecutionException(String message, Throwable cause) {
    super(message, cause);
  }
}
