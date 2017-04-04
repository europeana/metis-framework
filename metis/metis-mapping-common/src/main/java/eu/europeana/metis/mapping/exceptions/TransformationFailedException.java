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
 * Exception thrown when the transformation failed
 * Created by ymamakis on 6/9/16.
 */
public class TransformationFailedException extends Exception {

    /**
     * Default constructor
     * @param recordId The record identifier of the record
     * @param mapping The mapping used for the transformation
     */
    public TransformationFailedException(String recordId, String mapping){
        super (String.format("XSLT transformation failed for record %s using mapping %s", recordId, mapping));
    }
}
