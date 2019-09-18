package eu.europeana.enrichment.cache.proxy;

import eu.europeana.enrichment.service.RedisInternalEnricher;
import eu.europeana.metis.RestEndpoints;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Enrichment Cache REST endpoint.
 */
@Api(value = "/")
@Controller
public class EnrichmentCacheController {

  private final RedisInternalEnricher enricher;

  /**
   * Constructor with all required parameters.
   *
   * @param enricher the redis enricher to be used
   */
  @Autowired
  public EnrichmentCacheController(RedisInternalEnricher enricher) {
    this.enricher = enricher;
  }

  /**
   * Recreate the redis cache from the mongo datastore. This will take some time
   */
  @PostMapping(value = "/recreate")
  @ResponseStatus(value = HttpStatus.OK)
  @ApiOperation(value = "Start recreating the cache",
      notes = "Recreate the redis cache from the mongo datastore. This will take some time")
  public void populate() {
    enricher.recreate();
  }

  /**
   * Check the status of the recreation of the cache
   *
   * @return 'started' or 'finished'
   */
  @GetMapping(value = "/check")
  @ResponseBody
  @ApiOperation(value = "Check the status of the process filling the cache", notes = "return 'started' or 'finished'")

  public String check() {
    return enricher.check();
  }

  /**
   * Empty Cache. This will remove ALL entries in the cache (Redis). If the same redis
   * instance/cluster is used for multiple services then the cache for other services is cleared as
   * well.
   *
   * @return OK
   */
  @DeleteMapping(value = RestEndpoints.CACHE_EMPTY)
  @ResponseBody
  @ApiOperation(value = "Empty the cache", notes =
      "This will remove ALL entries in the cache (Redis). If the same redis instance/cluster "
          + "is used for multiple services then the cache for other services is cleared as well.")
  public void emptyCache() {
    enricher.emptyCache();
  }

}
