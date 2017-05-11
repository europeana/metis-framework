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

import static eu.europeana.metis.RestEndpoints.CRM_ORGANIZATIONS;
import static eu.europeana.metis.RestEndpoints.CRM_ORGANIZATION_ID;
import static eu.europeana.metis.RestEndpoints.ORGANIZATION;
import static eu.europeana.metis.RestEndpoints.ORGANIZATIONS;
import static eu.europeana.metis.RestEndpoints.ORGANIZATIONS_BYDATASET;
import static eu.europeana.metis.RestEndpoints.ORGANIZATIONS_ISOCODE;
import static eu.europeana.metis.RestEndpoints.ORGANIZATIONS_ROLES;
import static eu.europeana.metis.RestEndpoints.ORGANIZATION_ID;
import static eu.europeana.metis.RestEndpoints.ORGANIZATION_ID_DATASETS;
import static eu.europeana.metis.RestEndpoints.ORGANIZATION_OPTED_IN;
import static eu.europeana.metis.RestEndpoints.ORGANIZATION_SUGGEST;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.core.api.MetisKey;
import eu.europeana.metis.core.api.Options;
import eu.europeana.metis.core.api.Profile;
import eu.europeana.metis.core.common.Country;
import eu.europeana.metis.core.common.Role;
import eu.europeana.metis.core.dataset.DatasetList;
import eu.europeana.metis.core.exceptions.ApiKeyNotAuthorizedException;
import eu.europeana.metis.core.exceptions.BadContentException;
import eu.europeana.metis.core.exceptions.NoApiKeyFoundException;
import eu.europeana.metis.core.exceptions.NoOrganizationFoundException;
import eu.europeana.metis.core.exceptions.OrganizationAlreadyExistsException;
import eu.europeana.metis.core.organization.Organization;
import eu.europeana.metis.core.organization.OrganizationList;
import eu.europeana.metis.core.rest.response.MetisOrganizationView;
import eu.europeana.metis.core.rest.response.PublicOrganizationView;
import eu.europeana.metis.core.rest.utils.JsonUtils;
import eu.europeana.metis.core.rest.utils.ProviderUtils;
import eu.europeana.metis.core.service.MetisAuthorizationService;
import eu.europeana.metis.core.service.OrganizationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.StringUtils;
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
import org.springframework.web.servlet.ModelAndView;

/**
 * The organization controller
 * Created by ymamakis on 2/18/16.
 */
@Controller
@Api("/")
public class OrganizationController {

  private final Logger LOGGER = LoggerFactory.getLogger(OrganizationController.class);

  @Autowired
  private OrganizationService organizationService;
  @Autowired
  private MetisAuthorizationService authorizationService;

