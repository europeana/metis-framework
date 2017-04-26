/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
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
