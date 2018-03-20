package eu.europeana.enrichment.api.external.web;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Web error message holding the message details and the actual type of
 * exception. This class is returned as a response when an exception is thrown
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 * 
 */

@XmlRootElement
public class EnrichmentError {

	public EnrichmentError() {

	}

	/**
	 * Default Constructor
	 * 
	 * @param cause
	 *            The exception classname
	 * @param details
	 *            The original message details
	 */
	public EnrichmentError(String cause, String details) {
		this.cause = cause;
		this.details = details;

	}

	private String details;

	private String cause;

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

}
