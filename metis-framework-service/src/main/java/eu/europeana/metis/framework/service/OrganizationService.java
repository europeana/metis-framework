package eu.europeana.metis.framework.service;

import eu.europeana.metis.framework.dao.OrganizationDao;
import eu.europeana.metis.framework.dao.ZohoRestClient;
import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.organization.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

/**
 * Organization service
 * Created by ymamakis on 2/17/16.
 */
@Component
public class OrganizationService {

    @Autowired
    private OrganizationDao orgDao;

    @Autowired
    private ZohoRestClient restClient;

    /**
     * Create an organization
     * @param org The organization to create
     */
    public void createOrganization(Organization org){
        orgDao.create(org);
    }

    /**
     * Update an organization
     * @param org The organization to update
     */
    public void updateOrganization(Organization org){
        orgDao.update(org);
    }

    /**
     * Delete an organization
     * @param org The organization to delete
     */
    public void deleteOrganization(Organization org){
        orgDao.delete(org);
    }

    /**
     * List all the organizations
     * @return Retrieve all the organizations
     */
    public List<Organization> getAllOrganizations(){
        return orgDao.getAll();
    }

    /**
     * Get all the datasets of an organization
     * @param orgId The organization id to search on
     * @return The datasets for that organization
     */
    public List<Dataset> getDatasetsByOrganization(String orgId){
        return orgDao.getAllDatasetsByOrganization(orgId);
    }

    /**
     * Get an organization by id
     * @param id The id to search for
     * @return The organization with the requested id
     */
    public Organization getOrganizationById(String id){
        return  orgDao.getById(id);
    }

    /**
     * Get an organization by its organization Id
     * @param id The organization id to search on
     * @return The organization with that organization id
     */
    public Organization getOrganizationByOrganizationId(String id){
        return  orgDao.getByOrganizationId(id);
    }

    /**
     * Get an organization from CRM
     * @param id The organization to retrieve from CRM
     * @return The organization as its kept in CRM
     * @throws ParseException
     * @throws IOException
     */
    public Organization getOrganizationByIdFromCRM(String id) throws ParseException,IOException{
        return restClient.getOrganizationById(id);
    }

    public List<Organization> getAllOrganizationsFromCRM() throws ParseException,IOException{
        return restClient.getAllOrganizations();
    }
}
