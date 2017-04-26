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

import eu.europeana.metis.framework.common.AltLabel;
import eu.europeana.metis.framework.common.Country;
import eu.europeana.metis.framework.common.PrefLabel;
import eu.europeana.metis.framework.common.Role;
import eu.europeana.metis.framework.dao.OrganizationDao;
import eu.europeana.metis.framework.dao.ZohoClient;
import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.exceptions.NoOrganizationExceptionFound;
import eu.europeana.metis.framework.organization.Organization;
import eu.europeana.metis.search.common.OrganizationSearchBean;
import eu.europeana.metis.search.service.MetisSearchService;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
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

    @Autowired
    private MetisSearchService searchService;

    /**
     * Create an organization
     * @param org The organization to create
     */
    public void createOrganization(Organization org) throws IOException, SolrServerException {
        saveInSolr(org);
        orgDao.create(org);
    }

    private void saveInSolr(Organization org) throws IOException, SolrServerException {
        String id = org.getId().toString();
        String englabel = org.getName();
        List<String> restlabel = new ArrayList<>();
        restlabel.add(englabel);
        if(org.getPrefLabel()!=null) {
            for (PrefLabel label : org.getPrefLabel()) {
                restlabel.add(label.getLabel());
            }
        }
        if(org.getAltLabel()!=null){
            for(AltLabel label:org.getAltLabel()){
                restlabel.add(label.getLabel());
            }
        }

        searchService.addOrganizationForSearch(id,englabel,restlabel);
    }

    /**
     * Update an organization
     * @param org The organization to update
     */
    public void updateOrganization(Organization org) throws SolrServerException,IOException{

        orgDao.update(org);
        saveInSolr(org);
    }

    /**
     * Delete an organization
     * @param org The organization to delete
     */
    public void deleteOrganization(Organization org) throws IOException, SolrServerException {

        orgDao.delete(org);
        deleteFromSolr(org);
    }

    private void deleteFromSolr(Organization org) throws IOException, SolrServerException {
        searchService.deleteFromSearch(org.getId().toString());
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

    /**
     * Return organizations based on a specific search term
     * @param searchTerm The search term
     * @return The list of organizations that correspond to the term.
     *          For performance reasons only the id and the english name are returned
     * @throws IOException
     * @throws SolrServerException
     */
    public List<OrganizationSearchBean> suggestOrganizations(String searchTerm) throws IOException, SolrServerException {
        return searchService.getSuggestions(searchTerm);
    }

    /**
     * Get the organizations refered to by a dataset
     * @param datasetId The dataset Id to search for
     * @param providerId The ddata provider for this dataset <code>{@link Dataset#dataProvider}</code>
     * @return
     */
    public List<Organization> getByDatasetId(String datasetId,String providerId){
        return orgDao.getAllOrganizationsFromDataset(datasetId, providerId);
    }
}
