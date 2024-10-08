package eu.europeana.metis.mediaprocessing.exception;

/**
 * This exception represents a problem that occurred during serialization of an RDF object.
 * @deprecated Use {@link eu.europeana.metis.mediaprocessing.utils.ConverterUtils} and/or RdfSerializer from metis-schema library.
 */
public class RdfSerializationException extends Exception {

  /** This class implements {@link java.io.Serializable}. **/
  private static final long serialVersionUID = 1031549407979593963L;

  /**
   * Constructor.
   * 
   * @param message The exception message.
   * @param cause The cause.
   */
  public RdfSerializationException(String message, Throwable cause) {
    super(message, cause);
  }
}
