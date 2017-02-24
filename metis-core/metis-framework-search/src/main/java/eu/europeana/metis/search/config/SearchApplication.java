package eu.europeana.metis.search.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by gmamakis on 21-2-17.
 */
@Configuration
public class SearchApplication {

    @Value("${solr.url}")
    private String solrurl;

    @Bean
    public SolrClient getSolrClient(){
        if(System.getenv().containsKey("VCAP_SERVICES")){
            solrurl = System.getenv().get("solr_url");
        }
        return new HttpSolrClient.Builder().withBaseSolrUrl(System.getenv().get(solrurl)).build();
    }


}
