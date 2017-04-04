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
 * Exception thrown when an XSD fails to generate a template
 * Created by ymamakis on 6/13/16.
 */
public class TemplateGenerationFailedException extends Exception {
    /**
     * Default constructor
     * @param name The name of the mapping
     * @param file The file pointing to the XSD
     * @param reason The reason why the exception was thrown
     */
    public TemplateGenerationFailedException(String name, String file, String reason){
        super(String.format("Template generation failed for %s from file %s. Reason: %s",name,file,reason));
    }
}
