package eu.europeana.indexing;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoCredential;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import eu.europeana.indexing.service.PublishingService;
import eu.europeana.indexing.service.dao.FullBeanDao;

/**
 * This class creates instances of {@link Indexer}.
 */
public class IndexerFactory {

  private final IndexingSettings settings;

  /**
   * Constructor.
   * 
   * @param settings The settings to be applied to the indexer.
   */
  public IndexerFactory(IndexingSettings settings) {
    this.settings = settings;
  }

  /**
   * This method creates an indexer using the settings provided at construction.
   * 
   * @return An indexer.
   * @throws IndexerConfigurationException
   */
  public Indexer getIndexer() throws IndexerConfigurationException {

    // TODO JOCHEN Connect here? See sonarqube warning. Better to make Indexer closeable?

    // Set up the connection with the Solr server.
    final SolrServer solrServer;
    try {
      final LBHttpSolrServer httpSolrServer = new LBHttpSolrServer(
          settings.getSolrHosts().stream().map(URI::toString).toArray(String[]::new));
      if (settings.getZookeeperHosts().isEmpty()) {
        solrServer = httpSolrServer;
      } else {
        final String zookeeperHostString = settings.getZookeeperHosts().stream()
            .map(InetSocketAddress::toString).collect(Collectors.joining(","));
        final String zookeeperChrootString =
            settings.getZookeeperChroot() == null ? "" : settings.getZookeeperChroot();
        final CloudSolrServer cloudSolrServer =
            new CloudSolrServer(zookeeperHostString + zookeeperChrootString, httpSolrServer);
        cloudSolrServer.setDefaultCollection(settings.getSolrCollectionName());
        cloudSolrServer.connect();
        solrServer = cloudSolrServer;
      }
    } catch (MalformedURLException e) {
      throw new IndexerConfigurationException("Malformed URL provided in indexer settings.", e);
    }

    // Set up the connection with the Mongo server.
    final List<MongoCredential> mongoCredentials;
    if (settings.getMongoCredentials() == null) {
      mongoCredentials = Collections.emptyList();
    } else {
      mongoCredentials = Collections.singletonList(settings.getMongoCredentials());
    }
    final MongoClientOptions.Builder optionsBuilder =
        new Builder().sslEnabled(settings.mongoEnableSsl());
    final MongoClient mongoClient =
        new MongoClient(settings.getMongoHosts(), mongoCredentials, optionsBuilder.build());
    final EdmMongoServer mongoServer;
    try {
      mongoServer = new EdmMongoServerImpl(mongoClient, settings.getMongoDatabaseName());
    } catch (MongoDBException e) {
      throw new IndexerConfigurationException("Could not set up mongo server.", e);
    }

    // Set up the indexer.
    final FullBeanDao fullBeanDao = new FullBeanDao(mongoServer);
    final PublishingService publishingService = new PublishingService(fullBeanDao, solrServer);
    return new IndexerImpl(publishingService);
  }
}
