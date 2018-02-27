/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
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


import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
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

  @Autowired
  public  EnrichmentController(Enricher enricher, EntityRemover remover, Converter converter) {
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
  public void delete(@RequestBody UriList values)  {
    remover.remove(values.getUris());
  }

  /**
   * Get an enrichment by URI (rdf:about or owl:sameAs/skos:exactMatch
   *
   * @param uri The URI to retrieve
   */
  @RequestMapping(value = RestEndpoints.ENRICHMENT_BYURI, method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE} )
  @ApiOperation(value = "Retrieve an entity by URI or its sameAs", response = EnrichmentBase.class )
  @Produces({MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE} )
  @ResponseBody
  @ApiResponses(value = {
		  @ApiResponse(code = 400, message = "Error processing the result")
  })
  public EnrichmentBase getByUri(@ApiParam("uri") @RequestParam("uri") String uri)
      throws EnrichmentException{

    EntityWrapper wrapper = enricher.getByUri(uri);
    if (wrapper == null) {
      return null;
    }

    try {
     return converter.convert(wrapper);
    } catch (IOException e) {
      LOGGER.error("Error converting object.", e);
      throw new EnrichmentException(e.getMessage());
    }
  }

  /**
   * Enrich a number of values
   *
   * @param input A list of values
   */
  @RequestMapping(value = RestEndpoints.ENRICHMENT_ENRICH, method = RequestMethod.POST,
      consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
      produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE} )
  @ResponseBody
  @Consumes({MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
  @Produces({MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE} )
  @ApiOperation(value = "Enrich a series of field value pairs", response = EnrichmentResultList.class)
  @ApiResponses(value = {
		  @ApiResponse(code = 400, message = "Error processing the result")
  })
  public EnrichmentResultList enrich(@ApiParam("input") @RequestBody InputValueList input)
      throws EnrichmentException {
	  
	  try {
		  List<EntityWrapper> wrapperList = enricher.tagExternal(input.getInputValueList());

		  EnrichmentResultList result = converter.convert(wrapperList);
		  
		  if (result.getResult().isEmpty()) {
			  return null;
		  }
		  
		  return result;
	  } catch (IOException e) {
		  LOGGER.error("Error converting object.", e);
		  throw new EnrichmentException(e.getMessage());
	  }
  }
}
