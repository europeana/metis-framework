package eu.europeana.enrichment.service.exception;

public class ZohoAccessException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8724261367420984595L;

	public ZohoAccessException(String message, Throwable th){
		super(message, th);
	}
}
