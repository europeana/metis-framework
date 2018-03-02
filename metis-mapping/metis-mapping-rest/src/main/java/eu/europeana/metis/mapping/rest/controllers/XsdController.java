package eu.europeana.metis.mapping.rest.controllers;

import eu.europeana.metis.mapping.exceptions.TemplateGenerationFailedException;
import eu.europeana.metis.mapping.xsd.FileXSDUploadDTO;
import eu.europeana.metis.mapping.xsd.UrlXSDUploadDTO;
import eu.europeana.metis.service.XSDService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;

import static eu.europeana.metis.RestEndpoints.XSD_UPLOAD;
import static eu.europeana.metis.RestEndpoints.XSD_URL;

/**
 * A controller that exposes a REST API to load an XSD and generate a mapping from
 * Created by ymamakis on 6/13/16.
 */
@Controller
@Api(value = "/",description = "REST API to load an XSD and generate mappings")
public class XsdController {

    @Autowired
    private XSDService xsdService;

    /**
     * Upload a TGZ and generate a mapping out of it
     * @return The id of the mapping
     * @throws IOException
     * @throws TemplateGenerationFailedException
     */
    @RequestMapping(method = RequestMethod.POST, value = XSD_UPLOAD, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Read and XSD from a tgz and generate a mapping")
    public String readFromFile(@ApiParam @RequestBody FileXSDUploadDTO dto) throws IOException, TemplateGenerationFailedException {
        try {
           return xsdService.generateTemplateFromTgz(dto.getFile(), dto.getRootFile(), dto.getMappingName(),dto.getRootXPath(),dto.getSchema(),dto.getNamespaces());
        } catch (Exception e) {
            throw new TemplateGenerationFailedException(dto.getMappingName(), dto.getRootFile(), e.getMessage());
        }
    }
    /**
     * Instruct the API to download a TGZ file from a URL and generate a mapping out of it
     * @return The id of the mapping
     * @throws IOException
     * @throws TemplateGenerationFailedException
     */
    @RequestMapping(method = RequestMethod.POST, value = XSD_URL, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String readFromUrl(@ApiParam @RequestBody UrlXSDUploadDTO dto) throws IOException, TemplateGenerationFailedException {
        try {
           return xsdService.generateTemplateFromTgzUrl(dto.getUrl(), dto.getRootFile(), dto.getMappingName(), dto.getRootXPath(),dto.getSchema(),dto.getNamespaces());
        } catch (Exception e) {
            throw new TemplateGenerationFailedException(dto.getMappingName(), dto.getRootFile(), e.getMessage());
        }

    }
}
