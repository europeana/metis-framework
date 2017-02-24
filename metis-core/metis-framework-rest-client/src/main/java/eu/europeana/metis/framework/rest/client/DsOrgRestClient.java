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
package eu.europeana.metis.framework.rest.client;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.framework.common.Role;
import eu.europeana.metis.framework.dto.OrgDatasetDTO;
import eu.europeana.metis.framework.common.Contact;
import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.dataset.DatasetList;
import eu.europeana.metis.framework.organization.Organization;
import eu.europeana.metis.framework.organization.OrganizationList;
import eu.europeana.metis.framework.rest.ServerError;
import eu.europeana.metis.search.common.OrganizationSearchBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static eu.europeana.metis.RestEndpoints.*;


/**
 * Rest Client for Dataset and Organization Management
 * Created by ymamakis on 2/26/16.
 */
public class DsOrgRestClient {

    private RestTemplate template = new RestTemplate();

    private String apikey;
    private String hostUrl;
    public DsOrgRestClient(String hostUrl,String apikey){
        template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        this.hostUrl = hostUrl;
        this.apikey = apikey;
    }

    /**
     * Create an organization (OK)
     * @param org The organization to create
     */
    public void createOrganization(Organization org) throws ServerException {


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Organization> orgEntity = new HttpEntity<>(org,headers);
        ResponseEntity entity = template.exchange(hostUrl +
                RestEndpoints.resolve(ORGANIZATION,apikey),HttpMethod.POST,orgEntity,
                ResponseEntity.class);
        if (!entity.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
            throw new ServerException(((ServerError)entity.getBody()).getMessage());
        }
    }

