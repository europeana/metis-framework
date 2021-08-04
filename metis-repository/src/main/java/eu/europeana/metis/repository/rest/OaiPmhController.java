package eu.europeana.metis.repository.rest;

import com.lyncode.xml.exceptions.XmlWriteException;
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
import java.util.Date;
import javax.ws.rs.QueryParam;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.lang3.StringUtils;
import org.dspace.xoai.model.oaipmh.GetRecord;
import org.dspace.xoai.model.oaipmh.Granularity;
import org.dspace.xoai.model.oaipmh.Header;
import org.dspace.xoai.model.oaipmh.ListIdentifiers;
import org.dspace.xoai.model.oaipmh.Metadata;
import org.dspace.xoai.model.oaipmh.OAIPMH;
import org.dspace.xoai.model.oaipmh.Request;
import org.dspace.xoai.model.oaipmh.Verb;
import org.dspace.xoai.services.impl.SimpleResumptionTokenFormat;
import org.dspace.xoai.xml.XmlWriter;
import org.dspace.xoai.xml.XmlWriter.WriterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller for OAI-PMH harvesting.
 */
@RestController
@Tags(@Tag(name = OaiPmhController.CONTROLLER_TAG_NAME,
    description = "Controller providing access to OAI-PMH harvesting functionality."))
@Api(tags = OaiPmhController.CONTROLLER_TAG_NAME)
public class OaiPmhController {

  public static final String CONTROLLER_TAG_NAME = "OaiPmhController";
  private static final Logger LOGGER = LoggerFactory.getLogger(OaiPmhController.class);

  private RecordDao recordDao;

  @Autowired
  void setRecordDao(RecordDao recordDao) {
    this.recordDao = recordDao;
  }

  @GetMapping(value = RestEndpoints.OAI_ENDPOINT, produces = {MediaType.APPLICATION_XML_VALUE})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiOperation(value = "OAI endpoint (supporting only the ListIdentifiers and GetRecord verbs)")
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Illegal OAI request"),
          @ApiResponse(code = 404, message = "Unknown dataset or record ID"),
          @ApiResponse(code = 500, message = "Error processing the request")})
  public String oaiPmh(
          @ApiParam(value = "The verb (ListIdentifiers or GetRecords)", required = true) @QueryParam("verb") String verb,
          @ApiParam(value = "The set (required for ListIdentifiers)") @QueryParam("set") String setSpec,
          @ApiParam(value = "The metadataPrefix (only 'edm' is supported.)", required = true) @QueryParam("metadataPrefix") String metadataPrefix,
          @ApiParam(value = "The record identifier (required for GetRecord)") @QueryParam("identifier") String identifier) {

    // Check the metadata prefix
    if (!"edm".equals(metadataPrefix)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
              "Unsupported metadataPrefix value: " + metadataPrefix);
    }

    // Check the verb and delegate
    final Verb verbResult;
    if ("ListIdentifiers".equals(verb)) {
      verbResult = listIdentifiers(setSpec);
    } else if ("GetRecord".equals(verb)) {
      verbResult = getRecord(identifier);
    } else {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported verb: " + verb);
    }

    // Compile the result
    final OAIPMH result = new OAIPMH().withVerb(verbResult).withResponseDate(new Date())
            .withRequest(new Request(RestEndpoints.OAI_ENDPOINT).withVerbType(verbResult.getType()));
    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      final WriterContext context = new WriterContext(Granularity.Day, new SimpleResumptionTokenFormat());
      XmlWriter writer = new XmlWriter(outputStream, context);
      try {
        result.write(writer);
      } finally {
        writer.close();
      }
      return outputStream.toString();
    } catch (XMLStreamException | XmlWriteException e) {
      LOGGER.warn("A problem occurred while serializing the response.", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
              "A problem occurred while serializing the response.", e);
    }
  }

  private ListIdentifiers listIdentifiers(String setSpec) {
    if (StringUtils.isBlank(setSpec)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please provide a set value");
    }
    final ListIdentifiers result = new ListIdentifiers();
    recordDao.getAllRecordsFromDataset(setSpec)
            .forEach(record -> result.getHeaders().add(createHeader(record)));
    if (result.getHeaders().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No records found for this dataset.");
    }
    return result;
  }

  private GetRecord getRecord(String identifier) {
    if (StringUtils.isBlank(identifier)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please provide an identifier");
    }
    final Record record = recordDao.getRecord(identifier);
    if (record == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
              "No record found for this identifier.");
    }
    final org.dspace.xoai.model.oaipmh.Record resultRecord = new org.dspace.xoai.model.oaipmh.Record()
            .withHeader(createHeader(record)).withMetadata(new Metadata(record.getEdmRecord()));
    return new GetRecord(resultRecord);
  }

  private static Header createHeader(Record record) {
    return new Header().withDatestamp(record.getDateStamp())
            .withSetSpec(record.getDatasetId()).withIdentifier(record.getRecordId());
  }
}
