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
import eu.europeana.validation.model.ValidationResult;
import eu.europeana.validation.model.ValidationResultList;
import eu.europeana.validation.rest.exceptions.BatchValidationException;
import eu.europeana.validation.rest.exceptions.ServerException;
import eu.europeana.validation.rest.exceptions.ValidationException;
import eu.europeana.validation.service.SchemaProvider;
import eu.europeana.validation.service.ValidationExecutionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static eu.europeana.metis.RestEndpoints.SCHEMA_BATCH_VALIDATE;
import static org.springframework.http.MediaType.*;

/**
 * REST API Implementation of the Validation Service
 */

@Controller
@Api(value = "/", description = "Schema validation service")
public class ValidationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationController.class);


    private ValidationExecutionService validator;
    private SchemaProvider schemaProvider;

    /**
     * Cretes validation controller based on provided {@link ValidationExecutionService}
     * @param validator
     * @param schemaProvider
     */
    @Autowired
    public ValidationController(ValidationExecutionService validator, SchemaProvider schemaProvider) {
        this.validator = validator;
        this.schemaProvider = schemaProvider;
    }

    /**
     * Single Record service class. The target schema is supplied as a path parameter
     * and the record via POST as a form-data parameter
     *
     * @param targetSchema The schema to validate against
     * @param record       The record to validate
     * @return A serialized ValidationResult. The result is always an OK response unless an Exception is thrown (500)
     * @throws ValidationException 
     */
    @RequestMapping(value = RestEndpoints.SCHEMA_VALIDATE, method = RequestMethod.POST, consumes = APPLICATION_XML_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value = "Validate single record based on schema", response = ValidationResult.class)
    public ValidationResult validate(@ApiParam(value = "schema") @PathVariable("schema") String targetSchema,
                                     @RequestBody String record
    ) throws ValidationException {
        if(!schemaProvider.isPredefined(targetSchema)){
            throw new ValidationException("", "It is not predefined schema.");
        }

        ValidationResult result = validator.singleValidation(targetSchema,null, null, record);
        if (result.isSuccess()) {
            return result;
        } else {
            LOGGER.error(result.getMessage());
            throw new ValidationException(result.getRecordId(), result.getMessage());
        }
    }

    /**
     * Batch Validation REST API implementation. It is exposed via /validate/batch/EDM-{EXTERNAL,INTERNAL}. The parameters are
     * a zip file with records (folders are not currently supported so records need to be at the root of the file)
     *
     * @param targetSchema The schema to validate against
     * @param zipFile      A zip file
     * @return A Validation result List. If the service result is empty we assume that the success field is true
     */

    @RequestMapping(value = SCHEMA_BATCH_VALIDATE, method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "Validate zip file based on schema", response = ValidationResultList.class)
    public ValidationResultList batchValidate(@ApiParam(value = "schema") @PathVariable("schema") String targetSchema,
                                              @ApiParam(value = "file") @RequestParam("file") MultipartFile zipFile)
            throws ServerException, BatchValidationException {

        if(!schemaProvider.isPredefined(targetSchema)){
            throw new BatchValidationException("It is not predefined schema.", new ValidationResultList());
        }

        try {
            String fileName = "/tmp/" + zipFile.getName() + "/" + new Date().getTime();
            FileUtils.copyInputStreamToFile(zipFile.getInputStream(), new File(fileName + ".zip"));

            ZipFile file = new ZipFile(fileName + ".zip");
            file.extractAll(fileName);
            FileUtils.deleteQuietly(new File(fileName + ".zip"));
            File[] files = new File(fileName).listFiles();
            List<String> xmls = new ArrayList<>();
            for (File input : files) {
                if(!input.isDirectory()){
                    FileInputStream stream = new FileInputStream(input);
                    xmls.add(IOUtils.toString(stream));
                    stream.close();
                }
            }
            ValidationResultList list = validator.batchValidation(targetSchema, null, null, xmls);
            if (list.getResultList() != null || list.getResultList().isEmpty()) {
                list.setSuccess(true);
            }
            FileUtils.forceDelete(new File(fileName));
            if (list.isSuccess()) {
                return list;
            } else {
                throw new BatchValidationException("Batch service failed", list);
            }

        } catch (IOException | InterruptedException | ExecutionException | ZipException e) {
            throw new ServerException(e);
        }
    }
}
