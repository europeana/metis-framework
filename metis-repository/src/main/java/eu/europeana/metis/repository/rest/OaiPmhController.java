package eu.europeana.metis.repository.rest;

import eu.europeana.metis.repository.dao.Record;
import eu.europeana.metis.repository.dao.RecordDao;
import eu.europeana.metis.utils.RestEndpoints;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for OAI-PMH harvesting.
 */
@RestController
@Tags(@Tag(name = OaiPmhController.CONTROLLER_TAG_NAME,
    description = "Controller providing access to OAI-PMH harvesting functionality."))
@Api(tags = OaiPmhController.CONTROLLER_TAG_NAME)
public class OaiPmhController {

  public static final String CONTROLLER_TAG_NAME = "OaiPmhController";

  private RecordDao recordDao;

  @Autowired
  void setRecordDao(RecordDao recordDao) {
    this.recordDao = recordDao;
  }

  /**
   * Retrieves a single record
   *
   * @param identifier - The unique identifier of the record
   * @param metadataPrefix - It indicates the format of the record to the returned
   * @return The record found in the database
   */
  @GetMapping(value = RestEndpoints.GET_RECORD_OAI, produces = {MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Record getRecord(@RequestParam String identifier, @RequestParam String metadataPrefix) {

    Record recordResult = recordDao.getRecord(identifier);

    return null;
  }

}
