package eu.europeana.validation.edm.exceptions.exceptionmappers;

import eu.europeana.validation.edm.exceptions.ServerException;
import eu.europeana.validation.edm.model.ValidationResult;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created by ymamakis on 2/24/16.
 */
@Provider
public class ServerExceptionMapper implements ExceptionMapper<ServerException> {
    @Override
    public Response toResponse(ServerException e) {
        ValidationResult result  = new ValidationResult();
        result.setSuccess(false);
        result.setMessage(e.getMessage());
        return Response.serverError().entity(result).build();
    }
}
