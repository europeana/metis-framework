package eu.europeana.normalization.util;

public class NormalizationException extends Exception {

  /** Required for instances of {@link java.io.Serializable}. **/
  private static final long serialVersionUID = 2298940685190477391L;

  public NormalizationException(String message, Exception cause) {
    super(message, cause);
  }
}
