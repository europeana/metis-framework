package eu.europeana.metis.dereference.rest.controller;

import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.metis.dereference.DereferenceResult;
import eu.europeana.metis.dereference.rest.exceptions.DereferenceException;
import eu.europeana.metis.dereference.service.DereferenceService;
import eu.europeana.metis.utils.CommonStringValues;
import eu.europeana.metis.utils.RestEndpoints;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
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
 * Dereferencing REST endpoint Created by gmamakis on 12-2-16.
 */
@Controller
public class DereferencingController {

  private final DereferenceService dereferenceService;

  /**
   * Constructor.
   *
   * @param dereferenceService An instance for processing dereference requests.
   */
  @Autowired
  public DereferencingController(DereferenceService dereferenceService) {
    this.dereferenceService = dereferenceService;
  }

  /**
   * Dereference a record given a URI
   *
   * @param resourceId The resource ID (URI) of the entity to dereference
   * @return The dereferenced entities and status
   */
  @GetMapping(value = RestEndpoints.DEREFERENCE, produces = {MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE})
  @ResponseBody
  @Operation(description = "Dereference a URI",
      responses = {
          @ApiResponse(responseCode = "200", content = {
              @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = EnrichmentResultList.class))
          }),
      })
  public EnrichmentResultList dereference(@Parameter(name = "uri") @RequestParam("uri") String resourceId) {
    try {
      DereferenceResult dereferenceResult = dereferenceInternal(resourceId);
      return new EnrichmentResultList(
          List.of(new EnrichmentResultBaseWrapper(dereferenceResult.getEnrichmentBasesAsList(),
              dereferenceResult.getDereferenceStatus())));
    } catch (RuntimeException e) {
      throw new DereferenceException(generateExceptionMessage(resourceId, e), e);
    }
  }

  private DereferenceResult dereferenceInternal(String resourceId) {
    return dereferenceService.dereference(resourceId);
  }

  private static String generateExceptionMessage(String resourceId, Exception e) {
    return String.format("Dereferencing failed for uri: %s with root cause: %s",
        resourceId.replaceAll(CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, ""), e.getMessage());
  }

  /**
   * Dereference a record given a URI
   *
   * @param resourceIds The resource IDs to dereference
   * @return The dereferenced entities and status
   */
  @PostMapping(value = RestEndpoints.DEREFERENCE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseBody
  @Operation(description = "Dereference a list URI",
      responses = {
          @ApiResponse(responseCode = "200", content = {
              @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = EnrichmentResultList.class))
          }),
      })
  public EnrichmentResultList dereference(@RequestBody List<String> resourceIds) {
    try {
      return new EnrichmentResultList(resourceIds.stream()
                                                 .map(this::dereferenceInternal)
                                                 .map(item -> new EnrichmentResultBaseWrapper(item.getEnrichmentBasesAsList(),
                                                     item.getDereferenceStatus()))
                                                 .collect(Collectors.toList()));
    } catch (RuntimeException e) {
      throw new DereferenceException(generateExceptionMessage(String.join(",", resourceIds), e), e);
    }
  }
}
