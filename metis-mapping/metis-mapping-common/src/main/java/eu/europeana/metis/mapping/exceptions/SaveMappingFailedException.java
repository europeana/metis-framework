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
package eu.europeana.metis.mapping.exceptions;

/**
 * Exception thrown when the mapping CUD operations fail
 * Created by ymamakis on 6/16/16.
 */
public class SaveMappingFailedException extends Exception {
    /**
     * Defaut constructor
     * @param reason The reason why the mapping failed to persist or delete
     */
    public SaveMappingFailedException(String reason){
        super(String.format("Mapping failed. Reason: "+ reason));

    }
}
