package eu.europeana.metis.repository.rest;

import eu.europeana.metis.repository.dao.Record;
import eu.europeana.metis.repository.dao.RecordDao;
import eu.europeana.metis.utils.RestEndpoints;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
   * @return A zip of records
   */
  @GetMapping(value = RestEndpoints.GET_RECORDS_DATABASE, produces = "application/zip")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public byte[] getDatasetRecords(@PathVariable("dataset") String dataset,
      HttpServletResponse response) {

    Stream<Record> recordList = recordDao.getAllRecordsFromDataset(dataset);

    //setting headers
    response.addHeader("Content-Disposition", "attachment; filename=\"result.zip\"");

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
    ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream);

    recordList.forEach(record -> {
      try {
        zipOutputStream.putNextEntry(new ZipEntry(record.getRecordId() + ".xml"));
        InputStream recordToBeWritten = new ByteArrayInputStream(record.getEdmRecord().getBytes());
        IOUtils.copy(recordToBeWritten, zipOutputStream);
        recordToBeWritten.close();
        zipOutputStream.closeEntry();
      } catch (IOException e) {
        LOGGER.error("There was a problem while preparing the records to be zipped.");
      }
    });

    try {
      zipOutputStream.finish();
      zipOutputStream.flush();
      zipOutputStream.close();
      bufferedOutputStream.close();
      byteArrayOutputStream.close();
    } catch (IOException e) {
      LOGGER.warn("There was problems while zipping the records.");
    }

    return byteArrayOutputStream.toByteArray();
  }

}
