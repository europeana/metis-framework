package eu.europeana.normalization.language.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.europeana.normalization.language.LanguageNormalizationService;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

/**
 * Webapp controller for the service
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 16/05/2016
 */
@Controller
@Api("/")
public class LanguageNormalizationController {
    @Autowired
    private LanguageNormalizationService service;

    /**
     * Dereference a record given a URI
     * 
     * @param uri
     *            The uri of the entity
     * @return The dereferenced entities
     */
    @RequestMapping(method = RequestMethod.GET, value = "/normalize")
    @ResponseBody
    @ApiOperation(value = "Normalize a language value (for example, from dc:language)", response = String.class)
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public List<String> normalize(@ApiParam("value") @RequestParam("value") String value) {
        return service.normalize(value);
    }
}