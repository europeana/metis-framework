package eu.europeana.metis.core.search.config;

import eu.europeana.metis.core.rest.RequestLimits;
import eu.europeana.metis.core.search.service.MetisSearchService;
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
        return new MetisSearchService(RequestLimits.SUGGEST_TERMS_PER_REQUEST.getLimit());
    }


}
