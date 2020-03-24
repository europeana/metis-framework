package eu.europeana.metis.dereference.rest;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.DereferencingManagementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * REST controller for managing vocabularies and entities Created by gmamakis on 12-2-16.
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
   * Retrieve a list of all the registered vocabularies
   *
   * @return The List of all the registered vocabularies
   */
  @GetMapping(value = RestEndpoints.VOCABULARIES, produces = "application/json")
  @ResponseBody
  @ApiOperation(value = "Retrieve all the vocabularies", response = List.class)
  public List<Vocabulary> getAllVocabularies() {
    return service.getAllVocabularies();
  }

  /**
   * Empty Cache. This will remove ALL entries in the cache (Redis). If the same redis
   * instance/cluster is used for multiple services then the cache for other services is cleared as
   * well.
   */
  @DeleteMapping(value = RestEndpoints.CACHE_EMPTY)
  @ResponseBody
  @ApiOperation(value = "Empty the cache")
  public void emptyCache() {
    service.emptyCache();
  }
}