  @RequestMapping(value = RestEndpoints.ORGANIZATION, method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
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
        try {
          Organization storedOrganization = organizationService
              .getOrganizationByOrganizationId(organization.getOrganizationId());
          if (storedOrganization != null) {
            throw new OrganizationAlreadyExistsException(organization.getOrganizationId());
          }
        } catch (NoOrganizationFoundException e) {
          LOGGER.info("Organization not found, so can be created");
        }

        if (StringUtils.isEmpty(organization.getOrganizationId()))
          throw new BadContentException("OrganizationId cannot be null");

        organizationService.createOrganization(organization);
        LOGGER.info("Organization with id " + organization.getOrganizationId() + " created");
      } else {
        throw new ApiKeyNotAuthorizedException(apikey);
      }
    } else {
      throw new NoApiKeyFoundException(apikey);
    }
  }

  @RequestMapping(value = RestEndpoints.ORGANIZATION_ID, method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Successful response"),
      @ApiResponse(code = 401, message = "Api Key not authorized")})
  @ApiImplicitParams({
      @ApiImplicitParam(name = "apikey", value = "ApiKey", dataType = "string", paramType = "query", required = true),
      @ApiImplicitParam(name = "id", value = "OrganizationId", dataType = "string", paramType = "path", required = true)
  })
  @ApiOperation(value = "Delete an organization")
  public void deleteOrganization(@PathVariable("id"
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

  /**
   * Update an organization
   *
   * @param organization The organization to update
   */
  @RequestMapping(value = ORGANIZATION, method = RequestMethod.PUT, consumes = "application/json")
  @ResponseStatus(HttpStatus.OK)
  @ApiOperation(value = "Update an organization")
  public void updateOrganization(@RequestBody Organization organization,
      @PathVariable("apikey") String apikey)
      throws ApiKeyNotAuthorizedException, NoApiKeyFoundException, IOException, SolrServerException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null) {
      if (key.getOptions().equals(Options.WRITE)) {
        organizationService.updateOrganization(organization);
      } else {
        throw new ApiKeyNotAuthorizedException(apikey);
      }
    } else {
      throw new NoApiKeyFoundException(apikey);
    }
  }

  /**
   * Get all the organizations
   *
   * @return All the registered organizations in METIS
   */
  @RequestMapping(value = ORGANIZATIONS, method = RequestMethod.GET, produces = "application/json")

  @ApiOperation(value = "Retrieve all the organizations from METIS", response = OrganizationList.class)
  public ModelAndView getAllOrganizations(@RequestParam("apikey") String apikey)
      throws NoOrganizationFoundException, NoApiKeyFoundException, InstantiationException, IllegalAccessException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null) {
      List<Organization> orgs = organizationService.getAllOrganizations();
      return constructModelAndViewForList(key, orgs);
    }
    throw new NoApiKeyFoundException(apikey);
  }

  /**
   * Get all the organizations
   *
   * @return All the registered organizations in METIS
   */
  @RequestMapping(value = ORGANIZATIONS_ISOCODE, method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  @ApiOperation(value = "Retrieve all the organizations from METIS", response = OrganizationList.class)
  public ModelAndView getAllOrganizationsByCountry(@RequestParam("isoCode") String isoCode,
      @RequestParam("apikey") String apikey)
      throws NoOrganizationFoundException, IllegalAccessException, InstantiationException, NoApiKeyFoundException {

    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null) {
      List<Organization> orgs = organizationService
          .getAllOrganizationsByCountry(Country.toCountry(isoCode));
      return constructModelAndViewForList(key, orgs);
    }
    throw new NoApiKeyFoundException(apikey);

  }


  /**
   * Get all the organizations for specific roles
   *
   * @return All the registered organizations in METIS
   */
  @RequestMapping(value = ORGANIZATIONS_ROLES, method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  @ApiOperation(value = "Retrieve all the organizations from METIS", response = OrganizationList.class)
  public ModelAndView getAllOrganizationsByRoles(@RequestParam("role") List<String> roles,
      @RequestParam("apikey") String apikey)
      throws NoOrganizationFoundException, NoApiKeyFoundException, IllegalAccessException, InstantiationException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null) {
      if (roles != null) {
        Role[] role = new Role[roles.size()];
        int i = 0;
        for (String reqRole : roles) {
          if (ProviderUtils.getRoleFromString(reqRole) != null) {
            role[i] = ProviderUtils.getRoleFromString(reqRole);
          }
          i++;
        }

        List<Organization> orgs = organizationService.getAllProviders(role);
        return constructModelAndViewForList(key, orgs);
      }
      throw new NoOrganizationFoundException("No organization matching the criteria was found");
    }
    throw new NoApiKeyFoundException(apikey);
  }

  /**
   * Get all the datasets for an organization
   *
   * @param id The id of the organization
   * @return The datasets of the organization with the provided it
   */
  @RequestMapping(value = ORGANIZATION_ID_DATASETS, method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  @ApiOperation(value = "Get the datasets of an organization", response = DatasetList.class)
  public DatasetList getDatasetsByOrganization(@ApiParam("id") @PathVariable("id") String id)
      throws NoOrganizationFoundException {
    DatasetList list = new DatasetList();
    list.setDatasetList(organizationService.getDatasetsByOrganization(id));
    return list;
  }

  /**
   * Retrieve the organization with a specific id (datasets are transient)
   *
   * @param id The id of the organization to retrieve
   * @return The organization with the specified id
   */
  @RequestMapping(value = ORGANIZATION_ID, method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  @ApiOperation(value = "Get an organization by id", response = Organization.class)
  public ModelAndView getOrganizationById(@ApiParam("id") @PathVariable("id") String id,
      @ApiParam("apikey") @RequestParam("apikey") String apikey)
      throws NoApiKeyFoundException, NoOrganizationFoundException, InstantiationException, IllegalAccessException {
    Organization org = organizationService.getOrganizationById(id);
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null) {
      if (key.getProfile().equals(Profile.PUBLIC)) {
        return PublicOrganizationView.generateResponse(org);
      } else {
        return MetisOrganizationView.generateResponse(org);
      }
    }
    throw new NoApiKeyFoundException(apikey);

  }

  /**
   * Retrieve the organization with a specific organization id (datasets are transient)
   *
   * @param id The organization id of the organization to retrieve
   * @return The organization with the specified id
   */
  @RequestMapping(value = ORGANIZATION, method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  @ApiOperation(value = "Retrieve an organization by its CRM id", response = Organization.class)
  public ModelAndView getOrganizationByOrganizationId(
      @ApiParam("orgId") @RequestParam(value = "orgId") String id,
      @RequestParam("apikey") String apikey)
      throws NoOrganizationFoundException, InstantiationException, IllegalAccessException, NoApiKeyFoundException {
    Organization org = organizationService.getOrganizationByOrganizationId(id);
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null) {
      if (key.getProfile().equals(Profile.PUBLIC)) {
        return PublicOrganizationView.generateResponse(org);
      } else {
        return MetisOrganizationView.generateResponse(org);
      }
    }
    throw new NoApiKeyFoundException(apikey);
  }

  /**
   * Retrieve the organization with a specific organization from CRM
   *
   * @param id The organization id of the organization to retrieve
   * @return The organization with the specified id
   */
  @RequestMapping(value = CRM_ORGANIZATION_ID, method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  @ApiOperation(value = "Retrieve an organization from CRM", response = Organization.class)
  public ModelAndView getOrganizationByOrganizationIdFromCRM(
      @ApiParam("orgId") @PathVariable(value = "orgId") String id,
      @RequestParam("apikey") String apikey)
      throws NoOrganizationFoundException, IOException, ParseException, InstantiationException, IllegalAccessException, NoApiKeyFoundException {
    Organization org = organizationService.getOrganizationByIdFromCRM(id);
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null) {
      if (key.getProfile().equals(Profile.PUBLIC)) {
        return PublicOrganizationView.generateResponse(org);
      } else {
        return MetisOrganizationView.generateResponse(org);
      }
    }
    throw new NoApiKeyFoundException(apikey);
  }

  /**
   * Retrieve all the organizations from CRM
   *
   * @return The organization with the specified id
   */
  @RequestMapping(value = CRM_ORGANIZATIONS, method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  @ApiOperation(value = "Retrieve all the organizations from CRM", response = OrganizationList.class)
  public ModelAndView getOrganizationsFromCRM(@RequestParam("apikey") String apikey)
      throws NoOrganizationFoundException, IOException, ParseException, IllegalAccessException, InstantiationException, NoApiKeyFoundException {
    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null) {
      return constructModelAndViewForList(key, organizationService.getAllOrganizationsFromCRM());
    }
    throw new NoApiKeyFoundException(apikey);
  }

  /**
   * Check whether an organization is opted in for using the Image Service of Europeana or not
   *
   * @param id The id of the organization
   * @return true if opted in, false otherwise
   */
  @RequestMapping(value = ORGANIZATION_OPTED_IN, method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  @ApiOperation(value = "Check whether an organization is opted-in for IIIF or not")
  public ModelAndView isOptedIn(@PathVariable("id") String id) {
    ModelAndView view = new ModelAndView("json");
    view.addObject("result", organizationService.isOptedInForIIIF(id));
    return view;
  }

  /**
   * Autosuggestions for organizations
   *
   * @param suggestTerm The term to get the suggestions for
   * @return The List of organizations that fit in the suggestion
   */
  @RequestMapping(value = ORGANIZATION_SUGGEST, method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  @ApiOperation(value = "Suggest Organizations")
  public ModelAndView suggestOrganizations(@PathVariable("suggestTerm") String suggestTerm)
      throws IOException, SolrServerException {
    ModelAndView view = new ModelAndView("json");
    view.addObject("suggestions", organizationService.suggestOrganizations(suggestTerm));
    return view;
  }

  /**
   * Retrieve organizations by dataset and data provider id
   *
   * @param datasetId The dataset Id
   * @param dataproviderId The data provider id
   * @param apikey The API key
   * @return The lsit of organizatios fro a given dataset
   */
  @RequestMapping(value = ORGANIZATIONS_BYDATASET, method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  @ApiOperation(value = "Get organizations that refer to a dataset as provider")
  public ModelAndView getOrganizationsByDatasetId(@PathVariable("id") String datasetId,
      @RequestParam("dataProviderId") String dataproviderId,
      @RequestParam("apikey") String apikey)
      throws NoApiKeyFoundException, IllegalAccessException, InstantiationException {

    MetisKey key = authorizationService.getKeyFromId(apikey);
    if (key != null) {
      List<Organization> orgs = organizationService.getByDatasetId(datasetId, dataproviderId);
      return constructModelAndViewForList(key, orgs);
    }
    throw new NoApiKeyFoundException(apikey);
  }

  private ModelAndView constructModelAndViewForList(MetisKey key, List<Organization> orgs)
      throws InstantiationException, IllegalAccessException {
    if (key.getProfile().equals(Profile.PUBLIC)) {
      List<ModelAndView> organizationViews = new ArrayList<>();
      for (Organization org : orgs) {
        organizationViews.add(PublicOrganizationView.generateResponse(org));
      }
      return JsonUtils.toJson(organizationViews);
    } else {
      List<ModelAndView> organizationViews = new ArrayList<>();
      for (Organization org : orgs) {
        organizationViews.add(MetisOrganizationView.generateResponse(org));
      }
      return JsonUtils.toJson(organizationViews);
    }
  }

}
