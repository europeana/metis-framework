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

import eu.europeana.validation.model.ValidationResultList;


/**
 * Created by ymamakis on 2/24/16.
 */
public class BatchValidationException extends Exception {

    private final ValidationResultList list;

    /**
     * Cretes exception instance based on
     *
     * @param message description of the problem
     * @param list    list of results provided by validation engine
     */
    public BatchValidationException(String message, ValidationResultList list) {
        super(message);
        this.list = list;
    }

    public ValidationResultList getList() {
        return list;
    }

}
