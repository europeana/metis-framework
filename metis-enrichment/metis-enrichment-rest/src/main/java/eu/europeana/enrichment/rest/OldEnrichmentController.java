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
@Api("/old/")
@Controller
@Deprecated
public class OldEnrichmentController {

  private final EnrichmentService enrichmentService;

  /**
   * Autowired constructor.
   *
   * @param enrichmentService the service that contains logic for processing entities
   */
  @Autowired
  public OldEnrichmentController(EnrichmentService enrichmentService) {
    this.enrichmentService = enrichmentService;
  }

  /**
   * Get an enrichment providing a URI that matches an entity's about or owlSameAs.
   *
   * @param uri The URI to check for match
   * @return the structured result of the enrichment
   * @deprecated This method will no longer be available for use
   */
  @GetMapping(value = RestEndpoints.ENRICH_ENTITY_ABOUT_OR_OWLSAMEAS, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiOperation(value = "Get an enrichment providing a URI that matches an entity about or "
      + "owlSameAs", response = EnrichmentBase.class)
  @ResponseBody
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Error processing the result")})
  @Deprecated
  public EnrichmentBase enrichByEntityAboutOrOwlSameAs(
      @ApiParam("uri") @RequestParam("uri") String uri) {
    return enrichmentService.enrichByAboutOrOwlSameAs(uri);
  }

  /**
   * Get an enrichment providing a list of URIs where each of them could match a codeUri or
   * owlSameAs.
   *
   * @param uris The URIs to check for match
   * @return the structured result of the enrichment
   * @deprecated This method will no longer be available for use
   */
  @PostMapping(value = RestEndpoints.ENRICH_ENTITY_ABOUT_OR_OWLSAMEAS, consumes = MediaType.APPLICATION_JSON_VALUE, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiOperation(value = "Get an enrichment providing a list of URIs where each of them could "
      + "match an entity's about or owlSameAs", response = EnrichmentResultList.class)
  @ResponseBody
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Error processing the result")})
  @Deprecated
  public EnrichmentResultList enrichByEntityAboutOrOwlSameAs(@RequestBody List<String> uris) {
    final List<EnrichmentBaseWrapper> enrichmentBaseWrappers = uris.stream()
        .map(enrichmentService::enrichByAboutOrOwlSameAs).filter(Objects::nonNull)
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
  @PostMapping(value = RestEndpoints.ENRICH_ENTITY_ABOUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiOperation(value = "Get an enrichment providing a list of URIs where each of them could "
      + "match an entity's about", response = EnrichmentResultList.class)
  @ResponseBody
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Error processing the result")})
  @Deprecated
  public EnrichmentResultList enrichByEntityAbout(@RequestBody List<String> uris) {
    final List<EnrichmentBaseWrapper> enrichmentBaseWrappers = uris.stream()
        .map(enrichmentService::enrichByAbout).filter(Objects::nonNull)
        .map(enrichmentBase -> new EnrichmentBaseWrapper(null, enrichmentBase))
        .collect(Collectors.toList());
    return new EnrichmentResultList(enrichmentBaseWrappers);
  }

  /**
   * Get an enrichment by providing a list of {@link InputValueList}.
   *
   * @param inputValueList a list of structured input values with parameters
   * @return the enrichment values in a wrapped structured list
   * @deprecated This method will no longer be available for use
   */
  @PostMapping(value = RestEndpoints.ENRICH_INPUT_VALUE_LIST, consumes = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseBody
  @ApiOperation(value = "Get an enrichment by providing a list of InputValueList", response = EnrichmentResultList.class)
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Error processing the result")})
  @Deprecated
  public EnrichmentResultList enrichByInputValueList(
      @ApiParam("InputValueList") @RequestBody InputValueList inputValueList) {
    final List<EnrichmentBaseWrapper> enrichmentBaseWrappers = enrichmentService
        .enrichByInputValueList(inputValueList.getInputValues()).stream().filter(Objects::nonNull)
        .map(originalFieldEnrichmentBasePair -> new EnrichmentBaseWrapper(
            originalFieldEnrichmentBasePair.getKey(), originalFieldEnrichmentBasePair.getValue()))
        .collect(Collectors.toList());
    return new EnrichmentResultList(enrichmentBaseWrappers);
  }
}