    /**
     * Update an organization (OK)
     * @param org The organization to create
     */
    public void updateOrganization(Organization org) throws ServerException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Organization> orgEntity = new HttpEntity<>(org,headers);
        ResponseEntity entity =template.exchange(hostUrl + RestEndpoints.resolve(ORGANIZATION,apikey),
                HttpMethod.PUT, orgEntity, ResponseEntity.class);
        if (!entity.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
            throw new ServerException(((ServerError)entity.getBody()).getMessage());
        }
    }

    /**
     * Delete an organization (OK)
     * @param org
     * @throws ServerException
     */
    public void deleteOrganization(Organization org) throws ServerException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Organization> organizationHttpEntity = new HttpEntity<>(org,headers);
        ResponseEntity entity = template.exchange(hostUrl + RestEndpoints.resolve(ORGANIZATION,apikey),
                HttpMethod.DELETE, organizationHttpEntity, ResponseEntity.class);
        if (!entity.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
            throw new ServerException(((ServerError) entity.getBody()).getMessage());
        }
    }

    /**
     * Retrieve all the organizations stored in METIS (OK)
     * @return The list of all the organizations stored in METIS
     * @throws ServerException
     */
    public List<Organization> getAllOrganizations() throws ServerException {
        try {

            List<Organization> orgs =  template.getForObject(hostUrl + ORGANIZATIONS+"?apikey="+apikey, OrganizationListResponse.class).getResults();
            return orgs;
        } catch (Exception e) {
            throw new ServerException("Organizations could not be retrieved with error: " + e.getMessage());
        }
    }

    /**
     * Retrieve all the organizations stored in METIS (OK)
     * @return The list of all the organizations stored in METIS
     * @throws ServerException
     */
    public List<Organization> getAllOrganizationsByIsoCode(String isoCode) throws ServerException {
        try {

            List<Organization> orgs =  template.getForObject(hostUrl + ORGANIZATIONS_ISOCODE+"?apikey="+apikey+"&isoCode="+isoCode, OrganizationListResponse.class).getResults();
            return orgs;
        } catch (Exception e) {
            throw new ServerException("Organizations could not be retrieved with error: " + e.getMessage());
        }
    }

    /**
     * Retrieve all the organizations stored in METIS (OK)
     * @return The list of all the organizations stored in METIS
     * @throws ServerException
     */
    public List<Organization> getAllOrganizationsByRoles(List<Role> roles) throws ServerException {
        try {
            String roleParam="role=";
            for(Role role: roles){
                roleParam+=role.toString().toLowerCase()+",";
            }

            List<Organization> orgs =  template.getForObject(hostUrl + ORGANIZATIONS_ROLES+"?apikey="+apikey+"&"+StringUtils.substringBeforeLast(roleParam,","), OrganizationListResponse.class).getResults();
            return orgs;
        } catch (Exception e) {
            throw new ServerException("Organizations could not be retrieved with error: " + e.getMessage());
        }
    }

    /**
     * Get all the datasets for an organization (OK)
     * @param id the datasets of the organization with the specified id
     * @return The List of datasets for the organization
     * @throws ServerException
     */
    public List<Dataset> getDatasetsForOrganization(String id) throws ServerException {
        try {
            List<Dataset> datasets = template.getForObject(hostUrl + RestEndpoints.resolve(ORGANIZATION_ID_DATASETS,id), DatasetList.class).getDatasetList();
            return datasets;
        } catch (Exception e) {
            throw new ServerException("Datasets could not be retrieved with error: " + e.getMessage());
        }
    }

    /**
     * Retrieve an organization by its id (Mongo) (OK)
     * @param id The id to search for
     * @return The organization
     * @throws ServerException
     */
    public Organization getOrganizationById(String id) throws ServerException {
        try {
            return template.getForObject(hostUrl + RestEndpoints.resolve(ORGANIZATION_ID,id)+"apikey="+apikey,
                    Organization.class);
        } catch (Exception e) {
            throw new ServerException("Organization could not be retrieved with error: " + e.getMessage());
        }
    }

    /**
     * Retrieve an organization by its organization id (Zoho id) from the Mongo METIS backend (OK)
     * @param orgId The organization id to retrieve
     * @return The organization to retrieve
     * @throws ServerException
     */
    public Organization getOrganizationByOrganizationId(String orgId) throws ServerException {
        try {
            return template.getForObject(hostUrl + ORGANIZATION+ "?orgId="+orgId+"&apikey="+apikey, Organization.class);
        } catch (Exception e) {
            throw new ServerException("Organization could not be retrieved with error: " + e.getMessage());
        }
    }

    /**
     * Retrieve an organization from Zoho based on its organization id (OK)
     * @param id The id to search on
     * @return The organization from Zoho
     * @throws ServerException
     */
    public Organization getOrganizationFromCrm(String id) throws ServerException {
        try {
            return template.getForObject(hostUrl + RestEndpoints.resolve(CRM_ORGANIZATION_ID,id)+"?apikey="+apikey, Organization.class);
        } catch (Exception e) {
            throw new ServerException("Organization could not be retrieved with error: " + e.getMessage());
        }
    }

    /**
     * Get all the organizations from Zoho (OK)
     * @return A list of all the organizations from Zoho
     * @throws ServerException
     */
    public List<Organization> getOrganizationsFromCrm() throws ServerException {
        try {

            List<Organization> orgs = template.getForObject(hostUrl + CRM_ORGANIZATIONS, OrganizationListResponse.class).getResults();
            return orgs;
        } catch (Exception e) {
            throw new ServerException("Organizations could not be retrieved with error: " + e.getMessage());
        }
    }


    /**
     * Create a dataset (OK)
     *
     * @param dataset The dataset to create
     */
    public void createDataset(Organization org ,Dataset dataset) throws ServerException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        OrgDatasetDTO dto = new OrgDatasetDTO();
        dto.setOrganization(org);
        dto.setDataset(dataset);
        HttpEntity<OrgDatasetDTO> datasetEntity = new HttpEntity<>(dto,headers);

        ResponseEntity entity = template.exchange(hostUrl + DATASET, HttpMethod.POST, datasetEntity, ResponseEntity.class);
        if (!entity.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
            throw new ServerException(((ServerError) entity.getBody()).getMessage());
        }
    }

    /**
     * Update a dataset (OK)
     *
     * @param dataset The organization to update
     */
    public void updateDataset(Dataset dataset) throws ServerException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Dataset> datasetEntity = new HttpEntity<>(dataset,headers);
        ResponseEntity entity = template.exchange(hostUrl + DATASET, HttpMethod.PUT, datasetEntity, ResponseEntity.class);
        if (!entity.getStatusCode().equals(HttpStatus.OK)) {
            throw new ServerException(((ServerError) entity.getBody()).getMessage());
        }
    }

    /**
     * Delete a dataset (OK)
     * @param dataset the dataset to delete
     * @throws ServerException
     */
    public void deleteDataset(Organization org,Dataset dataset) throws ServerException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        OrgDatasetDTO dto = new OrgDatasetDTO();
        dto.setOrganization(org);
        dto.setDataset(dataset);
        HttpEntity<OrgDatasetDTO> datasetEntity = new HttpEntity<>(dto,headers);
        ResponseEntity entity = template.exchange(hostUrl + DATASET, HttpMethod.DELETE, datasetEntity, ResponseEntity.class);
        if (!entity.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
            throw new ServerException(((ServerError) entity.getBody()).getMessage());
        }
    }

    /**
     * Get a dataset by its name (OK)
     * @param name The name of the dataset
     * @return The dataset with the given name
     * @throws ServerException
     */
    public Dataset getDatasetByName(String name) throws ServerException{
        try {
            return template.getForEntity(hostUrl + RestEndpoints.resolve(DATASET_RETRIEVE, name), Dataset.class).getBody();
        } catch (Exception e){
            throw new ServerException("Dataset could not be retrieved with error: "+e.getMessage());
        }
    }


    /**
     * Get a user by email from Zoho
     * @param email The email of the user to search for
     * @return The user details from Zoho
     * @throws ServerException
     */
    public Contact getUserByEmail(String email) throws ServerException{
        try{
            return template.getForEntity(hostUrl+RestEndpoints.resolve(USERBYMAIL,email),Contact.class).getBody();
        } catch (Exception e){
            throw new ServerException("User could not be retrieved with error: "+e.getMessage());
        }

    }

    /**
     * Check whether an organization has opted in for the Image Service of Europeana
     * @param id The id of the organization
     * @return true if opted in false otherwise
     * @throws ServerException
     */
    public boolean isOptedIn(String id) throws ServerException{
        try {
            return template.getForEntity(hostUrl + RestEndpoints.resolve(RestEndpoints.ORGANIZATION_OPTED_IN, id), OptedInResponse.class).getBody().isResult();
        } catch (Exception e){
            throw new ServerException("Optin could not be retrieved with error: "+e.getMessage());
        }
    }

    public List<OrganizationSearchBean> suggestOrganizations(String term) throws ServerException{
        try {
            return template.getForObject(hostUrl + RestEndpoints.resolve(RestEndpoints.ORGANIZATION_SUGGEST,term), List.class);
        } catch (Exception e) {
            throw new ServerException("Organization suggestions could not be retrieved with error: " + e.getMessage());
        }
    }

}
