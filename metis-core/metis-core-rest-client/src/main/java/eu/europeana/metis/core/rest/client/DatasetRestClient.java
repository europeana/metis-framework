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

import static eu.europeana.metis.RestEndpoints.DATASETS;
import static eu.europeana.metis.RestEndpoints.DATASETS_DATAPROVIDER;
import static eu.europeana.metis.RestEndpoints.DATASETS_DATASETNAME;
import static eu.europeana.metis.RestEndpoints.DATASETS_DATASETNAME_UPDATENAME;
import static eu.europeana.metis.RestEndpoints.ORGANIZATIONS;
import static eu.europeana.metis.RestEndpoints.ORGANIZATIONS_COUNTRY_ISOCODE;
import static eu.europeana.metis.RestEndpoints.ORGANIZATIONS_ORGANIZATION_ID_DATASETS;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.core.dataset.Dataset;
import eu.europeana.metis.core.rest.ResponseListWrapper;
import eu.europeana.metis.core.rest.ServerError;
import java.util.List;
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
 * Rest Client for Dataset Management
 * Created by ymamakis on 2/26/16.
 */

public class DatasetRestClient {

    private RestTemplate template;

    private String apikey;
    private String hostUrl;

    public DatasetRestClient(String hostUrl,String apikey){
        this(new RestTemplate(), hostUrl, apikey);
    }

    public DatasetRestClient(RestTemplate restTemplate, String hostUrl, String apikey){
        Validate.notNull(restTemplate, "restTemplate parameter not set");
        template = restTemplate;
        template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        this.hostUrl = hostUrl;
        this.apikey = apikey;
    }

    /**
     * Create a dataset for an organization
     * @param dataset
     * @param organizationId id of the organization
     */
    public void createDatasetForOrganization(Dataset dataset, String organizationId)
        throws ServerException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Dataset> datasetEntity = new HttpEntity<>(dataset,headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(hostUrl + DATASETS)
            .queryParam("apikey", apikey)
            .queryParam("organizationId", organizationId);

        ResponseEntity entity = template.exchange(builder.toUriString(), HttpMethod.POST, datasetEntity, ResponseEntity.class);

        if (!entity.getStatusCode().equals(HttpStatus.CREATED)) {
            throw new ServerException(((ServerError) entity.getBody()).getMessage());
        }
    }


    public void updateDataset(Dataset dataset, String datasetName) throws ServerException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Dataset> datasetEntity = new HttpEntity<>(dataset,headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
            hostUrl + RestEndpoints.resolve(DATASETS_DATASETNAME, datasetName))
            .queryParam("apikey", apikey);

        ResponseEntity entity = template.exchange(builder.toUriString(), HttpMethod.PUT, datasetEntity, ResponseEntity.class);
        if (!entity.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
            throw new ServerException(((ServerError) entity.getBody()).getMessage());
        }
    }
    
    /**
     * Update a dataset (OK)
     *
     * @param dataset The dataset to update
     */
    public void updateDataset(Dataset dataset) throws ServerException {
        updateDataset(dataset, dataset.getDatasetName());
    }

    public void updateDatasetName(String datasetName, String newDatasetName)
        throws ServerException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> emptyEntity = new HttpEntity<>(null,headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
            hostUrl + RestEndpoints.resolve(DATASETS_DATASETNAME_UPDATENAME, datasetName))
            .queryParam("apikey", apikey)
            .queryParam("newDatasetName", newDatasetName);

        ResponseEntity entity = template.exchange(builder.toUriString(), HttpMethod.PUT, emptyEntity, ResponseEntity.class);
        if (!entity.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
            throw new ServerException(((ServerError) entity.getBody()).getMessage());
        }
    }

    public void deleteDataset(String datasetName) throws ServerException {
        HttpHeaders headers = new HttpHeaders();
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
            hostUrl + RestEndpoints.resolve(DATASETS_DATASETNAME, datasetName))
            .queryParam("apikey", apikey);
        
        ResponseEntity entity = template.exchange(builder.toUriString(), HttpMethod.DELETE, null, ResponseEntity.class);
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
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                hostUrl + RestEndpoints.resolve(DATASETS_DATASETNAME,name))
                .queryParam("apikey", apikey);

            return template.getForObject(builder.toUriString(), Dataset.class);
        } catch (Exception e){
            throw new ServerException("Dataset could not be retrieved with error: "+e.getMessage());
        }
    }


    public DatasetListResponse getAllDatasetsByDataProvider(String dataProvider, String nextPage)
        throws ServerException {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(
                hostUrl + RestEndpoints.resolve(DATASETS_DATAPROVIDER,dataProvider))
                .queryParam("apikey", apikey)
                .queryParam("nextPage", nextPage);

            return template.getForObject(builder.toUriString(), DatasetListResponse.class);
        } catch (Exception e){
            throw new ServerException("Datasets could not be retrieved with error: "+e.getMessage());
        }
    }


//    /**
//     * Create a dataset (OK)
//     *
//     * @param dataset The dataset to create
//     */
//    public void createDataset(Organization org ,Dataset dataset) throws ServerException {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        OrgDatasetDTO dto = new OrgDatasetDTO();
//        dto.setOrganization(org);
//        dto.setDataset(dataset);
//        HttpEntity<OrgDatasetDTO> datasetEntity = new HttpEntity<>(dto,headers);
//
//        ResponseEntity entity = template.exchange(hostUrl + DATASETS, HttpMethod.POST, datasetEntity, ResponseEntity.class);
//        if (!entity.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
//            throw new ServerException(((ServerError) entity.getBody()).getMessage());
//        }
//    }



//    /**
//     * Delete a dataset (OK)
//     * @param dataset the dataset to delete
//     * @throws ServerException
//     */
//    public void deleteDataset(Organization org,Dataset dataset) throws ServerException {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        OrgDatasetDTO dto = new OrgDatasetDTO();
//        dto.setOrganization(org);
//        dto.setDataset(dataset);
//        HttpEntity<OrgDatasetDTO> datasetEntity = new HttpEntity<>(dto,headers);
//        ResponseEntity entity = template.exchange(hostUrl + DATASETS, HttpMethod.DELETE, datasetEntity, ResponseEntity.class);
//        if (!entity.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
//            throw new ServerException(((ServerError) entity.getBody()).getMessage());
//        }
//    }



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
}
