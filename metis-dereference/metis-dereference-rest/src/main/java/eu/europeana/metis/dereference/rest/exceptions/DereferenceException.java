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
package eu.europeana.metis.dereference.rest.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by ymamakis on 2/25/16.
 */
@ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR, reason="Dereferencing failed")
public class DereferenceException extends RuntimeException {

	/** Required for implementations of {@link java.io.Serializable} **/
	private static final long serialVersionUID = 2510936695311769525L;

	public DereferenceException(String message, String uri) {
        super("Dereferencing failed for uri: "+ uri +" with root cause: " + message);
    }
}
