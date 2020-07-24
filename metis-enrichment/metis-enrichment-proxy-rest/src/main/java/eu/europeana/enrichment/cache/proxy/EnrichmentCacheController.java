package eu.europeana.enrichment.cache.proxy;

import eu.europeana.enrichment.service.CacheStatus;
import eu.europeana.enrichment.service.RedisInternalEnricher;
import eu.europeana.enrichment.service.exception.CacheStatusException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Enrichment Cache REST endpoint.
 */
@Api(value = "/")
@Controller
public class EnrichmentCacheController {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentCacheController.class);

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
   * @return a human readable string of the status.
   */
  @GetMapping(value = "/check")
  @ResponseBody
  @ApiOperation(value = "Check the status of the process filling the cache",
          notes = "Returns a string representation of the status.")
  public String check() {
    final CacheStatus status = enricher.getCurrentStatus();
    return String.format("%s: %s", status.name(), status.getExplanation());
  }

  /**
   * Check the status of the recreation of the cache
   *
   * @return 'started' or 'finished'
   */
  @PostMapping(value = "/trigger")
  @ResponseBody
  @ApiOperation(value = "Trigger a new process of filling the cache, to be executed upon the next deployment/restart.",
          notes = "Returns error code if the process could not be triggered.")
  public ResponseEntity trigger() {
    try {
      enricher.triggerRecreate();
      return ResponseEntity.ok().build();
    } catch (CacheStatusException e) {
      LOGGER.debug("Could not trigger a cache recreate.", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }
}
