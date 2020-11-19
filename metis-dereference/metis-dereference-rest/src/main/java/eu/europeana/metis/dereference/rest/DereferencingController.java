package eu.europeana.metis.dereference.rest;

import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.metis.utils.CommonStringValues;
import eu.europeana.metis.utils.RestEndpoints;
import eu.europeana.metis.dereference.rest.exceptions.DereferenceException;
import eu.europeana.metis.dereference.service.DereferenceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.net.URISyntaxException;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
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
   * @return The dereferenced entities
   */
  @GetMapping(value = RestEndpoints.DEREFERENCE, produces = {MediaType.APPLICATION_JSON_VALUE,
      MediaType.APPLICATION_XML_VALUE})
  @ResponseBody
  @ApiOperation(value = "Dereference a URI", response = EnrichmentResultList.class)
  public EnrichmentResultList dereference(@ApiParam("uri") @RequestParam("uri") String resourceId)
      throws URISyntaxException {
    try {
      return dereferenceService.dereference(resourceId);
    } catch (RuntimeException | JAXBException | TransformerException e) {
      LOGGER.warn(String.format("Problem occurred while dereferencing resource %s.",
          resourceId.replaceAll(CommonStringValues.REPLACEABLE_CRLF_CHARACTERS_REGEX, "")), e);
      throw new DereferenceException(String
          .format("Dereferencing failed for uri: %s with root cause: %s", resourceId,
              e.getMessage()), e);
    }
  }

  /**
   * Dereference a record given a URI
   *
   * @param resourceIds The resource IDs to dereference
   * @return The dereferenced entities
   */
  @PostMapping(value = RestEndpoints.DEREFERENCE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseBody
  @ApiOperation(value = "Dereference a list URI", response = EnrichmentResultList.class)
  public EnrichmentResultList dereference(@RequestBody List<String> resourceIds)
      throws URISyntaxException {
    EnrichmentResultList dereferencedEntities = new EnrichmentResultList();
    for (String resourceId : resourceIds) {
      EnrichmentResultList result = dereference(resourceId);
      if (result != null) {
        dereferencedEntities.getEnrichmentBaseResultWrapperList()
            .addAll(result.getEnrichmentBaseResultWrapperList());
      }
    }
    return dereferencedEntities;
  }
}
