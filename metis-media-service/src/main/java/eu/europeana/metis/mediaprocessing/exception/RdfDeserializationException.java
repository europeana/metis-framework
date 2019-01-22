package eu.europeana.metis.mediaprocessing.exception;

/**
 * This exception represents a problem that occurred during deserialization of an RDF object.
 */
public class RdfDeserializationException extends Exception {

  /** This class implements {@link java.io.Serializable}. **/
  private static final long serialVersionUID = -789223924131348847L;

  /**
   * Constructor.
   * 
   * @param message The exception message.
   * @param cause The cause.
   */
  public RdfDeserializationException(String message, Throwable cause) {
    super(message, cause);
  }
}
