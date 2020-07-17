package eu.europeana.enrichment.rest;

import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.InputValueList;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.rest.exception.EnrichmentException;
import eu.europeana.enrichment.service.Converter;
import eu.europeana.enrichment.service.EnrichmentService;
import eu.europeana.metis.RestEndpoints;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
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

  private final Converter converter;
  private final EnrichmentService enrichmentService;

  /**
   * Autowired constructor.
   *
   * @param converter utility class for conversion between different classes
   * @param enrichmentService the service that contains logic for processing entities
   */
  @Autowired
  public EnrichmentController(Converter converter, EnrichmentService enrichmentService) {
    this.converter = converter;
    this.enrichmentService = enrichmentService;
  }

  /**
   * Get an enrichment by URI (rdf:about or owl:sameAs/skos:exactMatch).
   *
   * @param uri The URI to retrieve
   * @return the structured result of the enrichment
   * @throws EnrichmentException if an exception occurred during enrichment
   */
  @GetMapping(value = RestEndpoints.ENRICHMENT_BYURI, produces = {MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE})
  @ApiOperation(value = "Retrieve an entity by rdf:about or owl:sameAs/skos:exactMatch", response = EnrichmentBase.class)
  @ResponseBody
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Error processing the result")})
  public EnrichmentBase getByCodeUriOrOwlSameAs(@ApiParam("uri") @RequestParam("uri") String uri)
      throws EnrichmentException {

    final EntityWrapper entiryWrapper = enrichmentService.getByCodeUriOrOwlSameAs(uri);
    if (entiryWrapper == null) {
      return null;
    }

    try {
      return converter.convert(entiryWrapper);
    } catch (IOException e) {
      throw new EnrichmentException("Error converting object to EnrichmentBase", e);
    }
  }

  /**
   * Get an enrichment by URI (rdf:about or owl:sameAs/skos:exactMatch).
   *
   * @param uriList The URI to retrieve
   * @return the structured result of the enrichment
   * @throws EnrichmentException if an exception occurred during enrichment
   */
  @PostMapping(value = RestEndpoints.ENRICHMENT_BYURI, consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiOperation(value = "Retrieve entities by rdf:about or owl:sameAs/skos:exactMatch", response = EnrichmentResultList.class)
  @ResponseBody
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Error processing the result")})
  public EnrichmentResultList getByCodeUriOrOwlSameAs(@RequestBody List<String> uriList)
      throws EnrichmentException {
    final List<EntityWrapper> wrapperList = uriList.stream()
        .map(enrichmentService::getByCodeUriOrOwlSameAs)
        .filter(Objects::nonNull).collect(Collectors.toList());
    try {
      return converter.convert(wrapperList);
    } catch (IOException e) {
      throw new EnrichmentException("Error converting object to EnrichmentBase", e);
    }
  }

  /**
   * Get an enrichment by ID (rdf:about).
   *
   * @param idList The ID to retrieve
   * @return the structured result of the enrichment
   * @throws EnrichmentException if an exception occurred during enrichment
   */
  @PostMapping(value = RestEndpoints.ENRICHMENT_BYID, consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiOperation(value = "Retrieve entities by rdf:about", response = EnrichmentResultList.class)
  @ResponseBody
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Error processing the result")})
  public EnrichmentResultList getByCodeUri(@RequestBody List<String> idList)
      throws EnrichmentException {
    final List<EntityWrapper> wrapperList = idList.stream().map(enrichmentService::getByCodeUri)
        .filter(Objects::nonNull).collect(Collectors.toList());
    try {
      return converter.convert(wrapperList);
    } catch (IOException e) {
      throw new EnrichmentException("Error converting object to EnrichmentBase", e);
    }
  }

  /**
   * Enrich a number of values
   *
   * @param input A list of values
   * @return the enrichment values in a wrapped structured list
   * @throws EnrichmentException if an exception occurred during enrichment
   */
  @PostMapping(value = RestEndpoints.ENRICHMENT_ENRICH, consumes = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseBody
  @ApiOperation(value = "Enrich a series of field value pairs", response = EnrichmentResultList.class)
  @ApiResponses(value = {
      @ApiResponse(code = 400, message = "Error processing the result")
  })
  public EnrichmentResultList enrich(@ApiParam("input") @RequestBody InputValueList input)
      throws EnrichmentException {

    try {
      List<EntityWrapper> wrapperList = enrichmentService
          .findEntitiesBasedOnValues(input.getInputValues());
      return converter.convert(wrapperList);
    } catch (IOException e) {
      throw new EnrichmentException("Error converting object.", e);
    }
  }
}
