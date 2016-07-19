package eu.europeana.metis.framework.rest.controllers;

import eu.europeana.metis.mapping.exceptions.MappingToXSLException;
import eu.europeana.metis.mapping.model.Mapping;
import eu.europeana.metis.service.XSLTGenerationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * A Controller providing a method to convert a Mapping to an XSL
 * Created by ymamakis on 6/13/16.
 */
@Controller
@Api(value = "/",description = "REST API to convert a Mapping to an XSL")
public class XslController {

    @Autowired
    private XSLTGenerationService generationService;

    /**
     * Generate an XSL from a Mapping
     * @param mapping The mapping to generate the XSL from
     * @return The resulting XSL
     * @throws MappingToXSLException
     */
    @ApiOperation(value = "Create an XSL from mapping")
    @RequestMapping(value = "/xsl/generate",method = RequestMethod.POST)
    public String createXSLFromMapping(@ApiParam @RequestBody Mapping mapping) throws MappingToXSLException {
        try {
            return generationService.generateXslFromMapping(mapping);
        } catch(Exception e){
            throw new MappingToXSLException(mapping.getDataset(),mapping.getName(), e.getMessage());
        }
    }

}
