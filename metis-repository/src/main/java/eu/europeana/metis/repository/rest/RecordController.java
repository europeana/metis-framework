package eu.europeana.metis.repository.rest;

import eu.europeana.metis.repository.dao.Record;
import eu.europeana.metis.repository.dao.RecordDao;
import eu.europeana.metis.utils.RestEndpoints;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for record management.
 */
@RestController
@Tags(@Tag(name = RecordController.CONTROLLER_TAG_NAME,
        description = "Controller providing access to record management functionality."))
@Api(tags = RecordController.CONTROLLER_TAG_NAME)
public class RecordController {

  public static final String CONTROLLER_TAG_NAME = "RecordController";

  private RecordDao recordDao;

  @Autowired
  void setRecordDao(RecordDao recordDao){
    this.recordDao = recordDao;
  }

  /**
   * It creates a record to be put into the database
   *
   * @param recordId - A unique record id
   * @param datasetId - The id of the dataset which the record belongs to
   * @param dateStamp - Last time the record was updated. It can also be the date of creation
   * @param edmRecord - The record itself
   * @return The record just saved in the database
   */
  @PostMapping(value = RestEndpoints.ADD_RECORD_TO_DATABASE, consumes = {
      MediaType.APPLICATION_XML_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE})
  @ResponseBody
  @ApiOperation(value = "The given record is put into the database", response = Record.class)
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Error processing the record")})
  public Record createRecord(@RequestParam("recordId") String recordId,
      @RequestParam("datasetId") String datasetId, @RequestParam("dateStamp") Date dateStamp,
      @RequestBody String edmRecord) {

    Record recordToSave = new Record(recordId, datasetId, dateStamp, edmRecord);
    return recordDao.createRecord(recordToSave);
  }

}
