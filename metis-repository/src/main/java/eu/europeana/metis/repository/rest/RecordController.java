package eu.europeana.metis.repository.rest;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.http.CompressedFileExtension;
import eu.europeana.metis.harvesting.http.HttpHarvesterImpl;
import eu.europeana.metis.repository.dao.Record;
import eu.europeana.metis.repository.dao.RecordDao;
import eu.europeana.metis.utils.RestEndpoints;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller for record management.
 */
@RestController
@Tags(@Tag(name = RecordController.CONTROLLER_TAG_NAME,
        description = "Controller providing access to record management functionality."))
@Api(tags = RecordController.CONTROLLER_TAG_NAME)
public class RecordController {

  public static final String CONTROLLER_TAG_NAME = "RecordController";
  private static final Logger LOGGER = LoggerFactory.getLogger(RecordController.class);

  private static final Pattern UNSUPPORTED_CHARACTERS_PATTERN = Pattern.compile("[^a-zA-Z0-9_]");
  private static final String REPLACEMENT_CHARACTER = "_";

  private RecordDao recordDao;

  @Autowired
  void setRecordDao(RecordDao recordDao){
    this.recordDao = recordDao;
  }

  /**
   * Save a single record into the database
   *
   * @param recordId - A unique record id
   * @param datasetId - The id of the dataset which the record belongs to
   * @param dateStamp - Last time the record was updated. It can also be the date of creation
   * @param edmRecord - The record itself
   * @return a summary of the performed actions.
   */
  @PostMapping(value = RestEndpoints.REPOSITORY_RECORDS_RECORD_ID,
      consumes = {MediaType.APPLICATION_XML_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiOperation(value = "The given record is put into the database. If the record ID already "
      + "exists, the record is overwritten. Note that record IDs are normalized to contain "
      + "only the characters a-z, A-Z, 0-9 and `_`.")
 @ApiResponses(value = {@ApiResponse(code = 404, message = "Illegal dataset or record ID"),
          @ApiResponse(code = 500, message = "Error processing the record")})
  public InsertionResult saveRecord(
          @ApiParam(value = "Record ID (new or existing)", required = true) @PathVariable("recordId") String recordId,
          @ApiParam(value = "Dataset ID (new or existing)", required = true) @RequestParam("datasetId") String datasetId,
          @ApiParam(value = "Date stamp (in ISO format)") @RequestParam(name = "dateStamp", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateStamp,
          @ApiParam(value = "The actual (EDM/RDF) record", required = true) @RequestBody String edmRecord) {
    verifyDatasetId(datasetId);
    final InsertionResult result = new InsertionResult(datasetId,
            Objects.requireNonNullElseGet(dateStamp, Instant::now));
    saveRecord(recordId, edmRecord, result);
    return result;
  }

  /**
   * Save multiple records into the database
   *
   * TODO The swagger console does not pick up the @ApiParam settings.
   *
   * @param datasetId - The id of the dataset which the record belongs to
   * @param dateStamp - Last time the record was updated. It can also be the date of creation
   * @param recordsZipFile - The records themselves in a zip file.
   * @return A summary of the performed actions.
   */
  @PostMapping(value = RestEndpoints.REPOSITORY_RECORDS,
      consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiOperation(value = "The given records are put into the database. If a record ID (i.e. a file "
      + "name without the extension) already exists, the record is overwritten. Note that record IDs "
      + "are normalized to contain only the characters a-z, A-Z, 0-9 and `_`.")
  @ApiResponses(value = {@ApiResponse(code = 404, message = "Illegal dataset or record ID"),
          @ApiResponse(code = 500, message = "Error processing the file archive")})
  public InsertionResult saveRecords(
          @ApiParam(value = "Dataset ID (new or existing)", required = true) @RequestPart("datasetId") String datasetId,
          @ApiParam(value = "Date stamp (in ISO format)") @RequestPart(name = "dateStamp", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateStamp,
          @ApiParam(value = "The (EDM/RDF) records", required = true) @RequestPart MultipartFile recordsZipFile) {
    verifyDatasetId(datasetId);
    final InsertionResult result = new InsertionResult(datasetId,
            Objects.requireNonNullElseGet(dateStamp, Instant::now));
    try (final InputStream inputStream = recordsZipFile.getInputStream()) {
      new HttpHarvesterImpl().harvestRecords(inputStream, CompressedFileExtension.ZIP, entry -> {
        final byte[] content = entry.getEntryContent().readAllBytes();
        final String recordId = FilenameUtils.getBaseName(entry.getEntryName());
        saveRecord(recordId, new String(content, StandardCharsets.UTF_8), result);
      });
    } catch (IOException | HarvesterException | RuntimeException e) {

      // Report any problems (also for individual records) as 500 code.
      LOGGER.warn("A problem occurred while processing the file archive.", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
    return result;
  }

  private void saveRecord(String providedRecordId, String edmRecord, InsertionResult result) {
    final String recordId = normalizeRecordId(providedRecordId);
    try {
      final Record recordToSave = new Record(recordId, result.getDatasetId(), result.getDateStamp(),
              edmRecord);
      if (recordDao.createRecord(recordToSave)) {
        result.addInsertedRecord(recordId);
      } else {
        result.addUpdatedRecord(recordId);
      }
    } catch (RuntimeException e) {

      // Report any problems (also for individual records) as 500 code.
      LOGGER.warn("A problem occurred while saving a record.", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  @GetMapping(value = RestEndpoints.REPOSITORY_RECORDS_RECORD_ID,
      produces = {MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiOperation(value = "The record is retrieved from the database.")
  @ApiResponses(value = {@ApiResponse(code = 404, message = "Record ID is invalid or unknown"),
      @ApiResponse(code = 500, message = "Error processing the request")})
  public RecordView getRecord(
      @ApiParam(value = "Record ID", required = true) @PathVariable("recordId") String recordId) {
    final Record record = recordDao.getRecord(recordId);
    if (record==null){
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          "No record found for this identifier.");
    }
    return new RecordView(record.getRecordId(), record.getDatasetId(), record.getDateStamp(),
        record.getEdmRecord());
  }

  @DeleteMapping(value = RestEndpoints.REPOSITORY_RECORDS_RECORD_ID)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiOperation(value = "The record is deleted from the database.")
  @ApiResponses(value = {@ApiResponse(code = 404, message = "Record ID is invalid or unknown"),
      @ApiResponse(code = 500, message = "Error processing the request")})
  public void deleteRecord(
      @ApiParam(value = "Record ID", required = true) @PathVariable("recordId") String recordId) {
    if (!recordDao.deleteRecord(recordId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          "No record found for this identifier.");
    }
  }

  private static void verifyDatasetId(String datasetId) {
    if (UNSUPPORTED_CHARACTERS_PATTERN.matcher(datasetId).matches()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid dataset ID.");
    }
  }

  private static String normalizeRecordId(String suggestedRecordId) {
    if (StringUtils.isEmpty(suggestedRecordId)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid record ID.");
    }
    return UNSUPPORTED_CHARACTERS_PATTERN.matcher(suggestedRecordId)
            .replaceAll(REPLACEMENT_CHARACTER);
  }
}
