package eu.europeana.metis.preview.persistence;

import com.mongodb.MongoClient;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.utils.construct.FullBeanHandler;
import eu.europeana.corelib.edm.utils.construct.SolrDocumentHandler;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import eu.europeana.metis.mongo.MongoProvider;
import eu.europeana.metis.utils.NetworkUtil;
import java.io.IOException;
import java.net.UnknownHostException;
import javax.annotation.PreDestroy;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Created by ymamakis on 9/5/16.
 */
@Configuration
public class AppConfig {
    private final int port;
    private MongoProvider mongoProvider;
    public AppConfig() throws IOException {
        //FIXME replacing with single instance until the replica problem is resolved
        //MongoReplicaSet.start(10000);
        port = NetworkUtil.getAvailableLocalPort();
        mongoProvider = new MongoProvider();
        mongoProvider.start(port);
    }
    @Bean
    @DependsOn("edmMongoServer")
    FullBeanHandler fullBeanHandler() {
        return new FullBeanHandler(edmMongoServer());
    }

    @Bean(name = "edmMongoServer")
    EdmMongoServer edmMongoServer() {
        MongoClient client = null;
        try {

            client = new MongoClient("127.0.0.1", port);
            return new EdmMongoServerImpl(client, "test_db", null, null);
        } catch (MongoDBException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Bean
    @DependsOn(value = "solrServer")
    SolrDocumentHandler solrDocumentHandler() {
        return new SolrDocumentHandler(solrServer());
    }

    @Bean(name = "solrServer")
    SolrServer solrServer() {
        CoreContainer coreContainer = new CoreContainer("target/test-classes/solr");
        coreContainer.load();
        EmbeddedSolrServer server = new EmbeddedSolrServer(coreContainer, "search");
        System.out.println("Cores are:"+ server.getCoreContainer().getAllCoreNames());
        return server;
    }

    @Bean
    RecordDao recordDao() {
        return new RecordDao();
    }

    @PreDestroy
    public void shutdown() {
        mongoProvider.stop();
    }
}
