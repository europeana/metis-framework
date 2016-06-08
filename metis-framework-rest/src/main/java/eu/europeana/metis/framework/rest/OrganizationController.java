package eu.europeana.metis.framework.rest;

import eu.europeana.metis.framework.dataset.DatasetList;
import eu.europeana.metis.framework.exceptions.NoOrganizationExceptionFound;
import eu.europeana.metis.framework.organization.Organization;
import eu.europeana.metis.framework.organization.OrganizationList;
import eu.europeana.metis.framework.service.OrganizationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;

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
    @RequestMapping(value = "/organization", method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Create an organization in METIS")
    public void createOrganization(@ApiParam @RequestBody Organization organization) {
        organizationService.createOrganization(organization);
    }

    /**
     * Delete an organization
     * @param organization The organization to delete
     */
    @RequestMapping(value = "/organization", method = RequestMethod.DELETE, consumes = "application/json")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Delete an organization")
    public void deleteOrganization(@ApiParam @RequestBody Organization organization) {
        organizationService.deleteOrganization(organization);
    }

    /**
     * Update an organization
     * @param organization The organization to update
     */
    @RequestMapping(value = "/organization", method = RequestMethod.PUT, consumes = "application/json")
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
    @RequestMapping(value = "/organizations", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ApiOperation(value = "Retrieve all the organizations from METIS", response = OrganizationList.class)
    public OrganizationList getAllOrganizations() throws NoOrganizationExceptionFound {
        OrganizationList list = new OrganizationList();
        list.setOrganizations(organizationService.getAllOrganizations());
        return list;
    }

    /**
     * Get allt he datasets for an organization
     *
     * @param id The id of the organization
     * @return The datasets of the organization with the provided it
     */
    @RequestMapping(value = "/organization/{id}/datasets", method = RequestMethod.GET, produces = "application/json")
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
    @RequestMapping(value = "/organization/{id}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ApiOperation(value = "Get an organization by id", response = Organization.class)
    public Organization getOrganizationById(@ApiParam("id") @PathVariable("id") String id) throws NoOrganizationExceptionFound {
        return organizationService.getOrganizationById(id);
    }

    /**
     * Retrieve the organization with a specific organization id (datasets are transient)
     *
     * @param id The organization id of the organization to retrieve
     * @return The organization with the specified id
     */
    @RequestMapping(value = "/organization", method = RequestMethod.GET, produces = "application/json")
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
    @RequestMapping(value = "/crm/organization/{orgId}", method = RequestMethod.GET, produces = "application/json")
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
    @RequestMapping(value = "/crm/organizations", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ApiOperation(value = "Retrieve all the organizations from CRM", response = OrganizationList.class)
    public OrganizationList getOrganizationsFromCRM() throws NoOrganizationExceptionFound, IOException, ParseException {

        OrganizationList list = new OrganizationList();
        list.setOrganizations(organizationService.getAllOrganizationsFromCRM());
        return list;
    }

}
