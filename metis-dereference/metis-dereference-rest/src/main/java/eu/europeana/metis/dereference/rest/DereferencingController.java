package eu.europeana.metis.dereference.rest;

import eu.europeana.enrichment.api.external.DereferenceResultStatus;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.metis.dereference.rest.exceptions.DereferenceException;
import eu.europeana.metis.dereference.service.DereferenceService;
import eu.europeana.metis.utils.CommonStringValues;
import eu.europeana.metis.utils.RestEndpoints;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Api("/")
public class DereferencingController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DereferencingController.class);

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
  @ApiOperation(value = "Dereference a URI", response = EnrichmentResultList.class)
  public EnrichmentResultList dereference(@ApiParam("uri") @RequestParam("uri") String resourceId) {
    try {
      Pair<List<EnrichmentBase>, DereferenceResultStatus> dereferenceResult = dereferenceInternal(resourceId);
      return new EnrichmentResultList(
          List.of(new EnrichmentResultBaseWrapper(dereferenceResult.getLeft(), dereferenceResult.getRight())));
    } catch (RuntimeException e) {
      throw new DereferenceException(generateExceptionMessage(resourceId, e), e);
    }
  }

  private Pair<List<EnrichmentBase>, DereferenceResultStatus> dereferenceInternal(String resourceId) {
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
  @ApiOperation(value = "Dereference a list URI", response = EnrichmentResultList.class)
  public EnrichmentResultList dereference(@RequestBody List<String> resourceIds) {
    try {
      return new EnrichmentResultList(resourceIds.stream()
                                                 .map(this::dereferenceInternal)
                                                 .map(item -> new EnrichmentResultBaseWrapper(
                                                     (List<EnrichmentBase>) item.getLeft(), item.getRight()))
                                                 .collect(Collectors.toList()));
    } catch (RuntimeException e) {
      throw new DereferenceException(generateExceptionMessage(resourceIds.stream().collect(Collectors.joining(",")), e), e);
    }
  }
}
