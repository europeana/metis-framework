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
package eu.europeana.metis.core.rest.client;

import static eu.europeana.metis.RestEndpoints.ORGANIZATIONS;
import static eu.europeana.metis.RestEndpoints.ORGANIZATIONS_COUNTRY_ISOCODE;
import static eu.europeana.metis.RestEndpoints.ORGANIZATIONS_ORGANIZATION_ID;
import static eu.europeana.metis.RestEndpoints.ORGANIZATIONS_ORGANIZATION_ID_DATASETS;
import static eu.europeana.metis.RestEndpoints.ORGANIZATIONS_ROLES;
import static eu.europeana.metis.RestEndpoints.USERBYMAIL;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.core.common.Contact;
import eu.europeana.metis.core.common.OrganizationRole;
import eu.europeana.metis.core.organization.Organization;
import eu.europeana.metis.core.rest.ServerError;
import eu.europeana.metis.core.search.common.OrganizationSearchBean;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


/**
 * Rest Client Organization Management
 * Created by ymamakis on 2/26/16.
 */
public class OrganizationRestClient {

    private RestTemplate template;

    private String apikey;
    private String hostUrl;

    public OrganizationRestClient(String hostUrl,String apikey){
        this(new RestTemplate(), hostUrl, apikey);
    }

    public OrganizationRestClient(RestTemplate restTemplate, String hostUrl,String apikey){
        Validate.notNull(restTemplate, "restTemplate parameter not set");
        template = restTemplate;
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

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(hostUrl + ORGANIZATIONS)
            .queryParam("apikey", apikey);

        ResponseEntity entity = template.exchange(builder.toUriString(), HttpMethod.POST, orgEntity,
                ResponseEntity.class);
        if (!entity.getStatusCode().equals(HttpStatus.CREATED)) {
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

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
            hostUrl + RestEndpoints.resolve(ORGANIZATIONS_ORGANIZATION_ID, org.getOrganizationId()))
            .queryParam("apikey", apikey);

        ResponseEntity entity = template.exchange(builder.toUriString(),
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
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
            hostUrl + RestEndpoints.resolve(ORGANIZATIONS_ORGANIZATION_ID, org.getOrganizationId()))
            .queryParam("apikey", apikey);

        ResponseEntity entity = template.exchange(builder.toUriString(),
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
    public OrganizationListResponse getAllOrganizations(String nextPage) throws ServerException {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(hostUrl + ORGANIZATIONS)
                .queryParam("apikey", apikey)
                .queryParam("nextPage", nextPage);

            OrganizationListResponse orgs =  template.getForObject(builder.toUriString(), OrganizationListResponse.class);
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
    public OrganizationListResponse getAllOrganizationsByIsoCode(String isoCode, String nextPage) throws ServerException {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                hostUrl + RestEndpoints.resolve(ORGANIZATIONS_COUNTRY_ISOCODE,isoCode))
                .queryParam("apikey", apikey)
                .queryParam("nextPage", nextPage);

            OrganizationListResponse orgs =  template.getForObject(builder.toUriString(), OrganizationListResponse.class);
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
    public OrganizationListResponse getAllOrganizationsByRoles(List<OrganizationRole> organizationRoles, String nextPage) throws ServerException {
        try {
            String roleParam = "";
            for(OrganizationRole organizationRole : organizationRoles){
                roleParam += organizationRole.toString().toLowerCase()+",";
            }
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(hostUrl + ORGANIZATIONS_ROLES)
                .queryParam("apikey", apikey)
                .queryParam("nextPage", nextPage)
                .queryParam("organizationRoles", StringUtils.substringBeforeLast(roleParam,","));

            OrganizationListResponse orgs =  template.getForObject( builder.toUriString() , OrganizationListResponse.class);
            return orgs;
        } catch (Exception e) {
            throw new ServerException("Organizations could not be retrieved with error: " + e.getMessage());
        }
    }

    /**
     * Get all the datasets for an organization (OK)
     * @param orgId the datasets of the organization with the specified id
     * @return The List of datasets for the organization
     * @throws ServerException
     */
    public DatasetListResponse getDatasetsForOrganization(String orgId, String nextPage) throws ServerException {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                hostUrl + RestEndpoints.resolve(ORGANIZATIONS_ORGANIZATION_ID_DATASETS,orgId))
                .queryParam("apikey", apikey)
                .queryParam("nextPage", nextPage);

            DatasetListResponse response = template.getForObject(builder.toUriString(), DatasetListResponse.class);
            return response;
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
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                hostUrl + RestEndpoints.resolve(ORGANIZATIONS_ORGANIZATION_ID,id))
                .queryParam("apikey", apikey);
            return template.getForObject(builder.toUriString(), Organization.class);
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
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(hostUrl + ORGANIZATIONS)
                .queryParam("apikey", apikey)
                .queryParam("orgId", orgId);

            return template.getForObject(builder.toUriString(), Organization.class);
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
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                hostUrl + RestEndpoints.resolve(RestEndpoints.ORGANIZATIONS_CRM_ORGANIZATION_ID,id))
                .queryParam("apikey", apikey);

            return template.getForObject(builder.toUriString(), Organization.class);
        } catch (Exception e) {
            throw new ServerException("Organization could not be retrieved with error: " + e.getMessage());
        }
    }

    /**
     * Get all the organizations from Zoho (OK)
     * @return A list of all the organizations from Zoho
     * @throws ServerException
     */
    public OrganizationListResponse getOrganizationsFromCrm(String nextPage) throws ServerException {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                hostUrl + RestEndpoints.ORGANIZATIONS_CRM)
                 .queryParam("apikey", apikey)
                 .queryParam("nextPage", nextPage);

            OrganizationListResponse orgs = template.getForObject(builder.toUriString(), OrganizationListResponse.class);
            return orgs;
        } catch (Exception e) {
            throw new ServerException("Organizations could not be retrieved with error: " + e.getMessage());
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
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                hostUrl + RestEndpoints.resolve(USERBYMAIL, email))
                    .queryParam("apikey", apikey);

            return template.getForObject(builder.toUriString(), Contact.class);
        } catch (Exception e){
            throw new ServerException("User could not be retrieved with error: "+e.getMessage());
        }
    }

//    /**
//     * Check whether an organization has opted in for the Image Service of Europeana
//     * @param id The id of the organization
//     * @return true if opted in false otherwise
//     * @throws ServerException
//     */
//    public boolean isOptedIn(String id) throws ServerException{
//        try {
//            return template.getForEntity(hostUrl + RestEndpoints.resolve(RestEndpoints.ORGANIZATIONS_ORGANIZATION_ID_OPTINIIIF, id), OptedInResponse.class).getBody().isResult();
//        } catch (Exception e){
//            throw new ServerException("Optin could not be retrieved with error: "+e.getMessage());
//        }
//    }

    public List<OrganizationSearchBean> suggestOrganizations(String searchTerm) throws ServerException{
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                hostUrl + RestEndpoints.resolve(RestEndpoints.ORGANIZATIONS_SUGGEST))
                .queryParam("apikey", apikey)
                .queryParam("searchTerm", searchTerm);

            return template.getForObject(builder.toUriString(), Suggestions.class).getSuggestions();
        } catch (Exception e) {
            throw new ServerException("Organization suggestions could not be retrieved with error: " + e.getMessage());
        }
    }

}
