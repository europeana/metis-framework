package eu.europeana.metis.preview.persistence;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.utils.construct.FullBeanHandler;
import eu.europeana.corelib.edm.utils.construct.SolrDocumentHandler;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;
import java.io.IOException;
import java.net.UnknownHostException;
import javax.annotation.PreDestroy;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.junit.Before;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Created by ymamakis on 9/5/16.
 */
@Configuration
public class AppConfig {

  private final int mongoPort;
  private EmbeddedLocalhostMongo embeddedLocalhostMongo;
  private final String mongoHost;
  private final String mongoDb = "test_db";

  public AppConfig() throws IOException {
    //FIXME replacing with single instance until the replica problem is resolved
    //MongoReplicaSet.start(10000);
    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    embeddedLocalhostMongo.start();
    mongoHost = embeddedLocalhostMongo.getMongoHost();
    mongoPort = embeddedLocalhostMongo.getMongoPort();
  }

  @Bean
  @DependsOn("edmMongoServer")
  FullBeanHandler fullBeanHandler() throws UnknownHostException {
    return new FullBeanHandler(edmMongoServer());
  }

  @Bean(name = "edmMongoServer")
  EdmMongoServer edmMongoServer() throws UnknownHostException {
    try {
      ServerAddress address = new ServerAddress(mongoHost,mongoPort);
      MongoClient mongoClient = new MongoClient(address);
      return new EdmMongoServerImpl(mongoClient, mongoDb);
    } catch (MongoDBException e) {
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
    System.out.println("Cores are:" + server.getCoreContainer().getAllCoreNames());
    return server;
  }

  @Bean
  @DependsOn(value = "solrServer")
  RecordDao recordDao() throws UnknownHostException {
    return new RecordDao(fullBeanHandler(), solrDocumentHandler(), solrServer(), edmMongoServer());
  }


  @PreDestroy
  public void shutdown() {
    embeddedLocalhostMongo.stop();
  }
}
