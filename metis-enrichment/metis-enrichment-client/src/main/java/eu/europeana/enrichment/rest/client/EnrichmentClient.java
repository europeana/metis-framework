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
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.EntityWrapperList;
import eu.europeana.enrichment.api.external.InputValueList;
import eu.europeana.metis.utils.InputValue;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * REST API wrapper class abstracting the REST calls and providing a clean POJO
 * implementation
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class EnrichmentClient {

    private String path;
    private RestTemplate template = new RestTemplate();

    public EnrichmentClient(String path) {
        this.path = path;
    }

    /**
     * Enrich REST call invocation
     *
     * @param values The values to be enriched
     * @param toEdm  Whether the enrichments should be retrieved in JSON (parsable
     *               to POJO through Jackson) or XML (for copy pasting)
     * @return The enrichments generated for the input values
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    public List<EntityWrapper> enrich(List<InputValue> values,
                                      boolean toEdm) throws IOException, UnknownException {
        InputValueList inList = new InputValueList();
        inList.setInputValueList(values);


        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("input", new ObjectMapper().writeValueAsString(inList));
        map.add("toXml", "" + toEdm);
        try {
            return new ObjectMapper().readValue(template.postForObject(path + ENRICHMENT_ENRICH, map, String.class),
                    EntityWrapperList.class).getWrapperList();
        } catch (Exception e){
            throw new UnknownException(e.getMessage());
        }
    }

    public String getByUri(String uri, boolean toXml) throws IOException {

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("uri", uri);
        map.add("toXml", "" + toXml);
        return template.postForObject(path + ENRICHMENT_BYURI, map, String.class);

    }

    private static class ExceptionGenerator {
        static Map<String, Class<? extends Exception>> exceptions = new HashMap<String, Class<? extends Exception>>() {
            /**
             *
             */
            private static final long serialVersionUID = 329292412348055056L;

            {
                put(JsonMappingException.class.getName(),
                        JsonMappingException.class);
                put(JsonGenerationException.class.getName(),
                        JsonGenerationException.class);
                put(UnknownException.class.getName(), UnknownException.class);
                put(IOException.class.getName(), IOException.class);
            }
        };
    }
}
