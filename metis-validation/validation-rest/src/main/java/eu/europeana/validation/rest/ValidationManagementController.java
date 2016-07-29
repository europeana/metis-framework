/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.validation.rest;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.validation.model.Schema;
import eu.europeana.validation.service.ValidationManagementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import static eu.europeana.metis.RestEndpoints.SCHEMAS_ALL;
import static eu.europeana.metis.RestEndpoints.SCHEMAS_MANAGE_BY_NAME;

/**
 * Created by ymamakis on 3/14/16.
 */
@Controller
@Api(value = "/", description = "Schema management for service")
public class ValidationManagementController {

    @Autowired
    private ValidationManagementService service;

    @RequestMapping(value = RestEndpoints.SCHEMAS_DOWNLOAD_BY_NAME, method = RequestMethod.GET)
    @ApiOperation(value = "Download the schema", response = InputStreamResource.class)
    @ResponseBody
    public void getZip(@ApiParam("name") @PathVariable("name") String name,
                       @ApiParam("version") @RequestParam(value = "version",
                               defaultValue = "undefined") String version, HttpServletResponse response) throws IOException {
        byte[] file = service.getZip(name, version);
        ByteArrayInputStream bain = new ByteArrayInputStream(file);
        String mimeType = "application/octet-stream";
        response.setContentType(mimeType);
        response.setHeader("Content-Disposition", String.format("inline; filename=\"" + name + ".zip\""));
        response.setContentLength(file.length);
        InputStream inputStream = new BufferedInputStream(bain);
        FileCopyUtils.copy(inputStream, response.getOutputStream());
    }

    @RequestMapping(value = SCHEMAS_MANAGE_BY_NAME, method = RequestMethod.POST)
    @ApiOperation(value = "Create a new Schema")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public String createSchema(@ApiParam("name") @PathVariable("name") String name, @ApiParam("schemaPath") @RequestParam("schemaPath") String schemaPath,
                               @ApiParam("schematronPath") @RequestParam("schematronPath") String schematronPath,
                               @ApiParam("version") @RequestParam("version") @DefaultValue("undefined") String version,
                               @ApiParam("file") @RequestParam("file") MultipartFile zipFile) throws IOException {
        service.createSchema(name, schemaPath, schematronPath, version, zipFile.getInputStream());
        return URI.create("/manage/schemas/schema/download/" + name).toString();
    }

    @RequestMapping(value = SCHEMAS_MANAGE_BY_NAME, method = RequestMethod.PUT)
    @ApiParam(value = "Update a schema")
    @ResponseStatus(value = HttpStatus.OK)
    public void updateSchema(@ApiParam("name") @PathVariable("name") String name, @ApiParam("schemaPath") @RequestParam("schemaPath") String schemaPath,
                             @ApiParam("schematronPath") @RequestParam("schematronPath") String schematronPath, @ApiParam("version") @RequestParam(value = "version", defaultValue = "undefined") String version,
                             @ApiParam("file") @RequestParam("file") MultipartFile zipFile) throws IOException {

        service.updateSchema(name, schemaPath, schematronPath, version, zipFile.getInputStream());
    }

    @RequestMapping(value = SCHEMAS_MANAGE_BY_NAME, method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete a schema")
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteSchema(@ApiParam("name") @PathVariable("name") String name, @ApiParam("version") @RequestParam(value = "version", defaultValue = "undefined") String version) {
        service.deleteSchema(name, version);
    }

    @RequestMapping(value = SCHEMAS_MANAGE_BY_NAME, method = RequestMethod.GET)
    @ApiOperation(value = "Get a schema", response = Schema.class)
    @ResponseBody
    public Schema getSchema(@ApiParam("name") @PathVariable("name") String name, @ApiParam("name") @RequestParam(value = "version", defaultValue = "undefined") String version) {
        return service.getSchemaByName(name, version);
    }

    @ApiOperation(value = "Get all available schemas", response = List.class)
    @RequestMapping(method = RequestMethod.GET, value = SCHEMAS_ALL)
    @ResponseBody
    public List<Schema> getAllSchema() {
        return service.getAll();
    }
}
