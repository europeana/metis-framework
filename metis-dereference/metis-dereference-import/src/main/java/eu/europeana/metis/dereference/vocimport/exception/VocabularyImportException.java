package eu.europeana.metis.dereference.vocimport.exception;

/**
 * Indicates an issue with importing vocabularies or a specific vocabulary.
 */
public class VocabularyImportException extends Exception {

  private static final long serialVersionUID = 3492077867356670740L;

  /**
   * Constructor
   *
   * @param message The message.
   */
  public VocabularyImportException(String message) {
    super(message);
  }

  /**
   * Constructor.
   *
   * @param message The message.
   * @param cause The cause.
   */
  public VocabularyImportException(String message, Throwable cause) {
    super(message, cause);
  }
}
