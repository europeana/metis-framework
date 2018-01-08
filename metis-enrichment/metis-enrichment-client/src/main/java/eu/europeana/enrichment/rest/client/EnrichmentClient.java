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
package eu.europeana.enrichment.rest.client;

import static eu.europeana.metis.RestEndpoints.ENRICHMENT_BYURI;
import static eu.europeana.metis.RestEndpoints.ENRICHMENT_ENRICH;

import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.InputValueList;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.metis.utils.InputValue;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * REST API wrapper class abstracting the REST calls and providing a clean POJO
 * implementation
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class EnrichmentClient {
    private String path;
    private RestTemplate template;
   
    public EnrichmentClient() {
        template = new RestTemplate();
    	Properties props = new Properties();
        
        try {
        	props.load(this.getClass().getClassLoader().getResourceAsStream("client.properties"));
        	path = props.getProperty("host.url"); 
        }
        catch (IOException e) {
        	e.printStackTrace();
        }
    }

    /**
     * Enrich REST call invocation
     *
     * @param values The values to be enriched
     * @return The enrichments generated for the input values
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    public EnrichmentResultList enrich(List<InputValue> values) throws IOException, UnknownException {
        InputValueList inList = new InputValueList();
        inList.setInputValueList(values);

        try {
        	EnrichmentResultList result = template.postForObject(path + ENRICHMENT_ENRICH, inList, EnrichmentResultList.class);
        	return result;
        } catch (Exception e){
            throw new UnknownException(e.getMessage());
        }
    }

    public EnrichmentBase getByUri(String uri) throws IOException {
        RestTemplate template = new RestTemplate();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(path + ENRICHMENT_BYURI)
            .queryParam("uri", uri);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", MediaType.APPLICATION_XML_VALUE);

        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<EnrichmentBase> x = template
            .exchange(builder.build(true).toUri(), HttpMethod.GET, entity,
                EnrichmentBase.class);

        return x.getBody();
    }
}
