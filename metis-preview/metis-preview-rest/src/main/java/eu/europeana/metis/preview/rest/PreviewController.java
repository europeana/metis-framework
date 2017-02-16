package eu.europeana.metis.preview.rest;

import eu.europeana.metis.preview.exceptions.PreviewValidationException;
import eu.europeana.metis.preview.service.ExtendedValidationResult;
import eu.europeana.metis.preview.service.PreviewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.jibx.runtime.JiBXException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Preview service REST controller
 * Created by ymamakis on 9/2/16.
 */
@Controller(value = "/preview")
@Api(value = "/preview", description = "Preview service REST API")
public class PreviewController {

    @Autowired
    private PreviewService service;

    /**
     * Persist records from a zip file
     * @param file The zip file
     * @param collectionId The collection id - can be null
     * @param applyCrosswalk Whether the records are in EDM-External (true) or EDM-Internal (false)
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
    public ExtendedValidationResult createRecords(@ApiParam @RequestParam("file") MultipartFile file,
                                                  @ApiParam @RequestParam(value = "collectionId", defaultValue = "") String collectionId,
                                                  @ApiParam(name = "edmExternal") @RequestParam(value = "edmExternal",defaultValue = "true")boolean applyCrosswalk,
                                                  @ApiParam(name="crosswalk") @RequestParam(value="crosswalk",defaultValue = "EDM_external2internal_v2.xsl") String crosswalkPath,
                                                  @ApiParam(name="individualRecords")@RequestParam(value = "individualRecords",defaultValue = "true")boolean individualRecords)
            throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, JiBXException,PreviewValidationException,
            IllegalAccessException, ParserConfigurationException, TransformerException, SolrServerException, ZipException, ExecutionException, InterruptedException {
        List<String> records = readFileToStringList(file);
        ExtendedValidationResult result = service.createRecords(records,collectionId,applyCrosswalk, crosswalkPath,individualRecords);
        if(!result.isSuccess()){
            throw new PreviewValidationException(result);
        }
        return result;
    }

    private List<String> readFileToStringList(MultipartFile zipFile) throws IOException, ZipException {
        String fileName = "/tmp/" + zipFile.getName() + "/" + new Date().getTime();
        FileUtils.copyInputStreamToFile(zipFile.getInputStream(), new File(fileName + ".zip"));

        ZipFile file = new ZipFile(fileName + ".zip");
        file.extractAll(fileName);
        FileUtils.deleteQuietly(new File(fileName + ".zip"));
        File[] files = new File(fileName).listFiles();
        List<String> xmls = new ArrayList<>();
        for (File input : files) {
            xmls.add(IOUtils.toString(new FileInputStream(input)));
        }
        return xmls;
    }

}
