package eu.europeana.metis.search.config;

import eu.europeana.metis.search.service.MetisSearchService;
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
        return new HttpSolrClient.Builder().withBaseSolrUrl(solrurl).build();
    }

    @Bean
    public MetisSearchService getMetisSearchService()
    {
        return new MetisSearchService();
    }


}
