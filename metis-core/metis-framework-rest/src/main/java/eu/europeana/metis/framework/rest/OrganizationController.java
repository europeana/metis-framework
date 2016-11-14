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

import eu.europeana.metis.framework.dataset.DatasetList;
import eu.europeana.metis.framework.exceptions.NoOrganizationExceptionFound;
import eu.europeana.metis.framework.organization.Organization;
import eu.europeana.metis.framework.organization.OrganizationList;
import eu.europeana.metis.framework.rest.utils.JsonUtils;
import eu.europeana.metis.framework.service.OrganizationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.text.ParseException;

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

    /**
     * Create an organization
     * @param organization The organization to create
     */
    @RequestMapping(value = ORGANIZATION, method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Create an organization in METIS")
    public void createOrganization(@ApiParam @RequestBody Organization organization) {
        organizationService.createOrganization(organization);
    }

    /**
     * Delete an organization
     * @param organization The organization to delete
     */
    @RequestMapping(value = ORGANIZATION, method = RequestMethod.DELETE, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Delete an organization")
    public void deleteOrganization(@ApiParam @RequestBody Organization organization) {
        organizationService.deleteOrganization(organization);
    }

    /**
     * Update an organization
     * @param organization The organization to update
     */
    @RequestMapping(value = ORGANIZATION, method = RequestMethod.PUT, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Update an organization")
    public void updateOrganization(@RequestBody Organization organization) {
        organizationService.updateOrganization(organization);
    }

    /**
     * Get all the organizations
     *
     * @return All the registered organizations in METIS
     */
    @RequestMapping(value = ORGANIZATIONS, method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ApiOperation(value = "Retrieve all the organizations from METIS", response = OrganizationList.class)
    public OrganizationList getAllOrganizations() throws NoOrganizationExceptionFound {
        OrganizationList list = new OrganizationList();
        list.setOrganizations(organizationService.getAllOrganizations());
        return list;
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
    public ModelAndView getOrganizationById(@ApiParam("id") @PathVariable("id") String id,@ApiParam("profile")@RequestParam("profile")String profile) throws NoOrganizationExceptionFound {
        return JsonUtils.toJson(organizationService.getOrganizationById(id));
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
    public Organization getOrganizationByOrganizationId(@ApiParam("orgId") @RequestParam(value = "orgId") String id) throws NoOrganizationExceptionFound {
        return organizationService.getOrganizationByOrganizationId(id);
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
    public Organization getOrganizationByOrganizationIdFromCRM(@ApiParam("orgId") @PathVariable(value = "orgId") String id) throws NoOrganizationExceptionFound, IOException, ParseException {
        Organization org = organizationService.getOrganizationByIdFromCRM(id);
        return org;
    }

    /**
     * Retrieve all the organizations from CRM
     *
     * @return The organization with the specified id
     */
    @RequestMapping(value = CRM_ORGANIZATIONS, method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ApiOperation(value = "Retrieve all the organizations from CRM", response = OrganizationList.class)
    public OrganizationList getOrganizationsFromCRM() throws NoOrganizationExceptionFound, IOException, ParseException {

        OrganizationList list = new OrganizationList();
        list.setOrganizations(organizationService.getAllOrganizationsFromCRM());
        return list;
    }

}
