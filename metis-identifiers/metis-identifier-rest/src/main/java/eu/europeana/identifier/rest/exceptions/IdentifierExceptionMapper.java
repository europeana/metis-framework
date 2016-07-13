package eu.europeana.identifier.rest.exceptions;

import eu.europeana.itemization.IdentifierError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created by ymamakis on 2/25/16.
 */
@Provider
public class IdentifierExceptionMapper implements ExceptionMapper<IdentifierException> {

    public Response toResponse(IdentifierException e) {
        IdentifierError error = new IdentifierError();
        error.setMessage(e.getMessage());
        return Response.status(Response.Status.fromStatusCode(422)).entity(error).build();
    }
}
