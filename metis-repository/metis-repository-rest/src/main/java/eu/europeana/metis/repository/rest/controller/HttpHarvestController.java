package eu.europeana.metis.repository.rest.controller;

import eu.europeana.metis.repository.rest.dao.Record;
import eu.europeana.metis.repository.rest.dao.RecordDao;
import eu.europeana.metis.utils.RestEndpoints;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller for HTTP harvesting.
 */
@RestController
@Tags(@Tag(name = HttpHarvestController.CONTROLLER_TAG_NAME,
    description = "Controller providing access to HTTP (zip) harvesting functionality."))
public class HttpHarvestController {

  public static final String CONTROLLER_TAG_NAME = "HttpHarvestController";

  private RecordDao recordDao;

  @Autowired
  public void setRecordDao(RecordDao recordDao) {
    this.recordDao = recordDao;
  }

  /**
   * It creates a zip of records that belong to dataset with datasetId
   *
   * @param datasetId The dataset ID of which to create a zipfile.
   * @return A zip of records
   */
  @Operation(
      summary = "Export dataset as ZIP",
      description = "The dataset is exported as a zip file for harvesting by Metis. " +
          "Records that are marked as deleted will be excluded."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "ZIP file generated successfully",
          content = @Content(mediaType = "application/zip")),
      @ApiResponse(responseCode = "404", description = "No records for this dataset"),
      @ApiResponse(responseCode = "500", description = "Error obtaining the records")
  })
  @GetMapping(value = RestEndpoints.REPOSITORY_HTTP_ENDPOINT_ZIP,
      produces = "application/zip")
  public ResponseEntity<byte[]> getDatasetRecords(
      @Parameter(
          description = "Dataset ID (new or existing)",
          required = true,
          example = "dataset-123"
      )
      @PathVariable("dataset") String datasetId) {

    // Create zip file in memory (and keep track on whether there are any records).
    final AtomicBoolean recordsFound = new AtomicBoolean(false);
    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try (final ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
      final Stream<Record> allRecordsFromDataset = recordDao.getAllRecordsFromDataset(datasetId);
      allRecordsFromDataset.forEach(datasetRecord -> {
        if (!datasetRecord.isDeleted()) {
          addRecordToZipFile(datasetRecord, zipOutputStream);
          recordsFound.set(true);
        }
      });
      zipOutputStream.finish();
      zipOutputStream.flush();
    } catch (RuntimeException | IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "There was problems while zipping the records.", e);
    }

    // If there are no records found, we return a 404 code.
    if (!recordsFound.get()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No records found for this dataset.");
    }

    // Return bytes as zip file.
    return ResponseEntity.ok()
                         .header("Content-Disposition", "attachment; filename=\"" + datasetId + ".zip\"")
                         .body(byteArrayOutputStream.toByteArray());
  }

  private static void addRecordToZipFile(Record oaiRecord, ZipOutputStream zipOutputStream) {
    try {
      zipOutputStream.putNextEntry(new ZipEntry(oaiRecord.getRecordId() + ".xml"));
      zipOutputStream.write(oaiRecord.getEdmRecord().getBytes(StandardCharsets.UTF_8));
      zipOutputStream.closeEntry();
    } catch (IOException e) {
      throw new IllegalStateException(
          "There was a problem while preparing the records to be zipped.", e);
    }
  }
}
