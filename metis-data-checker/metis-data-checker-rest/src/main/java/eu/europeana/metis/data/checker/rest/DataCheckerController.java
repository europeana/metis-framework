package eu.europeana.metis.data.checker.rest;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.data.checker.common.exception.DataCheckerServiceException;
import eu.europeana.metis.data.checker.common.exception.DataCheckerValidationException;
import eu.europeana.metis.data.checker.common.exception.ZipFileException;
import eu.europeana.metis.data.checker.common.model.ExtendedValidationResult;
import eu.europeana.metis.data.checker.service.DataCheckerService;
import eu.europeana.metis.data.checker.service.ZipService;
import eu.europeana.metis.exception.StructuredExceptionWrapper;
import eu.europeana.validation.model.ValidationResultList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * Data checker service REST controller
 * Created by ymamakis on 9/2/16.
 */
@Controller(value = "/")
@Api(value = "/", description = "Data checker service REST API")
public class DataCheckerController {
  
    private static final Logger LOGGER = LoggerFactory.getLogger(DataCheckerController.class);

    private final DataCheckerService service;
    private final ZipService zipService;
    private final Supplier<String> collectionIdGenerator;

    @Autowired
    public DataCheckerController(DataCheckerService service,
                ZipService zipService, Supplier<String> collectionIdGenerator) {
        this.service = service;
        this.zipService = zipService;
        this.collectionIdGenerator = collectionIdGenerator;
    }

    /**
     * Persist records from a zip file
     * @param file The zip file
     * @param applyCrosswalk Whether the records are in EDM-External (true) or EDM-Internal (false)
     * @param crosswalkPath path of xslt on the server (optional)
     * @param requestIndividualRecordsIds request individual record ids
     * @return The resulting record with validation information.
     * @throws ZipFileException Error processing the zipfile
     * @throws DataCheckerServiceException Error while processing the zipfile content
     * @throws DataCheckerValidationException Semantic errors resulting from processing the zipfile content
     */
    @RequestMapping(value = RestEndpoints.DATA_CHECKER_UPLOAD, method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "Validation Result with data checker URL", response = ExtendedValidationResult.class)
    @ApiResponses( {
        @ApiResponse(code = 200, message = "ok"),
        @ApiResponse(code = 400, message = "" ,response = StructuredExceptionWrapper.class),
        @ApiResponse(code = 422, message = "" ,response = ValidationResultList.class)
    })
    public ExtendedValidationResult createRecords(@ApiParam @RequestParam("file") MultipartFile file,
                                                  @ApiParam(name = "edmExternal") @RequestParam(value = "edmExternal",defaultValue = "true")boolean applyCrosswalk,
                                                  @ApiParam(name="crosswalk") @RequestParam(value="crosswalk",required=false) String crosswalkPath,
                                                  @ApiParam(name="individualRecords")@RequestParam(value = "individualRecords",defaultValue = "true")boolean requestIndividualRecordsIds)
        throws ZipFileException, DataCheckerServiceException, DataCheckerValidationException {
        final List<String> records = zipService.readFileToStringList(file);
        final Long start = System.currentTimeMillis();
        final String collectionId = collectionIdGenerator.get();
        final ExtendedValidationResult result = service.createRecords(records, collectionId, applyCrosswalk,
            crosswalkPath, requestIndividualRecordsIds);
        LOGGER.info("Duration: {} ms", System.currentTimeMillis() - start);
        if (!result.isSuccess()) {
          throw new DataCheckerValidationException(result);
        }
        return result;
    }
}
