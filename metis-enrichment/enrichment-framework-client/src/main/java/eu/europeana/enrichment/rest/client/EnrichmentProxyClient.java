package eu.europeana.enrichment.rest.client;

import org.springframework.web.client.RestTemplate;

/**
 * Created by ymamakis on 9/28/16.
 */
public class EnrichmentProxyClient {

    private RestTemplate template = new RestTemplate();
    private String path;
    public EnrichmentProxyClient(String path){
        this.path = path;
    }
    public String check(){
        return template.getForObject(path+"/check",String.class);
    }

    public void populate(){
        template.getForObject(path+"/recreate",Void.class);
    }
}
