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
