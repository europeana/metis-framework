package eu.europeana.enrichment.rest;

import eu.europeana.enrichment.api.external.InputValueList;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
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
   * Get an enrichment by URI, might match owl:sameAs.
   *
   * @param uri The URI to retrieve
   * @return the structured result of the enrichment
   */
  @GetMapping(value = RestEndpoints.ENRICH_CODEURI_OR_OWLSAMEAS, produces = {
      MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE})
  @ApiOperation(value = "Get an enrichment by URI, might match owl:sameAs", response = EnrichmentBase.class)
  @ResponseBody
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Error processing the result")})
  public EnrichmentBase getByCodeUriOrOwlSameAs(@ApiParam("uri") @RequestParam("uri") String uri) {
    final List<EnrichmentBaseWrapper> enrichmentBaseWrappers = enrichmentService
        .getByCodeUriOrOwlSameAs(uri).stream()
        .map(enrichmentBase -> new EnrichmentBaseWrapper(null, enrichmentBase))
        .collect(Collectors.toList());
    return enrichmentBaseWrappers.stream().findFirst().map(EnrichmentBaseWrapper::getEnrichmentBase)
        .orElse(null);
  }

  /**
   * Get an enrichment by providing a list of URIs, might match owl:sameAs.
   *
   * @param uriList The URI to retrieve
   * @return the structured result of the enrichment
   */
  @PostMapping(value = RestEndpoints.ENRICH_CODEURI_OR_OWLSAMEAS, consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiOperation(value = "Get an enrichment by providing a list of URIs, might match owl:sameAs", response = EnrichmentResultList.class)
  @ResponseBody
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Error processing the result")})
  public EnrichmentResultList getByCodeUriOrOwlSameAs(@RequestBody List<String> uriList) {
    final List<EnrichmentBaseWrapper> enrichmentBaseWrappers = uriList.stream()
        .map(enrichmentService::getByCodeUriOrOwlSameAs).flatMap(List::stream)
        .map(enrichmentBase -> new EnrichmentBaseWrapper(null, enrichmentBase))
        .collect(Collectors.toList());
    return new EnrichmentResultList(enrichmentBaseWrappers);
  }

  /**
   * Get an enrichment by providing a list of URIs.
   *
   * @param idList The ID to retrieve
   * @return the structured result of the enrichment
   */
  @PostMapping(value = RestEndpoints.ENRICH_CODEURI, consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiOperation(value = "Get an enrichment by providing a list of URIs", response = EnrichmentResultList.class)
  @ResponseBody
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Error processing the result")})
  public EnrichmentResultList getByCodeUri(@RequestBody List<String> idList) {
    final List<EnrichmentBaseWrapper> enrichmentBaseWrappers = idList.stream()
        .map(enrichmentService::getByCodeUri).filter(Objects::nonNull).flatMap(List::stream)
        .map(enrichmentBase -> new EnrichmentBaseWrapper(null, enrichmentBase))
        .collect(Collectors.toList());
    return new EnrichmentResultList(enrichmentBaseWrappers);
  }

  /**
   * Get an enrichment by providing a list of {@link InputValueList}.
   *
   * @param input A list of values
   * @return the enrichment values in a wrapped structured list
   */
  @PostMapping(value = RestEndpoints.ENRICH_INPUT_VALUE_LIST, consumes = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseBody
  @ApiOperation(value = "Get an enrichment by providing a list of InputValueList", response = EnrichmentResultList.class)
  @ApiResponses(value = {
      @ApiResponse(code = 400, message = "Error processing the result")
  })
  public EnrichmentResultList enrichByInputValueList(
      @ApiParam("InputValueList") @RequestBody InputValueList input) {
    final List<EnrichmentBaseWrapper> enrichmentBaseWrappers = enrichmentService
        .findEntitiesBasedOnValues(input.getInputValues()).stream().filter(Objects::nonNull)
        .map(originalFieldEnrichmentBasePair -> new EnrichmentBaseWrapper(
            originalFieldEnrichmentBasePair.getKey(), originalFieldEnrichmentBasePair.getValue()))
        .collect(Collectors.toList());
    return new EnrichmentResultList(enrichmentBaseWrappers);
  }
}
