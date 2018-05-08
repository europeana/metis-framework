package eu.europeana.validation.rest.exceptions;

/**
 * Created by ymamakis on 2/24/16.
 */
public class ServerException extends Exception {


  /** This class implements {@link java.io.Serializable}. **/
  private static final long serialVersionUID = 8072611399920895134L;

  /**
   * Creates exception instance based on provided {@link Throwable}
   *
   * @param cause The cause of the exception.
   */
  public ServerException(Throwable cause) {
    super(cause);
  }

  /**
   * Creates exception instance with the given message.
   * 
   * @param message The message of the exception.
   */
  public ServerException(String message) {
    super(message);
  }
}
