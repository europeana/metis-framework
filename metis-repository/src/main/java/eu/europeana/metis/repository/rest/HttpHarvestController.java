package eu.europeana.metis.repository.rest;

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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller for HTTP harvesting.
 */
@RestController
@Tags(@Tag(name = HttpHarvestController.CONTROLLER_TAG_NAME,
    description = "Controller providing access to HTTP (zip) harvesting functionality."))
@Api(tags = HttpHarvestController.CONTROLLER_TAG_NAME)
public class HttpHarvestController {

  public static final String CONTROLLER_TAG_NAME = "HttpHarvestController";
  private static final Logger LOGGER = LoggerFactory.getLogger(HttpHarvestController.class);

  private RecordDao recordDao;

  @Autowired
  void setRecordDao(RecordDao recordDao) {
    this.recordDao = recordDao;
  }

  /**
   * It creates a zip of records that belong to dataset with datasetId
   *
   * @param datasetId The dataset ID of which to create a zipfile.
   * @return A zip of records
   */
  @GetMapping(value = RestEndpoints.REPOSITORY_HTTP_ENDPOINT_ZIP, produces = "application/zip")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiOperation(value = "The dataset is exported as a zip file for harvesting by Metis. Records "
      + "that are marked as deleted will be excluded from the resulting zip file.")
  @ApiResponses(value = {@ApiResponse(code = 404, message = "No records for this dataset."),
          @ApiResponse(code = 500, message = "Error obtaining the records.")})
  public ResponseEntity<byte[]> getDatasetRecords(
          @ApiParam(value = "Dataset ID (new or existing)", required = true) @PathVariable("dataset") String datasetId) {

    // Create zip file in memory (and keep track on whether there are any records).
    final AtomicBoolean recordsFound = new AtomicBoolean(false);
    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try (final ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
      final Stream<Record> recordList = recordDao.getAllRecordsFromDataset(datasetId);
      recordList.forEach(record -> {
        if (!record.isDeleted()) {
          addRecordToZipFile(record, zipOutputStream);
          recordsFound.set(true);
        }
      });
      zipOutputStream.finish();
      zipOutputStream.flush();
    } catch (RuntimeException | IOException e) {

      // Report any problems (also for individual records) as 500 code.
      LOGGER.warn("There was problems while zipping the records.", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
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

  private static void addRecordToZipFile(Record record, ZipOutputStream zipOutputStream) {
    try {
      zipOutputStream.putNextEntry(new ZipEntry(record.getRecordId() + ".xml"));
      zipOutputStream.write(record.getEdmRecord().getBytes(StandardCharsets.UTF_8));
      zipOutputStream.closeEntry();
    } catch (IOException e) {
      throw new IllegalStateException(
              "There was a problem while preparing the records to be zipped.", e);
    }
  }
}
