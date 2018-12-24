package eu.europeana.metis.mediaprocessing.exception;

/**
 * This exception represents a problem that occurred during the execution of a command.
 */
public class CommandExecutionException extends Exception {

  /**
   * This class implements {@link java.io.Serializable}.
   **/
  private static final long serialVersionUID = 456553694666317036L;

  public CommandExecutionException(String s, Throwable throwable) {
    super(s, throwable);
  }
}
