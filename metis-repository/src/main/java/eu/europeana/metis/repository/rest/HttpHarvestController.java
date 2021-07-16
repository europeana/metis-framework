package eu.europeana.metis.repository.rest;

import eu.europeana.metis.repository.dao.Record;
import eu.europeana.metis.repository.dao.RecordDao;
import eu.europeana.metis.utils.RestEndpoints;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for HTTP harvesting.
 */
@RestController
@RequestMapping("http")
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
   * @return A zip of records
   */
  @GetMapping(value = RestEndpoints.GET_RECORDS_DATABASE, consumes = {
      MediaType.APPLICATION_XML_VALUE}, produces = "application/zip")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public byte[] getDatasetRecords(@PathVariable("dataset") String dataset,
      HttpServletResponse response) throws IOException {

    List<Record> recordList = recordDao.getAllRecordsFromDataset(dataset);

    //setting headers
    response.addHeader("Content-Disposition", "attachment; filename=\"result.zip\"");

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
    ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream);

    for (Record record : recordList) {
      zipOutputStream.putNextEntry(new ZipEntry(record.getRecordId() + ".xml"));
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(zipOutputStream);
      objectOutputStream.writeObject(record);
      objectOutputStream.flush();
      objectOutputStream.close();
      zipOutputStream.closeEntry();
    }

    zipOutputStream.finish();
    zipOutputStream.flush();
    IOUtils.closeQuietly(zipOutputStream);
    IOUtils.closeQuietly(bufferedOutputStream);
    IOUtils.closeQuietly(byteArrayOutputStream);
    return byteArrayOutputStream.toByteArray();
  }

}
