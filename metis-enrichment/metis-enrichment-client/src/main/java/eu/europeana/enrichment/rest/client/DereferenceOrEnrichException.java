package eu.europeana.enrichment.rest.client;

/**
 * This exception is thrown while trying to perform dereferencing or enrichment on a document.
 * 
 * @author jochen
 *
 */
public class DereferenceOrEnrichException extends Exception {

  /** This class implements {@link java.io.Serializable} **/
  private static final long serialVersionUID = -7728213088239715845L;

  /**
   * Constructor.
   * 
   * @param message
   * @param cause
   */
  public DereferenceOrEnrichException(String message, Throwable cause) {
    super(message, cause);
  }
}
