package eu.europeana.validation.rest;

import static eu.europeana.metis.RestEndpoints.SCHEMA_BATCH_VALIDATE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.utils.ZipFileReader;
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
   *
   * @param validationExecutionService the service that handles validation execution
   * @param schemaProvider the object that provides the schemas
   */
  @Autowired
  public ValidationController(ValidationExecutionService validationExecutionService,
      SchemaProvider schemaProvider) {
    this.validator = validationExecutionService;
    this.schemaProvider = schemaProvider;
  }

  /**
   * Single Record service class. The target schema is supplied as a path parameter
   * and the record via POST as a form-data parameter
   *
   * @param targetSchema The schema to validate against
   * @param record The record to validate
   * @return A serialized ValidationResult. The result is always an OK response unless an Exception is thrown (500)
   */
  @RequestMapping(value = RestEndpoints.SCHEMA_VALIDATE, method = RequestMethod.POST, consumes = APPLICATION_XML_VALUE, produces = APPLICATION_JSON_VALUE)
  @ResponseBody
  @ApiOperation(value = "Validate single record based on schema", response = ValidationResult.class)
  public ValidationResult validate(
      @ApiParam(value = "schema") @PathVariable("schema") String targetSchema,
      @RequestBody String record
  ) throws ValidationException {
    if (!schemaProvider.isPredefined(targetSchema)) {
      throw new ValidationException("", "", "It is not predefined schema.");
    }

    ValidationResult result = validator.singleValidation(targetSchema, null, null, record);
    if (result.isSuccess()) {
      return result;
    } else {
      LOGGER.error(result.getMessage());
      throw new ValidationException(result.getRecordId(), result.getNodeId(), result.getMessage());
    }
  }

  /**
   * Batch Validation REST API implementation. It is exposed via /validate/batch/EDM-{EXTERNAL,INTERNAL}. The parameters are
   * a zip file with records (folders are not currently supported so records need to be at the root of the file)
   *
   * @param targetSchema The schema to validate against
   * @param providedZipFile A zip file
   * @return A Validation result List. If the service result is empty we assume that the success field is true
   * @throws ServerException encapsulates several errors
   * @throws BatchValidationException if the schema does not exist or validation fails.
   */

  @RequestMapping(value = SCHEMA_BATCH_VALIDATE, method = RequestMethod.POST)
  @ResponseBody
  @ApiOperation(value = "Validate zip file based on schema", response = ValidationResultList.class)
  public ValidationResultList batchValidate(
      @ApiParam(value = "schema") @PathVariable("schema") String targetSchema,
      @ApiParam(value = "file") @RequestParam("file") MultipartFile providedZipFile)
      throws ServerException, BatchValidationException {

    if (!schemaProvider.isPredefined(targetSchema)) {
      throw new BatchValidationException("It is not predefined schema.",
          new ValidationResultList());
    }

    final List<String> records;
    try {
      records = new ZipFileReader().getRecordsFromZipFile(providedZipFile.getInputStream());
    } catch (IOException e) {
      throw new ServerException(e);
    }
    if (records.isEmpty()) {
      throw new ServerException("No suitable records found in zip file.");
    }
        
    try {
      ValidationResultList list = validator.batchValidation(targetSchema, null, null, records);
      if (list.getResultList() != null || list.getResultList().isEmpty()) {
        list.setSuccess(true);
      }
      if (list.isSuccess()) {
        return list;
      } else {
        throw new BatchValidationException("Batch service failed", list);
      }
    } catch (InterruptedException | ExecutionException e) {
      throw new ServerException(e);
    }
  }
}
