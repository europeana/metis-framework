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
package eu.europeana.metis.framework.rest;

import eu.europeana.metis.framework.api.MetisKey;
import eu.europeana.metis.framework.api.Options;
import eu.europeana.metis.framework.api.Profile;
import eu.europeana.metis.framework.common.Country;
import eu.europeana.metis.framework.common.Role;
import eu.europeana.metis.framework.dataset.DatasetList;
import eu.europeana.metis.framework.exceptions.NoApiKeyFoundException;
import eu.europeana.metis.framework.exceptions.NoOrganizationExceptionFound;
import eu.europeana.metis.framework.exceptions.NotAuthorizedException;
import eu.europeana.metis.framework.organization.Organization;
import eu.europeana.metis.framework.organization.OrganizationList;
import eu.europeana.metis.framework.rest.response.MetisOrganizationView;
import eu.europeana.metis.framework.rest.response.PublicOrganizationView;
import eu.europeana.metis.framework.rest.utils.JsonUtils;
import eu.europeana.metis.framework.rest.utils.ProviderUtils;
import eu.europeana.metis.framework.service.MetisAuthorizationService;
import eu.europeana.metis.framework.service.OrganizationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static eu.europeana.metis.RestEndpoints.*;

/**
 * The organization controller
 * Created by ymamakis on 2/18/16.
 */
@Controller
@Api("/")
public class OrganizationController {
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private MetisAuthorizationService authorizationService;

