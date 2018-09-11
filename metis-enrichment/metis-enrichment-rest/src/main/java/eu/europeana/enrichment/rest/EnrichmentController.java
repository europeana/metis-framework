package eu.europeana.enrichment.rest;

import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.InputValueList;
import eu.europeana.enrichment.api.external.UriList;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.rest.exception.EnrichmentException;
import eu.europeana.enrichment.service.Converter;
import eu.europeana.enrichment.service.Enricher;
import eu.europeana.enrichment.service.EntityRemover;
import eu.europeana.metis.RestEndpoints;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Enrichment REST API
 */
@Api("/")
@Controller
public class EnrichmentController {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentController.class);
  private final Converter converter;
  private final Enricher enricher;
  private final EntityRemover remover;

  /**
   * Autowired constructor.
   *
   * @param enricher class that handles enrichment functionality
   * @param remover class that removed values from mongo and redis
   * @param converter utility class for conversion between different classes
   */
  @Autowired
  public EnrichmentController(Enricher enricher, EntityRemover remover, Converter converter) {
    this.converter = converter;
    this.enricher = enricher;
    this.remover = remover;
  }

  /**
   * Delete uris
   *
   * @param values the URIs to delete
   */
  @ResponseStatus(value = HttpStatus.OK)
  @RequestMapping(value = RestEndpoints.ENRICHMENT_DELETE, method = RequestMethod.DELETE)
  @ApiOperation(value = "Delete a list of URIs")
  @ApiResponses(value = {
      @ApiResponse(code = 400, message = "Error processing the result")
  })
  public void delete(@RequestBody UriList values) {
    remover.remove(values.getUris());
  }

  /**
   * Get an enrichment by URI (rdf:about or owl:sameAs/skos:exactMatch
   *
   * @param uri The URI to retrieve
   * @return the structured result of the enrichment
   * @throws EnrichmentException if an exception occurred during enrichment
   */
  @RequestMapping(value = RestEndpoints.ENRICHMENT_BYURI, method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ApiOperation(value = "Retrieve an entity by URI or its sameAs", response = EnrichmentBase.class)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 400, message = "Error processing the result")
  })
  public EnrichmentBase getByUri(@ApiParam("uri") @RequestParam("uri") String uri)
      throws EnrichmentException {

    EntityWrapper wrapper = enricher.getByUri(uri);
    if (wrapper == null) {
      return null;
    }

    try {
      return converter.convert(wrapper);
    } catch (IOException e) {
      LOGGER.error("Error converting object to EnrichmentBase", e);
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
  @RequestMapping(value = RestEndpoints.ENRICHMENT_ENRICH, method = RequestMethod.POST,
      consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @ResponseBody
  @ApiOperation(value = "Enrich a series of field value pairs", response = EnrichmentResultList.class)
  @ApiResponses(value = {
      @ApiResponse(code = 400, message = "Error processing the result")
  })
  public EnrichmentResultList enrich(@ApiParam("input") @RequestBody InputValueList input)
      throws EnrichmentException {

    try {
      List<EntityWrapper> wrapperList = enricher.tagExternal(input.getInputValues());
      return converter.convert(wrapperList);
    } catch (IOException e) {
      LOGGER.error("Error converting object.", e);
      throw new EnrichmentException("Error converting object.", e);
    }
  }
}
