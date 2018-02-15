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
package eu.europeana.metis.identifier;

import eu.europeana.itemization.Request;
import eu.europeana.itemization.RequestResult;
import eu.europeana.metis.RestEndpoints;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Rest Client for Identifier and Itemization Service of Europeana
 * Created by ymamakis on 8/1/16.
 */
public class RestClient {
    private RestTemplate template = new RestTemplate();
    private String identifierEndpoint;
    private String itemizationEndpoint;

    public RestClient() {
        Properties props = new Properties();
        try {
            props.load(this.getClass().getClassLoader().getResourceAsStream("identifier.properties"));
            identifierEndpoint = props.getProperty("identifier.endpoint");
            itemizationEndpoint = props.getProperty("itemization.endpoint");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create Europeana valid record identifier from a collection (dataset) identifier and a record identifier
     * @param collectionId The dataset identifier of the record
     * @param recordId The record identifier of the record
     * @return The valid Europeana Identifier
     * @throws UnsupportedEncodingException 
     */
    public String generateIdentifier(String collectionId, String recordId) throws UnsupportedEncodingException {
            recordId = URLEncoder.encode(recordId, StandardCharsets.UTF_8.name());
            return template.getForObject(identifierEndpoint + RestEndpoints.resolve(
                    RestEndpoints.IDENTIFIER_GENERATE, collectionId) + "?recordId=" + recordId, String.class);
    }

    /**
     * Correctly link the EDM classes with the edm:ProvidedCHO rdf:about
     * @param edm The EDM xml of the record
     * @return The normalized EDM xml for the record
     */
    public String normalizeRecord(String edm) {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("record", edm);
        return template.postForObject(identifierEndpoint+RestEndpoints.IDENTIFIER_NORMALIZE_SINGLE,parts,String.class);
    }

    /**
     * Correctly link the EDM classes with the edm:ProvidedCHO rdf:about for multiple records
     * @param records A wrapper for EDM xml string records
     * @return The normalized EDM xml strings for the records
     */
    public RequestResult normalizeRecords(Request records){
        return template.postForObject(identifierEndpoint+RestEndpoints.IDENTIFIER_NORMALIZE_BATCH,records,
                RequestResult.class);
    }

    /**
     * Itemize records of a dataset from a URL that exposes a tgz
     * @param url The url of the tgz file
     * @return The itemized EDM records
     */
    public RequestResult itemizeTgzFromUrl(String url){
        return template.postForObject(identifierEndpoint+RestEndpoints.ITEMIZE_URL,url,
                RequestResult.class);
    }

    /**
     * Itemize  a list of records
     * @param records A record list wrapper. Each record is provided as a string
     * @return The itemized EDM records
     */
    public RequestResult itemizeRecords(Request records){
        return template.postForObject(identifierEndpoint+RestEndpoints.ITEMIZE_RECORDS,records,
                RequestResult.class);
    }

    /**
     * Itemize  a list of records in a tgz file
     * @param file A tgz file with records
     * @return The itemized EDM records
     */
    public RequestResult itemizeTgzFile(File file){
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", new FileSystemResource(file));
        return template.postForObject(identifierEndpoint+RestEndpoints.ITEMIZE_FILE,parts,
                RequestResult.class);
    }
}
