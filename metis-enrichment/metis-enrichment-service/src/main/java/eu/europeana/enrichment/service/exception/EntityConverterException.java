package eu.europeana.enrichment.service.exception;

/**
 * Catched exception identifying Wikidata access errors
 * 
 * @author GrafR
 *
 */
public class EntityConverterException extends Exception {

  public static final String COULD_NOT_BE_WRITTEN_TO_FILE_ERROR = "Content could not be written to a file.";
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 7724261367420984593L;

	public EntityConverterException(String message, Throwable th) {
		super(message, th);
	}
}
