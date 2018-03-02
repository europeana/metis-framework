package eu.europeana.redirects.rest;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.redirects.model.RedirectRequest;
import eu.europeana.redirects.model.RedirectRequestList;
import eu.europeana.redirects.model.RedirectResponse;
import eu.europeana.redirects.model.RedirectResponseList;
import eu.europeana.redirects.service.RedirectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * REST Endpoint for Europeana Redirects Service module
 * Created by ymamakis on 1/15/16.
 */
@Controller("/")
@Api("/")
public class RedirectController {
    @Autowired
    private  RedirectService redirectService;

    @RequestMapping(method = RequestMethod.POST,value = RestEndpoints.REDIRECT_SINGLE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ApiOperation(value="Generate a single redirect",response = RedirectResponse.class)
    public RedirectResponse redirectSingle(@ApiParam @RequestBody RedirectRequest request){
            return redirectService.createRedirect(request);

    }

    @RequestMapping(method = RequestMethod.POST,value = RestEndpoints.REDIRECT_BATCH)
    @ResponseBody
    @ApiOperation(value="Generate batch redirects",response = RedirectResponseList.class)
    public RedirectResponseList redirectBatch(@ApiParam @RequestBody RedirectRequestList requestList){
        return redirectService.createRedirects(requestList);
    }

}
