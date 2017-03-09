package eu.europeana.metis.search.test;

import eu.europeana.metis.search.service.MetisSearchService;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;

import org.apache.solr.schema.FieldProperties;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.StrField;
import org.apache.solr.search.FieldParams;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Path;

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
        return new MetisSearchService();
    }
}
