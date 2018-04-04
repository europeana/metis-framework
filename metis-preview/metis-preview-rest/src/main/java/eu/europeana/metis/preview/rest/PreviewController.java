package eu.europeana.metis.preview.rest;

import java.io.IOException;
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
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.exception.StructuredExceptionWrapper;
import eu.europeana.metis.preview.common.exception.PreviewServiceException;
import eu.europeana.metis.preview.common.exception.PreviewValidationException;
import eu.europeana.metis.preview.common.exception.ZipFileException;
import eu.europeana.metis.preview.common.model.ExtendedValidationResult;
import eu.europeana.metis.preview.service.PreviewService;
import eu.europeana.metis.preview.service.ZipService;
import eu.europeana.validation.model.ValidationResultList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Preview service REST controller
 * Created by ymamakis on 9/2/16.
 */
@Controller(value = "/")
@Api(value = "/", description = "Preview service REST API")
public class PreviewController {
  
    private static final Logger LOGGER = LoggerFactory.getLogger(PreviewController.class);

    private final PreviewService service;
    private final ZipService zipService;
    private final Supplier<String> collectionIdGenerator;

    @Autowired
    public PreviewController(PreviewService service,
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
     * @throws PreviewServiceException Error while processing the zipfile content
     * @throws PreviewValidationException Semantic errors resulting from processing the zipfile content
     */
    @RequestMapping(value = RestEndpoints.PREVIEW_UPLOAD, method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "Validation Result with preview URL", response = ExtendedValidationResult.class)
    @ApiResponses( {
        @ApiResponse(code = 200, message = "ok"),
        @ApiResponse(code = 400, message = "" ,response = StructuredExceptionWrapper.class),
        @ApiResponse(code = 422, message = "" ,response = ValidationResultList.class)
    })
    public ExtendedValidationResult createRecords(@ApiParam @RequestParam("file") MultipartFile file,
                                                  @ApiParam(name = "edmExternal") @RequestParam(value = "edmExternal",defaultValue = "true")boolean applyCrosswalk,
                                                  @ApiParam(name="crosswalk") @RequestParam(value="crosswalk",required=false) String crosswalkPath,
                                                  @ApiParam(name="individualRecords")@RequestParam(value = "individualRecords",defaultValue = "true")boolean requestIndividualRecordsIds)
        throws ZipFileException, PreviewServiceException, PreviewValidationException {
        final List<String> records = zipService.readFileToStringList(file);
        final Long start = System.currentTimeMillis();
        final String collectionId = collectionIdGenerator.get();
        final ExtendedValidationResult result = service.createRecords(records, collectionId, applyCrosswalk,
            crosswalkPath, requestIndividualRecordsIds);
        LOGGER.info("Duration: {} ms", System.currentTimeMillis() - start);
        if (!result.isSuccess()) {
          throw new PreviewValidationException(result);
        }
        return result;
    }
}
