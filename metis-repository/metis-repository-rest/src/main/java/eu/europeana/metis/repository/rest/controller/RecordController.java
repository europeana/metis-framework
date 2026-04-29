package eu.europeana.metis.repository.rest.controller;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.http.HttpHarvesterImpl;
import eu.europeana.metis.repository.rest.dao.Record;
import eu.europeana.metis.repository.rest.dao.RecordDao;
import eu.europeana.metis.repository.rest.view.InsertionResult;
import eu.europeana.metis.repository.rest.view.RecordView;
import eu.europeana.metis.utils.CompressedFileExtension;
import eu.europeana.metis.utils.RestEndpoints;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
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
public class RecordController {

  public static final String CONTROLLER_TAG_NAME = "RecordController";
  public static final String NOT_FOUND_LOG_STRING = "No record found for this identifier.";
  private static final Pattern UNSUPPORTED_CHARACTERS_PATTERN = Pattern.compile("\\W");
  private static final String REPLACEMENT_CHARACTER = "_";

  private RecordDao recordDao;

  @Autowired
  public void setRecordDao(RecordDao recordDao) {
    this.recordDao = recordDao;
  }

  /**
   * Save a single record into the database
   *
   * @param recordId - A unique record id
   * @param datasetId - The id of the dataset which the record belongs to
   * @param dateStamp - Last time the record was updated. It can also be the date of creation
   * @param markAsDeleted Mark record as deleted
   * @param edmRecord - The record itself
   * @return a summary of the performed actions.
   */
  @PostMapping(value = RestEndpoints.REPOSITORY_RECORDS_RECORD_ID,
      consumes = {MediaType.APPLICATION_XML_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @Operation(
      summary = "Save record",
      description = "The given record is put into the database. If the record ID already exists, "
          + "the record is overwritten. Note that record IDs are normalized to contain "
          + "only the characters a-z, A-Z, 0-9 and `_`. But contrary to the batch upload method, they "
          + "are NOT prefixed by the dataset ID.")
  @ApiResponse(responseCode = "200", description = "Record saved successfully")
  @ApiResponse(responseCode = "404", description = "Illegal dataset or record ID")
  @ApiResponse(responseCode = "500", description = "Error processing the record")
  public InsertionResult saveRecord(
      @Parameter(description = "Record ID (new or existing)", required = true)
      @PathVariable("recordId") String recordId,

      @Parameter(description = "Dataset ID (new or existing)", required = true)
      @RequestParam("datasetId") String datasetId,

      @Parameter(description = "Date stamp (in ISO format)")
      @RequestParam(name = "dateStamp", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateStamp,

      @Parameter(description = "Whether the record is to be marked as deleted", required = true)
      @RequestParam("markAsDeleted") boolean markAsDeleted,

      @Parameter(description = "The actual (EDM/RDF) record", required = true)
      @RequestBody String edmRecord) {
    verifyDatasetId(datasetId);
    final InsertionResult result = new InsertionResult(datasetId,
        Objects.requireNonNullElseGet(dateStamp, Instant::now));
    saveRecord(recordId, edmRecord, result, markAsDeleted);
    return result;
  }

  /**
   * Save multiple records into the database
   * <p>
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
  @Operation(
      summary = "Upload dataset records",
      description = "The given records are put into the database as non-deleted records. " +
          "Record IDs are computed to be the file name (without extension) prefixed by the dataset ID. " +
          "If a record ID already exists, the record is overwritten. " +
          "IDs are normalized to contain only characters a-z, A-Z, 0-9 and '_'."
  )
  @ApiResponse(responseCode = "200", description = "Records uploaded successfully")
  @ApiResponse(responseCode = "404", description = "Illegal dataset or record ID")
  @ApiResponse(responseCode = "500", description = "Error processing the file archive")
  public InsertionResult saveRecords(
      @Parameter(
          description = "Dataset ID (new or existing)",
          required = true,
          example = "dataset-123"
      )
      @RequestParam("datasetId") String datasetId,
      @Parameter(
          description = "Date stamp (ISO-8601 format)",
          example = "2025-01-15T12:30:00Z"
      )
      @RequestParam(name = "dateStamp", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateStamp,
      @Parameter(
          description = "ZIP file containing EDM/RDF records",
          required = true,
          content = @Content(
              mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
              schema = @Schema(type = "string", format = "binary")
          )
      )
      @RequestPart("recordsZipFile") MultipartFile recordsZipFile) {
    verifyDatasetId(datasetId);
    final InsertionResult result = new InsertionResult(datasetId,
        Objects.requireNonNullElseGet(dateStamp, Instant::now));
    try (final InputStream inputStream = recordsZipFile.getInputStream()) {
      new HttpHarvesterImpl().harvestFullRecords(inputStream, CompressedFileExtension.ZIP, entry -> {
        final byte[] content;
        try (InputStream contentStream = entry.getContent()) {
          content = contentStream.readAllBytes();
        }
        final String recordId = datasetId + "_" + FilenameUtils.getBaseName(entry.getHarvestingIdentifier());
        saveRecord(recordId, new String(content, StandardCharsets.UTF_8), result, false);
        return true;
      });
    } catch (IOException | HarvesterException | RuntimeException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
    return result;
  }

  /**
   * Update record header (metadata information) of the record given by the record ID.
   *
   * @param recordId A unique record id
   * @param datasetId The id of the dataset which the record belongs to
   * @param dateStamp Last time the record was updated. It can also be the date of creation
   * @param markAsDeleted Mark record as deleted
   * @return a summary of the performed actions.
   */
  @PutMapping(value = RestEndpoints.REPOSITORY_RECORDS_RECORD_ID_HEADER,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @Operation(
      summary = "Update record header",
      description = "Updates the header fields of the given record."
  )
  @ApiResponse(responseCode = "200", description = "Record header updated successfully")
  @ApiResponse(responseCode = "404", description = "Illegal dataset or unknown record ID")
  @ApiResponse(responseCode = "500", description = "Error processing the record")
  public InsertionResult updateRecordHeader(
      @Parameter(
          description = "Record ID (existing)",
          required = true,
          example = "record-123"
      )
      @PathVariable("recordId") String recordId,

      @Parameter(
          description = "Dataset ID (new or existing)",
          required = true,
          example = "dataset-456"
      )
      @RequestParam("datasetId") String datasetId,

      @Parameter(
          description = "Date stamp (ISO-8601 format)",
          example = "2025-01-15T12:30:00Z"
      )
      @RequestParam(name = "dateStamp", required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateStamp,

      @Parameter(
          description = "Whether the record is to be marked as deleted",
          required = true,
          example = "false"
      )
      @RequestParam("markAsDeleted") boolean markAsDeleted) {
    final Record oaiRecord = recordDao.getRecord(recordId);
    if (oaiRecord == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND_LOG_STRING);
    }
    return saveRecord(recordId, datasetId, dateStamp, markAsDeleted, oaiRecord.getEdmRecord());
  }

  private void saveRecord(String providedRecordId, String edmRecord, InsertionResult result,
      boolean markedAsDeleted) {
    final String recordId = normalizeRecordId(providedRecordId);
    try {
      final Record recordToSave = new Record(recordId, result.getDatasetId(), result.getDateStamp(),
          markedAsDeleted, edmRecord);
      if (recordDao.createRecord(recordToSave)) {
        result.addInsertedRecord(recordId);
      } else {
        result.addUpdatedRecord(recordId);
      }
    } catch (RuntimeException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Get a record from the database using an identifier.
   *
   * @param recordId the record identifier
   * @return the record
   */
  @GetMapping(value = RestEndpoints.REPOSITORY_RECORDS_RECORD_ID,
      produces = {MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @Operation(
      summary = "Get record",
      description = "Retrieves a record from the database by its identifier."
  )
  @ApiResponse(
      responseCode = "200",
      description = "Record retrieved successfully",
      content = @Content(
          mediaType = MediaType.APPLICATION_XML_VALUE,
          schema = @Schema(implementation = RecordView.class)
      )
  )
  @ApiResponse(responseCode = "404", description = "Record ID is invalid or unknown")
  @ApiResponse(responseCode = "500", description = "Error processing the request")
  public RecordView getRecord(
      @Parameter(
          description = "Record ID",
          required = true,
          example = "record-123"
      )
      @PathVariable("recordId") String recordId) {
    final Record oaiRecord = recordDao.getRecord(recordId);
    if (oaiRecord == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND_LOG_STRING);
    }
    return new RecordView(oaiRecord.getRecordId(), oaiRecord.getDatasetId(), oaiRecord.getDateStamp(),
        oaiRecord.isDeleted(), oaiRecord.getEdmRecord());
  }

  /**
   * Delete a record from the database given a record identifier.
   *
   * @param recordId the record identifier
   */
  @DeleteMapping(value = RestEndpoints.REPOSITORY_RECORDS_RECORD_ID)
  @ResponseStatus(HttpStatus.OK)
  @Operation(
      summary = "Delete record",
      description = "Deletes the record from the database. Note: this is not the same as marking a record as deleted."
  )
  @ApiResponse(responseCode = "204", description = "Record deleted successfully")
  @ApiResponse(responseCode = "404", description = "Record ID is invalid or unknown")
  @ApiResponse(responseCode = "500", description = "Error processing the request")
  public void deleteRecord(
      @Parameter(
          description = "Record ID",
          required = true,
          example = "record-123"
      )
      @PathVariable("recordId") String recordId) {
    if (!recordDao.deleteRecord(recordId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, NOT_FOUND_LOG_STRING);
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
