package eu.eruoepaan.metis.framework.rest.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.framework.common.HarvestType;
import eu.europeana.metis.framework.common.OAIMetadata;
import eu.europeana.metis.framework.organization.Organization;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Created by ymamakis on 2/18/16.
 */
public class Main {

    public static void main(String[] args){
        RestTemplate restTemplate = new RestTemplate();
        Organization org = new Organization();
        org.setOrganizationId("newId");
        org.setOrganizationUri("http://tempuri");
        OAIMetadata oai = new OAIMetadata();
        oai.setHarvestUrl("http://tempuri");
        oai.setMetadataFormat("EDM");
        oai.setHarvestType(HarvestType.OAIPMH);
        org.setHarvestingMetadata(oai);

        //restTemplate.getForObject("http://localhost:8080/metis-framework-rest-1.0-SNAPSHOT/organizations", List.class);
        try {
            System.out.println(new ObjectMapper().writeValueAsString(org));
            restTemplate.postForObject("http://localhost:8080/metis-framework-rest-1.0-SNAPSHOT/organization",org,Void.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
