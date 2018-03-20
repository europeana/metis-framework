package eu.europeana.normalization.util;

/**
 * Exception that is thrown in case of XML related issues.
 * 
 * @author jochen
 *
 */
public class XmlException extends Exception {

  /** Required for instances of {@link java.io.Serializable}. **/
  private static final long serialVersionUID = 8066739348399102725L;

  /**
   * Constructor.
   * 
   * @param message The message of the exception. Can be null.
   * @param cause The cause of the exception. Can be null.
   */
  public XmlException(String message, Exception cause) {
    super(message, cause);
  }
}
