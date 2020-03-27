package eu.europeana.enrichment.cache.proxy;

import eu.europeana.enrichment.service.RedisInternalEnricher;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
}
