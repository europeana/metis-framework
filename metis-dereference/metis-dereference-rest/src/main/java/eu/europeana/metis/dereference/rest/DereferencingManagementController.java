package eu.europeana.metis.dereference.rest;

import static eu.europeana.metis.RestEndpoints.CACHE_EMPTY;
import static eu.europeana.metis.RestEndpoints.ENTITY;
import static eu.europeana.metis.RestEndpoints.ENTITY_DELETE;
import static eu.europeana.metis.RestEndpoints.VOCABULARIES;
import static eu.europeana.metis.RestEndpoints.VOCABULARY_BYNAME;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.DereferencingManagementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * REST controller for managing vocabularies and entities
 * Created by gmamakis on 12-2-16.
 */
@Controller
@Api("/")
public class DereferencingManagementController {
    private final DereferencingManagementService service;

    @Autowired
    public DereferencingManagementController(DereferencingManagementService service) {
        this.service = service;
    }
    
    /**
     * Save a vocabulary
     * @param vocabulary The vocabulary to save
     * @return OK
     */
    @RequestMapping(value = RestEndpoints.VOCABULARY,consumes = "application/json", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Save a vocabulary")
    public void saveVocabulary(@ApiParam @RequestBody Vocabulary vocabulary) {
        service.saveVocabulary(vocabulary);
    }

    /**
     * Update a vocabulary
     * @param vocabulary The vocabulary to update
     * @return OK
     */
    @RequestMapping(value = RestEndpoints.VOCABULARY,consumes = "application/json", method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Update a vocabulary")
    public void updateVocabulary(@ApiParam @RequestBody Vocabulary vocabulary) {
        service.updateVocabulary(vocabulary);
    }

    /**
     * Delete a vocabulary
     * @param name The vocabulary to delete
     * @return OK
     */
    @RequestMapping(value = RestEndpoints.VOCABULARY_BYNAME, method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Delete a vocabulary")
    public void deleteVocabulary(@ApiParam("name") @PathVariable("name") String name) {
        service.deleteVocabulary(name);
    }

    /**
     * Retrieve a vocabulary by name
     * @param name The name of the vocabulary
     * @return The Vocabulary with this name
     */
    @RequestMapping(value = VOCABULARY_BYNAME,produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "Retrieve a vocabulary by name")
    public Vocabulary getVocabulary(@ApiParam("name") @PathVariable("name") String name) {
        return service.findByName(name);
    }

    /**
     * Retrieve a list of all the registered vocabularies
     * @return The List of all the registered vocabularies
     */
    @RequestMapping(value = VOCABULARIES,produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "Retrieve all the vocabularies", response = List.class)
    public List<Vocabulary> getAllVocabularies() {
        return service.getAllVocabularies();
    }

    /**
     * Delete an entity based on a URI
     * @param uri The uri of the entity to delete
     * @return OK
     */
	@RequestMapping(value = ENTITY_DELETE, method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Delete an entity")
	public void deleteEntity(@ApiParam("uri") @PathVariable("uri") String uri) {
      try {
        service.removeEntity(URLDecoder.decode(uri, StandardCharsets.UTF_8.name()));
      } catch (UnsupportedEncodingException e) {
        // This cannot really happen.
        throw new IllegalStateException(e.getMessage(), e);
      }
	}

    /**
     * Update an entity
     * @param uri The uri of the entity
     * @param xml The xml of the entity
     * @return OK
     */
    @RequestMapping(value=ENTITY, method = RequestMethod.PUT)
    @ResponseBody
    @ApiOperation(value = "Update an entity")
    public void updateEntity(@ApiParam("uri") @RequestParam(value = "uri") String uri,@ApiParam("xml") @RequestParam(value = "xml") String xml) {
        service.updateEntity(uri,xml);
    }

    /**
     * Empty Cache. This will remove ALL entries in the cache (Redis). If the same redis instance/cluster
     * is used for multiple services then the cache for other services is cleared as well.
     * @return OK
     */
    @RequestMapping(value=CACHE_EMPTY, method = RequestMethod.DELETE)
    @ResponseBody
    @ApiOperation(value = "Empty the cache")
    public void emptyCache() {
        service.emptyCache();
    }
}
