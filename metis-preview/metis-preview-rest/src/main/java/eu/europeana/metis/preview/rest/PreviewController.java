package eu.europeana.metis.preview.rest;

import eu.europeana.metis.preview.exceptions.PreviewValidationException;
import eu.europeana.metis.preview.model.ExtendedValidationResult;
import eu.europeana.metis.preview.service.PreviewService;
import eu.europeana.validation.model.ValidationResultList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.jibx.runtime.JiBXException;
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
 * Preview service REST controller
 * Created by ymamakis on 9/2/16.
 */
@Controller(value = "/preview")
@Api(value = "/preview", description = "Preview service REST API")
public class PreviewController {
    private final Logger LOGGER = LoggerFactory.getLogger(PreviewController.class);

    private PreviewService service;
    @Autowired
    public PreviewController(PreviewService service) {
        this.service = service;
    }

    /**
     * Persist records from a zip file
     * @param file The zip file
     * @param collectionId The collection id - can be null
     * @param applyCrosswalk Whether the records are in EDM-External (true) or EDM-Internal (false)
     * @param crosswalkPath path of xslt on the server (optional)
     * @param requestIndividualRecordsIds request individual record ids
     * @return
     * @throws IOException
     * @throws InstantiationException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws JiBXException
     * @throws IllegalAccessException
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws SolrServerException
     * @throws ZipException
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "Validation Result with preview URL", response = ExtendedValidationResult.class)
    @ApiResponses( {
        @ApiResponse(code = 200, message = "ok"),
        @ApiResponse(code = 422, message = "" ,response = ValidationResultList.class)
    }
    )
    public ExtendedValidationResult createRecords(@ApiParam @RequestParam("file") MultipartFile file,
                                                  @ApiParam @RequestParam(value = "collectionId", defaultValue = "") String collectionId,
                                                  @ApiParam(name = "edmExternal") @RequestParam(value = "edmExternal",defaultValue = "true")boolean applyCrosswalk,
                                                  @ApiParam(name="crosswalk") @RequestParam(value="crosswalk",defaultValue = "EDM_external2internal_v2.xsl") String crosswalkPath,
                                                  @ApiParam(name="individualRecords")@RequestParam(value = "individualRecords",defaultValue = "true")boolean requestIndividualRecordsIds)
            throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, JiBXException,PreviewValidationException,
            IllegalAccessException, ParserConfigurationException, TransformerException, SolrServerException, ZipException, ExecutionException, InterruptedException {
        List<String> records = readFileToStringList(file);
        Long start = System.currentTimeMillis();
        ExtendedValidationResult result = service.createRecords(records,collectionId,applyCrosswalk, crosswalkPath, requestIndividualRecordsIds);
        LOGGER.info("Duration: {} ms", System.currentTimeMillis()-start);
        if(!result.isSuccess()){
            throw new PreviewValidationException(result);
        }
        return result;
    }

    private List<String> readFileToStringList(MultipartFile multipartFile) throws IOException, ZipException {
        String prefix = String.valueOf(new Date().getTime());
        File tempFile = File.createTempFile(prefix, ".zip");
        FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), tempFile);
        LOGGER.info("Temp file: " + tempFile + " created.");

        ZipFile zipFile = new ZipFile(tempFile);
        File unzippedDirectory = new File(tempFile.getParent(), prefix + "-unzipped");
        zipFile.extractAll(unzippedDirectory.getAbsolutePath());
        LOGGER.info("Unzipped contents into: " + unzippedDirectory);
        FileUtils.deleteQuietly(tempFile);
        File[] files = unzippedDirectory.listFiles();
        List<String> xmls = new ArrayList<>();
        for (File input : files) {
            xmls.add(IOUtils.toString(new FileInputStream(input)));
        }
        FileUtils.deleteQuietly(unzippedDirectory);
        return xmls;
    }

}
