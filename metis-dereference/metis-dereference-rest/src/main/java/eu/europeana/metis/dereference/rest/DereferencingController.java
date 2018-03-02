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
package eu.europeana.metis.dereference.rest;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.dereference.rest.exceptions.DereferenceException;
import eu.europeana.metis.dereference.service.DereferenceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Dereferencing REST endpoint
 * Created by gmamakis on 12-2-16.
 */
@Controller
@Api("/")
public class DereferencingController {
  
    private static final Charset URI_CHARSET = StandardCharsets.UTF_8;
  
    private final DereferenceService dereferenceService;

    @Autowired
    public DereferencingController(DereferenceService dereferenceService) {
        this.dereferenceService = dereferenceService;
    }

    /**
     * Dereference a record given a URI
     *
     * @param resourceId The resource ID (URI) of the entity to dereference
     * @return The dereferenced entities
     * @throws JAXBException 
     */
    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.DEREFERENCE,
    		produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE} )
    @ResponseBody
    @ApiOperation(value = "Dereference a URI", response = EnrichmentResultList.class)
    public EnrichmentResultList dereference(@ApiParam("uri") @RequestParam("uri") String resourceId) throws JAXBException {
    	try {
    	  return dereferenceService.dereference(URLDecoder.decode(resourceId, URI_CHARSET.name()));
    	} catch (TransformerException | IOException e) {
    		throw new DereferenceException(e.getMessage(), resourceId);
    	}
    }

    /**
     * Dereference a record given a URI
     *
     * @param resourceIds The resource IDs to dereference
     * @return The dereferenced entities
     * @throws JAXBException 
     */
    @RequestMapping(method = RequestMethod.POST, value = RestEndpoints.DEREFERENCE,
    		consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
    		produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE} )
    @ResponseBody
    @ApiOperation(value = "Dereference a list URI", response = EnrichmentResultList.class)
    public EnrichmentResultList dereference(@RequestBody List<String> resourceIds) throws JAXBException {
    	EnrichmentResultList dereferencedEntities = new EnrichmentResultList();
        for (String resourceId : resourceIds) {
            EnrichmentResultList result = dereference(resourceId);
            if (result != null) {
                dereferencedEntities.getResult().addAll(result.getResult());
            }
        }
    	return dereferencedEntities;
    }
}
