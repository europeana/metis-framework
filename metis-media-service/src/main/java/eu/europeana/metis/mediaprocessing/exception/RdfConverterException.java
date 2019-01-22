package eu.europeana.metis.mediaprocessing.exception;

/**
 * This exception represents a problem that occurred during setup of one of the RDF conversion
 * (serialization or deserialization) objects in the media processor library.
 */
public class RdfConverterException extends Exception {

  /** This class implements {@link java.io.Serializable}. **/
  private static final long serialVersionUID = -3232418723950326069L;

  /**
   * Constructor.
   * 
   * @param message The exception message.
   * @param cause The cause.
   */
  public RdfConverterException(String message, Throwable cause) {
    super(message, cause);
  }
}
