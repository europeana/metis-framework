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
 * Mapping not found exception. Response code 404
 * Created by ymamakis on 6/16/16.
 */
public class MappingNotFoundException extends Exception {

    /**
     * Default constructor
     * @param mappingId The id requested
     */
    public MappingNotFoundException(String mappingId){
        super(String.format("Mapping with id: %s Not Found. Reason: ", mappingId));

    }
}
