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
package eu.europeana.metis.core.rest;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.core.api.MetisKey;
import eu.europeana.metis.core.api.Options;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.OrganizationRole;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.exceptions.ApiKeyNotAuthorizedException;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.EmptyApiKeyException;
import eu.europeana.metis.core.exceptions.NoApiKeyFoundException;
import eu.europeana.metis.core.exceptions.NoOrganizationFoundException;
import eu.europeana.metis.core.exceptions.OrganizationAlreadyExistsException;
import eu.europeana.metis.core.organization.Organization;
import eu.europeana.metis.core.search.common.OrganizationSearchBean;
import eu.europeana.metis.core.search.common.OrganizationSearchListWrapper;
import eu.europeana.metis.core.service.DatasetService;
import eu.europeana.metis.core.service.MetisAuthorizationService;
import eu.europeana.metis.core.service.OrganizationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The organization controller
 * Created by ymamakis on 2/18/16.
 */
@Controller
@Api("/")
public class OrganizationController extends ApiKeySecuredControllerBase {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationController.class);
  private static final String BATCH_ORGANIZATIONS_WITH_NEXT_PAGE = "Batch of: {} organizations returned, using batch nextPage: {}";

  private final OrganizationService organizationService;
  private final DatasetService datasetService;

  @Autowired
  public OrganizationController(
      OrganizationService organizationService,
      DatasetService datasetService,
      MetisAuthorizationService authorizationService) {
    super(authorizationService);
    this.organizationService = organizationService;
    this.datasetService = datasetService;
  }

  @RequestMapping(value = RestEndpoints.ORGANIZATIONS, method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.CREATED)
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Successful response"),
      @ApiResponse(code = 401, message = "Api Key not authorized"),
      @ApiResponse(code = 406, message = "Bad content"),
      @ApiResponse(code = 409, message = "Organization already exists")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "apikey", value = "ApiKey", dataType = "string", paramType = "query", required = true)
  })
  @ApiOperation(value = "Create an organization")
  public void createOrganization(
      @RequestBody Organization organization,
      @QueryParam("apikey") String apikey)
      throws IOException, SolrServerException, ApiKeyNotAuthorizedException, NoApiKeyFoundException, OrganizationAlreadyExistsException, BadContentException, EmptyApiKeyException {
    MetisKey key = ensureValidKey(apikey);

    ensureActionAuthorized(apikey, key, Options.WRITE);
    organizationService.checkRestrictionsOnCreate(organization);
    organizationService.createOrganization(organization);
    LOGGER.info("Organization with id {} created", organization.getOrganizationId());
  }

  @RequestMapping(value = RestEndpoints.ORGANIZATIONS_ORGANIZATION_ID, method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Successful response"),
      @ApiResponse(code = 401, message = "Api Key not authorized")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "apikey", value = "ApiKey", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "organizationId", value = "OrganizationId", dataType = "string", paramType = "path", required = true)
  })
  @ApiOperation(value = "Delete an organization by organization Id")
  public void deleteOrganization(
      @PathVariable("organizationId") String organizationId,
      @QueryParam("apikey") String apikey)
      throws IOException, SolrServerException, ApiKeyNotAuthorizedException, NoApiKeyFoundException, EmptyApiKeyException {
    MetisKey key = ensureValidKey(apikey);
    ensureActionAuthorized(apikey, key, Options.WRITE);
    organizationService.deleteOrganizationByOrganizationId(organizationId);
    LOGGER.info("Organization with id {} deleted", organizationId);
  }

  @RequestMapping(value = RestEndpoints.ORGANIZATIONS_ORGANIZATION_ID, method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Successful response"),
      @ApiResponse(code = 404, message = "Organization not found"),
      @ApiResponse(code = 406, message = "Bad content")
  })
  @ApiImplicitParams({
      @ApiImplicitParam(name = "apikey", value = "ApiKey", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "organizationId", value = "OrganizationId", dataType = "string", paramType = "path", required = true)
  })
  @ApiOperation(value = "Update an organization by organization Id")
  public void updateOrganization(@RequestBody Organization organization,
      @PathVariable("organizationId") String organizationId,
      @QueryParam("apikey") String apikey)
      throws ApiKeyNotAuthorizedException, NoApiKeyFoundException, IOException, SolrServerException, NoOrganizationFoundException, BadContentException, EmptyApiKeyException {

    MetisKey key = ensureValidKey(apikey);
    ensureActionAuthorized(apikey, key, Options.WRITE);

    organizationService.checkRestrictionsOnUpdate(organization, organizationId);
    organizationService.updateOrganization(organization);
    LOGGER.info("Organization with id {} updated", organizationId);
  }

  @RequestMapping(value = RestEndpoints.ORGANIZATIONS, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response"),
      @ApiResponse(code = 401, message = "Api Key not authorized")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "apikey", value = "ApiKey", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "nextPage", value = "nextPage", dataType = "string", paramType = "query")
  })
  @ApiOperation(value = "Get all organizations", response = ResponseListWrapper.class)
  public ResponseListWrapper<Organization> getAllOrganizations(
      @QueryParam("nextPage") String nextPage,
      @QueryParam("apikey") String apikey)
      throws NoApiKeyFoundException, ApiKeyNotAuthorizedException, EmptyApiKeyException {

    MetisKey key = ensureValidKey(apikey);
    ensureReadOrWriteAccess(apikey, key);

    List<Organization> organizations = organizationService.getAllOrganizations(nextPage);
    ResponseListWrapper<Organization> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper.setResultsAndLastPage(organizations,
        organizationService.getOrganizationsPerRequestLimit());
    LOGGER.info(BATCH_ORGANIZATIONS_WITH_NEXT_PAGE,
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  @RequestMapping(value = RestEndpoints.ORGANIZATIONS_ORGANIZATION_ID, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response"),
      @ApiResponse(code = 401, message = "Api Key not authorized"),
      @ApiResponse(code = 404, message = "Organization not found")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "apikey", value = "ApiKey", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "organizationId", value = "OrganizationId", dataType = "string", paramType = "path", required = true)
  })
  @ApiOperation(value = "Get an organization by organization Id", response = Organization.class)
  public Organization getOrganizationByOrganizationId(
      @PathVariable("organizationId") String organizationId,
      @QueryParam("apikey") String apikey)
      throws NoApiKeyFoundException, ApiKeyNotAuthorizedException, NoOrganizationFoundException, EmptyApiKeyException {

    MetisKey key = ensureValidKey(apikey);
    ensureReadOrWriteAccess(apikey, key);

    Organization organization = organizationService
        .getOrganizationByOrganizationId(organizationId);
    LOGGER.info("Organization with id {} found", organizationId);
    return organization;

  }

  @RequestMapping(value = RestEndpoints.ORGANIZATIONS_COUNTRY_ISOCODE, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response"),
      @ApiResponse(code = 401, message = "Api Key not authorized")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "apikey", value = "ApiKey", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "nextPage", value = "nextPage", dataType = "string", paramType = "query"),
      @ApiImplicitParam(name = "isoCode", value = "IsoCode", dataType = "string", paramType = "path", required = true)
  })
  @ApiOperation(value = "Get all organizations by county isoCode", response = ResponseListWrapper.class)
  public ResponseListWrapper<Organization> getAllOrganizationsByCountryIsoCode(
      @PathVariable("isoCode") String isoCode,
      @QueryParam("nextPage") String nextPage,
      @QueryParam("apikey") String apikey)
      throws NoApiKeyFoundException, ApiKeyNotAuthorizedException, EmptyApiKeyException {

    MetisKey key = ensureValidKey(apikey);
    ensureReadOrWriteAccess(apikey, key);

    List<Organization> organizations = organizationService
        .getAllOrganizationsByCountry(Country.toCountry(isoCode), nextPage);
    ResponseListWrapper<Organization> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper.setResultsAndLastPage(organizations,
        organizationService.getOrganizationsPerRequestLimit());
    LOGGER.info(BATCH_ORGANIZATIONS_WITH_NEXT_PAGE,
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  @RequestMapping(value = RestEndpoints.ORGANIZATIONS_ROLES, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response"),
      @ApiResponse(code = 401, message = "Api Key not authorized"),
      @ApiResponse(code = 406, message = "Bad content")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "apikey", value = "ApiKey", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "nextPage", value = "nextPage", dataType = "string", paramType = "query"),
      @ApiImplicitParam(name = "organizationRoles", value = "comma separated values, e.g. CONTENT_PROVIDER,EUROPEANA", allowMultiple = true, dataType = "string", paramType = "query", required = true)
  })
  @ApiOperation(value = "Get all organizations by organization roles", response = ResponseListWrapper.class)
  public ResponseListWrapper<Organization> getAllOrganizationsByOrganizationRoles(
      @RequestParam("organizationRoles") List<OrganizationRole> organizationRoles,
      @QueryParam("nextPage") String nextPage,
      @QueryParam("apikey") String apikey)
      throws BadContentException, NoApiKeyFoundException, ApiKeyNotAuthorizedException, EmptyApiKeyException {

    MetisKey key = ensureValidKey(apikey);
    ensureReadOrWriteAccess(apikey, key);
    ensureRoles(organizationRoles);

    List<Organization> organizations = organizationService
        .getAllOrganizationsByOrganizationRole(organizationRoles, nextPage);
    ResponseListWrapper<Organization> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper.setResultsAndLastPage(organizations,
        organizationService.getOrganizationsPerRequestLimit());
    LOGGER.info(BATCH_ORGANIZATIONS_WITH_NEXT_PAGE,
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  @RequestMapping(value = RestEndpoints.ORGANIZATIONS_SUGGEST, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response"),
      @ApiResponse(code = 401, message = "Api Key not authorized")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "apikey", value = "ApiKey", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "searchTerm", value = "search value to get suggestions from", dataType = "string", paramType = "query")
  })
  @ApiOperation(value = "Suggest Organizations by a search term")
  public OrganizationSearchListWrapper suggestOrganizations(
      @QueryParam("searchTerm") String searchTerm,
      @QueryParam("apikey") String apikey)
      throws IOException, SolrServerException, NoApiKeyFoundException, ApiKeyNotAuthorizedException, EmptyApiKeyException {
    MetisKey key = ensureValidKey(apikey);
    ensureReadOrWriteAccess(apikey, key);
    List<OrganizationSearchBean> organizationSearchBeans = organizationService
        .suggestOrganizations(searchTerm);
    LOGGER.info("Found {} suggestions", organizationSearchBeans.size());
    return new OrganizationSearchListWrapper(organizationSearchBeans);
  }

  @RequestMapping(value = RestEndpoints.ORGANIZATIONS_ORGANIZATION_ID_DATASETS, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response"),
      @ApiResponse(code = 401, message = "Api Key not authorized"),
      @ApiResponse(code = 404, message = "Organization not found")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "apikey", value = "ApiKey", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "nextPage", value = "nextPage", dataType = "string", paramType = "query"),
      @ApiImplicitParam(name = "organizationId", value = "OrganizationId", dataType = "string", paramType = "path", required = true)
  })
  @ApiOperation(value = "Get all the datasets by organization Id", response = ResponseListWrapper.class)
  public ResponseListWrapper<Dataset> getAllDatasetsByOrganizationId(
      @PathVariable("organizationId") String organizationId,
      @QueryParam("nextPage") String nextPage, @QueryParam("apikey") String apikey)
      throws NoApiKeyFoundException, ApiKeyNotAuthorizedException, NoOrganizationFoundException, EmptyApiKeyException {

    MetisKey key = ensureValidKey(apikey);
    ensureReadOrWriteAccess(apikey, key);

    ResponseListWrapper<Dataset> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper.setResultsAndLastPage(
        organizationService.getAllDatasetsByOrganizationId(organizationId, nextPage),
        datasetService.getDatasetsPerRequestLimit());
    LOGGER.info("Batch of: {} datasets returned, using batch nextPage: {}",
        responseListWrapper.getListSize(), nextPage);
    return responseListWrapper;
  }

  @RequestMapping(value = RestEndpoints.ORGANIZATIONS_ORGANIZATION_ID_OPTINIIIF, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response"),
      @ApiResponse(code = 401, message = "Api Key not authorized"),
      @ApiResponse(code = 404, message = "Organization not found")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "apikey", value = "ApiKey", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "organizationId", value = "OrganizationId", dataType = "string", paramType = "path", required = true)
  })
  @ApiOperation(value = "Check if an organization is opted-in for IIIF or not", response = ResultMap.class)
  public ResultMap<Boolean> isOrganizationIdOptedIn(
      @PathVariable("organizationId") String organizationId, @QueryParam("apikey") String apikey)
      throws NoOrganizationFoundException, NoApiKeyFoundException, ApiKeyNotAuthorizedException, EmptyApiKeyException {

    MetisKey key = ensureValidKey(apikey);
    ensureReadOrWriteAccess(apikey, key);

    return new ResultMap<>(
        Collections.singletonMap("optInIIIF", organizationService.isOptedInIIIF(organizationId)));
  }

  @RequestMapping(value = RestEndpoints.ORGANIZATIONS_CRM_ORGANIZATION_ID, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response"),
      @ApiResponse(code = 401, message = "Api Key not authorized"),
      @ApiResponse(code = 404, message = "Organization not found")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "apikey", value = "ApiKey", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "organizationId", value = "OrganizationId", dataType = "string", paramType = "path", required = true)
  })
  @ApiOperation(value = "Get an organization from CRM", response = Organization.class)
  public Organization getOrganizationByOrganizationIdFromCRM(
      @PathVariable("organizationId") String organizationId, @QueryParam("apikey") String apikey)
      throws ParseException, IOException, NoOrganizationFoundException, NoApiKeyFoundException, ApiKeyNotAuthorizedException, EmptyApiKeyException {

    MetisKey key = ensureValidKey(apikey);
    ensureReadOrWriteAccess(apikey, key);

    Organization organization = organizationService.getOrganizationByIdFromCRM(organizationId);
    LOGGER.info("Organization with id {} found in CRM", organizationId);
    return organization;
  }

  @RequestMapping(value = RestEndpoints.ORGANIZATIONS_CRM, method = RequestMethod.GET, produces = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successful response"),
      @ApiResponse(code = 401, message = "Api Key not authorized")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "apikey", value = "ApiKey", dataType = "string", paramType = "query", required = true)
  })
  @ApiOperation(value = "Get all organizations from CRM", response = ResponseListWrapper.class)
  public ResponseListWrapper<Organization> getAllOrganizationsFromCRM(
      @RequestParam("apikey") String apikey)
      throws ParseException, IOException, NoApiKeyFoundException, ApiKeyNotAuthorizedException, EmptyApiKeyException {

    MetisKey key = ensureValidKey(apikey);
    ensureReadOrWriteAccess(apikey, key);

    List<Organization> organizations = organizationService.getAllOrganizationsFromCRM();
    ResponseListWrapper<Organization> responseListWrapper = new ResponseListWrapper<>();
    responseListWrapper.setResults(organizations);
    return responseListWrapper;
  }

  private void ensureRoles(List<OrganizationRole> organizationRoles)
      throws BadContentException {
    if (organizationRoles == null || organizationRoles.isEmpty()) {
      throw new BadContentException("Organization roles malformed or empty");
    }
  }
}
