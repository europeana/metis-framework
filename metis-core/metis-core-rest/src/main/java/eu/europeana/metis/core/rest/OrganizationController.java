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
import eu.europeana.metis.common.model.OrganizationRole;
import eu.europeana.metis.core.api.MetisKey;
import eu.europeana.metis.core.api.Options;
import eu.europeana.metis.core.common.Country;
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
import java.io.IOException;
import java.util.Collections;
import java.util.List;
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

@Controller
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
  public void createOrganization(
      @RequestBody Organization organization,
      @RequestParam("apikey") String apikey)
      throws IOException, SolrServerException, ApiKeyNotAuthorizedException, NoApiKeyFoundException, OrganizationAlreadyExistsException, BadContentException, EmptyApiKeyException {
    MetisKey key = ensureValidKey(apikey);

    ensureActionAuthorized(apikey, key, Options.WRITE);
    organizationService.checkRestrictionsOnCreate(organization);
    organizationService.createOrganization(organization);
    LOGGER.info("Organization with id {} created", organization.getOrganizationId());
  }

  @RequestMapping(value = RestEndpoints.ORGANIZATIONS_ORGANIZATION_ID, method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteOrganization(
      @PathVariable("organizationId") String organizationId,
      @RequestParam("apikey") String apikey)
      throws IOException, SolrServerException, ApiKeyNotAuthorizedException, NoApiKeyFoundException, EmptyApiKeyException {
    MetisKey key = ensureValidKey(apikey);
    ensureActionAuthorized(apikey, key, Options.WRITE);
    organizationService.deleteOrganizationByOrganizationId(organizationId);
    LOGGER.info("Organization with id {} deleted", organizationId);
  }

  @RequestMapping(value = RestEndpoints.ORGANIZATIONS_ORGANIZATION_ID, method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateOrganization(@RequestBody Organization organization,
      @PathVariable("organizationId") String organizationId,
      @RequestParam("apikey") String apikey)
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
  public ResponseListWrapper<Organization> getAllOrganizations(
      @RequestParam("nextPage") String nextPage,
      @RequestParam("apikey") String apikey)
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
  public Organization getOrganizationByOrganizationId(
      @PathVariable("organizationId") String organizationId,
      @RequestParam("apikey") String apikey)
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
  public ResponseListWrapper<Organization> getAllOrganizationsByCountryIsoCode(
      @PathVariable("isoCode") String isoCode,
      @RequestParam("nextPage") String nextPage,
      @RequestParam("apikey") String apikey)
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
  public ResponseListWrapper<Organization> getAllOrganizationsByOrganizationRoles(
      @RequestParam("organizationRoles") List<OrganizationRole> organizationRoles,
      @RequestParam("nextPage") String nextPage,
      @RequestParam("apikey") String apikey)
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
  public OrganizationSearchListWrapper suggestOrganizations(
      @RequestParam("searchTerm") String searchTerm,
      @RequestParam("apikey") String apikey)
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
  public ResponseListWrapper<Dataset> getAllDatasetsByOrganizationId(
      @PathVariable("organizationId") String organizationId,
      @RequestParam("nextPage") String nextPage, @RequestParam("apikey") String apikey)
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
  public ResultMap<Boolean> isOrganizationIdOptedIn(
      @PathVariable("organizationId") String organizationId, @RequestParam("apikey") String apikey)
      throws NoOrganizationFoundException, NoApiKeyFoundException, ApiKeyNotAuthorizedException, EmptyApiKeyException {

    MetisKey key = ensureValidKey(apikey);
    ensureReadOrWriteAccess(apikey, key);

    return new ResultMap<>(
        Collections.singletonMap("optInIIIF", organizationService.isOptedInIIIF(organizationId)));
  }

  private void ensureRoles(List<OrganizationRole> organizationRoles)
      throws BadContentException {
    if (organizationRoles == null || organizationRoles.isEmpty()) {
      throw new BadContentException("Organization roles malformed or empty");
    }
  }
}
