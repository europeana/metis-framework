package eu.europeana.normalization.util;

public class XmlException extends Exception {

  /** Required for instances of {@link java.io.Serializable}. **/
  private static final long serialVersionUID = 8066739348399102725L;

  public XmlException(String message, Exception cause) {
    super(message, cause);
  }

  public XmlException(String message) {
    this(message, null);
  }
}
