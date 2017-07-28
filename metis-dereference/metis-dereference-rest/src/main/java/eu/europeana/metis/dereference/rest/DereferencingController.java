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

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.dereference.rest.exceptions.DereferenceException;
import eu.europeana.metis.dereference.service.DereferenceService;
import eu.europeana.metis.dereference.service.MongoDereferenceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Dereferencing REST endpoint
 * Created by gmamakis on 12-2-16.
 */
@Controller
@Api("/")
public class DereferencingController {
    private final DereferenceService dereferenceService;

    @Autowired
    public DereferencingController(DereferenceService dereferenceService) {
        this.dereferenceService = dereferenceService;
    }

    /**
     * Dereference a record given a URI
     *
     * @param uri The uri of the entity
     * @return The dereferenced entities
     */
    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.DEREFERENCE)
    @ResponseBody
    @ApiOperation(value = "Dereference a URI", response = String.class)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<String> dereference(@ApiParam("uri") @RequestParam("uri") String uri) throws DereferenceException {
        try {
            return dereferenceService.dereference(URLDecoder.decode(uri, "UTF-8"));
        } catch (TransformerException | ParserConfigurationException | IOException e) {
            throw new DereferenceException(e.getMessage(), uri);
        }
    }

    /**
     * Dereference a record given a URI
     *
     * @param uris The uris to dereference
     * @return The dereferenced entities
     */
    @RequestMapping(method = RequestMethod.POST, value = RestEndpoints.DEREFERENCE)
    @ResponseBody
    @ApiOperation(value = "Dereference a list URI", response = String.class)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<String> dereference(@RequestBody List<String> uris) throws DereferenceException {
        List<String> dereferencedEntities = new ArrayList<>();

        for (String uri : uris) {
            try {
                dereferencedEntities.addAll(dereference(URLDecoder.decode(uri, "UTF-8")));
            } catch (UnsupportedEncodingException e) {
                throw new DereferenceException(e.getMessage(),uri);
            }
        }
        return dereferencedEntities;
    }
}
