package eu.europeana.enrichment.service.exception;

/**
 * Catched exception identifying Wikidata access errors
 * 
 * @author GrafR
 *
 */
public class WikidataAccessException extends Exception {

  public static final String TRANSFORMER_CONFIGURATION_ERROR = "Transformer could not be initialized.";
  public static final String TRANSFORM_WIKIDATA_TO_RDF_XML_ERROR = "Error by transforming of Wikidata in RDF/XML.";
  public static final String XML_COULD_NOT_BE_WRITTEN_TO_FILE_ERROR = "XML could not be written to a file.";
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 7724261367420984595L;

	public WikidataAccessException(String message, Throwable th) {
		super(message, th);
	}
}
