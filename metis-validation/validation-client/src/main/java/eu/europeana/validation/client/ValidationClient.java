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
package eu.europeana.validation.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.validation.model.Record;
import eu.europeana.validation.model.ValidationResult;
import eu.europeana.validation.model.ValidationResultList;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Rest client for the Schema validation REST API
 * Created by ymamakis on 8/1/16.
 */
public class ValidationClient {
    private RestTemplate template = new RestTemplate();
    private String validationEndpoint;
    public ValidationClient(){
        Properties props = new Properties();
        try {
            template.setErrorHandler(new ValidationResponseHandler());
            props.load(this.getClass().getClassLoader().getResourceAsStream("validation.properties"));
            validationEndpoint = props.getProperty("validation.endpoint");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ValidationClient(String validationEndpoint){
        this.validationEndpoint = validationEndpoint;
        template.setErrorHandler(new ValidationResponseHandler());
    }

    /**
     * Validate a single record against a schema
     * @param schemaName The schema name to validate the record against
     * @param record The record to validate
     * @param version The version of the schema (can be null)
     * @return The result of single record validation
     */
    public ValidationResult validateRecord(String schemaName, String record, String version){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Record record1  = new Record();
        record1.setRecord(record);
        HttpEntity<Record> entity = new HttpEntity<>(record1,headers);
        //String response = template.postForEntity(validationEndpoint+ RestEndpoints.resolve(RestEndpoints.SCHEMA_VALIDATE,schemaName,version),
        //        entity,String.class).getBody();

        return template.postForEntity(validationEndpoint+ RestEndpoints.resolve(RestEndpoints.SCHEMA_VALIDATE,schemaName,version),
                entity,ValidationResult.class).getBody();
    }

    /**
     * Validate list of records
     * @param schemaName The schema name to validate the records against
     * @param records The records to validate
     * @param version The version of the schema (can be null)
     * @return A list of validation results
     */
    public ValidationResultList validateRecords(String schemaName, List<String> records, String version){
        List<Record> records1 = new ArrayList<>();
        for(String rec:records){
            Record record = new Record();
            record.setRecord( rec);
            records1.add(record);
        }
        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        try {
            params.add("records",new ObjectMapper().writeValueAsString(records1));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return template.postForObject(validationEndpoint+ RestEndpoints.resolve(RestEndpoints.SCHEMA_RECORDS_BATCH_VALIDATE,schemaName,version),
                params,ValidationResultList.class);
    }

    /**
     * Validate list of of records from a tgz file
     * @param schemaName The schema name to validate against
     * @param file The file containing the records
     * @param version The version of the schema (can be null)
     * @return A list of validation results
     */
    public ValidationResultList validateRecordsInFile(String schemaName, File file, String version){
       MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", new FileSystemResource(file));
        return template.postForObject(validationEndpoint+ RestEndpoints.resolve(RestEndpoints.SCHEMA_BATCH_VALIDATE,schemaName,version),
                parts,ValidationResultList.class);
    }
}
