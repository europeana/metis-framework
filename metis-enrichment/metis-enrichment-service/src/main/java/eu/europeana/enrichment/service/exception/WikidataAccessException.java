package eu.europeana.enrichment.service.exception;

/**
 * Catched exception identifying Wikidata access errors
 * 
 * @author GrafR
 *
 */
public class WikidataAccessException extends Exception {

  public static final String TRANSFORMER_CONFIGURATION_ERROR = "Transformer could not be initialized.";
  public static final String OUTPUT_WIKIDATA_FILE_NOT_FOUND_ERROR = "Output file for wikidata results not found.";
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 7724261367420984595L;

	public WikidataAccessException(String message, Throwable th) {
		super(message, th);
	}
}
