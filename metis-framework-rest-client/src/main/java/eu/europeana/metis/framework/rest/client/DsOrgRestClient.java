package eu.europeana.metis.framework.rest.client;

import eu.europeana.metis.framework.dataset.Dataset;
import eu.europeana.metis.framework.organization.Organization;
import eu.europeana.metis.framework.rest.ServerError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Properties;


/**
 * Rest Client for Dataset and Organization Management
 * Created by ymamakis on 2/26/16.
 */
public class DsOrgRestClient {

    private RestTemplate template = new RestTemplate();

    private String hostUrl;
    public DsOrgRestClient(String hostUrl){
        this.hostUrl = hostUrl;
    }

    /**
     * Create an organization
     *
     * @param org The organization to create
     */
    public void createOrganization(Organization org) throws ServerException {
        ResponseEntity entity = template.postForObject(hostUrl + "/organization", org, ResponseEntity.class);
        if (!entity.getStatusCode().equals(HttpStatus.OK)) {
            throw new ServerException(((ServerError) entity.getBody()).getMessage());
        }
    }

    /**
     * Update an organization
     *
     * @param org The organization to create
     */
    public void updateOrganization(Organization org) throws ServerException {
        HttpEntity<Organization> organizationHttpEntity = new HttpEntity<>(org);
        ResponseEntity entity = template.exchange(hostUrl + "/organization", HttpMethod.PUT, organizationHttpEntity, ResponseEntity.class);
        if (!entity.getStatusCode().equals(HttpStatus.OK)) {
            throw new ServerException(((ServerError) entity.getBody()).getMessage());
        }
    }

    /**
     * Delete an organization
     * @param org
     * @throws ServerException
     */
    public void deleteOrganization(Organization org) throws ServerException {
        HttpEntity<Organization> organizationHttpEntity = new HttpEntity<>(org);
        ResponseEntity entity = template.exchange(hostUrl + "/organization", HttpMethod.DELETE, organizationHttpEntity, ResponseEntity.class);
        if (!entity.getStatusCode().equals(HttpStatus.OK)) {
            throw new ServerException(((ServerError) entity.getBody()).getMessage());
        }
    }

    /**
     * Retrieve all the organizations stored in METIS
     * @return The list of all the organizations stored in METIS
     * @throws ServerException
     */
    public List<Organization> getAllOrganizations() throws ServerException {
        try {
            List<Organization> orgs = (List<Organization>) template.getForEntity(hostUrl + "/organizations", List.class).getBody();
            return orgs;
        } catch (Exception e) {
            throw new ServerException("Organizations could not be retrieved with error: " + e.getMessage());
        }
    }

    /**
     * Get all the datasets for an organization
     * @param id the datasets of the organization with the specified id
     * @return The List of datasets for the organization
     * @throws ServerException
     */
    public List<Dataset> getDatasetsForOrganization(String id) throws ServerException {
        try {
            List<Dataset> datasets = (List<Dataset>) template.getForEntity(hostUrl + "/organization/"+id+"/datasets", List.class).getBody();
            return datasets;
        } catch (Exception e) {
            throw new ServerException("Datasets could not be retrieved with error: " + e.getMessage());
        }
    }

    /**
     * Retrieve an organization by its id (Mongo)
     * @param id The id to search for
     * @return The organization
     * @throws ServerException
     */
    public Organization getOrganizationById(String id) throws ServerException {
        try {
            return template.getForEntity(hostUrl + "/organization/"+id, Organization.class).getBody();
        } catch (Exception e) {
            throw new ServerException("Organization could not be retrieved with error: " + e.getMessage());
        }
    }

    /**
     * Retrieve an organization by its organization id (Zoho id) from the Mongo METIS backend
     * @param orgId The organization id to retrieve
     * @return The organization to retrieve
     * @throws ServerException
     */
    public Organization getOrganizationByOrganizationId(String orgId) throws ServerException {
        try {
            return template.getForEntity(hostUrl + "/organization?orgId="+orgId, Organization.class).getBody();
        } catch (Exception e) {
            throw new ServerException("Organization could not be retrieved with error: " + e.getMessage());
        }
    }

    /**
     * Retrieve an organization from Zoho based on its organization id
     * @param id The id to search on
     * @return The organization from Zoho
     * @throws ServerException
     */
    public Organization getOrganizationFromCrm(String id) throws ServerException {
        try {
            return template.getForEntity(hostUrl + "/organization/crm/"+id, Organization.class).getBody();
        } catch (Exception e) {
            throw new ServerException("Organization could not be retrieved with error: " + e.getMessage());
        }
    }

    /**
     * Get all the organizations from Zoho
     * @return A list of all the organizations from Zoho
     * @throws ServerException
     */
    public List<Organization> getOrganizationsFromCrm() throws ServerException {
        try {
            List<Organization> orgs = (List<Organization>) template.getForEntity(hostUrl + "/organizations/crm", List.class).getBody();
            return orgs;
        } catch (Exception e) {
            throw new ServerException("Organizations could not be retrieved with error: " + e.getMessage());
        }
    }


    /**
     * Create a dataset
     *
     * @param dataset The dataset to create
     */
    public void createDataset(Dataset dataset) throws ServerException {
        ResponseEntity entity = template.postForObject(hostUrl + "/dataset", dataset, ResponseEntity.class);
        if (!entity.getStatusCode().equals(HttpStatus.OK)) {
            throw new ServerException(((ServerError) entity.getBody()).getMessage());
        }
    }

    /**
     * Update a dataset
     *
     * @param dataset The organization to update
     */
    public void updateDataset(Dataset dataset) throws ServerException {
        HttpEntity<Dataset> datasetEntity = new HttpEntity<>(dataset);
        ResponseEntity entity = template.exchange(hostUrl + "/dataset", HttpMethod.PUT, datasetEntity, ResponseEntity.class);
        if (!entity.getStatusCode().equals(HttpStatus.OK)) {
            throw new ServerException(((ServerError) entity.getBody()).getMessage());
        }
    }

    /**
     * Delete a dataset
     * @param dataset the dataset to delete
     * @throws ServerException
     */
    public void deleteDataset(Dataset dataset) throws ServerException {
        HttpEntity<Dataset> datasetEntity = new HttpEntity<>(dataset);
        ResponseEntity entity = template.exchange(hostUrl + "/organization", HttpMethod.DELETE, datasetEntity, ResponseEntity.class);
        if (!entity.getStatusCode().equals(HttpStatus.OK)) {
            throw new ServerException(((ServerError) entity.getBody()).getMessage());
        }
    }

    /**
     * Get a dataset by its name
     * @param name The name of the dataset
     * @return The dataset with the given name
     * @throws ServerException
     */
    public Dataset getDatasetByName(String name) throws ServerException{
        try {
            return template.getForEntity(hostUrl + "/dataset/" + name, Dataset.class).getBody();
        } catch (Exception e){
            throw new ServerException("Dataset could not be retrieved with error: "+e.getMessage());
        }
    }
}
