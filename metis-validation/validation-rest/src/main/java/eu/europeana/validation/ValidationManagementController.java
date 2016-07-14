package eu.europeana.validation;

import eu.europeana.validation.model.Schema;
import eu.europeana.validation.validation.ValidationManagementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Created by ymamakis on 3/14/16.
 */
@Controller("/schemas")
@Api(value = "/schemas", description = "Schema management for validation")
public class ValidationManagementController {

    @Autowired
    private ValidationManagementService service;
    @RequestMapping(value = "/schema/download/{name}",method = RequestMethod.GET)
    @ApiOperation(value = "Download the schema", produces = MediaType.APPLICATION_OCTET_STREAM)
    @ResponseBody
    public ResponseEntity<InputStreamResource> getZip(@ApiParam("name")@PathVariable("name") String name,
                                                      @ApiParam("version") @RequestParam(value = "version",
                                                              defaultValue = "undefined")String version ) throws IOException {
        InputStreamResource res = new InputStreamResource(new ByteArrayInputStream(service.getZip(name, version)));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(res.contentLength());
        return new ResponseEntity<>(res,headers,HttpStatus.OK);
    }

    @RequestMapping(value = "/schema/{name}", method = RequestMethod.POST)
    @ApiOperation(value = "Create a new Schema")
    @ResponseStatus(HttpStatus.CREATED)
    public URI createSchema(@ApiParam("name") @PathVariable("name") String name,@ApiParam("schemaPath") @RequestParam("schemaPath") String schemaPath,
                                 @ApiParam("schematronPath") @RequestParam("schematronPath")String schematronPath,
                                 @ApiParam("version")@RequestParam("version")@DefaultValue("undefined")String version,
                                 @ApiParam("file")@RequestParam("file") MultipartFile zipFile) throws IOException{
        service.createSchema(name,schemaPath,schematronPath,version,zipFile.getInputStream());
        return URI.create("/schema/download/"+name);
    }

    @RequestMapping(value = "/schema/{name}", method = RequestMethod.PUT)
    @ApiParam(value="Update a schema")
    @ResponseStatus (value = HttpStatus.OK)
    public void updateSchema(@ApiParam("name")@PathVariable("name") String name, @ApiParam("schemaPath")@RequestParam("schemaPath") String schemaPath,
                                 @ApiParam("schematronPath") @RequestParam("schematronPath")String schematronPath, @ApiParam("version")@RequestParam(value = "version", defaultValue = "undefined") String version,
                                 @ApiParam("file")@RequestParam("file") MultipartFile zipFile) throws IOException{

        service.updateSchema(name,schemaPath,schematronPath,version, zipFile.getInputStream());
    }

    @RequestMapping(value = "/schema/{name}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete a schema")
    @ResponseStatus (value = HttpStatus.OK)
    public void deleteSchema(@ApiParam("name")@PathVariable("name") String name, @ApiParam("version")@RequestParam(value = "version", defaultValue = "undefined") String version){
        service.deleteSchema(name,version);
    }

    @RequestMapping(value = "/schema/{name}", method = RequestMethod.GET)
    @ApiOperation(value="Get a schema", response = Schema.class)
    @ResponseBody
    public Schema getSchema(@ApiParam("name")@PathVariable("name") String name, @ApiParam("name")@RequestParam(value = "version", defaultValue = "undefined") String version){
        return service.getSchemaByName(name,version);
    }

    @ApiOperation(value = "Get all available schemas", response = List.class)
    @RequestMapping(method = RequestMethod.GET, value = "/all")
    @ResponseBody
    public List<Schema> getAllSchema(){
        return service.getAll();
    }
}
