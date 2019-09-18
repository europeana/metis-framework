package eu.europeana.enrichment.service.exception;

/**
 * Catched exception identifying Wikidata access errors
 *
 * @author GrafR
 */
public class WikidataAccessException extends Exception {

  public static final String TRANSFORMER_CONFIGURATION_ERROR = "Transformer could not be initialized.";
  public static final String TRANSFORM_WIKIDATA_TO_RDF_XML_ERROR = "Error by transforming of Wikidata in RDF/XML.";
  public static final String XML_COULD_NOT_BE_WRITTEN_TO_FILE_ERROR = "XML could not be written to a file.";
  public static final String CANNOT_ACCESS_WIKIDATA_RESOURCE_ERROR = "Cannot access wikidata resource: ";

  private static final long serialVersionUID = 7724261367420984595L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the
   * {@link #getMessage()} method.
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   * (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent or unknown.)
   */
  public WikidataAccessException(String message, Throwable cause) {
    super(message, cause);
  }
}
