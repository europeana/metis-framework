package eu.europeana.validation.edm.exceptions.exceptionmappers;

import eu.europeana.validation.edm.exceptions.BatchValidationException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Created by ymamakis on 2/24/16.
 */
public class BatchValidationExceptionMapper implements ExceptionMapper<BatchValidationException> {
    @Override
    public Response toResponse(BatchValidationException e) {
        return  Response.status(Response.Status.fromStatusCode(422)).entity(e.getList()).build();
    }
}