    /**
     * Create an organization
     *
     * @param organization The organization to create
     */
    @RequestMapping(value = ORGANIZATION, method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Create an organization in METIS")
    public void createOrganization(@ApiParam @RequestBody Organization organization, @PathVariable("apikey") String apikey) throws NoApiKeyFoundException, NotAuthorizedException, IOException, SolrServerException {
        MetisKey key = authorizationService.getKeyFromId(apikey);
        if(key!=null) {
            if (key.getOptions().equals(Options.WRITE)) {
                organizationService.createOrganization(organization);
            } else {
                throw new NotAuthorizedException(apikey);
            }
        } else {
            throw  new NoApiKeyFoundException(apikey);
        }
    }

    /**
     * Delete an organization
     *
     * @param organization The organization to delete
     */
    @RequestMapping(value = ORGANIZATION, method = RequestMethod.DELETE, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Delete an organization")
    public void deleteOrganization(@ApiParam @RequestBody Organization organization, @PathVariable("apikey")String apikey) throws NotAuthorizedException, NoApiKeyFoundException, IOException, SolrServerException {
        MetisKey key = authorizationService.getKeyFromId(apikey);
        if(key!=null) {
            if (key.getOptions().equals(Options.WRITE)) {
                organizationService.deleteOrganization(organization);
            } else {
                throw new NotAuthorizedException(apikey);
            }
        } else {
            throw  new NoApiKeyFoundException(apikey);
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
    public void updateOrganization(@RequestBody Organization organization, @PathVariable("apikey")String apikey) throws NotAuthorizedException, NoApiKeyFoundException, IOException, SolrServerException {
        MetisKey key = authorizationService.getKeyFromId(apikey);
        if(key!=null) {
            if (key.getOptions().equals(Options.WRITE)) {
                organizationService.updateOrganization(organization);
            } else {
                throw new NotAuthorizedException(apikey);
            }
        } else {
            throw  new NoApiKeyFoundException(apikey);
        }
    }

    /**
     * Get all the organizations
     *
     * @return All the registered organizations in METIS
     */
    @RequestMapping(value = ORGANIZATIONS, method = RequestMethod.GET, produces = "application/json")

    @ApiOperation(value = "Retrieve all the organizations from METIS", response = OrganizationList.class)
    public ModelAndView getAllOrganizations(@RequestParam ("apikey")String apikey) throws NoOrganizationExceptionFound, NoApiKeyFoundException, InstantiationException, IllegalAccessException {
        MetisKey key = authorizationService.getKeyFromId(apikey);
        if(key!=null) {
            List<Organization> orgs = organizationService.getAllOrganizations();
            return constructModelAndViewForList(key,orgs);
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
    public ModelAndView getAllOrganizationsByCountry(@RequestParam("isoCode") String isoCode,@RequestParam("apikey") String apikey) throws NoOrganizationExceptionFound, IllegalAccessException, InstantiationException, NoApiKeyFoundException {

        MetisKey key = authorizationService.getKeyFromId(apikey);
        if(key!=null) {
            List<Organization> orgs = organizationService.getAllOrganizationsByCountry(Country.toCountry(isoCode));
            return constructModelAndViewForList(key,orgs);
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
    public ModelAndView getAllOrganizationsByRoles(@RequestParam("role") List<String> roles,@RequestParam("apikey") String apikey) throws NoOrganizationExceptionFound, NoApiKeyFoundException, IllegalAccessException, InstantiationException {
        MetisKey key = authorizationService.getKeyFromId(apikey);
        if(key!=null) {
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
                return constructModelAndViewForList(key,orgs);
            }
            throw new NoOrganizationExceptionFound("No organization matching the criteria was found");
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
    public DatasetList getDatasetsByOrganization(@ApiParam("id") @PathVariable("id") String id) throws NoOrganizationExceptionFound {
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
    public ModelAndView getOrganizationById(@ApiParam("id") @PathVariable("id") String id, @ApiParam("apikey") @RequestParam("apikey") String apikey) throws NoApiKeyFoundException,NoOrganizationExceptionFound, InstantiationException, IllegalAccessException {
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
    public ModelAndView getOrganizationByOrganizationId(@ApiParam("orgId") @RequestParam(value = "orgId") String id, @RequestParam ("apikey") String apikey) throws NoOrganizationExceptionFound, InstantiationException, IllegalAccessException, NoApiKeyFoundException {
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
    public ModelAndView getOrganizationByOrganizationIdFromCRM(@ApiParam("orgId") @PathVariable(value = "orgId") String id, @RequestParam("apikey")String apikey) throws NoOrganizationExceptionFound, IOException, ParseException, InstantiationException, IllegalAccessException, NoApiKeyFoundException {
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
    public ModelAndView getOrganizationsFromCRM(@RequestParam("apikey")String apikey) throws NoOrganizationExceptionFound, IOException, ParseException, IllegalAccessException, InstantiationException, NoApiKeyFoundException {
        MetisKey key = authorizationService.getKeyFromId(apikey);
        if (key != null) {
            return constructModelAndViewForList(key, organizationService.getAllOrganizationsFromCRM());
        }
        throw new NoApiKeyFoundException(apikey);
    }

    /**
     * Check whether an organization is opted in for using the Image Service of Europeana or not
     * @param id The id of the organization
     * @return true if opted in, false otherwise
     */
    @RequestMapping(value = ORGANIZATION_OPTED_IN, method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ApiOperation(value="Check whether an organization is opted-in for IIIF or not")
    public ModelAndView isOptedIn(@PathVariable("id") String id){
        ModelAndView view = new ModelAndView("json");
        view.addObject("result",organizationService.isOptedInForIIIF(id));
        return view;
    }

    public ModelAndView suggestOrganizations(@PathVariable("suggestTerm") String suggestTerm) throws IOException, SolrServerException {
        ModelAndView view = new ModelAndView("json");
        view.addObject("suggestions",organizationService.suggestOrganizations(suggestTerm));
        return view;
    }

    private ModelAndView constructModelAndViewForList(MetisKey key, List<Organization> orgs) throws InstantiationException, IllegalAccessException {
        if(key.getProfile().equals(Profile.PUBLIC)){
            List<ModelAndView> organizationViews = new ArrayList<>();
            for (Organization org: orgs) {
                organizationViews.add(PublicOrganizationView.generateResponse(org));
            }
            return JsonUtils.toJson(organizationViews);
        } else {
            List<ModelAndView> organizationViews = new ArrayList<>();
            for (Organization org: orgs) {
                organizationViews.add(MetisOrganizationView.generateResponse(org));
            }
            return JsonUtils.toJson(organizationViews);
        }
    }

}
