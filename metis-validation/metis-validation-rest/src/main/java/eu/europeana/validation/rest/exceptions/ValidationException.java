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
package eu.europeana.validation.rest.exceptions;

/**
 * Created by ymamakis on 2/24/16.
 */
public class ValidationException extends Exception {

    private final String id;

    /**
     * Creates class instance
     */
    public ValidationException() {
        super();
        id = null;
    }

    /**
     * Creates exception instance based on provided parameters
     * @param id record id that causes the problem
     * @param message message provided by validation engine
     */
    public ValidationException(String id, String message) {
        super(message);
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
