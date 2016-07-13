package eu.europeana.validation.edm.exceptions.exceptionmappers;

import eu.europeana.validation.edm.exceptions.ValidationException;
import eu.europeana.validation.edm.model.ValidationResult;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created by ymamakis on 2/24/16.
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
    @Override
    public Response toResponse(ValidationException e) {
        ValidationResult result = new ValidationResult();
        if(StringUtils.isNotEmpty(e.getId())) {
            result.setRecordId(e.getId());
        }
        result.setMessage(e.getMessage());
        result.setSuccess(false);
        return Response.status(Response.Status.EXPECTATION_FAILED).entity(result).build();
    }
}
