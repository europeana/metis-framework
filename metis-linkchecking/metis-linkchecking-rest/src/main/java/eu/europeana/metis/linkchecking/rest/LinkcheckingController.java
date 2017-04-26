package eu.europeana.metis.linkchecking.rest;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.linkchecking.LinkcheckRequest;
import eu.europeana.metis.linkchecking.LinkcheckStatus;
import eu.europeana.metis.linkchecking.service.LinkcheckingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by ymamakis on 11/4/16.
 */
@Controller("/")
@Api(value = "/")
public class LinkcheckingController {

    @Autowired
    private LinkcheckingService service;

    @RequestMapping(value = RestEndpoints.LINKCHECK,method = RequestMethod.POST, consumes = "application/json",produces = "application/json")
    @ResponseBody
    @ApiOperation(value = "Check a list of links whether they resolve or not")
    public List<LinkcheckStatus> linkcheck(@RequestBody List<LinkcheckRequest> requests){
        return service.generateLinkCheckingReport(requests);
    }

}
