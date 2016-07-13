package eu.europeana.redirects.rest;

import eu.europeana.redirects.model.RedirectRequest;
import eu.europeana.redirects.model.RedirectRequestList;
import eu.europeana.redirects.model.RedirectResponse;
import eu.europeana.redirects.model.RedirectResponseList;
import eu.europeana.redirects.service.RedirectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Endpoint for Europeana Redirects Service module
 * Created by ymamakis on 1/15/16.
 */
@Path("/")
@Api("/")
public class RedirectResource {
    @Inject private  RedirectService redirectService;



    @POST
    @Path("/redirect/single")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Generate a single redirect",response = RedirectResponse.class)
    public Response redirectSingle(@ApiParam("record") @FormParam("record")RedirectRequest request){
            return Response.ok().entity(redirectService.createRedirect(request)).build();

    }
    @POST
    @Path("/redirect/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Generate batch redirects",response = RedirectResponseList.class)
    public Response redirectBatch(@ApiParam("records") @FormParam("records")RedirectRequestList requestList){
        return Response.ok().entity(redirectService.createRedirects(requestList)).build();
    }

}
