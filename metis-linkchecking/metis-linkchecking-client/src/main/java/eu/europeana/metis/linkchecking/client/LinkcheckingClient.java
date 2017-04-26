package eu.europeana.metis.linkchecking.client;

import eu.europeana.metis.RestEndpoints;
import eu.europeana.metis.linkchecking.LinkcheckRequest;
import eu.europeana.metis.linkchecking.LinkcheckStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Created by ymamakis on 11/4/16.
 */
public class LinkcheckingClient {

    private String restEndpoint;
    private RestTemplate restTemplate;
    public LinkcheckingClient(String restEndpoint){
        this.restEndpoint = restEndpoint;
        restTemplate = new RestTemplate();
    }

    public LinkcheckingClient(RestTemplate restTemplate){
        this.restEndpoint = "";
        this.restTemplate = restTemplate;
    }
    public List<LinkcheckStatus> getLinkCheckingReport(List<LinkcheckRequest> requests){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<LinkcheckRequest>> entity = new HttpEntity<>(requests, headers);
        return (List<LinkcheckStatus>)restTemplate.postForEntity(restEndpoint+ RestEndpoints.LINKCHECK,entity,List.class).getBody();

    }
}
