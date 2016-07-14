package eu.europeana.identifier.rest;

import eu.europeana.identifier.rest.exceptions.IdentifierException;
import eu.europeana.identifier.service.IdentifierService;
import eu.europeana.itemization.Request;
import eu.europeana.itemization.RequestResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.jibx.runtime.JiBXException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


/**
 * Identifier REST API
 * Created by ymamakis on 2/9/16.
 */
@Controller(value = "/rest/identifier")
@Api(value = "/rest/identifier", description = "Generate Europeana identifier")
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

    @RequestMapping(method = RequestMethod.GET, value = "/generate/{collectionId}/{recordId}")
    @ApiOperation(value = "Generate record identifier", response = String.class)
    public String generateIdentifier(@ApiParam("collectionId") @PathVariable("collectionId") String collectionId,
                                       @ApiParam("recordId") @PathVariable("recordId") String recordId) {
        return idService.generateIdentifier(collectionId, recordId);
    }


    /**
     * Normalize the identifiers of a single record
     *
     * @param record The record to normalize the identifiers
     * @return The normalized record
     */

    @RequestMapping(method = RequestMethod.POST,value = "/normalize/single")
    @ResponseBody
    @ApiOperation(value = "Fix the identifiers of a record for internal semantic linking", response = String.class)
    public String normalize(@ApiParam("record") @RequestBody String record) throws IdentifierException {
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
     */

    @RequestMapping(value = "/normalize/batch", method = RequestMethod.POST)
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
