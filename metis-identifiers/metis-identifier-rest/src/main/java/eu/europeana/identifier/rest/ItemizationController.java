/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.identifier.rest;

import eu.europeana.identifier.rest.exceptions.IdentifierException;
import eu.europeana.identifier.service.ItemizationService;
import eu.europeana.identifier.service.exceptions.DeduplicationException;
import eu.europeana.itemization.Request;
import eu.europeana.itemization.RequestResult;
import eu.europeana.metis.RestEndpoints;
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
@Controller
@Api(value = "/", description = "Itemize records REST API")
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
    @RequestMapping(value = RestEndpoints.ITEMIZE_URL, method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "Itemize a remote tar.gz file", response = RequestResult.class)
    public RequestResult itemizeByUrl(@ApiParam("url") @RequestParam("url") String url) throws IdentifierException {
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
    @RequestMapping(value=RestEndpoints.ITEMIZE_RECORDS, method = RequestMethod.POST)
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
    @RequestMapping(method = RequestMethod.POST,value = RestEndpoints.ITEMIZE_FILE)
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
