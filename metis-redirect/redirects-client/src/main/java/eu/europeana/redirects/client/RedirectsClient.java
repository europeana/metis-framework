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
package eu.europeana.redirects.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europeana.metis.RestEndpoints;
import eu.europeana.redirects.model.RedirectRequest;
import eu.europeana.redirects.model.RedirectRequestList;
import eu.europeana.redirects.model.RedirectResponse;
import eu.europeana.redirects.model.RedirectResponseList;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;


/**
 * Redirects REST client
 * Created by ymamakis on 1/15/16.
 */
public class RedirectsClient {

    private RestTemplate restTemplate = new RestTemplate();
    private Config config = new Config();

    /**
     * Request for a redirect for a single record
     * @param request The request for a redirect
     * @return A response with the redirect generated if any
     * @throws JsonProcessingException
     */

    public RedirectResponse redirectSingle(RedirectRequest request) throws JsonProcessingException{

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RedirectRequest> req = new HttpEntity<>(request, headers);
       return restTemplate.postForEntity(config.getRedirectsPath()+ RestEndpoints.REDIRECT_SINGLE,req,RedirectResponse.class).getBody();
    }

    /**
     * Request for batch redirects
     * @param requests The list of redirect requests
     * @return A list of responses for each redirect
     * @throws JsonProcessingException
     */
    public RedirectResponseList redirectBatch(RedirectRequestList requests) throws JsonProcessingException{
        return restTemplate.postForObject(config.getRedirectsPath()+ RestEndpoints.REDIRECT_BATCH,requests,RedirectResponseList.class);
    }
}
