package eu.europeana.metis.dereference.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.dereference.rest.exceptions.DereferenceException;
import eu.europeana.metis.dereference.service.DereferenceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Dereferencing REST endpoint
 * Created by gmamakis on 12-2-16.
 */
@Controller
@Api("/")
public class DereferencingController {
    private final DereferenceService dereferenceService;

    @Autowired
    public DereferencingController(DereferenceService dereferenceService) {
        this.dereferenceService = dereferenceService;
    }

    /**
     * Dereference a record given a URI
     *
     * @param uri The uri of the entity
     * @return The dereferenced entities
     */
    @RequestMapping(method = RequestMethod.GET, value = RestEndpoints.DEREFERENCE,
    		produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE} )
    @ResponseBody
    @ApiOperation(value = "Dereference a URI", response = EnrichmentResultList.class)
    public EnrichmentResultList dereference(@ApiParam("uri") @RequestParam("uri") String uri)
    		throws DereferenceException, JAXBException {
    	try {
    		EnrichmentResultList x = dereferenceService
    				.dereference(URLDecoder.decode(uri, "UTF-8"));
    		
    		return x;
    		//return dereferenceService.dereference(URLDecoder.decode(uri, "UTF-8"));
    	} catch (TransformerException | ParserConfigurationException | IOException e) {
    		throw new DereferenceException(e.getMessage(), uri);
    	}
    }

    /**
     * Dereference a record given a URI
     *
     * @param uris The uris to dereference
     * @return The dereferenced entities
     */
    @RequestMapping(method = RequestMethod.POST, value = RestEndpoints.DEREFERENCE,
    		consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
    		produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE} )
    @ResponseBody
    @ApiOperation(value = "Dereference a list URI", response = EnrichmentResultList.class)
    public EnrichmentResultList dereference(@RequestBody List<String> uris)
    		throws DereferenceException, JAXBException {
    	EnrichmentResultList dereferencedEntities = new EnrichmentResultList();

    	for (String uri : uris) {
    		try {
    			String decodeUri = URLDecoder.decode(uri, "UTF-8");
    			EnrichmentResultList res = dereference(decodeUri);
    			if (res == null) {continue;}
    			dereferencedEntities.getResult().addAll(res.getResult());
    		} catch (UnsupportedEncodingException e) {
    			throw new DereferenceException(e.getMessage(),uri);
    		}
    	}
    	
    	return dereferencedEntities;
    }
}
