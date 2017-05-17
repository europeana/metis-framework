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
import eu.europeana.metis.core.dataset.DatasetListWrapper;
import eu.europeana.metis.core.exceptions.ApiKeyNotAuthorizedException;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.NoApiKeyFoundException;
import eu.europeana.metis.core.exceptions.NoOrganizationFoundException;
import eu.europeana.metis.core.exceptions.OrganizationAlreadyExistsException;
import eu.europeana.metis.core.organization.Organization;
import eu.europeana.metis.core.organization.OrganizationListWrapper;
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
public class OrganizationController {

  private final Logger LOGGER = LoggerFactory.getLogger(OrganizationController.class);

  private OrganizationService organizationService;
  private DatasetService datasetService;
  private MetisAuthorizationService authorizationService;

  @Autowired
  public OrganizationController(
      OrganizationService organizationService,
      DatasetService datasetService,
      MetisAuthorizationService authorizationService) {
    this.organizationService = organizationService;
    this.datasetService = datasetService;
    this.authorizationService = authorizationService;
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
  public void createOrganization(@RequestBody Organization organization,
      @QueryParam("apikey") String apikey)
      throws IOException, SolrServerException, ApiKeyNotAuthorizedException, NoApiKeyFoundException, OrganizationAlreadyExistsException, BadContentException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null) {
      if (key.getOptions().equals(Options.WRITE)) {
        organizationService.checkRestrictionsOnCreate(organization);
        organizationService.createOrganization(organization);
        LOGGER.info("Organization with id " + organization.getOrganizationId() + " created");
      } else {
        throw new ApiKeyNotAuthorizedException(apikey);
      }
    } else {
      throw new NoApiKeyFoundException(apikey);
    }
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
  public void deleteOrganization(@PathVariable("organizationId"
  ) String organizationId, @QueryParam("apikey") String apikey)
      throws IOException, SolrServerException, ApiKeyNotAuthorizedException, NoApiKeyFoundException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null) {
      if (key.getOptions().equals(Options.WRITE)) {
        organizationService.deleteOrganizationByOrganizationId(organizationId);
        LOGGER.info("Organization with id " + organizationId + " deleted");
      } else {
        throw new ApiKeyNotAuthorizedException(apikey);
      }
    } else {
      throw new NoApiKeyFoundException(apikey);
    }
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
      @PathVariable("organizationId"
      ) String organizationId, @QueryParam("apikey") String apikey)
      throws ApiKeyNotAuthorizedException, NoApiKeyFoundException, IOException, SolrServerException, NoOrganizationFoundException, BadContentException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null) {
      if (key.getOptions().equals(Options.WRITE)) {
        organizationService.checkRestrictionsOnUpdate(organization, organizationId);
        organizationService.updateOrganization(organization);
        LOGGER.info("Organization with id " + organizationId + " updated");
      } else {
        throw new ApiKeyNotAuthorizedException(apikey);
      }
    } else {
      throw new NoApiKeyFoundException(apikey);
    }
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
  @ApiOperation(value = "Get all organizations")
  public OrganizationListWrapper getAllOrganizations(@QueryParam("nextPage"
  ) String nextPage, @QueryParam("apikey") String apikey)
      throws IllegalAccessException, InstantiationException, NoApiKeyFoundException, ApiKeyNotAuthorizedException, NoOrganizationFoundException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null && (key.getOptions().equals(Options.WRITE) || key.getOptions()
        .equals(Options.READ))) {
      List<Organization> organizations = organizationService.getAllOrganizations(nextPage);
      OrganizationListWrapper organizationListWrapper = new OrganizationListWrapper();
      organizationListWrapper.setOrganizationsAndLastPage(organizations,
          organizationService.getOrganizationsPerRequestLimit());
      LOGGER.info("Batch of: " + organizationListWrapper.getListSize()
          + " organizations returned, using batch nextPage: " + nextPage);
      return organizationListWrapper;
    } else if (key == null) {
      throw new NoApiKeyFoundException(apikey);
    } else {
      throw new ApiKeyNotAuthorizedException(apikey);
    }
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
      @PathVariable("organizationId") String organizationId, @QueryParam("apikey") String apikey)
      throws NoApiKeyFoundException, ApiKeyNotAuthorizedException, NoOrganizationFoundException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null && (key.getOptions().equals(Options.WRITE) || key.getOptions()
        .equals(Options.READ))) {
      Organization organization = organizationService
          .getOrganizationByOrganizationId(organizationId);
      LOGGER.info("Organization with id " + organizationId + " found");
      return organization;
    } else if (key == null) {
      throw new NoApiKeyFoundException(apikey);
    } else {
      throw new ApiKeyNotAuthorizedException(apikey);
    }
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
  @ApiOperation(value = "Get all organizations by county isoCode", response = OrganizationListWrapper.class)
  public OrganizationListWrapper getAllOrganizationsByCountryIsoCode(
      @PathVariable("isoCode") String isoCode, @QueryParam("nextPage"
  ) String nextPage, @QueryParam("apikey") String apikey)
      throws NoOrganizationFoundException, NoApiKeyFoundException, ApiKeyNotAuthorizedException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null && (key.getOptions().equals(Options.WRITE) || key.getOptions()
        .equals(Options.READ))) {
      List<Organization> organizations = organizationService
          .getAllOrganizationsByCountry(Country.toCountry(isoCode), nextPage);
      OrganizationListWrapper organizationListWrapper = new OrganizationListWrapper();
      organizationListWrapper.setOrganizationsAndLastPage(organizations,
          organizationService.getOrganizationsPerRequestLimit());
      LOGGER.info("Batch of: " + organizationListWrapper.getListSize()
          + " organizations returned, using batch nextPage: " + nextPage);
      return organizationListWrapper;
    } else if (key == null) {
      throw new NoApiKeyFoundException(apikey);
    } else {
      throw new ApiKeyNotAuthorizedException(apikey);
    }
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
  @ApiOperation(value = "Get all organizations by organization roles", response = OrganizationListWrapper.class)
  public OrganizationListWrapper getAllOrganizationsByOrganizationRoles(
      @RequestParam("organizationRoles") List<OrganizationRole> organizationRoles,
      @QueryParam("nextPage") String nextPage,
      @QueryParam("apikey") String apikey)
      throws BadContentException, NoApiKeyFoundException, ApiKeyNotAuthorizedException, NoOrganizationFoundException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null && (key.getOptions().equals(Options.WRITE) || key.getOptions()
        .equals(Options.READ))) {
      if (organizationRoles != null) {
        List<Organization> organizations = organizationService
            .getAllOrganizationsByOrganizationRole(organizationRoles, nextPage);
        OrganizationListWrapper organizationListWrapper = new OrganizationListWrapper();
        organizationListWrapper.setOrganizationsAndLastPage(organizations,
            organizationService.getOrganizationsPerRequestLimit());
        LOGGER.info("Batch of: " + organizationListWrapper.getListSize()
            + " organizations returned, using batch nextPage: " + nextPage);
        return organizationListWrapper;
      } else {
        throw new BadContentException("Organization roles malformed or empty");
      }
    } else if (key == null) {
      throw new NoApiKeyFoundException(apikey);
    } else {
      throw new ApiKeyNotAuthorizedException(apikey);
    }
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
      throws IOException, SolrServerException, NoApiKeyFoundException, ApiKeyNotAuthorizedException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null && (key.getOptions().equals(Options.WRITE) || key.getOptions()
        .equals(Options.READ))) {
      List<OrganizationSearchBean> organizationSearchBeans = organizationService
          .suggestOrganizations(searchTerm);
      LOGGER.info("Found " + organizationSearchBeans.size() + " suggestions");
      return new OrganizationSearchListWrapper(organizationSearchBeans);
    } else if (key == null) {
      throw new NoApiKeyFoundException(apikey);
    } else {
      throw new ApiKeyNotAuthorizedException(apikey);
    }
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
  @ApiOperation(value = "Get all the datasets by organization Id", response = DatasetListWrapper.class)
  public DatasetListWrapper getAllDatasetsByOrganizationId(
      @PathVariable("organizationId") String organizationId,
      @QueryParam("nextPage") String nextPage, @QueryParam("apikey") String apikey)
      throws NoApiKeyFoundException, ApiKeyNotAuthorizedException, NoOrganizationFoundException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null && (key.getOptions().equals(Options.WRITE) || key.getOptions()
        .equals(Options.READ))) {
      DatasetListWrapper datasetListWrapper = new DatasetListWrapper();
      datasetListWrapper.setDatasetsAndLastPage(
          organizationService.getAllDatasetsByOrganizationId(organizationId, nextPage),
          datasetService.getDatasetsPerRequestLimit());
      LOGGER.info("Batch of: " + datasetListWrapper.getListSize()
          + " datasets returned, using batch nextPage: " + nextPage);
      return datasetListWrapper;
    } else if (key == null) {
      throw new NoApiKeyFoundException(apikey);
    } else {
      throw new ApiKeyNotAuthorizedException(apikey);
    }
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
      throws NoOrganizationFoundException, NoApiKeyFoundException, ApiKeyNotAuthorizedException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null && (key.getOptions().equals(Options.WRITE) || key.getOptions()
        .equals(Options.READ))) {
      return new ResultMap<>(
          Collections.singletonMap("optInIIIF", organizationService.isOptedInIIIF(organizationId)));
    } else if (key == null) {
      throw new NoApiKeyFoundException(apikey);
    } else {
      throw new ApiKeyNotAuthorizedException(apikey);
    }
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
      throws ParseException, IOException, NoOrganizationFoundException, NoApiKeyFoundException, ApiKeyNotAuthorizedException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null && (key.getOptions().equals(Options.WRITE) || key.getOptions()
        .equals(Options.READ))) {
      Organization organization = organizationService.getOrganizationByIdFromCRM(organizationId);
      LOGGER.info("Organization with id " + organizationId + " found in CRM");
      return organization;
    } else if (key == null) {
      throw new NoApiKeyFoundException(apikey);
    } else {
      throw new ApiKeyNotAuthorizedException(apikey);
    }
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
  @ApiOperation(value = "Get all organizations from CRM", response = OrganizationListWrapper.class)
  public OrganizationListWrapper getAllOrganizationsFromCRM(@RequestParam("apikey") String apikey)
      throws ParseException, IOException, NoOrganizationFoundException, NoApiKeyFoundException, ApiKeyNotAuthorizedException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null && (key.getOptions().equals(Options.WRITE) || key.getOptions()
        .equals(Options.READ))) {
      List<Organization> organizations = organizationService.getAllOrganizationsFromCRM();
      OrganizationListWrapper organizationListWrapper = new OrganizationListWrapper();
      organizationListWrapper.setOrganizations(organizations);
      return organizationListWrapper;
    } else if (key == null) {
      throw new NoApiKeyFoundException(apikey);
    } else {
      throw new ApiKeyNotAuthorizedException(apikey);
    }
  }

//  private ModelAndView constructModelAndViewForList(MetisKey key, List<Organization> orgs)
//      throws InstantiationException, IllegalAccessException {
//    if (key.getProfile().equals(Profile.PUBLIC)) {
//      List<ModelAndView> organizationViews = new ArrayList<>();
//      for (Organization org : orgs) {
//        organizationViews.add(PublicOrganizationView.generateResponse(org));
//      }
//      return JsonUtils.toJson(organizationViews);
//    } else {
//      List<ModelAndView> organizationViews = new ArrayList<>();
//      for (Organization org : orgs) {
//        organizationViews.add(MetisOrganizationView.generateResponse(org));
//      }
//      return JsonUtils.toJson(organizationViews);
//    }
//  }
}
