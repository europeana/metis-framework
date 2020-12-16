package eu.europeana.metis.schema.convert;

/**
 * Exception that marks a failure in serializing or deserializing.
 */
public class SerializationException extends Exception {

  private static final long serialVersionUID = 2094687445199925308L;

  /**
   * Constructor.
   *
   * @param message The message.
   * @param cause The cause.
   */
  public SerializationException(String message, Throwable cause) {
    super(message, cause);
  }
}
