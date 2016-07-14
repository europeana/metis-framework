package eu.europeana.identifier.rest;

import eu.europeana.identifier.rest.exceptions.IdentifierException;
import eu.europeana.identifier.service.ItemizationService;
import eu.europeana.identifier.service.exceptions.DeduplicationException;
import eu.europeana.itemization.Request;
import eu.europeana.itemization.RequestResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.FileUtils;
import org.jibx.runtime.JiBXException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

/**
 * Itemization REST API
 * Created by ymamakis on 2/9/16.
 */
@Controller(value = "/rest/itemize")
@Api("/rest/itemize")
public class ItemizationController {

    @Autowired
    public ItemizationService service;

    /**
     * Itemize based on a URL of a file. The file will be downloaded unzipped and itemized
     *
     * @param url The url to download
     * @return A list of itemized EDM records
     * @throws IOException
     * @throws DeduplicationException
     * @throws JiBXException
     */
    @RequestMapping(value = "/url", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "Itemize a remote tar.gz file", response = RequestResult.class)
    public RequestResult itemizeByUrl(@ApiParam("url") @RequestBody String url) throws IdentifierException {
        RequestResult res = new RequestResult();
        try {
            res.setItemizedRecords(service.itemize(new URL(url)));
        } catch (IOException | DeduplicationException | JiBXException e) {
            throw new IdentifierException(e.getMessage());
        }
        return res;
    }

    /**
     * Itemize a list of Records
     *
     * @param request The list of Records to itemize
     * @return A list of itemized EDM records
     * @throws IOException
     * @throws DeduplicationException
     * @throws JiBXException
     */
    @RequestMapping(value="/records", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "Itemize a list of records", response = RequestResult.class)
    public RequestResult itemizeRecords(@ApiParam("records") @RequestBody Request request) throws IdentifierException {
        RequestResult res = new RequestResult();
        try {
            res.setItemizedRecords(service.itemize(request.getRecords()));
        } catch (DeduplicationException | JiBXException e) {
            throw new IdentifierException(e.getMessage());
        }
        return res;
    }

    /**
     * Itemize a tgz file
     *
     * @param zipFile         A tgz file
     * @return A list of itemized EDM records
     * @throws IOException
     * @throws DeduplicationException
     * @throws JiBXException
     */
    @RequestMapping(method = RequestMethod.POST,value = "/file")
    @ResponseBody
    @ApiOperation(value = "Itemize a file of records", response = RequestResult.class)
    public RequestResult itemizeFile(@ApiParam("file") @RequestParam("file") MultipartFile zipFile) throws IdentifierException {
        try {
            String fileName = "/tmp/" + zipFile.getName() + "/" + new Date().getTime();
            File f = new File(fileName + ".tgz");
            FileUtils.copyInputStreamToFile(zipFile.getInputStream(), f);
            RequestResult res = new RequestResult();
            res.setItemizedRecords(service.itemize(f));
            return res;
        } catch (IOException | DeduplicationException | JiBXException e) {
            throw new IdentifierException(e.getMessage());
        }
    }

}
