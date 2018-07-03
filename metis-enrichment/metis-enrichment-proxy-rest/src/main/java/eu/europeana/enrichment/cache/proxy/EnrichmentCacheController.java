package eu.europeana.enrichment.cache.proxy;

import eu.europeana.metis.RestEndpoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import eu.europeana.enrichment.service.RedisInternalEnricher;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Enrichment Cache REST endpoint
 * Created by gmamakis on 12-2-16.
 */
@Api(value = "/")
@Controller
public class EnrichmentCacheController {

  private final RedisInternalEnricher enricher;

  @Autowired
  public EnrichmentCacheController(RedisInternalEnricher enricher) {
    this.enricher = enricher;
  }

  /**
   * Recreate the redis cache from the mongo datastore. This will take some time
   */
  @RequestMapping(value = "/recreate", method = RequestMethod.POST)
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
  @RequestMapping(value = "/check", method = RequestMethod.GET)
  @ResponseBody
  @ApiOperation(value = "Check the status of the process filling the cache", notes = "return 'started' or 'finished'")

  public String check() {
    return enricher.check();
  }

  /**
   * Empty Cache. This will remove ALL entries in the cache (Redis). If the same redis instance/cluster
   * is used for multiple services then the cache for other services is cleared as well.
   *
   * @return OK
   */
  @RequestMapping(value = RestEndpoints.CACHE_EMPTY, method = RequestMethod.DELETE)
  @ResponseBody
  @ApiOperation(value = "Empty the cache", notes =
      "This will remove ALL entries in the cache (Redis). If the same redis instance/cluster "
          + "is used for multiple services then the cache for other services is cleared as well.")
  public void emptyCache() {
    enricher.emptyCache();
  }

}
