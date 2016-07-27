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


import eu.europeana.validation.rest.exceptions.BatchValidationException;
import eu.europeana.validation.rest.exceptions.ServerException;
import eu.europeana.validation.rest.exceptions.ValidationException;
import eu.europeana.validation.model.ValidationResult;
import eu.europeana.validation.model.ValidationResultList;
import eu.europeana.validation.service.ValidationExecutionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.DefaultValue;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * REST API Implementation of the Validation Service
 */

@Controller
@Api(value = "/", description = "Schema validation service" )
public class ValidationController {

    private static final Logger logger =  Logger.getRootLogger();

    @Autowired
    private ValidationExecutionService validator;


    /**
     * Single Record service class. The target schema is supplied as a path parameter
     * and the record via POST as a form-data parameter
     * @param targetSchema The schema to validate against
     * @param record The record to validate
     * @return A serialized ValidationResult. The result is always an OK response unless an Exception is thrown (500)
     */
    @RequestMapping(value = "/validate/{schema}",method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "Validate single record based on schema", response = ValidationResult.class)
    public ValidationResult validate(@ApiParam(value="schema")@PathVariable("schema") String targetSchema,
                             @ApiParam(value = "record") @RequestParam(value = "record") String record,
                             @ApiParam(value="version")@RequestParam(value = "version",
                                     defaultValue = "undefined")String version)
            throws ValidationException, ServerException {
        try {
            ValidationResult result = null;
            result = validator.singleValidation(targetSchema, version, record);
            if(result.isSuccess()) {
                return result;
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
     * @return A Validation result List. If the service result is empty we assume that the success field is true
     */

    @RequestMapping(method = RequestMethod.POST,value = "/validate/batch/{schema}")
    @ResponseBody
    @ApiOperation(value = "Validate zip file based on schema", response = ValidationResultList.class)
    public ValidationResultList batchValidate(@ApiParam(value="schema")@PathVariable("schema") String targetSchema,
                                  @ApiParam(value="version")@RequestParam(value = "version", defaultValue = "undefined") String version,
                                  @ApiParam(value="file")@RequestParam("file") MultipartFile zipFile) throws ServerException, BatchValidationException {


        try {
            String fileName = "/tmp/" + zipFile.getName() + "/" + new Date().getTime();
            FileUtils.copyInputStreamToFile(zipFile.getInputStream(), new File(fileName + ".zip"));

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
                return list;
            } else {
               throw new BatchValidationException("Batch service failed",list);
            }

        } catch (IOException | InterruptedException|ExecutionException|ZipException e) {
            logger.error(e.getMessage());
            throw new ServerException(e.getMessage());
        }
    }

    /**
     * Batch service based on a list of records
     * @param targetSchema The target schema
     * @param documents The list of records
     * @return The Validation results
     */
    @RequestMapping(method = RequestMethod.POST,value = "/validate/batch/records/{schema}")
    @ResponseBody
    @ApiOperation(value = "Batch validate based on schema", response = ValidationResult.class)
    public ValidationResultList batchValidate(@ApiParam(value="schema")@PathVariable("schema") String targetSchema,
                                  @ApiParam(value="version")@PathVariable("version")@DefaultValue("undefined") String version,
                                  @ApiParam(value="records")@RequestParam("records") List<String> documents)
            throws ServerException, BatchValidationException {
        try {
            ValidationResultList list = validator.batchValidation(targetSchema,version,documents);
            if(list.getResultList()!=null||list.getResultList().size()==0){
                list.setSuccess(true);
            }
            if(list.isSuccess()) {
                return list;
            } else {
               throw new BatchValidationException("Batch service failed", list);
            }

        } catch (InterruptedException|ExecutionException e) {
            logger.error(e.getMessage());
            throw new ServerException(e.getMessage());
        }
    }

}
