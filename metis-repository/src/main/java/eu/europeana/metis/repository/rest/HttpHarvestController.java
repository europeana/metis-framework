package eu.europeana.metis.repository.rest;

import eu.europeana.metis.repository.dao.Record;
import eu.europeana.metis.repository.dao.RecordDao;
import eu.europeana.metis.utils.RestEndpoints;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

  private RecordDao recordDao;

  @Autowired
  void setRecordDao(RecordDao recordDao){
    this.recordDao = recordDao;
  }

  /**
   *
   * @return
   */
  @GetMapping(value = RestEndpoints.GET_RECORDS_DATABASE, consumes = {
      MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public MultipartFile getDatasetRecords(@PathVariable("dataset") String dataset){

    List<Record> recordList = recordDao.getAllRecordsFromDataset(dataset);

    return null;
  }

}
