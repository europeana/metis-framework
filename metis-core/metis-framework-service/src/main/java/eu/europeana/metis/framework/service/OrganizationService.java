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
package eu.europeana.metis.framework.service;

import eu.europeana.metis.framework.common.Country;
import eu.europeana.metis.framework.common.Role;
import eu.europeana.metis.framework.dao.OrganizationDao;
import eu.europeana.metis.framework.dao.ZohoClient;
import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.exceptions.NoOrganizationExceptionFound;
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
    private ZohoClient restClient;

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
    public List<Organization> getAllOrganizations() throws NoOrganizationExceptionFound{
        List<Organization> organizations = orgDao.getAll();
        if(organizations == null||organizations.size()==0){
            throw new NoOrganizationExceptionFound("No organization found in METIS");
        }
        return organizations;
    }

    public List<Organization> getAllProviders(Role... roles){
        return orgDao.getAllProviders(roles);
    }
    /**
     * List all the organizations
     * @return Retrieve all the organizations
     */
    public List<Organization> getAllOrganizationsByCountry(Country country) throws NoOrganizationExceptionFound{
        List<Organization> organizations = orgDao.getAllByCountry(country);
        if(organizations == null||organizations.size()==0){
            throw new NoOrganizationExceptionFound("No organization found in METIS");
        }
        return organizations;
    }
    /**
     * Get all the datasets of an organization
     * @param orgId The organization id to search on
     * @return The datasets for that organization
     */
    public List<Dataset> getDatasetsByOrganization(String orgId) throws NoOrganizationExceptionFound{
        return orgDao.getAllDatasetsByOrganization(orgId);
    }

    /**
     * Get an organization by id
     * @param id The id to search for
     * @return The organization with the requested id
     */
    public Organization getOrganizationById(String id)throws NoOrganizationExceptionFound{
        Organization organization = orgDao.getById(id);
        if(organization == null){
            throw new NoOrganizationExceptionFound("No organization found with id: "+id+" in METIS");
        }
        return  organization;
    }

    /**
     * Get an organization by its organization Id
     * @param id The organization id to search on
     * @return The organization with that organization id
     */
    public Organization getOrganizationByOrganizationId(String id) throws NoOrganizationExceptionFound{

        Organization organization= orgDao.getByOrganizationId(id);
        if(organization == null){
            throw new NoOrganizationExceptionFound("No organization found with organization id: "+id+" in METIS");
        }
        return  organization;
    }

    /**
     * Get an organization from CRM
     * @param id The organization to retrieve from CRM
     * @return The organization as its kept in CRM
     * @throws ParseException
     * @throws IOException
     */
    public Organization getOrganizationByIdFromCRM(String id) throws ParseException,IOException, NoOrganizationExceptionFound{
        Organization organization= restClient.getOrganizationById(id);
        if(organization == null){
            throw new NoOrganizationExceptionFound("No organization found with organization id: "+id+" in CRM");
        }
        return  organization;
    }

    /**
     * Get all organizations from CRM
     * @return GEt the list of all the organizations for CRM
     * @throws ParseException
     * @throws IOException
     */
    public List<Organization> getAllOrganizationsFromCRM() throws ParseException,IOException,NoOrganizationExceptionFound {
        List<Organization> organizations= restClient.getAllOrganizations();
        if(organizations == null||organizations.size()==0){
            throw new NoOrganizationExceptionFound("No organization found in CRM");
        }
        return organizations;
    }

    /**
     * Check whether an organization has opted in or not
     * @param organizationId The organization id to check for
     * @return true if opted in false otherwise
     */
    public boolean isOptedInForIIIF(String organizationId){
        Organization org = orgDao.getById(organizationId);
        return org != null && org.isOptInIIIF();
    }
}
