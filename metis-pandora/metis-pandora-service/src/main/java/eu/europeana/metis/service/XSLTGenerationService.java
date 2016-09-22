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
package eu.europeana.metis.service;

import eu.europeana.metis.mapping.model.Mapping;
import eu.europeana.metis.mapping.utils.XSLTGenerator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Service for converting a Mapping to an XSL
 * Created by ymamakis on 6/13/16.
 */
public class XSLTGenerationService {

    @Autowired
    private MongoMappingService service;

    /**
     * Generate an XSL from a mapping
     * @param mapping The mapping to generate the XSL from
     * @return The resulting XSL
     */
    public String generateXslFromMapping(Mapping mapping){
        XSLTGenerator generator = new XSLTGenerator();
        String xsl = generator.generateFromMappings(mapping);
        mapping.setXsl(xsl);
        service.updateMapping(mapping);
        return xsl;
    }
}
