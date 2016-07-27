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
package eu.europeana.metis.framework.rest.controllers;

import eu.europeana.metis.mapping.exceptions.TemplateGenerationFailedException;
import eu.europeana.metis.mapping.model.MappingSchema;
import eu.europeana.metis.service.XSDService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

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
     * @param rootFile The root XSD file
     * @param mappingName The name of the mapping to apply. It will automatically get a "template_" prefix
     * @param file The file containing the Schema definition
     * @return The id of the mapping
     * @throws IOException
     * @throws TemplateGenerationFailedException
     */
    @RequestMapping(method = RequestMethod.POST, value = "/xsd/upload")
    @ApiOperation(value = "Read and XSD from a tgz and generate a mapping")
    public String readFromFile(@ApiParam("rootFile") @RequestParam("rootFile") String rootFile,
                               @ApiParam("mappingName") @RequestParam("mappingName") String mappingName,
                               @ApiParam @RequestBody MappingSchema schema,
                               @ApiParam("rootXPath") @RequestParam("rootXPath") String rootXPath,
                               @ApiParam("file") @RequestParam("file") MultipartFile file,
                               @ApiParam("namespaces") @RequestParam("namespaces") Map<String,String> namespaces) throws IOException, TemplateGenerationFailedException {
        try {
           return xsdService.generateTemplateFromTgz(file.getBytes(), rootFile, mappingName,rootXPath,schema,namespaces);
        } catch (Exception e) {
            throw new TemplateGenerationFailedException(mappingName, rootFile, e.getMessage());
        }
    }
    /**
     * Instruct the API to download a TGZ file from a URL and generate a mapping out of it
     * @param rootFile The root XSD file
     * @param mappingName The name of the mapping to apply. It will automatically get a "template_" prefix
     * @param url The URL that points to a file containing the Schema definition
     * @return The id of the mapping
     * @throws IOException
     * @throws TemplateGenerationFailedException
     */
    @RequestMapping(method = RequestMethod.POST, value = "/xsd/url")
    public String readFromUrl(@ApiParam("rootFile") @RequestParam("rootFile") String rootFile,
                              @ApiParam("mappingName") @RequestParam("mappingName") String mappingName,
                              @ApiParam @RequestBody MappingSchema schema,
                              @ApiParam("url") @RequestParam("url") String url,
                              @ApiParam("rootXPath") @RequestParam ("rootXPath") String rootXPath,
                              @ApiParam("namespaces") @RequestParam("namespaces") Map<String,String> namespaces) throws IOException, TemplateGenerationFailedException {
        try {
           return xsdService.generateTemplateFromTgzUrl(url, rootFile, mappingName, rootXPath,schema,namespaces);
        } catch (Exception e) {
            throw new TemplateGenerationFailedException(mappingName, rootFile, e.getMessage());
        }

    }
}
