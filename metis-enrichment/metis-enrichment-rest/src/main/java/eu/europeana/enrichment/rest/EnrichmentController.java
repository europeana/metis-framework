package eu.europeana.enrichment.rest;

import eu.europeana.enrichment.api.external.EnrichmentSearch;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.service.EnrichmentService;
import eu.europeana.metis.RestEndpoints;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Enrichment REST API
 */
@Api("/")
@Controller
public class EnrichmentController {

  private final EnrichmentService enrichmentService;

  /**
   * Autowired constructor.
   *
   * @param enrichmentService the service that contains logic for processing entities
   */
  @Autowired
  public EnrichmentController(EnrichmentService enrichmentService) {
    this.enrichmentService = enrichmentService;
  }

  /**
   * Get an enrichment by providing a list of {@link EnrichmentSearch}.
   *
   * @param enrichmentSearch a list of structured input values with parameters
   * @return the enrichment values in a wrapped structured list
   * @deprecated This method will no longer be available for use
   */
  @PostMapping(value = RestEndpoints.ENRICH_ENTITY_SEARCH, consumes = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseBody
  @ApiOperation(value = "Get an enrichment by providing a list of SearchValue", response = EnrichmentResultList.class)
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Error processing the result")})
  public EnrichmentResultList search(
      @ApiParam("SearchTerms") @RequestBody EnrichmentSearch enrichmentSearch) {

    final List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrappers = enrichmentService
        .enrichByEnrichmentSearchValues(enrichmentSearch.getSearchValues()).stream().filter(Objects::nonNull)
        .collect(Collectors.toList());
    return new EnrichmentResultList(enrichmentResultBaseWrappers);
  }

  /**
   * Get an enrichment providing a URI that matches an entity's about or owlSameAs.
   *
   * @param uri The URI to check for match
   * @return the structured result of the enrichment
   * @deprecated This method will no longer be available for use
   */
  @GetMapping(value = RestEndpoints.ENRICH_ENTITY_EQUIVALENCE, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiOperation(value = "Get an enrichment providing a URI that matches an entity about or "
      + "owlSameAs", response = EnrichmentBase.class)
  @ResponseBody
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Error processing the result")})
  public EnrichmentBase equivalence(
      @ApiParam("uri") @RequestParam("uri") String uri) {
    return enrichmentService.enrichByEquivalenceValues(uri);
  }

  /**
   * Get an enrichment providing a list of URIs where each of them could match a codeUri or
   * owlSameAs.
   *
   * @param uris The URIs to check for match
   * @return the structured result of the enrichment
   * @deprecated This method will no longer be available for use
   */
  @PostMapping(value = RestEndpoints.ENRICH_ENTITY_EQUIVALENCE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiOperation(value = "Get an enrichment providing a list of URIs where each of them could "
      + "match an entity's about or owlSameAs", response = EnrichmentResultList.class)
  @ResponseBody
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Error processing the result")})
  public EnrichmentResultList equivalence(@RequestBody List<String> uris) {
    final List<EnrichmentBaseWrapper> enrichmentBaseWrappers = uris.stream()
        .map(enrichmentService::enrichByEquivalenceValues).filter(Objects::nonNull)
        .map(enrichmentBase -> new EnrichmentBaseWrapper(null, enrichmentBase))
        .collect(Collectors.toList());
    return new EnrichmentResultList(enrichmentBaseWrappers);

  }

  /**
   * Get an enrichment providing a list of URIs where each of them could match a codeUri.
   *
   * @param uris The URIs to check for match
   * @return the structured result of the enrichment
   * @deprecated This method will no longer be available for use
   */
  @PostMapping(value = RestEndpoints.ENRICH_ENTITY_ID, consumes = MediaType.APPLICATION_JSON_VALUE, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiOperation(value = "Get an enrichment providing a list of URIs where each of them could "
      + "match an entity's about", response = EnrichmentResultList.class)
  @ResponseBody
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Error processing the result")})
  public EnrichmentResultBaseWrapper entityId(@RequestBody List<String> uris) {
    List<EnrichmentBase> enrichmentBaseWrappers = uris.stream()
        .map(enrichmentService::enrichById).filter(Objects::nonNull)
        .collect(Collectors.toList());

    return new EnrichmentResultBaseWrapper(enrichmentBaseWrappers);

  }
}
