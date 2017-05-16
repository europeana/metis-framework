package eu.europeana.metis.search.test;

import eu.europeana.metis.core.rest.RequestLimits;
import eu.europeana.metis.core.search.service.MetisSearchService;
import java.io.File;
import java.nio.file.Path;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by gmamakis on 22-2-17.
 */
@Configuration
public class Config {

    @Bean
    SolrClient solrClient(){
        Path solrHome = new File("src/test/resources/solr").toPath();
        EmbeddedSolrServer server = new EmbeddedSolrServer(solrHome,"org_core");
        return server;
    }

    @Bean
    MetisSearchService searchService(){
        return new MetisSearchService(RequestLimits.SUGGEST_TERMS_PER_REQUEST.getLimit());
    }
}
