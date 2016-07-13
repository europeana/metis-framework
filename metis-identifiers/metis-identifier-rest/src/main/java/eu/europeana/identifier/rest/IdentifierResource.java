package eu.europeana.identifier.rest;

import eu.europeana.identifier.rest.exceptions.IdentifierException;
import eu.europeana.identifier.service.IdentifierService;
import eu.europeana.itemization.Request;
import eu.europeana.itemization.RequestResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.jibx.runtime.JiBXException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Identifier REST API
 * Created by ymamakis on 2/9/16.
 */
@Path("/identifier")
@Api(value = "/identifier", description = "Generate Europeana identifier")
public class IdentifierResource {

    private IdentifierService idService = new IdentifierService();

    /**
     * Generate a Europeana Identifier
     *
     * @param collectionId The collection Id the records belongs to
     * @param recordId     The record identifier
     * @return A Europeana identifier
     */
    @GET
    @Path("/generate/{collectionId}/{recordId}")
    @ApiOperation(value = "Generate record identifier", response = String.class)
    public Response generateIdentifier(@ApiParam("collectionId") @PathParam("collectionId") String collectionId,
                                       @ApiParam("recordId") @PathParam("recordId") String recordId) {
        return Response.ok().entity(idService.generateIdentifier(collectionId, recordId)).build();
    }


    /**
     * Normalize the identifiers of a single record
     *
     * @param record The record to normalize the identifiers
     * @return The normalized record
     */
    @POST
    @Path("/normalize/single")
    @Produces(MediaType.APPLICATION_XML)
    @ApiOperation(value = "Fix the identifiers of a record for internal semantic linking", response = String.class)
    public Response normalize(@ApiParam("record") @FormParam("record") String record) throws IdentifierException {
        try {
            return Response.ok().entity(idService.fixIdentifiers(record)).build();
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
    @POST
    @Path("/normalize/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Batch normalizations of records", response = RequestResult.class)
    public Response normalize(@FormParam("records") Request records) throws IdentifierException {
        try {
            RequestResult res = new RequestResult();
            res.setItemizedRecords(idService.fixIdentifiers(records.getRecords()));
            return Response.ok().entity(res).build();
        } catch (JiBXException e) {
            throw new IdentifierException(e.getMessage());
        }
    }
}
