package eu.europeana.validation.edm;

import eu.europeana.validation.edm.model.Schema;
import eu.europeana.validation.edm.validation.ValidationManagementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

/**
 * Created by ymamakis on 3/14/16.
 */
@Path("/schemas")
@Api(value = "/schemas", description = "Schema management for validation")
public class ValidationManagementResource {

    private ValidationManagementService service = new ValidationManagementService();
    @GET
    @Path("/schema/download/{name}")
    @ApiOperation(value = "Download the schema", produces = MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getZip(@ApiParam("name")@PathParam("name") String name,@ApiParam("version") @QueryParam("version")@DefaultValue("undefined")String version ){
        return Response.ok(new ByteArrayInputStream(service.getZip(name, version)),MediaType.APPLICATION_OCTET_STREAM).
                header("Content-Disposition", "attachment; filename=\"" + name +"-"+version + ".zip\"" ).build();
    }

    @POST
    @Path("/schema/{name}")
    @ApiOperation(value = "Create a new Schema")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createSchema(@ApiParam("name") @PathParam("name") String name,@ApiParam("schemaPath") @QueryParam("schemaPath") String schemaPath,
                                 @ApiParam("schematronPath") @QueryParam("schematronPath")String schematronPath,
                                 @ApiParam("version")@QueryParam("version")@DefaultValue("undefined")String version,
                                 @ApiParam("file")@FormDataParam("file") InputStream zipFile,
                                 @ApiParam("file")@FormDataParam("file") FormDataContentDisposition fileDisposition) throws IOException{
        service.createSchema(name,schemaPath,schematronPath,version,zipFile);
        return Response.created(URI.create("/schema/download/"+name)).build();
    }

    @PUT
    @Path("/schema/{name}")
    @ApiParam(value="Update a schema")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateSchema(@ApiParam("name")@PathParam("name") String name, @ApiParam("schemaPath")@QueryParam("schemaPath") String schemaPath,
                                 @ApiParam("schematronPath") @QueryParam("schematronPath")String schematronPath, @ApiParam("version")@QueryParam("version")@DefaultValue("undefined")String version,
                                 @ApiParam("file")@FormDataParam("file") InputStream zipFile,
                                 @ApiParam("file")@FormDataParam("file") FormDataContentDisposition fileDisposition) throws IOException{

        service.updateSchema(name,schemaPath,schematronPath,version, zipFile);
        return Response.ok().build();
    }

    @DELETE
    @ApiOperation(value = "Delete a schema")
    @Path("/schema/{name}")
    public Response deleteSchema(@ApiParam("name")@PathParam("name") String name, @ApiParam("version")@QueryParam("version")@DefaultValue("undefined") String version){

        service.deleteSchema(name,version);
        return Response.ok().build();
    }

    @GET
    @Path("/schema/{name}")
    @ApiOperation(value="Get a schema", response = Schema.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSchema(@ApiParam("name")@PathParam("name") String name, @ApiParam("name")@QueryParam("version")@DefaultValue("undefined") String version){
        return Response.ok(service.getSchemaByName(name,version)).build();
    }
    @GET
    @Path("/all")
    @ApiOperation(value = "Get all available schemas", response = List.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllSchema(){
        return Response.ok(service.getAll()).build();
    }
}
