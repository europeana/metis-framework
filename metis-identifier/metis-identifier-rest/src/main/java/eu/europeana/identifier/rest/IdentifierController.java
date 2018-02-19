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
package eu.europeana.identifier.rest;

import eu.europeana.identifier.rest.exceptions.IdentifierException;
import eu.europeana.identifier.service.IdentifierService;
import eu.europeana.itemization.Request;
import eu.europeana.itemization.RequestResult;
import eu.europeana.metis.RestEndpoints;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.jibx.runtime.JiBXException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;


/**
 * Identifier REST API
 * Created by ymamakis on 2/9/16.
 */
@Controller
@Api(value = "/", description = "Generate Europeana identifier")
public class IdentifierController {

    @Autowired
    private IdentifierService idService;

    /**
     * Generate a Europeana Identifier
     *
     * @param collectionId The collection Id the records belongs to
     * @param recordId     The record identifier
     * @return A Europeana identifier
     */

    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.IDENTIFIER_GENERATE)
    @ApiOperation(value = "Generate record identifier", response = String.class)
    @ResponseBody
    public String generateIdentifier(@ApiParam("collectionId") @PathVariable("collectionId") String collectionId,
                                       @ApiParam("recordId") @RequestParam("recordId") String recordId) {
        return idService.generateIdentifier(collectionId, URLDecoder.decode(recordId));
    }


    /**
     * Normalize the identifiers of a single record
     *
     * @param record The record to normalize the identifiers
     * @return The normalized record
     * @throws IdentifierException 
     */
    @RequestMapping(method = RequestMethod.POST,value = RestEndpoints.IDENTIFIER_NORMALIZE_SINGLE)
    @ResponseBody
    @ApiOperation(value = "Fix the identifiers of a record for internal semantic linking", response = String.class)
    public String normalize(@ApiParam("record") @RequestParam("record") String record) throws IdentifierException {
        try {
            return idService.fixIdentifiers(record);
        } catch (JiBXException e) {
            throw new IdentifierException(e.getMessage());
        }
    }

    /**
     * Batch normalization of record identifiers
     *
     * @param records The records to normalize the identifiers
     * @return
     * @throws IdentifierException 
     */

    @RequestMapping(value = RestEndpoints.IDENTIFIER_NORMALIZE_BATCH, method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    @ApiOperation(value = "Batch normalizations of records", response = RequestResult.class)
    public RequestResult normalize(@RequestBody Request records) throws IdentifierException {
        try {
            RequestResult res = new RequestResult();
            res.setItemizedRecords(idService.fixIdentifiers(records.getRecords()));
            return res;
        } catch (JiBXException e) {
            throw new IdentifierException(e.getMessage());
        }
    }
}
