package eu.europeana.validation.edm;


import eu.europeana.validation.edm.exceptions.BatchValidationException;
import eu.europeana.validation.edm.exceptions.ServerException;
import eu.europeana.validation.edm.exceptions.ValidationException;
import eu.europeana.validation.edm.model.ValidationResult;
import eu.europeana.validation.edm.model.ValidationResultList;
import eu.europeana.validation.edm.validation.ValidationExecutionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * REST API Implementation of the Validation Service
 */
@Path("/")
@Api( value = "/", description = "Schema validation" )
public class ValidationResource {

    private static final Logger logger =  Logger.getRootLogger();

    private final ValidationExecutionService validator = new ValidationExecutionService();


    /**
     * Single Record validation class. The target schema is supplied as a path parameter
     * and the record via POST as a form-data parameter
     * @param targetSchema The schema to validate against
     * @param record The record to validate
     * @return A serialized ValidationResult. The result is always an OK response unless an Exception is thrown (500)
     */
    @POST
    @Path("/{schema}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Validate single record based on schema", response = ValidationResult.class)
    public Response validate(@ApiParam(value="schema")@PathParam("schema") String targetSchema,
                             @ApiParam(value="record")@FormParam("record")@DefaultValue("undefined") String record,
                             @ApiParam(value="version")@QueryParam("version") String version) throws ValidationException, ServerException {


        try {
            ValidationResult result = null;
            result = validator.singleValidation(targetSchema, version, record);
            if(result.isSuccess()) {
                return Response.ok().entity(result).build();
            } else {
                throw new ValidationException(result.getRecordId(),result.getMessage());
            }
        } catch (InterruptedException|ExecutionException e) {
            throw new ServerException(e.getMessage());
        }



    }

    /**
     * Batch Validation REST API implementation. It is exposed via /validate/batch/EDM-{EXTERNAL,INTERNAL}. The parameters are
     * a zip file with records (folders are not currently supported so records need to be at the root of the file)
     * @param targetSchema The schema to validate against
     * @param zipFile A zip file
     * @param fileDisposition The zip file parameters
     * @return A Validation result List. If the validation result is empty we assume that the success field is true
     */
    @POST
    @Path("/batch/{schema}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Validate zip file based on schema", response = ValidationResultList.class)
    public Response batchValidate(@ApiParam(value="schema")@PathParam("schema") String targetSchema,
                                  @ApiParam(value="version")@PathParam("version")@DefaultValue("undefined") String version,
                                  @ApiParam(value="file")@FormDataParam("file") InputStream zipFile,
                                  @ApiParam(value="file")@FormDataParam("file") FormDataContentDisposition fileDisposition) throws ServerException, BatchValidationException {


        try {
            String fileName = "/tmp/" + fileDisposition.getName() + "/" + new Date().getTime();
            FileUtils.copyInputStreamToFile(zipFile, new File(fileName + ".zip"));

            ZipFile file = new ZipFile(fileName + ".zip");
            file.extractAll(fileName);
            FileUtils.deleteQuietly(new File(fileName + ".zip"));
            File[] files = new File(fileName).listFiles();
            List<String> xmls = new ArrayList<>();
            for (File input : files) {
                xmls.add(IOUtils.toString(new FileInputStream(input)));
            }
            ValidationResultList list = validator.batchValidation(targetSchema, version, xmls);
            if(list.getResultList()!=null||list.getResultList().size()==0){
                list.setSuccess(true);
            }
            FileUtils.forceDelete(new File(fileName));
            if(list.isSuccess()) {
                return Response.ok().entity(list).build();
            } else {
               throw new BatchValidationException("Batch validation failed",list);
            }

        } catch (IOException | InterruptedException|ExecutionException|ZipException e) {
            logger.error(e.getMessage());
            throw new ServerException(e.getMessage());
        }
    }

    /**
     * Batch validation based on a list of records
     * @param targetSchema The target schema
     * @param documents The list of records
     * @return The Validation results
     */
    @POST
    @Path("/batch/records/{schema}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Batch validate based on schema", response = ValidationResult.class)
    public Response batchValidate(@ApiParam(value="schema")@PathParam("schema") String targetSchema,
                                  @ApiParam(value="version")@PathParam("version")@DefaultValue("undefined") String version,
                                  @ApiParam(value="records")@FormParam("records") List<String> documents)
            throws ServerException, BatchValidationException {
        try {
            ValidationResultList list = validator.batchValidation(targetSchema,version,documents);
            if(list.getResultList()!=null||list.getResultList().size()==0){
                list.setSuccess(true);
            }
            if(list.isSuccess()) {
                return Response.ok().entity(list).build();
            } else {
               throw new BatchValidationException("Batch validation failed", list);
            }

        } catch (InterruptedException|ExecutionException e) {
            logger.error(e.getMessage());
            throw new ServerException(e.getMessage());
        }
    }

}
