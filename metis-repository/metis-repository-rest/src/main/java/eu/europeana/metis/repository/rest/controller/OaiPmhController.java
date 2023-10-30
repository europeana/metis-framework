package eu.europeana.metis.repository.rest.controller;


import eu.europeana.metis.repository.rest.dao.Record;
import eu.europeana.metis.repository.rest.dao.RecordDao;
import eu.europeana.metis.utils.RestEndpoints;
import io.gdcc.xoai.model.oaipmh.Granularity;
import io.gdcc.xoai.model.oaipmh.OAIPMH;
import io.gdcc.xoai.model.oaipmh.Request;
import io.gdcc.xoai.model.oaipmh.results.record.Header;
import io.gdcc.xoai.model.oaipmh.results.record.Metadata;
import io.gdcc.xoai.model.oaipmh.verbs.GetRecord;
import io.gdcc.xoai.model.oaipmh.verbs.ListIdentifiers;
import io.gdcc.xoai.model.oaipmh.verbs.Verb;
import io.gdcc.xoai.xml.WriterContext;
import io.gdcc.xoai.xml.XmlWriter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.ws.rs.QueryParam;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.util.Date;

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

    private static Header createHeader(Record oaiRecord) {
        final Header result = new Header().withDatestamp(Date.from(oaiRecord.getDateStamp()).toInstant())
                .withSetSpec(oaiRecord.getDatasetId()).withIdentifier(oaiRecord.getRecordId());
        if (oaiRecord.isDeleted()) {
            result.withStatus(Header.Status.DELETED);
        }
        return result;
    }

    @Autowired
    public void setRecordDao(RecordDao recordDao) {
        this.recordDao = recordDao;
    }

    @GetMapping(value = RestEndpoints.REPOSITORY_OAI_ENDPOINT,
            produces = {MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @ApiOperation(value = "OAI endpoint (supporting only the ListIdentifiers and GetRecord verbs)")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Illegal OAI request"),
            @ApiResponse(code = 404, message = "Unknown dataset or record ID"),
            @ApiResponse(code = 500, message = "Error processing the request")})
    public String oaiPmh(
            @ApiParam(value = "The verb (ListIdentifiers or GetRecords)", required = true) @QueryParam("verb") String verb,
            @ApiParam(value = "The set (required for ListIdentifiers)") @QueryParam("set") String set,
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
            verbResult = listIdentifiers(set);
        } else if ("GetRecord".equals(verb)) {
            verbResult = getRecord(identifier);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported verb: " + verb);
        }

        // Compile the result
        final OAIPMH result = new OAIPMH().withVerb(verbResult).withResponseDate(new Date().toInstant())
                .withRequest(
                        new Request(RestEndpoints.REPOSITORY_OAI_ENDPOINT)
                                .withVerb(verbResult.getType())
                );
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final WriterContext context = new XmlOAIWriterContext();

            try (XmlWriter writer = new XmlWriter(outputStream, context)) {
                result.write(writer);
            }

            return outputStream.toString();
        } catch (XMLStreamException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "A problem occurred while serializing the response.",
                    e);
        }
    }

    private ListIdentifiers listIdentifiers(String setSpec) {
        if (StringUtils.isBlank(setSpec)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please provide a set value");
        }
        final ListIdentifiers result = new ListIdentifiers();
        recordDao.getAllRecordsFromDataset(setSpec)
                .forEach(datasetRecord -> result.getHeaders().add(createHeader(datasetRecord)));
        if (result.getHeaders().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No records found for this dataset.");
        }
        return result;
    }

    private GetRecord getRecord(String identifier) {
        if (StringUtils.isBlank(identifier)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please provide an identifier");
        }
        final Record oaiRecord = recordDao.getRecord(identifier);
        if (oaiRecord == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No record found for this identifier.");
        }
        final io.gdcc.xoai.model.oaipmh.results.Record resultRecord = new io.gdcc.xoai.model.oaipmh.results.Record()
                .withHeader(createHeader(oaiRecord)).withMetadata(new Metadata(oaiRecord.getEdmRecord()));
        return new GetRecord(resultRecord);
    }

    private static class XmlOAIWriterContext implements WriterContext {
        @Override
        public Granularity getGranularity() {
            return Granularity.Day;
        }
    }
}
