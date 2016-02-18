package eu.europeana.metis.framework.rest;

import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.organization.Organization;
import eu.europeana.metis.framework.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The organization controller
 * Created by ymamakis on 2/18/16.
 */
@Controller
public class OrganizationController {
    @Autowired
    private OrganizationService organizationService;

    /**
     * Create an organization
     * @param organization The organization to create
     */
    @RequestMapping(value = "/organization",method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    public void createOrganization(@RequestBody Organization organization){
        organizationService.createOrganization(organization);
    }

    /**
     * Delete an organization
     * @param organization The organization to delete
     */
    @RequestMapping(value = "/organization",method = RequestMethod.DELETE, consumes = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    public void deleteOrganization(@RequestBody Organization organization){
        organizationService.deleteOrganization(organization);
    }

    /**
     * Update an organization
     * @param organization The organization to update
     */
    @RequestMapping(value = "/organization",method = RequestMethod.PUT, consumes = "application/json")
    @ResponseStatus(value = HttpStatus.OK)
    public void updateOrganization(@RequestBody Organization organization){
        organizationService.updateOrganization(organization);

    }

    /**
     * Get all the organizations
     * @return All the registered organizations in METIS
     */
    @RequestMapping(value = "/organizations",method = RequestMethod.GET, produces = "application/json",consumes = "application/json")
    @ResponseBody
    public List<Organization> getAllOrganizations(){
        return organizationService.getAllOrganizations();
    }

    /**
     * Get allt he datasets for an organization
     * @param id The id of the organization
     * @return The datasets of the organization with the provided it
     */
    @RequestMapping(value = "/organization/{id}/datasets",method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<Dataset> getDatasetsByOrganization(@PathVariable String id){
        return organizationService.getDatasetsByOrganization(id);
    }

    /**
     * Retrieve the organization with a specific id (datasets are transient)
     * @param id The id of the organization to retrieve
     * @return The organization with the specified id
     */
    @RequestMapping(value = "/organization/{id}",method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Organization getOrganizationById(@PathVariable String id){
        return organizationService.getOrganizationById(id);
    }

    /**
     * Retrieve the organization with a specific organization id (datasets are transient)
     * @param id The organization id of the organization to retrieve
     * @return The organization with the specified id
     */
    @RequestMapping(value = "/organization",method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Organization getOrganizationByOrganizationId(@RequestParam(value = "orgId") String id){
        return organizationService.getOrganizationByOrganizationId(id);
    }
}
